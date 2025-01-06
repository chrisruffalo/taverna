package io.github.chrisruffalo.taverna.pki.validate;

import io.github.chrisruffalo.taverna.error.Codes;
import io.github.chrisruffalo.taverna.model.Cert;

import java.util.ArrayList;
import java.util.List;

public class ValidationStatus {

    private int returnCode = Codes.OK;

    private boolean valid = false;

    private List<Cert> usedCerts = new ArrayList<>(0);

    private List<Cert> missingCerts = new ArrayList<>(0);

    public int getReturnCode() {
        return returnCode;
    }

    public void setReturnCode(int returnCode) {
        this.returnCode = returnCode;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public List<Cert> getUsedCerts() {
        return usedCerts;
    }

    public void setUsedCerts(List<Cert> usedCerts) {
        this.usedCerts = usedCerts;
    }

    public List<Cert> getMissingCerts() {
        return missingCerts;
    }

    public void setMissingCerts(List<Cert> missingCerts) {
        this.missingCerts = missingCerts;
    }
}
