package io.github.chrisruffalo.taverna.opt;

import com.beust.jcommander.Parameter;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class OptGlobal {

    public static final String DEFAULT_STORE_PASSWORD = "changeit";

    @Parameter(names = {"-d", "--domain"}, arity = 1, description = "A domain that the application needs to trust. A single domain can be a fqdn or fqdn:port. If no port is provided 443 is assumed. (This option may be specified multiple times.)")
    private List<String> domains = new ArrayList<>(0);

    @Parameter(names = {"-D", "--domains"}, arity = 1, description = "A file that contains a list of domains that the application needs to trust. A single domain can be a fqdn or fqdn:port. If no port is provided 443 is assumed. (This option may be specified multiple times.)")
    private List<File> domainFiles = new ArrayList<>(0);

    @Parameter(names = {"-s", "--source"}, arity = 1, description = "A source of trust material. Can be a directory, a file, or a Java trust store. If the source is a Java trust store with a password you may provide it by appending \":password\" to the source or by using the \"--storepass\" option. (This option may be specified multiple times.)")
    private List<String> source = new ArrayList<>(0);

    @Parameter(names = {"-p", "--storepass"}, defaultValueDescription = "The default value is '" + DEFAULT_STORE_PASSWORD + "'.", description = "The default password to use for all Java trust stores that are specified without providing a password.")
    private String storePass = DEFAULT_STORE_PASSWORD;

    @Parameter(names = {"-o", "--outstore"}, description = "If specified the current trust profile will be written to the output keystore.")
    private Path outstore;

    @Parameter(names = {"-T", "--outstoretype"}, defaultValueDescription = "The default value is 'PKCS12'.", description = "Specifies the format of the output keystore.")
    private String outstoreType;

    @Parameter(names = {"-P", "--outstorepass"}, defaultValueDescription = "The default value is '" + DEFAULT_STORE_PASSWORD + "'.", description = "The password for the output keystore.")
    private String outStorePass = DEFAULT_STORE_PASSWORD;

    @Parameter(names = {"-F", "--outfile"}, description = "If specified the current trust profile will be written to a single PEM encoded file.")
    private Path outfile;

    @Parameter(names = {"-O", "--outdir"}, description = "If specified the current trust profile will be written to individual PEM encoded files in the specified directory, one certificate per file.")
    private Path outdir;

    @Parameter(names = {"--simplify"}, description = "If this flag is set the output trust will contain the minimal set of trust material to validate the input domains.")
    private boolean simplify;

    @Parameter(names = {"--complete"}, description = "If this flag is set the output trust will close the gaps in the missing trust material, adding new certificates so that all domains are trusted.")
    private boolean complete;

    @Parameter(names = {"--completion-mode"}, description = "Specifies the mode to use when completion is requested. In `DIRECT` mode the domain certificate will be added to the trust, in `FIRST_SUBORDINATE` mode the first subordinate certificate from the domain will be added. In `MOST_TRUSTED` mode the deepest certificate in the chain will be trusted.", defaultValueDescription = "The default value is `FIRST_SUBORDINATE` which allows a narrower trust to be accepted.")
    private CompletionMode completionMode = CompletionMode.FIRST_SUBORDINATE;

    @Parameter(names = {"--no-verify"}, description = "If this flag is set then the connection to verify a domain after finding the appropriate trust will not be made. (Skips the verify step after determining trust.)")
    private boolean noVerify = false;

    @Parameter(names = {"--no-domains"}, description = "If this flag is set then no domains will be checked. (Acknowledges the \"no domains\" error and continues to be able to output trust sources.)")
    private boolean noDomains = false;

    @Parameter(names = {"-h", "--help"}, description = "Show the help message.", help = true)
    private boolean help;

    @Parameter(names = {"-v", "--version"}, description = "Show version information")
    private boolean version;

    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    public List<File> getDomainFiles() {
        return domainFiles;
    }

    public void setDomainFiles(List<File> domainFiles) {
        this.domainFiles = domainFiles;
    }

    public List<String> getSource() {
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }

    public String getStorePass() {
        return storePass;
    }

    public void setStorePass(String storePass) {
        this.storePass = storePass;
    }

    public Path getOutstore() {
        return outstore;
    }

    public void setOutstore(Path outstore) {
        this.outstore = outstore;
    }

    public String getOutstoreType() {
        return outstoreType;
    }

    public void setOutstoreType(String outstoreType) {
        this.outstoreType = outstoreType;
    }

    public String getOutStorePass() {
        return outStorePass;
    }

    public void setOutStorePass(String outStorePass) {
        this.outStorePass = outStorePass;
    }

    public boolean isSimplify() {
        return simplify;
    }

    public void setSimplify(boolean simplify) {
        this.simplify = simplify;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isNoVerify() {
        return noVerify;
    }

    public void setNoVerify(boolean noVerify) {
        this.noVerify = noVerify;
    }

    public Path getOutfile() {
        return outfile;
    }

    public void setOutfile(Path outfile) {
        this.outfile = outfile;
    }

    public Path getOutdir() {
        return outdir;
    }

    public void setOutdir(Path outdir) {
        this.outdir = outdir;
    }

    public CompletionMode getCompletionMode() {
        return completionMode;
    }

    public void setCompletionMode(CompletionMode completionMode) {
        this.completionMode = completionMode;
    }

    public boolean isNoDomains() {
        return noDomains;
    }

    public void setNoDomains(boolean noDomains) {
        this.noDomains = noDomains;
    }

    public boolean isHelp() {
        return help;
    }

    public void setHelp(boolean help) {
        this.help = help;
    }

    public boolean isVersion() {
        return version;
    }

    public void setVersion(boolean version) {
        this.version = version;
    }
}
