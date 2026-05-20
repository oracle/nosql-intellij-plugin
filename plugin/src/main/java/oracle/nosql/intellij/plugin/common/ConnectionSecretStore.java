/*
 * Copyright (C) 2026, 2026 Oracle and/or its affiliates.
 *
 * Licensed under the Universal Permissive License v 1.0 as shown at
 * https://oss.oracle.com/licenses/upl/
 */

package oracle.nosql.intellij.plugin.common;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.openapi.project.Project;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;
import java.util.UUID;

/**
 * Stores connection secrets outside project XML.
 *
 * <p>Connection settings are persisted as project-level state and may be
 * committed with a repository. Passwords, passphrases, and CloudSim bearer-token
 * material must therefore be written to IntelliJ PasswordSafe and represented in
 * project XML only by an opaque random reference.</p>
 */
final class ConnectionSecretStore {
    private static final String SERVICE_PREFIX =
            "Oracle NoSQL IntelliJ Plugin";
    private static final String SECRET_REF_PREFIX =
            "__oracle_nosql_secret_ref__:";

    private ConnectionSecretStore() {
    }

    static boolean isSensitiveKey(String key) {
        if (key == null) {
            return false;
        }
        /*
         * Keep this list narrow and explicit: only values that can authenticate
         * or unlock key material are moved out of project XML. Non-secret
         * connection metadata remains visible/editable in normal project state.
         */
        String normalized = key.toUpperCase(Locale.ROOT);
        return normalized.endsWith("/PASSWORD") ||
                normalized.endsWith("/PASSPHRASE") ||
                normalized.endsWith("/TS_PASSPHRASE") ||
                normalized.endsWith("/TENANT_ID");
    }

    static boolean isSecretReference(String value) {
        return value != null && value.startsWith(SECRET_REF_PREFIX);
    }

    static String store(Project project,
                        String key,
                        String secret,
                        String existingStoredValue) {
        if (secret == null || secret.isEmpty()) {
            return "";
        }
        if (isSecretReference(secret)) {
            return secret;
        }

        /*
         * Reuse the previous reference when editing a connection so the XML key
         * remains stable. New secrets get a random reference that reveals
         * nothing about the secret value itself.
         */
        String reference = isSecretReference(existingStoredValue) ?
                existingStoredValue :
                SECRET_REF_PREFIX + UUID.randomUUID();
        PasswordSafe.getInstance().set(attributes(project, key, reference),
                new Credentials(key, secret));
        return reference;
    }

    static String resolve(Project project, String key, String storedValue) {
        if (!isSecretReference(storedValue)) {
            return storedValue;
        }

        /*
         * First look up the project-scoped secret. If older code created a
         * reference before a project was attached, fall back to the application
         * scope so users are not locked out during migration.
         */
        Credentials credentials = PasswordSafe.getInstance().get(
                attributes(project, key, storedValue));
        if (credentials == null && project != null) {
            /*
             * Fallback for legacy migrations or tests that created references
             * before a Project instance was available.
             */
            credentials = PasswordSafe.getInstance().get(
                    attributes(null, key, storedValue));
        }
        if (credentials == null || credentials.getPasswordAsString() == null) {
            return "";
        }
        return credentials.getPasswordAsString();
    }

    static String migrateIfPlainText(Project project,
                                     String key,
                                     String storedValue) {
        if (!isSensitiveKey(key) || storedValue == null ||
                storedValue.isEmpty() || isSecretReference(storedValue)) {
            return storedValue;
        }
        /*
         * Existing project files may already contain plaintext credentials.
         * Convert them to PasswordSafe references as soon as state is loaded or
         * saved so future project XML no longer contains the secret value.
         */
        return store(project, key, storedValue, null);
    }

    static String publicIdentifier(String storedValue) {
        if (storedValue == null || storedValue.isEmpty()) {
            return "";
        }
        if (!isSecretReference(storedValue)) {
            return "secret-set";
        }
        /*
         * UIDs need a stable, non-secret discriminator. Hashing the opaque
         * reference distinguishes entries without exposing the actual token.
         */
        return "secret-" + sha256(storedValue).substring(0, 12);
    }

    private static CredentialAttributes attributes(Project project,
                                                   String key,
                                                   String reference) {
        String scope = project == null ? "application" :
                project.getLocationHash();
        String serviceName = CredentialAttributesKt.generateServiceName(
                SERVICE_PREFIX,
                scope + ':' + reference.substring(SECRET_REF_PREFIX.length()));
        return new CredentialAttributes(serviceName, key);
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is not available", e);
        }
    }
}