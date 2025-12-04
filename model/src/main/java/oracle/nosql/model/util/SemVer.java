/*
* Copyright (C) 2019, 2025 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.model.util;

import java.io.IOException;
import java.util.Enumeration;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Representation of version as semantic version.
 * 
 * @author Jashkumar Dave
 *
 */
public class SemVer implements Comparable<SemVer> {
    private final String version;
    private static final Name VERSION_KEY = new Name("Implementation-version");
    private static final String MANIFEST_ENTRY = "META-INF/MANIFEST.MF";
    private static final Pattern SEMVAR_PATTERN =
            Pattern.compile("(?<major>\\d+)" +
                    "\\.(?<minor>\\d+)" +
                    "(\\.(?<patch>\\d+))?" +
                    "(\\-(?<sem>.*))?");

    /**
     * Creates a semantic version string.
     * 
     * @param version a semantic version string.
     * @throws IllegalArgumentException if given string does not match semantic
     * version pattern.
     */
    public SemVer(String input) {
        if (!SEMVAR_PATTERN.matcher(input).matches()) {
            throw new IllegalArgumentException("invalid version " + input);
        }
        this.version = input;
    }

    public static SemVer fromArchive(ZipFile zip) throws IOException {
        Manifest manifest = getManifest(zip);
        if (manifest == null)
            return null;
        String input = manifest.getMainAttributes().getValue(VERSION_KEY);
        if (input == null) {
            throw new RuntimeException("no " + VERSION_KEY + " in maniest");
        }
        return new SemVer(input);
    }

    public static SemVer fromArchive(String path) throws IOException {
        ZipFile zip = new ZipFile(path);
        Manifest manifest = getManifest(zip);
        if (manifest == null) {
            throw new RuntimeException("no maniest entry in " + path);
        }
        String input = manifest.getMainAttributes().getValue(VERSION_KEY);
        if (input == null) {
            throw new RuntimeException(
                    "no " + VERSION_KEY + " in maniest of " + path);
        }
        return new SemVer(input);
    }

    public static Manifest getManifest(ZipFile zip) throws IOException {
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            if (MANIFEST_ENTRY.equalsIgnoreCase(entry.getName())) {
                return new Manifest(zip.getInputStream(entry));
            }
        }
        return null;
    }

    /**
     * Gets major number of this version.
     * 
     * @return major number of this version.
     */
    public int getMajor() {
        return Integer.parseInt(parseGroup("major"));
    }

    /**
     * Gets minor number of this version.
     * 
     * @return minor number of this version.
     */
    public int getMinor() {
        return Integer.parseInt(parseGroup("minor"));
    }

    /**
     * Gets patch number of this version.
     * 
     * @return patch number of this version. <code>0</code> if patch number
     * does not exist.
     */
    public int getPatch() {
        return hasPatchNumber() ? Integer.parseInt(parseGroup("patch")) : 0;
    }

    public boolean hasPatchNumber() {
        return parseGroup("patch") != null;
    }

    /**
     * Gets semantic part of this version string.
     * 
     * @return can be null.
     */
    public String getSemanticPart() {
        return parseGroup("sem");
    }

    public boolean hasSemanticPart() {
        return getSemanticPart() != null;
    }

    /**
     * parse a group of given group name.
     * 
     * @param groupName
     * @return
     */
    private String parseGroup(String groupName) {
        Matcher matcher = SEMVAR_PATTERN.matcher(version);
        matcher.matches();
        return matcher.group(groupName);
    }

    public boolean equals(Object other) {
        if (this == other)
            return true;
        return this.toString().equals(other.toString());
    }

    public String toString() {
        return version;
    }

    @Override
    public int compareTo(SemVer o) {
        if (this.getMajor() == o.getMajor()) {
            if (this.getMinor() == o.getMinor()) {
                if (this.getPatch() == o.getPatch()) {
                    return 0;
                } else {
                    return this.getPatch() - o.getPatch();
                }
            } else {
                return this.getMinor() - o.getMinor();
            }
        } else {
            return this.getMajor() - o.getMajor();
        }
    }
}
