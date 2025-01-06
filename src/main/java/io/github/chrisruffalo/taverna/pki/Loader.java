package io.github.chrisruffalo.taverna.pki;

import io.github.chrisruffalo.taverna.model.Cert;

import java.util.List;

/**
 * Interface to allow common loading of certificates
 */
public interface Loader<CONF extends LoaderConfig> {

    List<Cert> load(CONF configuration);

}
