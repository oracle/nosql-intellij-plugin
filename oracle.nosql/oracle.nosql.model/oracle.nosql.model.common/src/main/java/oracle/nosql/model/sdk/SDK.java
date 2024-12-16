/*
* Copyright (C) 2019, 2024 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.sdk;

import java.net.URL;

import oracle.nosql.model.util.SemVer;

/**
 * Abstract representation of SDK. This interface provides the API to access
 * the libraries and version of SDK.
 * 
 * @author Jashkumar Dave
 *
 */
public interface SDK {
    /**
     * @return Runtime libraries from the SDK
     */
    public URL[] getRuntimeLibraries();

    /**
     * @return Compiletime libraries from the SDK
     */
    public URL[] getCompileTimeLibraries();

    /**
     * @return SDK's version as {@link SemVer} instance.
     */
    public SemVer getVersion();
}
