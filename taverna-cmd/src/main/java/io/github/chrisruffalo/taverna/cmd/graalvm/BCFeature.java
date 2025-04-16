package io.github.chrisruffalo.taverna.cmd.graalvm;

import io.github.chrisruffalo.taverna.bc.BouncyCastleLoader;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.security.Security;
import java.util.List;

public class BCFeature implements Feature {

    public static final String BC_RELOACTED_NAME = "org.nofipsbouncycastle.jce.provider.BouncyCastleProvider";

    private final List<String> providers = List.of(BouncyCastleLoader.BCFIPS_NAME, BC_RELOACTED_NAME);

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        for (String provider : providers) {
            final Class<?> providerClass = access.findClassByName(provider);
            if (providerClass != null) {
                RuntimeReflection.register(providerClass);
            }
        }
    }

    @Override
    public void duringSetup(Feature.DuringSetupAccess access) {
        for (String provider : providers) {
            final Class<?> providerClass = access.findClassByName(provider);
            if (providerClass != null) {

            }
        }
    }
}
