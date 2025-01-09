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
the details are of exactly why are not obvious. That error, and similar errors, only let you know that _something_
is wrong or missing in your trust material and `taverna` is here to help figure out specifically _what_ is wrong.

Yes, there are tools and commands that could do these things individually but they have to be chained
together. The intent of `taverna` is to put all of those things under one roof, so to speak, and allow
developers/deployers/maintainers to create "tailored trust" packages for applications.

## Goals
Taverna is designed to take a list of domains and a set of trust material and ensure that, based on that trust,
every single domain can be verified/trusted. Taverna also gives good output to show relevant details of the
trust that is loaded from disk or expected by a domain. It can also, optionally, create configuration output for 
applications to use that trust. It can also create single unified trust sources (directory, file, or truststore) that 
contain all the trust expected to verify the given domains. Finally, it can find gaps in the trust and fill those using the 
certificates advertised by the domains.

## Running Taverna
Each release of `taverna` provides an executable Java JAR file and binaries for windows, linux, and macOS (arm64). To
execute `taverna` download the appropriate executable for the target platform and execute it as a command. For
conformity this documentation uses the executable JAR but every binary accepts the same inputs
and works the same way. The artifact name for the Java JAR will be `taverna-${release version}-executable.jar` and is
presented as `taverna.jar` for succinctness.

A container image is also provided at `https://hub.docker.com/r/chrisruffalo/taverna` and can be run similarly to the executable commands the chosen container runtime.
```shell
[]$ podman run docker.io/chrisruffalo/taverna:1.2 --version
taverna - 1.2
```
Checksums for each build are also provided by the build system. For more information about verifying the software 
provenance read the [build verification documentation](docs/BUILD_VERIFICATION.md).

## Usage and Examples
For a full listing of usage examples [see here](docs/EXAMPLES.md).

### Simple Trust Verification
Given a certificate (google-r1.pem) that contains the root certificate for `google.com` (and other domains) as provided
by the Google Trust repository it can be verified against the `google.com` domain.
```shell
[]$ java -jar taverna.jar -s google-r1.pem -d google.com
loaded 1 certificates from file google-r1.pem
        [serial=203e5936f31b01349886ba217] CN=GTS Root R1,O=Google Trust Services LLC,C=US [d947432abde7b7fa90fc2e6b59101b1280e0e1c7e4e40fa3c6887fff57a7f4cf] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US]
loaded 1 total certificates
certificate chain from google.com:
        hostname verified
        [serial=d11170199087111609fd1685afb67434] CN=*.google.com [f2287b3a1b41cf6f0173ac057fc02a9bfe4d0f09109629d37391d257f5a4be47] [issuer=CN=WR2,O=Google Trust Services,C=US, not in trust, not trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, in trust, trusted] 
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not in trust, not trusted] 
        trusted
        anchored by: [serial=203e5936f31b01349886ba217] CN=GTS Root R1,O=Google Trust Services LLC,C=US [d947432abde7b7fa90fc2e6b59101b1280e0e1c7e4e40fa3c6887fff57a7f4cf]
        verified trusted connection

```
All the information the loaded certificates (the trust store) and the domain, as well as the certificate anchors the chain, is
presented in the output. There are more examples [in the documentation](docs/EXAMPLES.md).

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
| `--version`         | `-v`    | If present the version of the executable is printed and then the process exits.                                                                                                                                                                                                                                                                                                                                           |               |
| `--help`            | `-h`    | If present the help message is printed and the process exits.                                                                                                                                                                                                                                                                                                                                                             |               |


### Real World Use
For more information about a real-world use-case for `taverna` and tailored trust see [the tailored trust documentation](docs/TAILORED_TRUST.md).

## Building
A normal java build uses standard maven commands.
```shell
[]$ mvn clean install
```
If you have a graal-style VM installed and all the binary prerequisites you can build the native package for your platform.
```shell
[]$ mvn clean install -Pnative
```

## Notes
Unless otherwise stated all truststores in this project use the password "changeit". None of
these truststores are used for anything other than testing.