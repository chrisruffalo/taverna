# Ταβερνα (Taverna)
Taverna gets its name from the phrase "_**T**rust **A**nd **V**erify_". It is a tool for comparing an applications
current trust material against the domains to trust and ensuring that the trust chain is correct before
finding out the hard way (in production, at night, while on PTO).

## Problem
Taverna comes from having to deliver immutable applications to various environments (production and non-production) that
have different trust profiles. There are a lot of ways to handle this mechanically but that won't stop
phone calls out of hours if some remote host changes their certificates and it won't help debug the trust
issues in the face of esoteric or meaningless error messages.

How many of us are tired of "PKIX path building to target" errors? Even if the error is understood sometimes
the details are of exactly why are not obvious.

Yes, there are tools and commands that could do these things individually but they have to be chained
together. The intent of `taverna` is to put all of those things under one roof, so to speak, and allow
developers/deployers/maintainers to create "tailored trust" packages for applications.

## Goals
Taverna is designed to take a list of domains and a set of trust material and ensure that, based on that trust,
every single domain can be verified/trusted. Taverna also gives good output to show relevant details of the
trust that is loaded from disk or expected by a domain. It can also, optionally, create configuration output for Java applications
to use that trust. It can also create single unified trust sources (directory, file, or truststore) that contain
all the trust expected to verify the given domains. Finally, it can find gaps in the trust and fill those using the certificates advertised by the domains.

## Usage Model
Taverna should be used in build or deployment pipelines to ensure that the correct trust is included
for the application environment. Ideally build or a deployment would fail if the new application would
fail to trust the remote endpoints it needs to communicate with _or_ the trust could be automatically updated.
Automatic trust updating is only recommended at development time when the output can be manually
verified instead of using it blindly.

## Running Taverna
Each release of `taverna` provides an executable Java JAR file and binaries for windows, linux, and macOS (arm64). To
execute `taverna` download the appropriate executable for your platform and execute it as a command on your path. For
conformity this documentation assumes you are using the executable JAR but every binary accepts the same inputs
and works the same way. The artifact name for the Java JAR will be `taverna-${release version}-executable.jar` and is
presented as `taverna.jar` for succinctness.

A container image is also provided at `https://hub.docker.com/r/chrisruffalo/taverna` and can be run similarly to the executable commands in your container runtime
of choice.
```shell
[]$ podman run docker.io/chrisruffalo/taverna:1.1 --version
taverna - 1.1
```

