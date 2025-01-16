package io.github.chrisruffalo.taverna.operator.customresource;

import io.fabric8.kubernetes.api.model.Namespaced;
import io.fabric8.kubernetes.client.CustomResource;
import io.fabric8.kubernetes.model.annotation.Group;
import io.fabric8.kubernetes.model.annotation.Version;
import io.github.chrisruffalo.taverna.operator.meta.Constants;

@Group(value = Constants.GROUP)
@Version(value = Constants.VERSION)
public class VerifiedTrust extends CustomResource<VerifiedTrustSpec, VerifiedTrustStatus> implements Namespaced {


}
