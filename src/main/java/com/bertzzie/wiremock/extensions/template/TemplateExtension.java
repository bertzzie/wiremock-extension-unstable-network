/*
 * Copyright (C) 2025 Alex Xandra Albert Sim
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.bertzzie.wiremock.extensions.template;

import com.github.tomakehurst.wiremock.extension.Extension;
import com.github.tomakehurst.wiremock.extension.ExtensionFactory;
import com.github.tomakehurst.wiremock.extension.WireMockServices;
import com.github.tomakehurst.wiremock.extension.responsetemplating.TemplateEngine;
import com.bertzzie.wiremock.extensions.template.extensions.UnstableNetworkResponseDefinitionTransformer;

import java.util.Collections;
import java.util.List;
import java.util.function.DoubleSupplier;

/**
 * Factory to register all extension classes of this extension.
 * <p>
 * This class is intended to be initiated when registering the extension programmatically. It can thereby have constructor parameters. All extensions
 * added here are only constructed once, so they should organize any required state with care (or ideally don't have any).
 */
public class TemplateExtension implements ExtensionFactory {

    private final UnstableNetworkResponseDefinitionTransformer responseTransformer;

    public TemplateExtension() {
        this.responseTransformer = new UnstableNetworkResponseDefinitionTransformer(
            UnstableNetworkResponseDefinitionTransformer.defaultRandomDoubleSupplier(),
            UnstableNetworkResponseDefinitionTransformer.DEFAULT_CHANCE
        );
    }

    public TemplateExtension(DoubleSupplier doubleSupplier, double chance) {
        this.responseTransformer = new UnstableNetworkResponseDefinitionTransformer(doubleSupplier, chance);
    }

    @Override
    public List<Extension> create(WireMockServices services) {
        return List.of(
            responseTransformer
        );
    }
}
