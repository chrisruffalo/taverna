# Ταβερνα (Taverna)
Taverna gets its name from the phrase "_**T**rust **A**nd **V**erify_". It is a tool for comparing your current trust material
against the domains you want to trust and ensuring that the trust chain is correct before you find out the hard
way (in production). 

## Problem
Taverna comes from having to deliver immutable applications to various environments (production and non-production) that
have different trust profiles. There are a lot of ways to handle this mechanically but that won't stop 
phone calls out of hours if some remote host changes their certificates and it won't help debug the trust
issues in the face of esoteric or meaningless error messages.

How many of us are tired of "PKIX path building to target" errors? Even if the error is understood sometimes
the details are of exactly why are not obvious.

Yes, there are tools and commands that could do these things for you but you have to stitch them together yourself. The
intent of `taverna` is to put all of those things under one roof, so to speak.

## Goals
Taverna is designed to take a list of domains and a set of trust material and ensure that, based on that trust,
every single domain can be verified/trusted. Taverna also gives good output to show relevant details of the 
trust that is loaded from disk or expected by a domain. It can also, optionally, create configuration output for Java applications
to use that trust. It can also create single unified trust sources (directory, file, or truststore) that contain
all the trust expected to verify the given domains. Finally, it can find gaps in the trust and fill those using the certificates advertised by the domains.

## Usage Model
Taverna should be used in build or deployment pipelines to ensure that the correct trust is included 
for the application environment. Ideally you could fail a build or a deployment if the new application would
fail to trust the remote endpoints it needs to communicate with _or_ you could automatically update
the trust.

## Real-World Example of Tailored Trust
The clearest real-world use, aside from verifying trust stores, is enabling something that could be called "tailored trust".
What this means is that each deployment/environment/region has a specific trust set that is valid. This prevents
a small subset of exploits (or, more likely, configuration errors) by preventing the application from trusting
domains (servers) that it should not.

Let's take an application that has been built according to contemporary practices. For the sake of discussion it uses
auth0 for authentication in production but in development and staging it talks to an environment-specific Keycloak instance.
It probably wouldn't be an issue if your development and staging deployments can trust the auth0 IDP but it _could_
be a potential issue if your production service could talk to either environment.

When developing the application you could ensure that you trust the auth0 root certificate and add it to some
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

The output of this command is a single certificate that the application _could_ trust in order to be able to verify the domain `auth0.com`.
While every output of `taverna` should be inspected for accuracy you can simply use the command to check.
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
Between this message and the fairly noisy output of `taverna` the error can be quickly found.

## Notes
Unless otherwise stated all truststores in this project use the password "changeit". None of
these truststores are used for anything other than testing.