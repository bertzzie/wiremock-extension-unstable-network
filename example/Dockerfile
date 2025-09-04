FROM  wiremock/wiremock:3.13.1

COPY ./stubs /home/wiremock/mappings
COPY ./extensions /var/wiremock/extensions

#CMD ["--extensions", "com.bertzzie.wiremock.extensions.template.extensions.UnstableNetworkResponseDefinitionTransformer"]