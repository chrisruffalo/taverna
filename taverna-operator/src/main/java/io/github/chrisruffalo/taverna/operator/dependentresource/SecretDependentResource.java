package io.github.chrisruffalo.taverna.operator.dependentresource;

import io.fabric8.kubernetes.api.model.Secret;
import io.github.chrisruffalo.taverna.operator.VerifiedTrustReconciler;
import io.github.chrisruffalo.taverna.operator.customresource.VerifiedTrust;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent(labelSelector = VerifiedTrustReconciler.SELECTOR)
public class SecretDependentResource extends CRUDKubernetesDependentResource<Secret, VerifiedTrust> {

    public SecretDependentResource(Class<Secret> resourceType) {
        super(resourceType);
    }


}
