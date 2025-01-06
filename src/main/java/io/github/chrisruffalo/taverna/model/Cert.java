package io.github.chrisruffalo.taverna.model;

import io.github.chrisruffalo.resultify.Result;

import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.*;

/**
 * basic details of a cert regardless of source
 */
public class Cert {

    private Date notBefore;

    private Date notAfter;

    private String serialNumber;

    private String subject;

    private String subjectUniqueId;

    private String issuer;

    private String issuerUniqueId;

    private String thumbprint;

    private byte[] encoded;

    private Certificate original;

    private Set<String> alternateNames = new HashSet<>();

    public Date getNotBefore() {
        return notBefore;
    }

    public void setNotBefore(Date notBefore) {
        this.notBefore = notBefore;
    }

    public Date getNotAfter() {
        return notAfter;
    }

    public void setNotAfter(Date notAfter) {
        this.notAfter = notAfter;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSubjectUniqueId() {
        return subjectUniqueId;
    }

    public void setSubjectUniqueId(String subjectUniqueId) {
        this.subjectUniqueId = subjectUniqueId;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }

    public String getIssuerUniqueId() {
        return issuerUniqueId;
    }

    public void setIssuerUniqueId(String issuerUniqueId) {
        this.issuerUniqueId = issuerUniqueId;
    }

    public String getThumbprint() {
        return thumbprint;
    }

    public void setThumbprint(String thumbprint) {
        this.thumbprint = thumbprint;
    }

    public byte[] getEncoded() {
        return encoded;
    }

    public void setEncoded(byte[] encoded) {
        this.encoded = encoded;
    }

    public Certificate getOriginal() {
        return original;
    }

    public void setOriginal(Certificate original) {
        this.original = original;
    }

    public void setAlternateNames(Set<String> alternateNames) {
        this.alternateNames = alternateNames;
    }

    public Set<String> getAlternateNames() {
        return Collections.unmodifiableSet(this.alternateNames);
    }

    public boolean isSelfSigned() {
        if (this.original instanceof final X509Certificate x509Certificate) {
            return Objects.equals(x509Certificate.getIssuerX500Principal(), x509Certificate.getSubjectX500Principal());
        }
        return false;
    }

    /**
     * Static method for calculating thumbprint
     *
     * @param encoded certificate value
     * @return consistent thumbprint (SHA-1 hash)
     */
    public static String thumbprint(byte[] encoded) {
        return Result.of(encoded)
                .map(enc -> {
                    final MessageDigest md = MessageDigest.getInstance("SHA-256");
                    return HexFormat.of().formatHex(md.digest(enc));
                }).getOrFailsafe("");
    }

    public String toString() {
        return String.format("[serial=%s] %s [%s]", serialNumber, subject, thumbprint);
    }

    public boolean isIssuedBy(final Cert issuer) {
        // simple barrier to entry, does the issuer name match the subject
        if (!Objects.equals(this.getIssuer(), issuer.getSubject())) {
            return false;
        }

        // incomplete data
        if (this.original == null || issuer.getOriginal() == null) {
            return false;
        }

        return Result.from(() -> {
            this.getOriginal().verify(issuer.getOriginal().getPublicKey());
            return true;
        }).getOrFailsafe(false);
    }

    public String getPemEncoded() {
        return "-----BEGIN CERTIFICATE-----\n" +
                Base64.getMimeEncoder(64, new byte[]{'\n'}).encodeToString(this.getEncoded()) +
                "\n-----END CERTIFICATE-----";
    }

    public static Cert fromX509(X509Certificate inputCertificate) {
        final Cert rep = new Cert();
        rep.setSubject(inputCertificate.getSubjectX500Principal().getName());
        if (rep.getSubject().isEmpty()) {
            rep.setSubject("(UNKNOWN)");
        }
        rep.setIssuer(inputCertificate.getIssuerX500Principal().getName());
        rep.setNotAfter(inputCertificate.getNotAfter());
        rep.setNotBefore(inputCertificate.getNotBefore());

        rep.setIssuerUniqueId(getUniqueId(inputCertificate.getIssuerUniqueID()));
        rep.setSubjectUniqueId(getUniqueId(inputCertificate.getSubjectUniqueID()));

        rep.setSerialNumber(inputCertificate.getSerialNumber().toString(16)); // as hex string

        // calculate thumbprint
        rep.setEncoded(Result.from(inputCertificate::getEncoded).getOrFailsafe(new byte[0]));
        rep.setThumbprint(Cert.thumbprint(rep.getEncoded()));

        try {
            Collection<List<?>> alternativeNameReturn = inputCertificate.getSubjectAlternativeNames();
            if (alternativeNameReturn != null) {
                List<String> names = alternativeNameReturn
                        .stream()
                        .flatMap(List::stream)
                        .filter(x -> x instanceof String)
                        .map(String::valueOf)
                        .toList();
                Set<String> nameSet = new HashSet<>(names);
                rep.setAlternateNames(nameSet);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }

        // keep original
        rep.setOriginal(inputCertificate);

        return rep;
    }

    private static String getUniqueId(boolean[] input) {
        if (input == null) {
            return "";
        }

        final BitSet bitSet = new BitSet(input.length);
        for (int i = 0; i < input.length; i++) {
            if (input[i]) {
                bitSet.set(i);
            }
        }
        return HexFormat.of().formatHex(bitSet.toByteArray());
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Cert cert)) return false;
        return Objects.equals(thumbprint, cert.thumbprint);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(thumbprint);
    }
}
