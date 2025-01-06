package io.github.chrisruffalo.taverna.opt;

public enum CompletionMode {

    /**
     * Completes the trust by directly trusting the first (domain-level) certificate from the domain.
     */
    DIRECT,

    /**
     * Completes the trust by trusting the first subordinate certificate (the issuer of the domain certificate)
     * if it is provided in the trust chain.
     */
    FIRST_SUBORDINATE,

    /**
     * Completes the trust by trusting the deepest certificate in the domain's chain.
     */
    MOST_TRUSTED

}
