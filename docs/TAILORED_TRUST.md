# Real-World Example of Tailored Trust
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
[]$ java -jar taverna-cmd.jar -d auth0.com --complete --outdir project/trust
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
[]$ java -jar taverna-cmd.jar -d auth0.com -s project/trust
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
java -jar taverna-cmd.jar -d auth0.com -s not-enough.p12
EXIT_CODE=$?
if [[ "x0" != "x${EXIT_CODE}" ]]; then
  echo "certificates for auth0 could not be verified with the given trust store"
  exit ${EXIT_CODE}
fi
```
You could also set it up in reverse to make sure you can't connect to where you don't want to connect.
```bash
java -jar taverna-cmd.jar -d internal.idp.corp -s overly_broad.p12
EXIT_CODE=$?
if [[ "x0" == "x${EXIT_CODE}" ]]; then
  echo "this service should not be able to trust the internal IDP, exiting"
  exit 1
fi
```
Between these message and the fairly noisy output of `taverna` the error can be quickly found which is helpful
when trying to quickly figure out the problem.