Artifact signature attestations are also provided with each build. In order to verify an artifact you will need
the `gh` command from [GitHub](https://github.com/cli/cli/releases), the `attestation.json` file from the release, and the release artifact itself.
```shell
# for release 1.1
gh attestation verify -R chrisruffalo/taverna -b attestation.json taverna-1.1-linux-amd64 
```
The main difference between this and a checksum file is that this proves that the file came from the GitHub build system and
was not replaced (along with the checksum file) for malicious purposes.


## Usage and Examples

### Determining the Trust Chain for a Domain
In the most basic sense `taverna` allows inspection of the trust chain of a single domain(:port). While the `openssl s_client`
command _can_ (and probably also should) do this the view taverna provides is a little more succinct.
```shell
# view the trust of a single domain
[]$ java -jar taverna.jar -d google.com
loaded 0 total certificates
certificate chain from google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, not trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not trusted] 
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not trusted] 
        not trusted
```
This output has the entire trust chain and if the hostname (domain) of the service matches one of the names provided in the certificate.
Also provided are the DNs (distinguished names) of the certificate and the issuer as well as the serial and SHA-256 thumbrint.

Multiple domains can be specified. A port can be specified by appending ":\<port\>" to the domain name.
```shell
[]$ java -jar taverna.jar -d google.com -d amazon.com:443
loaded 0 total certificates
certificate chain from google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, not trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not trusted] 
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not trusted] 
        not trusted
certificate chain from amazon.com:
        hostname verified
        [serial=3ae261164db2d8f280b8821f48199c6] CN=*.peg.a2z.com [f98ecfdb6fcffa280d8ff68be54376847608b885da73589f2c5728440c3c3b64] [issuer=CN=DigiCert Global CA G2,O=DigiCert Inc,C=US, not trusted] 
        [serial=c8ee0c90d6a89158804061ee241f9af] CN=DigiCert Global CA G2,O=DigiCert Inc,C=US [8fac576439c9fd3ef153b51f9edd0d381b5df7b87559cebeca04297dd44a639b] [issuer=CN=DigiCert Global Root G2,OU=www.digicert.com,O=DigiCert Inc,C=US, not trusted] 
        [serial=4b2b0115cde5c7481b3cddfede11169e] CN=DigiCert Global Root G2,OU=www.digicert.com,O=DigiCert Inc,C=US [aadadd5a879d2eb8c41a89597291292709d42052f5b6399541c694c3b7353cd1] [issuer=CN=VeriSign Class 3 Public Primary Certification Authority - G5,OU=(c) 2006 VeriSign\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\, Inc.,C=US, not trusted] 
        not trusted
```

### Validating Trust for a Domain
Once the trust for a domain has been inspected the best thing to do is look for the certificate to add to
the trust material for the application. In the above example there are three certificates in the trust chain with the last
one being signed by an issuer that is not provided in the chain. In order to trust this certificate one of the certificates in the chain
should be added into the application's trust store _or_ the "CN=GTS Root R1" certificate could be downloaded from Google
and added. The "CN=GlobalSign Root CA" could also be downloaded and added. To test the trust against a domain the trust sources need
to be added. A trust source can be a PEM or DER encoded single certificate file, a file with multiple certificates, a directory or
directory tree, or a Java truststore.
```shell
# check the trust of a domain against a trust source
[]$ java -jar taverna.jar -s google-r1.pem -d google.com
loaded 1 certificates from file google-r1.pem
        [serial=203e5936f31b01349886ba217] CN=GTS Root R1,O=Google Trust Services LLC,C=US [d947432abde7b7fa90fc2e6b59101b1280e0e1c7e4e40fa3c6887fff57a7f4cf] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
loaded 1 total certificates
certificate chain from google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, not trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, trusted] 
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not trusted] 
        trusted
        anchored by: [serial=203e5936f31b01349886ba217] CN=GTS Root R1,O=Google Trust Services LLC,C=US [d947432abde7b7fa90fc2e6b59101b1280e0e1c7e4e40fa3c6887fff57a7f4cf]
        verified trusted connection
```
The output of this command establishes that one certificate was loaded, that it can be used to trust the domain google.com,
and that it is _different_ from the cross-signed certificate in the certificate chain returned by the domain (but cryptographically contains the same material).

This same command can be run against the subordinate certificate from Google.
```shell
# check trust using subordinate certificate
[]$ java -jar taverna.jar -s google-wr2.pem -d google.com
loaded 1 certificates from file google-wr2.pem
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
loaded 1 total certificates
certificate chain from google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not trusted] (in store)
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not trusted] 
        trusted
        anchored by: [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4]
        verified trusted connection
```
Note that the trust is anchored bny a different certificate (CN=WR2) in this case.

The trust for multiple domains can be checked as well based on a single trust configuration.
```shell
[]$ java -jar taverna.jar -s google-wr2.pem -d google.com -d amazon.com
loaded 1 certificates from file google-wr2.pem
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
loaded 1 total certificates
certificate chain from google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not trusted] (in store)
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not trusted] 
        trusted
        anchored by: [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4]
        verified trusted connection
certificate chain from amazon.com:
        hostname verified
        [serial=3ae261164db2d8f280b8821f48199c6] CN=*.peg.a2z.com [f98ecfdb6fcffa280d8ff68be54376847608b885da73589f2c5728440c3c3b64] [issuer=CN=DigiCert Global CA G2,O=DigiCert Inc,C=US, not trusted] 
        [serial=c8ee0c90d6a89158804061ee241f9af] CN=DigiCert Global CA G2,O=DigiCert Inc,C=US [8fac576439c9fd3ef153b51f9edd0d381b5df7b87559cebeca04297dd44a639b] [issuer=CN=DigiCert Global Root G2,OU=www.digicert.com,O=DigiCert Inc,C=US, not trusted] 
        [serial=4b2b0115cde5c7481b3cddfede11169e] CN=DigiCert Global Root G2,OU=www.digicert.com,O=DigiCert Inc,C=US [aadadd5a879d2eb8c41a89597291292709d42052f5b6399541c694c3b7353cd1] [issuer=CN=VeriSign Class 3 Public Primary Certification Authority - G5,OU=(c) 2006 VeriSign\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\, Inc.,C=US, not trusted] 
        not trusted: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
[]$ echo $?
3    
```
In this case the domain `google.com` is trusted (anchored by CN=WR2) but the `amazon.com` domain is not trusted. The return
code is non-zero in this case (3).

### Creating Trust Output
The `taverna` command supports multiple output types:
* A directory that will be populated with the PEM-encoded certificates being used as trust sources
* A single file that will contain the PEM-encoded certificates being used as trust sources
* A Java PKCS12 trust store that will contain the certificates being used as trust sources

The `taverna` command can be used to copy the trust sources into a single collated output with or without
validation of domains. This is a convenience feature to reduce the number of commands required for trust management.
It is analogous to the `openssl x509 -in -out`, `keytool -import`, and `keytool -export` commands. It replaces
most options with strong defaults (opinions).

In the simplest scenario the certificate chosen for the domain google.com is verified and written to the trust store.
The default trust store password of 'changeit' will be used unless one is specified.
```shell
[]$ java -jar taverna.jar -s google-wr2.pem -d google.com -outstore trust.p12 -outstorepass "n3wtRust!"
loaded 1 certificates from file google-wr2.pem
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
loaded 1 total certificates
certificate chain from google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not trusted] (in store)
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not trusted] 
        trusted
        anchored by: [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4]
        verified trusted connection
wrote trust store to 'trust.p12'
```

If **any** of the domains specified fail validation then no output files will be written.
```shell
[]$ java -jar taverna.jar -s google-wr2.pem -d google.com -d amazon.com --outstore trust.p12 --outstorepass "n3wtRust!"
loaded 1 certificates from file google-wr2.pem
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
loaded 1 total certificates
certificate chain from google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not trusted] (in store)
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not trusted] 
        trusted
        anchored by: [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4]
        verified trusted connection
certificate chain from amazon.com:
        hostname verified
        [serial=3ae261164db2d8f280b8821f48199c6] CN=*.peg.a2z.com [f98ecfdb6fcffa280d8ff68be54376847608b885da73589f2c5728440c3c3b64] [issuer=CN=DigiCert Global CA G2,O=DigiCert Inc,C=US, not trusted] 
        [serial=c8ee0c90d6a89158804061ee241f9af] CN=DigiCert Global CA G2,O=DigiCert Inc,C=US [8fac576439c9fd3ef153b51f9edd0d381b5df7b87559cebeca04297dd44a639b] [issuer=CN=DigiCert Global Root G2,OU=www.digicert.com,O=DigiCert Inc,C=US, not trusted] 
        [serial=4b2b0115cde5c7481b3cddfede11169e] CN=DigiCert Global Root G2,OU=www.digicert.com,O=DigiCert Inc,C=US [aadadd5a879d2eb8c41a89597291292709d42052f5b6399541c694c3b7353cd1] [issuer=CN=VeriSign Class 3 Public Primary Certification Authority - G5,OU=(c) 2006 VeriSign\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\, Inc.,C=US, not trusted] 
        not trusted: PKIX path building failed: sun.security.provider.certpath.SunCertPathBuilderException: unable to find valid certification path to requested target
there was an error during domain validation, no certificate output was written
```

The `taverna` command can also simplify trust. While this is always a rough guess it tries to use only
the first certificate (least trusted) that is found for a given domain. This makes the most restrictive
trust output from the given input which minimizes the chance of trusting unintended domains.
```shell
[]$ java -jar taverna.jar -s google-wr2.pem -s google-r1.pem -s globalsign.pem -d google.com --outstore trust.p12 --outstorepass "n3wtRust!"
loaded 1 certificates from file google-wr2.pem
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
loaded 1 certificates from file google-r1.pem
        [serial=203e5936f31b01349886ba217] CN=GTS Root R1,O=Google Trust Services LLC,C=US [d947432abde7b7fa90fc2e6b59101b1280e0e1c7e4e40fa3c6887fff57a7f4cf] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
loaded 1 certificates from file globalsign.pem
        [serial=40000000001154b5ac394] CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE [ebd41040e4bb3ec742c9e381d31ef2a41a48b6685c96e7cef3c1df6cd4331c99] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE]
loaded 3 total certificates
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, trusted] (in store)
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, trusted] 
        trusted
        anchored by: [serial=40000000001154b5ac394] CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE [ebd41040e4bb3ec742c9e381d31ef2a41a48b6685c96e7cef3c1df6cd4331c99]
        verified trusted connection
simplified trust (1 entries):
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4]
wrote trust store to 'trust.p12'
```
The simplified trust only contains one entry, the first certificate from among the trust sources that could
be used to trust the chain of certificates returned by the target domain.

This command is more useful if you have an entire pile of certificates that you want to load and just let `taverna`
sort it out.
```shell
[]$ java -jar taverna.jar -s pki -d google.com -d amazon.com --outstore trust.p12 --outstorepass "n3wtRust!"
loaded 17 certificates from directory src/test/resources/pem
        [serial=203e5aec58d04251aab1125aa] CN=GTS Root R2,O=Google Trust Services LLC,C=US [8d25cd97229dbf70356bda4eb3cc734031e24cf00fafcfd32dc76eb5841c7ea8] [issuer=CN=GTS Root R2,O=Google Trust Services LLC,C=US]
        [serial=203e57ef53f93fda50921b2a6] CN=GlobalSign,O=GlobalSign,OU=GlobalSign ECC Root CA - R4 [b085d70b964f191a73e4af0d54ae7a0e07aafdaf9b71dd0862138ab7325a24a2] [issuer=CN=GlobalSign,O=GlobalSign,OU=GlobalSign ECC Root CA - R4]
        [serial=7fd9e2c2d2048a0474b627a26d0868a7] CN=WR1,O=Google Trust Services,C=US [b10b6f00e609509e8700f6d34687a2bfce38ea05a8fdf1cdc40c3a2a0d0d0e45] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
        [serial=7fd9e2c2d2048a0474b627a26d0868a7] CN=WR1,O=Google Trust Services,C=US [b10b6f00e609509e8700f6d34687a2bfce38ea05a8fdf1cdc40c3a2a0d0d0e45] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
        [serial=203e5936f31b01349886ba217] CN=GTS Root R1,O=Google Trust Services LLC,C=US [d947432abde7b7fa90fc2e6b59101b1280e0e1c7e4e40fa3c6887fff57a7f4cf] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
        [serial=203e5c068ef631a9c72905052] CN=GTS Root R4,O=Google Trust Services LLC,C=US [349dfa4058c5e263123b398ae795573c4e1313c83fe68f93556cd5e8031b3c7d] [issuer=CN=GTS Root R4,O=Google Trust Services LLC,C=US]
        [serial=203e5b882eb20f825276d3d66] CN=GTS Root R3,O=Google Trust Services LLC,C=US [34d8a73ee208d9bcdb0d956520934b4e40e69482596e8b6f73c8426b010a6f48] [issuer=CN=GTS Root R3,O=Google Trust Services LLC,C=US]
        [serial=77312380b9d6688a33b1ed9bf9ccda68e0e0f] CN=Amazon RSA 2048 M01,O=Amazon,C=US [5338ebec8fb2ac60996126d3e76aa34fd0f3318ac78ebb7ac8f6f1361f484b33] [issuer=CN=Amazon Root CA 1,O=Amazon,C=US]
        [serial=250ce8e030612e9f2b89f7054d7cf8fd] CN=VeriSign Class 3 Public Primary Certification Authority - G5,OU=(c) 2006 VeriSign\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\, Inc.,C=US [8420dfbe376f414bf4c0a81e6936d24ccc03f304835b86c7a39142fca723a689] [issuer=OU=Class 3 Public Primary Certification Authority,O=VeriSign\, Inc.,C=US]
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
        [serial=203e5936f31b01349886ba217] CN=GTS Root R1,O=Google Trust Services LLC,C=US [d947432abde7b7fa90fc2e6b59101b1280e0e1c7e4e40fa3c6887fff57a7f4cf] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
        [serial=203e5aec58d04251aab1125aa] CN=GTS Root R2,O=Google Trust Services LLC,C=US [8d25cd97229dbf70356bda4eb3cc734031e24cf00fafcfd32dc76eb5841c7ea8] [issuer=CN=GTS Root R2,O=Google Trust Services LLC,C=US]
        [serial=203e5b882eb20f825276d3d66] CN=GTS Root R3,O=Google Trust Services LLC,C=US [34d8a73ee208d9bcdb0d956520934b4e40e69482596e8b6f73c8426b010a6f48] [issuer=CN=GTS Root R3,O=Google Trust Services LLC,C=US]
        [serial=203e5c068ef631a9c72905052] CN=GTS Root R4,O=Google Trust Services LLC,C=US [349dfa4058c5e263123b398ae795573c4e1313c83fe68f93556cd5e8031b3c7d] [issuer=CN=GTS Root R4,O=Google Trust Services LLC,C=US]
        [serial=203e57ef53f93fda50921b2a6] CN=GlobalSign,O=GlobalSign,OU=GlobalSign ECC Root CA - R4 [b085d70b964f191a73e4af0d54ae7a0e07aafdaf9b71dd0862138ab7325a24a2] [issuer=CN=GlobalSign,O=GlobalSign,OU=GlobalSign ECC Root CA - R4]
        [serial=40000000001154b5ac394] CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE [ebd41040e4bb3ec742c9e381d31ef2a41a48b6685c96e7cef3c1df6cd4331c99] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE]
loaded 17 total certificates
certificate chain from google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, in trust, trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, in trust, trusted] (in store)
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, in trust, trusted] 
        trusted
        anchored by: [serial=40000000001154b5ac394] CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE [ebd41040e4bb3ec742c9e381d31ef2a41a48b6685c96e7cef3c1df6cd4331c99]
        verified trusted connection
certificate chain from amazon.com:
        hostname verified
        [serial=3ae261164db2d8f280b8821f48199c6] CN=*.peg.a2z.com [f98ecfdb6fcffa280d8ff68be54376847608b885da73589f2c5728440c3c3b64] [issuer=CN=DigiCert Global CA G2,O=DigiCert Inc,C=US, not in trust, not trusted] 
        [serial=c8ee0c90d6a89158804061ee241f9af] CN=DigiCert Global CA G2,O=DigiCert Inc,C=US [8fac576439c9fd3ef153b51f9edd0d381b5df7b87559cebeca04297dd44a639b] [issuer=CN=DigiCert Global Root G2,OU=www.digicert.com,O=DigiCert Inc,C=US, not in trust, not trusted] 
        [serial=4b2b0115cde5c7481b3cddfede11169e] CN=DigiCert Global Root G2,OU=www.digicert.com,O=DigiCert Inc,C=US [aadadd5a879d2eb8c41a89597291292709d42052f5b6399541c694c3b7353cd1] [issuer=CN=VeriSign Class 3 Public Primary Certification Authority - G5,OU=(c) 2006 VeriSign\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\, Inc.,C=US, in trust, trusted] 
        trusted
        anchored by: [serial=250ce8e030612e9f2b89f7054d7cf8fd] CN=VeriSign Class 3 Public Primary Certification Authority - G5,OU=(c) 2006 VeriSign\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\, Inc.,C=US [8420dfbe376f414bf4c0a81e6936d24ccc03f304835b86c7a39142fca723a689]
        verified trusted connection
simplified trust (2 entries):
        [serial=250ce8e030612e9f2b89f7054d7cf8fd] CN=VeriSign Class 3 Public Primary Certification Authority - G5,OU=(c) 2006 VeriSign\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\, Inc.,C=US [8420dfbe376f414bf4c0a81e6936d24ccc03f304835b86c7a39142fca723a689]
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4]
wrote trust store to 'trust.p12'
```
Given the fairly broad start that was specified a final output trust with 2 entries is  chosen. This will better tailor
the trust to the environment. The trust can also be verified with the same store.
```shell
[]$ java -jar taverna.jar -s "trust.p12:n3wtRust!" -d google.com -d amazon.com
loaded 2 certificates from truststore trust.p12
        [serial=250ce8e030612e9f2b89f7054d7cf8fd] CN=VeriSign Class 3 Public Primary Certification Authority - G5,OU=(c) 2006 VeriSign\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\, Inc.,C=US [8420dfbe376f414bf4c0a81e6936d24ccc03f304835b86c7a39142fca723a689] [issuer=OU=Class 3 Public Primary Certification Authority,O=VeriSign\, Inc.,C=US]
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
loaded 2 total certificates
certificate chain from google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, in trust, trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not in trust, not trusted] (in store)
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not in trust, not trusted] 
        trusted
        anchored by: [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4]
        verified trusted connection
certificate chain from amazon.com:
        hostname verified
        [serial=3ae261164db2d8f280b8821f48199c6] CN=*.peg.a2z.com [f98ecfdb6fcffa280d8ff68be54376847608b885da73589f2c5728440c3c3b64] [issuer=CN=DigiCert Global CA G2,O=DigiCert Inc,C=US, not in trust, not trusted] 
        [serial=c8ee0c90d6a89158804061ee241f9af] CN=DigiCert Global CA G2,O=DigiCert Inc,C=US [8fac576439c9fd3ef153b51f9edd0d381b5df7b87559cebeca04297dd44a639b] [issuer=CN=DigiCert Global Root G2,OU=www.digicert.com,O=DigiCert Inc,C=US, not in trust, not trusted] 
        [serial=4b2b0115cde5c7481b3cddfede11169e] CN=DigiCert Global Root G2,OU=www.digicert.com,O=DigiCert Inc,C=US [aadadd5a879d2eb8c41a89597291292709d42052f5b6399541c694c3b7353cd1] [issuer=CN=VeriSign Class 3 Public Primary Certification Authority - G5,OU=(c) 2006 VeriSign\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\, Inc.,C=US, in trust, trusted] 
        trusted
        anchored by: [serial=250ce8e030612e9f2b89f7054d7cf8fd] CN=VeriSign Class 3 Public Primary Certification Authority - G5,OU=(c) 2006 VeriSign\, Inc. - For authorized use only,OU=VeriSign Trust Network,O=VeriSign\, Inc.,C=US [8420dfbe376f414bf4c0a81e6936d24ccc03f304835b86c7a39142fca723a689]
        verified trusted connection
```

### Building Trust
Perhaps the most dangerous (read: useful) feature offered by `taverna` is the "completion" option which attempts to "complete" the
trust chain automatically, closing gaps in the trust sources.
```shell
[]$ java -jar taverna.jar -d google.com --complete
loaded 0 total certificates
certificate chain from google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, not in trust, not trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not in trust, not trusted] 
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not in trust, not trusted] 
        not trusted
        adding to trusted material: [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4]
        connection verified with updated trust
```
Trust completion has several modes. The above example is in the default, "FIRST_SUBORDINATE", mode which will find the
first certificate beyond the domain's specific certificate.

Another mode is "DIRECT" meaning that the domain certificate itself will be added.
```shell
[]$ java -jar taverna.jar -d google.com --complete --completion-mode DIRECT
loaded 0 total certificates
certificate chain from google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, not in trust, not trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not in trust, not trusted] 
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not in trust, not trusted] 
        not trusted
        adding to trusted material: [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78]
        connection verified with updated trust
```

The final mode is "MOST_TRUSTED" which will add the deepest certificate (closest to the anchor) provided by the service.
```shell
[]$ java -jar taverna.jar -d google.com --complete --completion-mode MOST_TRUSTED
loaded 0 total certificates
certificate chain from google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, not in trust, not trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not in trust, not trusted] 
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not in trust, not trusted] 
        not trusted
        adding to trusted material: [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5]
        connection verified with updated trust
```

The completion option can be combined with other options to produce the output trust material.
```shell
[]$ java -jar taverna.jar -d mail.google.com -d calendar.google.com -d drive.google.com --simplify --complete --outstore google.p12 --outstorepass "google"
loaded 0 total certificates
certificate chain from mail.google.com:
        hostname verified
        [serial=65952a0b7b57007010fe42108c5eac0f] CN=mail.google.com [d85f91441c2325d79c0c9f9139c3dca4cc1374319c6258d4bdb818874c8ba704] [issuer=CN=WR2,O=Google Trust Services,C=US, not in trust, not trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not in trust, not trusted] 
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not in trust, not trusted] 
        not trusted
        adding to trusted material: [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4]
        connection verified with updated trust
certificate chain from calendar.google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, not in trust, not trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not in trust, not trusted] 
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not in trust, not trusted] 
        not trusted
        adding to trusted material: [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4]
        connection verified with updated trust
certificate chain from drive.google.com:
        hostname verified
        [serial=fc125bbd5b36ea6b123b0a5549884c9f] CN=*.google.com [40ab219abbf5008df6a5a254ef1de84c2025e0e2670f76d050eae39d5a7cdf78] [issuer=CN=WR2,O=Google Trust Services,C=US, not in trust, not trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not in trust, not trusted] 
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not in trust, not trusted] 
        not trusted
        adding to trusted material: [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4]
        connection verified with updated trust
simplified trust (1 entries):
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4]
wrote trust store to 'google.p12'
```

### Convenience Functions
The `taverna` command is also helpful for general trust viewing and manipulation. As demonstrated elsewhere in this
README `taverna` can be used to inspect the certificate chain from a given domain. The same thing is possible
with individual trust sources.

To inspect a single or multiple trust sources a command can be run in "no domain" mode with the flag "--no-domains"
which tells the command that it can ignore the error that normally happens when no domains are specified.
```shell
[]$ java -jar taverna.jar -s google-r1.pem -s google-wr2.pem --no-domains
loaded 1 certificates from file google-wr2.pem
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
loaded 1 certificates from file google-r1.pem
        [serial=203e5936f31b01349886ba217] CN=GTS Root R1,O=Google Trust Services LLC,C=US [d947432abde7b7fa90fc2e6b59101b1280e0e1c7e4e40fa3c6887fff57a7f4cf] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
loaded 2 total certificates
```

While the "--simplify" option will not work in this mode (because there are no domains to check against) the tool
can still be used to import/export and manage trust.
```shell
[]$ java -jar taverna.jar -s google-r1.pem -s google-wr2.pem --no-domains --outdir pki/google --outfile google-all.pem --outstore google.p12 --outstorepass "google"
loaded 1 certificates from file google-wr2.pem
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
loaded 1 certificates from file google-r1.pem
        [serial=203e5936f31b01349886ba217] CN=GTS Root R1,O=Google Trust Services LLC,C=US [d947432abde7b7fa90fc2e6b59101b1280e0e1c7e4e40fa3c6887fff57a7f4cf] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
loaded 2 total certificates
wrote trust store to 'google.p12'
writing 2 certificates to pki/google
        wrote [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] to 'pki/google/CN=WR2,O=Google Trust Services,C=US.pem'
        wrote [serial=203e5936f31b01349886ba217] CN=GTS Root R1,O=Google Trust Services LLC,C=US [d947432abde7b7fa90fc2e6b59101b1280e0e1c7e4e40fa3c6887fff57a7f4cf] to 'pki/google/CN=GTS Root R1,O=Google Trust Services LLC,C=US.pem'
wrote 2 certificates to file 'google-all.pem'
```
This feature set, provided for ease of use, allows users to manage certificates and trust sources with a single command.

### Command Line Options
| flag                | aliases | description                                                                                                                                                                                                                                                                                                                                                                                                               | default value |
|---------------------|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------|
| `--source`          | `-s`    | Source of trust. This can be a PEM-encoded file, a DER-encoded file, a Java trust store (JKS, JCEKS, or PKCS12), or a directory containing any combination of the above. If specifying a trust store the password for that specific store can be specified by appending a ":" and then the password (ex: "trust.p12:somepassword").                                                                                       |               |
| `--domain`          | `-d`    | Domain to test given trust against. The format of this input is either just the domain name (FQDN) or the domain name followed by a ":" and then the port to contact the service on (ex: "cloudflare.com:1103").                                                                                                                                                                                                          |               |
| `--domains`         | `-D`    | The path to a file containing a list of domain names for testing the trust against. The domain names in this file follow the same format as individual domain names.                                                                                                                                                                                                                                                      |               |
| `--storepass`       | `-p`    | The default store password that will be used for all Java trust stores if no password is provided for the specific store.                                                                                                                                                                                                                                                                                                 | changeit      |
| `--outstore`        | `-o`    | If specified the current trust profile will be written to the output keystore.                                                                                                                                                                                                                                                                                                                                            |               |
| `--outstorepass`    | `-P`    | The password for the output keystore.                                                                                                                                                                                                                                                                                                                                                                                     | changeit      | 
| `--outstoretype`    | `-T`    | Specifies the format of the output keystore.                                                                                                                                                                                                                                                                                                                                                                              | PKCS12        |
| `--outfile`         | `-F`    | If specified the current trust profile will be written to a single PEM encoded file.                                                                                                                                                                                                                                                                                                                                      |               |
| `--outdir`          | `-O`    | If specified the current trust profile will be written to individual PEM encoded files in the specified directory, one certificate per file.                                                                                                                                                                                                                                                                              |               |
| `--simplify`        |         | If this flag is set the output trust will contain the minimal set of trust material to validate the input domains.                                                                                                                                                                                                                                                                                                        |               |
| `--complete`        |         | If this flag is set the output trust will close the gaps in the missing trust material, adding new certificates so that all domains are trusted.                                                                                                                                                                                                                                                                          |               |
| `--completion-mode` |         | Specifies the mode to use when completion is requested. In `DIRECT` mode the domain certificate will be added to the trust, in `FIRST_SUBORDINATE` mode the first subordinate certificate from the domain will be added. In `MOST_TRUSTED` mode the deepest certificate in the chain will be trusted.", defaultValueDescription = "The default value is `FIRST_SUBORDINATE` which allows a narrower trust to be accepted. |               |
| `--no-verify`       |         | If this flag is set then the connection to verify a domain after finding the appropriate trust will not be made. (Skips the verify step after determining trust.) This option will reduce the number of outbound network connections and potential errors at the cost of skipping one step.                                                                                                                               |               |               
| `--no-domains`      |         | If this flag is set then no domains will be checked. (Acknowledges the "no domains" error and continues to be able to output trust sources.)                                                                                                                                                                                                                                                                              |               |

## Real-World Example of Tailored Trust
The clearest real-world use, aside from verifying trust stores, is enabling something that could be called "tailored trust".
What this means is that each deployment/environment/region has a specific trust set that is valid. This prevents
a small subset of exploits (or, more likely, configuration errors) by preventing the application from trusting
domains (servers) that it should not.

Let's take an application that has been built according to contemporary practices. For the sake of discussion it uses
auth0 for authentication in production but in development and staging it talks to an environment-specific Keycloak instance.
It probably wouldn't be an issue if development and staging deployments can trust the auth0 IDP but it _could_
be a potential issue if the production service could talk to either environment.

When developing the application maintainers could ensure that the application trusts the auth0 root certificate and add it to some
artifact that is used to deploy the application to production.
```shell
# verifies the domain auth0.com against the trust given (none) and completes the trust chain in a simple way,
# writing the trust artifact(s) to project/trust as individual certificates in PEM format.  
[]$ java -jar taverna.jar -d auth0.com --complete --outdir project/trust
loaded 0 total certificates
certificate chain from auth0.com:
        hostname verified
        [serial=3b3d6dd8d7b556f4f9826bfc0f4333e22bb] CN=auth0.com [00757d828571abe5ebfee3bb243861d7949b38c96cef3795dfd46a2ee3391077] [issuer=CN=E6,O=Let's Encrypt,C=US, not trusted] 
        [serial=b0573e9173972770dbb487cb3a452b38] CN=E6,O=Let's Encrypt,C=US [76e9e288aafc0e37f4390cbf946aad997d5c1c901b3ce513d3d8fadbabe2ab85] [issuer=CN=ISRG Root X1,O=Internet Security Research Group,C=US, not trusted] 
        not trusted
        adding to trusted material: [serial=b0573e9173972770dbb487cb3a452b38] CN=E6,O=Let's Encrypt,C=US [76e9e288aafc0e37f4390cbf946aad997d5c1c901b3ce513d3d8fadbabe2ab85]
        connection verified with updated trust
writing 1 certificates to project/trust
        wrote [serial=b0573e9173972770dbb487cb3a452b38] CN=E6,O=Let's Encrypt,C=US [76e9e288aafc0e37f4390cbf946aad997d5c1c901b3ce513d3d8fadbabe2ab85] to 'project/trust/CN=E6,O=Let's Encrypt,C=US.pem'
```

The output of this command shows a single certificate that the application _could_ trust in order to be able to verify the domain `auth0.com`.
```shell
[]$ java -jar taverna.jar -d auth0.com -s project/trust
loaded 1 certificates from directory project/trust
        [serial=b0573e9173972770dbb487cb3a452b38] CN=E6,O=Let's Encrypt,C=US [76e9e288aafc0e37f4390cbf946aad997d5c1c901b3ce513d3d8fadbabe2ab85] [issuer=CN=ISRG Root X1,O=Internet Security Research Group,C=US]
loaded 1 total certificates
certificate chain from auth0.com:
hostname verified
        [serial=3b3d6dd8d7b556f4f9826bfc0f4333e22bb] CN=auth0.com [00757d828571abe5ebfee3bb243861d7949b38c96cef3795dfd46a2ee3391077] [issuer=CN=E6,O=Let's Encrypt,C=US, trusted] 
        [serial=b0573e9173972770dbb487cb3a452b38] CN=E6,O=Let's Encrypt,C=US [76e9e288aafc0e37f4390cbf946aad997d5c1c901b3ce513d3d8fadbabe2ab85] [issuer=CN=ISRG Root X1,O=Internet Security Research Group,C=US, not trusted] (in store)
trusted
anchored by: [serial=b0573e9173972770dbb487cb3a452b38] CN=E6,O=Let's Encrypt,C=US [76e9e288aafc0e37f4390cbf946aad997d5c1c901b3ce513d3d8fadbabe2ab85]
verified trusted connection
```

Taverna can also be used at runtime or deployment time to verify that the trust the application is going to load is sufficient. A startup
script can be modified to check the trust and stop the application from starting if it is insufficient.
```bash
java -jar taverna.jar -d auth0.com -s not-enough.p12
EXIT_CODE=$?
if [[ "x0" != "x${EXIT_CODE}" ]]; then
  echo "certificates for auth0 could not be verified with the given trust store"
  exit ${EXIT_CODE}
fi
```
Between this message and the fairly noisy output of `taverna` the error can be quickly found which is helpful
when trying to quickly figure out the problem.

## Building
A normal java build uses standard maven commands.
```shell
[]$ mvn clean install
```
If you have a graal-style VM installed and all of the binary preerquisites you can build the native package for your platform.
```shell
[]$ mvn clean install -Pnative
```

## Notes
Unless otherwise stated all truststores in this project use the password "changeit". None of
these truststores are used for anything other than testing.