package io.github.chrisruffalo.taverna.opt;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DefaultOptions implements Options {

    private List<String> domains = new ArrayList<>(0);

    private List<Path> domainFiles = new ArrayList<>(0);

    private List<String> source = new ArrayList<>(0);

    private String storePass = DEFAULT_STORE_PASSWORD;

    private Path outstore;

    private String outstoreType;

    private String outStorePass = DEFAULT_STORE_PASSWORD;

    private Path outfile;

    private Path outdir;

    private boolean simplify;

    private boolean complete;

    private CompletionMode completionMode = CompletionMode.FIRST_SUBORDINATE;

    private boolean noVerify = false;

    private boolean noDomains = false;

    private boolean help;

    private boolean version;

    @Override
    public List<String> getDomains() {
        return domains;
    }

    public void setDomains(List<String> domains) {
        this.domains = domains;
    }

    @Override
    public List<Path> getDomainFiles() {
        return domainFiles;
    }

    public void setDomainFiles(List<Path> domainFiles) {
        this.domainFiles = domainFiles;
    }

    @Override
    public List<String> getSource() {
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }

    @Override
    public String getStorePass() {
        return storePass;
    }

    public void setStorePass(String storePass) {
        this.storePass = storePass;
    }

    @Override
    public Path getOutstore() {
        return outstore;
    }

    public void setOutstore(Path outstore) {
        this.outstore = outstore;
    }

    @Override
    public String getOutstoreType() {
        return outstoreType;
    }

    public void setOutstoreType(String outstoreType) {
        this.outstoreType = outstoreType;
    }

    @Override
    public String getOutStorePass() {
        return outStorePass;
    }

    public void setOutStorePass(String outStorePass) {
        this.outStorePass = outStorePass;
    }

    @Override
    public boolean isSimplify() {
        return simplify;
    }

    public void setSimplify(boolean simplify) {
        this.simplify = simplify;
    }

    @Override
    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    @Override
    public boolean isNoVerify() {
        return noVerify;
    }

    public void setNoVerify(boolean noVerify) {
        this.noVerify = noVerify;
    }

    @Override
    public Path getOutfile() {
        return outfile;
    }

    public void setOutfile(Path outfile) {
        this.outfile = outfile;
    }

    @Override
    public Path getOutdir() {
        return outdir;
    }

    public void setOutdir(Path outdir) {
        this.outdir = outdir;
    }

    @Override
    public CompletionMode getCompletionMode() {
        return completionMode;
    }

    public void setCompletionMode(CompletionMode completionMode) {
        this.completionMode = completionMode;
    }

    @Override
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
