FROM alpine:latest

ARG INPUT_BINARY=taverna-cmd
ARG VERSION=1.4
ARG FULL_VERSION=1.4-SNAPSHOT
ARG GIT_HASH=n/a

RUN apk add gcompat

COPY target/${INPUT_BINARY} /usr/bin/taverna-cmd
RUN chmod g+x /usr/bin/taverna-cmd

USER 1000:0

LABEL version=${VERSION} \
      source=https://github.com/chrisruffalo/taverna \
      revision=${GIT_HASH} \
      io.github.chrisruffalo.taverna.version=${FULL_VERSION}

ENTRYPOINT ["/usr/bin/taverna-cmd"]
CMD ["--help"]