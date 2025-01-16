package io.github.chrisruffalo.taverna.operator;

import io.github.chrisruffalo.taverna.operator.customresource.VerifiedTrust;
import io.javaoperatorsdk.operator.api.reconciler.*;

public class VerifiedTrustReconciler implements Reconciler<VerifiedTrust>, Cleaner<VerifiedTrust> {

    public static final String SELECTOR = "verified-trust";

    @Override
    public DeleteControl cleanup(VerifiedTrust verifiedTrust, Context<VerifiedTrust> context) {
        return null;
    }

    @Override
    public UpdateControl<VerifiedTrust> reconcile(VerifiedTrust verifiedTrust, Context<VerifiedTrust> context) throws Exception {
        return null;
    }
}
