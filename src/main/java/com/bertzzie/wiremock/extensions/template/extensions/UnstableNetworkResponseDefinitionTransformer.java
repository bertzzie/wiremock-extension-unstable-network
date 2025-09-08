package com.bertzzie.wiremock.extensions.template.extensions;

import com.bertzzie.wiremock.extensions.template.extensions.models.HttpRequest;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;
import com.github.tomakehurst.wiremock.extension.GlobalSettingsListener;
import com.github.tomakehurst.wiremock.extension.ResponseDefinitionTransformerV2;
import com.github.tomakehurst.wiremock.global.GlobalSettings;
import com.github.tomakehurst.wiremock.http.Fault;
import com.github.tomakehurst.wiremock.http.ResponseDefinition;
import com.github.tomakehurst.wiremock.stubbing.ServeEvent;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.DoubleSupplier;
import java.util.stream.Collectors;

public class UnstableNetworkResponseDefinitionTransformer implements ResponseDefinitionTransformerV2, GlobalSettingsListener {
    public static DoubleSupplier defaultRandomDoubleSupplier() {
        return Math::random;
    }

    public static double DEFAULT_CHANCE = 0.25;

    public static String CHANCE_EXTENDED_PARAMS_KEY = "unstableNetworkDefinitionChance";
    public static String TARGETS_EXTENDED_PARAMS_KEY = "unstableNetworkDefinitionTargets";

    private final DoubleSupplier doubleSupplier;

    // A thread-safe Set to track requests that have already been called once.
    // This allows us to fail the *first* call deterministically.
    private final Set<HttpRequest> firstCallTracker = Collections.newSetFromMap(new ConcurrentHashMap<>());

    private double chance;

    private List<HttpRequest> targets;

    public UnstableNetworkResponseDefinitionTransformer(DoubleSupplier doubleSupplier, double chance) {
        this.doubleSupplier = doubleSupplier;
        this.chance = chance;
        this.targets = new ArrayList<>();
    }

    // I noticed this method was also clearing targets, which could be an unintended side-effect.
    // I've removed that line to make the method's behavior more specific.
    public void setChance(double chance) {
        this.chance = chance;
    }

    public double getChance() {
        return this.chance;
    }

    public List<HttpRequest> getTargets() {
        return targets;
    }

    public void setTargets(List<HttpRequest> targets) {
        this.targets = targets;
    }

    @Override
    public String getName() {
        return "UnstableNetworkResponseDefinitionTransformer";
    }

    @Override
    public ResponseDefinition transform(ServeEvent serveEvent) {
        if (!this.isRequestTargeted(serveEvent.getRequest())) {
            return serveEvent.getResponseDefinition();
        }

        HttpRequest currentRequest = HttpRequest.fromLoggedRequest(serveEvent.getRequest());

        // The .add() method is atomic and returns true if the set did not already contain the element.
        // We use this to determine if it's the first call for this specific request.
        boolean isFirstCall = firstCallTracker.add(currentRequest);

        if (isFirstCall) {
            // This is the first time we've seen this request, so always inject the fault.
            return ResponseDefinitionBuilder.responseDefinition()
                    .withFault(Fault.CONNECTION_RESET_BY_PEER)
                    .build();
        }

        // For all subsequent calls, revert to the original random chance behavior.
        if (doubleSupplier.getAsDouble() < this.getChance()) {
            return ResponseDefinitionBuilder.responseDefinition()
                    .withFault(Fault.CONNECTION_RESET_BY_PEER)
                    .build();
        }

        return serveEvent.getResponseDefinition();
    }

    private boolean isRequestTargeted(LoggedRequest request) {
        if (this.getTargets().isEmpty()) {
            return true; // not set == target ALL requests
        }

        var current = HttpRequest.fromLoggedRequest(request);

        return this.getTargets().contains(current);
    }

    @Override
    public void afterGlobalSettingsUpdated(GlobalSettings oldSettings, GlobalSettings newSettings) {
        System.out.println("\nUnstableNetworkResponseDefinitionTransformer - Detected config change:");
        System.out.printf("old chance: %s, old targets: %s\n", this.getChance(), this.getTargets());

        var newChance = this.parseChance(newSettings.getExtended().get(CHANCE_EXTENDED_PARAMS_KEY));
        this.setChance(newChance);

        var newTargets = this.parseTargets(newSettings.getExtended().get(TARGETS_EXTENDED_PARAMS_KEY));
        this.setTargets(newTargets);

        // It's good practice to reset the state when the configuration changes.
        this.firstCallTracker.clear();
        System.out.println("First call tracker has been reset due to config update.");

        System.out.printf("new chance: %s, new targets: %s\n", this.getChance(), this.getTargets());
    }

    private List<HttpRequest> parseTargets(Object targets) {
        if (targets == null) {
            // we have default in constructor so when it's not set we'll get default
            return this.getTargets();
        }

        if (!(targets instanceof List)) {
            System.out.printf(
                    "Config %s is not a list, but %s. Falling back to last value: %s",
                    TARGETS_EXTENDED_PARAMS_KEY,
                    targets,
                    this.getTargets()
            );
            return this.getTargets();
        }

        try {
            return ((List<?>) targets).stream()
                    .map(t -> (LinkedHashMap<String, Object>) t)
                    .map(HttpRequest::fromLinkedHashMap)
                    .collect(Collectors.toList());
        } catch (ClassCastException e) {
            System.out.printf(
                    "Config %s is not a list of expected type." +
                            "The format we're expecting is {\"method\": \"GET|POST|...\", \"\": \"/some/path\"}, but got %s." +
                            "Falling back to last value: %s.",
                    TARGETS_EXTENDED_PARAMS_KEY,
                    targets,
                    this.getTargets()
            );

            return this.getTargets();
        }
    }

    private double parseChance(Object chance) {
        if (chance == null) {
            // we have default in constructor so when it's not set we'll get default
            return this.getChance();
        }

        if (!(chance instanceof Number)) {
            System.out.printf(
                    "Config %s is not a number, but %s. Falling back chance to last value: %.2f\n",
                    CHANCE_EXTENDED_PARAMS_KEY,
                    chance.getClass(),
                    DEFAULT_CHANCE
            );

            return DEFAULT_CHANCE;
        }

        try {
            return ((Number) chance).doubleValue();
        } catch (NumberFormatException e) {
            System.out.printf(
                    "Config %s is has invalid value %s. Falling back chance to last value: %.2f\n",
                    CHANCE_EXTENDED_PARAMS_KEY,
                    chance,
                    DEFAULT_CHANCE
            );

            return DEFAULT_CHANCE;
        }
    }
}