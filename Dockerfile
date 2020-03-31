FROM xebialabs/xl-deploy:9.5-debian-slim

COPY build/distributions/*.xldp /opt/xebialabs/xl-deploy-server/default-plugins
