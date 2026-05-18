/*
* Copyright (C) 2026, 2026 Oracle and/or its affiliates.
*
* Licensed under the Universal Permissive License v 1.0 as shown at
* https://oss.oracle.com/licenses/upl/
*/

package oracle.nosql.intellij.plugin.common;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Locale;
import java.util.Objects;
import java.util.TreeMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Guards connection attempts whose parameters are stored in project files.
 *
 * <p>Oracle NoSQL connection settings are project-level state, so they may come
 * from a cloned repository. This class centralizes the security decision before
 * any NoSQL driver handle is created: validate URL shape, warn for sensitive
 * local/private targets, and require an explicit user approval for the current
 * project/connection fingerprint.</p>
 */
public final class ConnectionSecurityGuard {
    private static final Set<String> APPROVED_CONNECTIONS =
            ConcurrentHashMap.newKeySet();

    private ConnectionSecurityGuard() {
    }

    public static void approveSelectedConnectionIfNeeded(Project project)
            throws SecurityException {
        /*
         * The selected connection is kept in ConnectionDataProviderService.
         * Reading this state is safe; creating the NoSQL handle is delayed until
         * after validation and user approval below.
         */
        ConnectionDataProviderService.State state =
                ConnectionDataProviderService.getInstance(project).getState();
        if (state == null || state.dict.isEmpty()) {
            return;
        }

        ConnectionSummary summary = summarize(state);
        if (summary.requiresUrlValidation) {
            /*
             * Onprem/Cloudsim accept arbitrary endpoints. Restricting these to
             * normal HTTP(S) URLs blocks malformed schemes such as file:, jar:,
             * and credential-bearing URLs before the driver sees them.
             */
            validateHttpUrl(summary.profileType, summary.connectionTarget);
        }

        String fingerprint = fingerprint(project, state);
        if (APPROVED_CONNECTIONS.contains(fingerprint)) {
            return;
        }

        if (!askUserToApprove(project, summary)) {
            throw new SecurityException("Connection attempt was not approved");
        }
        /*
         * Approval is intentionally session-only. We do not persist this flag in
         * project files because an attacker-controlled repository could then
         * carry a pre-approved marker.
         */
        APPROVED_CONNECTIONS.add(fingerprint);
    }

    private static ConnectionSummary summarize(ConnectionDataProviderService.State state) {
        String profileType = state.dict.get(ConnectionDataProviderService.KEY_PROFILE_TYPE);
        if ("Onprem".equals(profileType)) {
            return new ConnectionSummary(profileType,
                    state.dict.get("/Onprem/Onprem/proxy-url"), true);
        }
        if ("Cloudsim".equals(profileType)) {
            return new ConnectionSummary(profileType,
                    state.dict.get("/Cloudsim/Cloudsim/service-url"), true);
        }
        if ("Cloud".equals(profileType)) {
            String useConfigFile = state.dict.get("/Cloud/USE_CONFIG_FILE");
            if ("true".equalsIgnoreCase(useConfigFile)) {
                return new ConnectionSummary(profileType,
                        state.dict.get("/Cloud/CONFIG_FILE"), false);
            }
            return new ConnectionSummary(profileType,
                    state.dict.get("/Cloud/Cloud/endpoint"), false);
        }
        return new ConnectionSummary(profileType, "", false);
    }

    private static void validateHttpUrl(String profileType, String target) {
        if (target == null || target.trim().isEmpty()) {
            throw new SecurityException(profileType + " connection URL is empty");
        }
        try {
            URI uri = new URI(target.trim());
            String scheme = uri.getScheme();
            if (scheme == null ||
                    !("http".equalsIgnoreCase(scheme) ||
                            "https".equalsIgnoreCase(scheme))) {
                throw new SecurityException(profileType +
                        " connection URL must use http or https");
            }
            if (uri.getHost() == null || uri.getHost().trim().isEmpty()) {
                throw new SecurityException(profileType +
                        " connection URL must include a host");
            }
            if (uri.getUserInfo() != null) {
                throw new SecurityException(profileType +
                        " connection URL must not include embedded credentials");
            }
        } catch (URISyntaxException e) {
            throw new SecurityException(profileType +
                    " connection URL is invalid", e);
        }
    }

