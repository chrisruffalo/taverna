# Ταβερνα (Taverna)
Taverna gets its name from the phrase "_**T**rust **A**nd **V**erify_". It is a tool for comparing an applications
current trust material against the domains to trust and ensuring that the trust chain is correct before
finding out the hard way (in production, at night, while on PTO).

## Overview
For more information (along with plenty of examples) see the [GitHub Project Page](https://github.com/chrisruffalo/taverna).

## Container Execution
The container can be executed just like the binary.
```shell
[]$ podman run docker.io/chrisruffalo/taverna:1.2 -d google.com
loaded 0 total certificates
certificate chain from google.com:
        hostname verified
        [serial=d11170199087111609fd1685afb67434] CN=*.google.com [f2287b3a1b41cf6f0173ac057fc02a9bfe4d0f09109629d37391d257f5a4be47] [issuer=CN=WR2,O=Google Trust Services,C=US, not in trust, not trusted] 
        [serial=7ff005a07c4cded100ad9d66a5107b98] CN=WR2,O=Google Trust Services,C=US [e6fe22bf45e4f0d3b85c59e02c0f495418e1eb8d3210f788d48cd5e1cb547cd4] [issuer=CN=GTS Root R1,O=Google Trust Services LLC,C=US, not in trust, not trusted] 
        [serial=77bd0d6cdb36f91aea210fc4f058d30d] CN=GTS Root R1,O=Google Trust Services LLC,C=US [3ee0278df71fa3c125c4cd487f01d774694e6fc57e0cd94c24efd769133918e5] [issuer=CN=GlobalSign Root CA,OU=Root CA,O=GlobalSign nv-sa,C=BE, not in trust, not trusted] 
        not trusted
```