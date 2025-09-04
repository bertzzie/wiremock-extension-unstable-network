package com.bertzzie.wiremock.extensions.template.extensions;

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.Response;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;

import java.util.function.DoubleSupplier;

public class UnstableNetworkResponseDefinitionTransformer implements ResponseDefinitionTransformerV2 {
    public static DoubleSupplier defaultRandomDoubleSupplier() {
        return Math::random;
    }

    public static double DEFAULT_CHANCE = 0.25;

    private final DoubleSupplier doubleSupplier;

    private final double chance;

    public UnstableNetworkResponseDefinitionTransformer(DoubleSupplier doubleSupplier, double chance) {
        this.doubleSupplier = doubleSupplier;
        this.chance = chance;
    }

    @Override
    public String getName() {
        return "UnstableNetworkResponseDefinitionTransformer";
    }

    @Override
    public ResponseDefinition transform(ServeEvent serveEvent) {
        if (doubleSupplier.getAsDouble() < this.chance) {
            return ResponseDefinitionBuilder.responseDefinition()
                .withFault(Fault.CONNECTION_RESET_BY_PEER)
                .build();
        }

        return serveEvent.getResponseDefinition();
    }
}
