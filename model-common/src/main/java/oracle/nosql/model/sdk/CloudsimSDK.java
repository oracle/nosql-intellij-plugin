/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.sdk;

import oracle.nosql.model.util.SemVer;

import java.io.File;
import java.net.URL;

public class CloudsimSDK implements SDK {
    private final String path;

    public CloudsimSDK(String path) throws Exception {
    	String errorMessage = validate(path);
        if (errorMessage != null) {
            throw new IllegalArgumentException(errorMessage);
        }
        this.path = path;
    }

    @Override
    public URL[] getRuntimeLibraries() {
        return new URL[0];
    }

    @Override
    public URL[] getCompileTimeLibraries() {
        try {
            File sdkDir = new File(path + File.separator + "lib" + File.separator);
            File[] jarFiles = sdkDir.listFiles((dir, name) -> name.endsWith(".jar"));
            URL libs[] = new URL[jarFiles.length];
            int i = 0;
            for (File libFile : jarFiles ){
                libs[i++] = libFile.toURI().toURL();
            }
            return libs;
        } catch (Exception e) {
            return new URL[0];
        }
    }

    @Override
    public SemVer getVersion() {
        try {
            return SemVer.fromArchive(getCompileTimeLibraries()[0].getPath());
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * @param path - Full path to Cloudsim SDK
     * @return Error message if any, otherwise null on no error.
     */
    public static String validate(String path) {
        // This is basic validation
        File sdkDir = new File(path + File.separator + "lib" +File.separator);
        return sdkDir.isDirectory() ? null : "Invalid SDK path, please set a correct SDK path.";
    }
}
