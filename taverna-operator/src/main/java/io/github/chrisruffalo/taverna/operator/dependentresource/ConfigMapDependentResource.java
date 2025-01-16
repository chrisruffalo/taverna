package io.github.chrisruffalo.taverna.operator.dependentresource;

import io.fabric8.kubernetes.api.model.ConfigMap;
import io.github.chrisruffalo.taverna.operator.VerifiedTrustReconciler;
import io.github.chrisruffalo.taverna.operator.customresource.VerifiedTrust;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.CRUDKubernetesDependentResource;
import io.javaoperatorsdk.operator.processing.dependent.kubernetes.KubernetesDependent;

@KubernetesDependent(labelSelector = VerifiedTrustReconciler.SELECTOR)
public class ConfigMapDependentResource extends CRUDKubernetesDependentResource<ConfigMap, VerifiedTrust> {

    public ConfigMapDependentResource(Class<ConfigMap> resourceType) {
        super(resourceType);
    }


}
