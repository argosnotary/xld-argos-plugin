FROM xebialabs/xl-deploy:9.5-debian-slim

USER root
RUN apt-get update && apt-get install -y --no-install-recommends ssh

USER xebialabs
COPY build/distributions/*.xldp /opt/xebialabs/xl-deploy-server/default-plugins
