FROM alpine:latest

ARG INPUT_BINARY=taverna
ARG OUTPUT_BINARY=taverna

RUN apk add gcompat

COPY target/${INPUT_BINARY} /usr/bin/taverna
RUN chmod g+x /usr/bin/taverna

USER 1000:0

ENTRYPOINT ["/usr/bin/taverna"]