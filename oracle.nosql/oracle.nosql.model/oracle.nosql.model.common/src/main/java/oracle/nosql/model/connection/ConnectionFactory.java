/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.connection;

import oracle.nosql.model.profiletype.Cloudsim;
import oracle.nosql.model.profiletype.Onprem;
import oracle.nosql.model.profiletype.PublicCloud;
import oracle.nosql.model.sdk.SDK;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * ConnectionFactory creates {@link IConnectionProfile connection profile}.
 * Each concrete profile requires its own specific properties to be configured.
 * ConnectionFactory covers such difference by having name-value pairs to
 * configure specific {@link IConnectionProfile connection profile}. <br>
 * This factory statically declares each profile it supports. Each
 * {@link IConnectionProfileType}, on the other hand,
 * {@link #register(IConnectionProfileType, Class) registers} statically to
 * this factory.
 * 
 * 
 * @author pinaki poddar
 *
 */
public class ConnectionFactory {
    private final static Map<IConnectionProfileType, String> registry =
            new LinkedHashMap<IConnectionProfileType, String>();
    private final static Map<String, ClassLoader> classLoaderCache =
            new HashMap<String, ClassLoader>();
    private static File libDir;
    private final static String jacksonJarName = "databind.jar";
    private final static String cloudJarName =
            "oracle.nosql.model.cloud-1.2.0.jar";
    static {
        try {
            registerByDynamicClassLoading(
                    "oracle.nosql.model.profiletype.PublicCloud");
            registerByDynamicClassLoading(
                    "oracle.nosql.model.profiletype.Onprem");
            registerByDynamicClassLoading(
                    "oracle.nosql.model.profiletype.Cloudsim");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static void setLibDir(File libDirl) {
        libDir = libDirl;
    }

    /**
     * Registers a profile by its type and implementation class.
     * 
     * @param type the type of profile
     * @param profileClass implementation class of the profile
     */
    public static void register(IConnectionProfileType type,
            String profileClass) {
        registry.put(type, profileClass);
    }

    /**
     * Registers a connection profile type by name of an implementation class.
     * Side-effect of loading the class is to register the profile to this
     * factory. However, the type is loaded by class name to create any
     * dependency between this factory and any profile as this factory is
     * agnostic of any profile at compilation. <br>
     * If the given profile type can not be loaded, this profile would not be
     * registered. <br>
     * 
     * 
     * @param profileTypeClassName
     */
    private static void
            registerByDynamicClassLoading(String profileTypeClassName) {
        try {
            Class.forName(profileTypeClassName);
        } catch (Throwable t) {
            System.err.println("Profile type can not be registered beause " +
                    profileTypeClassName +
                    " can not be loaded." +
                    "See nested excetion for details");
            t.printStackTrace();
        }
    }

    /**
     * Creates profile of given type.
     * 
     * @param profileType the type name of the profile. Has to be one of
     * {@link #getProfileTypes()}.
     * @param profileName the name of the profile created.
     * @param SDKPath path to software development kit (SDK) folder, to access
     * libraries.
     * @param eclipseCL Eclipse's classloader to act as parent classloader.
     * @return a connection profile.
     * @throws Exception
     */
    public static IConnectionProfile<?> createProfile(
            IConnectionProfileType profileType,
            String profileName,
            String SDKPath,
            ClassLoader eclipseCL) throws Exception {
        if (!registry.containsKey(profileType)) {
            throw new RuntimeException("Unknown connection profile type " +
                    profileType +
                    ". Known types are " +
                    registry.keySet());
        }
        ClassLoader classLoader;
        if (classLoaderCache.containsKey(SDKPath)) {
            classLoader = classLoaderCache.get(SDKPath);
        } else {
            SDK sdk = profileType.getSDK(SDKPath);
            URL rtURLs[] = sdk.getRuntimeLibraries();
            URL ctURLs[] = sdk.getCompileTimeLibraries();
            URL DAURLs[] = null;
            if (Cloudsim.class.isInstance(profileType)
                    || PublicCloud.class.isInstance(profileType)
                    || Onprem.class.isInstance(profileType)) {
                DAURLs = new URL[2];
                DAURLs[0] = new File(libDir, jacksonJarName).toURI().toURL();
                DAURLs[1] = new File(libDir, cloudJarName).toURI().toURL();
            }
            URL libURLs[] =
                    new URL[rtURLs.length + ctURLs.length + DAURLs.length];
            System.arraycopy(DAURLs, 0, libURLs, 0, DAURLs.length);
            System.arraycopy(rtURLs, 0, libURLs, DAURLs.length, rtURLs.length);
            System.arraycopy(ctURLs,
                    0,
                    libURLs,
                    rtURLs.length + DAURLs.length,
                    ctURLs.length);
            classLoader = new URLClassLoader(libURLs, eclipseCL);
            classLoaderCache.put(SDKPath, classLoader);
        }
        String profileQualifiedName = registry.get(profileType);
        IConnectionProfile<?> profile = (IConnectionProfile<
                ?>) classLoader.loadClass(profileQualifiedName).newInstance();
        profile.setName(profileName);
        return profile;
    }

    /**
     * Gets the profile types registered to this factory. It is a
     * responsibility of a profile type to register.
     * 
     * @return an array of profile types registered to this factory.
     */
    public static IConnectionProfileType[] getProfileTypes() {
        return registry.keySet().toArray(
                new IConnectionProfileType[registry.size()]);
    }

    /**
     * Gets one of the registered/known profile type by given name
     * 
     * @param profileTypeName name of the profile type. The name is matched
     * ignoring case.
     * @return a profile type
     * @exception RuntimeException if no profile type of given name is known
     */
    public static IConnectionProfileType
            getProfileTypeByName(String profileTypeName) {
        if (profileTypeName == null) {
            throw new RuntimeException("null profile type name");
        }
        for (IConnectionProfileType type : registry.keySet()) {
            String key = profileTypeName.toLowerCase();
            String typeName = type.getName().toLowerCase();
            if (typeName.equals(key)) {
                return type;
            }
        }
        throw new RuntimeException("no registered profile of  " +
                profileTypeName +
                " type" +
                ". Available profile types are " +
                registry.keySet());
    }
}
