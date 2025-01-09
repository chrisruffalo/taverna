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

In development and testing environments you would only be validating against the trust stores required for the internal IDP instances
using the cluster or organizational trust artifacts. In that case you would want to _make sure_ that you _couldn't_ trust the `auth0.com`
domain.
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
You could also set it up in reverse to make sure you can't connect to where you don't want to connect.
```bash
java -jar taverna.jar -d internal.idp.corp -s overly_broad.p12
EXIT_CODE=$?
if [[ "x0" == "x${EXIT_CODE}" ]]; then
  echo "this service should not be able to trust the internal IDP, exiting"
  exit 1
fi
```
Between these message and the fairly noisy output of `taverna` the error can be quickly found which is helpful
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