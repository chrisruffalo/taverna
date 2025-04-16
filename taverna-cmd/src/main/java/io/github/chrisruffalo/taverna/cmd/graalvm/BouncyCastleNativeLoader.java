package io.github.chrisruffalo.taverna.cmd.graalvm;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;
import io.github.chrisruffalo.resultify.Result;
import io.github.chrisruffalo.taverna.bc.BouncyCastleClasspathLoader;
import io.github.chrisruffalo.taverna.bc.BouncyCastleLoader;
import io.github.chrisruffalo.taverna.opt.Options;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.nofipsbouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Provider;

@TargetClass(BouncyCastleClasspathLoader.class)
public final class BouncyCastleNativeLoader implements BouncyCastleLoader {

    @Override
    @Substitute
    public Result<Provider> getBouncyCastleFipsProvider(Options options) {
        try {
            return Result.of(new BouncyCastleFipsProvider());
        } catch (Exception ex) {
            return Result.of(null, ex);
        }
    }

    @Override
    @Substitute
    public Result<Provider> getBouncyCastleProvider(Options options) {
        try {
            return Result.of(new BouncyCastleProvider());
        } catch (Exception ex) {
            return Result.of(null, ex);
        }
    }

}