    private static boolean askUserToApprove(Project project,
                                            ConnectionSummary summary) {
        AtomicBoolean approved = new AtomicBoolean(false);
        Runnable prompt = () -> approved.set(Messages.YES ==
                Messages.showYesNoDialog(project,
                        approvalMessage(summary),
                        "Approve Oracle NoSQL Connection",
                        Messages.getWarningIcon()));
        /*
         * Connection attempts normally run from background tasks. IntelliJ UI
         * prompts must run on the EDT, so marshal the approval dialog when
         * required and block the connection until the user decides.
         */
        if (ApplicationManager.getApplication().isDispatchThread()) {
            prompt.run();
        } else {
            ApplicationManager.getApplication().invokeAndWait(prompt,
                    ModalityState.any());
        }
        return approved.get();
    }

    private static String approvalMessage(ConnectionSummary summary) {
        StringBuilder message = new StringBuilder();
        message.append("Oracle NoSQL is about to open a network connection ")
                .append("using settings stored in this project.\n\n")
                .append("Profile: ")
                .append(valueOrUnknown(summary.profileType))
                .append("\nTarget: ")
                .append(valueOrUnknown(summary.connectionTarget));
        if (summary.requiresUrlValidation) {
            String warning = localNetworkWarning(summary.connectionTarget);
            if (!warning.isEmpty()) {
                message.append("\n\nWarning: ").append(warning);
            }
        }
        message.append("\n\nOnly approve this connection if you trust this project ")
                .append("and the target shown above.");
        return message.toString();
    }

    private static String localNetworkWarning(String target) {
        if (target == null) {
            return "the target is empty or unknown.";
        }
        try {
            URI uri = new URI(target.trim());
            String host = uri.getHost();
            if (host == null) {
                return "the target is not a standard network URL.";
            }
            String normalized = host.toLowerCase(Locale.ROOT);
            if ("localhost".equals(normalized) || "::1".equals(normalized) ||
                    normalized.startsWith("127.") ||
                    normalized.startsWith("10.") ||
                    normalized.startsWith("192.168.") ||
                    normalized.startsWith("169.254.") ||
                    is172PrivateAddress(normalized)) {
                return "this target appears to be local, private, or link-local.";
            }
        } catch (URISyntaxException ignored) {
            return "the target is not a standard network URL.";
        }
        return "";
    }

    private static boolean is172PrivateAddress(String host) {
        if (!host.startsWith("172.")) {
            return false;
        }
        String[] parts = host.split("\\.");
        if (parts.length < 2) {
            return false;
        }
        try {
            int secondOctet = Integer.parseInt(parts[1]);
            return secondOctet >= 16 && secondOctet <= 31;
        } catch (NumberFormatException ignored) {
            return false;
        }
    }

    private static String fingerprint(Project project,
                                      ConnectionDataProviderService.State state) {
        /*
         * Include the project location hash and the full selected state. A user
         * approval for one project or one set of connection parameters should
         * not silently approve another repository or a later modified endpoint.
         */
        return project.getLocationHash() + ':' + new TreeMap<>(state.dict);
    }

    private static String valueOrUnknown(String value) {
        return value == null || value.trim().isEmpty() ? "<unknown>" : value;
    }

    private static final class ConnectionSummary {
        private final String profileType;
        private final String connectionTarget;
        private final boolean requiresUrlValidation;

        private ConnectionSummary(String profileType, String connectionTarget,
                                  boolean requiresUrlValidation) {
            this.profileType = Objects.toString(profileType, "<unknown>");
            this.connectionTarget = Objects.toString(connectionTarget, "");
            this.requiresUrlValidation = requiresUrlValidation;
        }
    }
}