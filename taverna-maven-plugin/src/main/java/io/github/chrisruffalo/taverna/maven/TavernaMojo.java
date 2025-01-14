package io.github.chrisruffalo.taverna.maven;

import io.github.chrisruffalo.taverna.Engine;
import io.github.chrisruffalo.taverna.maven.log.MavenLogger;
import io.github.chrisruffalo.taverna.maven.opt.MavenOpts;
import io.github.chrisruffalo.taverna.opt.CompletionMode;
import io.github.chrisruffalo.taverna.opt.Options;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Mojo(name = "tav", requiresProject = false)
public class TavernaMojo extends AbstractMojo {

    @Parameter(name = "domains", property = "taverna.domains")
    List<String> domains = new ArrayList<>(0);

    @Parameter(name = "domainFiles", property = "taverna.domainFiles")
    List<Path> domainFiles = new ArrayList<>(0);

    @Parameter(name = "sources", property = "taverna.sources")
    List<String> sources = new ArrayList<>(0);

    @Parameter(name = "storePass", defaultValue = Options.DEFAULT_STORE_PASSWORD, property = "taverna.storePass")
    String storePass;

    @Parameter(name = "outStore", property = "taverna.outStore")
    Path outStore;

    @Parameter(name = "outStorePass", defaultValue = Options.DEFAULT_STORE_PASSWORD, property = "taverna.outStorePass")
    String outStorePass;

    @Parameter(name = "outStoreType", defaultValue = Options.DEFAULT_STORE_TYPE, property = "taverna.outStoreType")
    String outStoreType;

    @Parameter(name = "outFile", property = "taverna.outFile")
    Path outFile;

    @Parameter(name = "outDir", property = "taverna.outDir")
    Path outDir;

    @Parameter(name = "simplify", defaultValue = "false", property = "taverna.simplify")
    boolean simplify = false;

    @Parameter(name = "complete", defaultValue = "false", property = "taverna.complete")
    boolean complete = false;

    @Parameter(name = "completionMode", defaultValue = "FIRST_SUBORDINATE", property = "taverna.completionMode")
    CompletionMode completionMode = CompletionMode.FIRST_SUBORDINATE;

    @Parameter(name = "noVerify", defaultValue = "false", property = "taverna.noVerify")
    boolean noVerify = false;

    @Parameter(name = "noDomains", defaultValue = "false", property = "taverna.noDomains")
    boolean noDomains = false;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        // create option from maven
        final MavenOpts mavenOpts = new MavenOpts();
        mavenOpts.setDomains(domains);
        mavenOpts.setDomainFiles(domainFiles);
        mavenOpts.setSource(sources);
        mavenOpts.setStorePass(storePass);
        mavenOpts.setOutstore(outStore);
        mavenOpts.setOutStorePass(outStorePass);
        mavenOpts.setOutstoreType(outStoreType);
        mavenOpts.setOutfile(outFile);
        mavenOpts.setOutdir(outDir);
        mavenOpts.setSimplify(simplify);
        mavenOpts.setComplete(complete);
        mavenOpts.setCompletionMode(completionMode);
        mavenOpts.setNoVerify(noVerify);
        mavenOpts.setNoDomains(noDomains);

        // create logger
        final MavenLogger logger = new MavenLogger(getLog());

        // run engine
        final Engine engine = new Engine(logger, mavenOpts);
        engine.run();
    }
}
