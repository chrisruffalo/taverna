package io.github.chrisruffalo.taverna.pki.validate;

import java.util.Collection;

public class HostnameVerifier {

    /**
     * Verifies the hostname against a list of SANs.
     * @param hostname The hostname to verify.
     * @param subjectAltNames The list of SANs from the certificate.
     * @return true if the hostname matches any SAN, false otherwise.
     */
    public static boolean verifyHostname(String hostname, Collection<String> subjectAltNames) {
        if (subjectAltNames == null || subjectAltNames.isEmpty()) {
            return false; // No SANs to verify against
        }

        for (String san : subjectAltNames) {
            if (matchesHostname(hostname, san)) {
                return true; // Match found
            }
        }

        return false; // No match found
    }

    /**
     * Checks if the hostname matches a specific SAN entry.
     * @param hostname The hostname to check.
     * @param san The SAN entry to compare against.
     * @return true if they match, false otherwise.
     */
    private static boolean matchesHostname(String hostname, String san) {
        if (san == null || san.isEmpty()) {
            return false;
        }

        // Handle wildcard entries
        if (san.startsWith("*.")) {
            String sanDomain = san.substring(2); // Remove "*."
            return hostname.endsWith(sanDomain) &&
                    hostname.split("\\.").length == sanDomain.split("\\.").length + 1;
        }

        // Direct comparison for non-wildcard SANs
        return hostname.equalsIgnoreCase(san);
    }

}
