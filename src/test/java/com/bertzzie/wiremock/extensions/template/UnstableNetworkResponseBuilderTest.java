package com.bertzzie.wiremock.extensions.template;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.common.Json;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.net.SocketException;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching;
import static io.restassured.RestAssured.given;

public class UnstableNetworkResponseBuilderTest extends AbstractTestBase {

    @BeforeEach
    public void beforeEach() {
        createStubs();
    }

    @DisplayName("unstable network always fail")
    @Test
    public void test_socketException() {
        Assertions.assertThrows(SocketException.class, () -> {
            given()
                .accept(ContentType.JSON)
                .get(wm.getRuntimeInfo().getHttpBaseUrl() + "/unstable-network")
                .wait();
        });
    }

    private void createStubs() {
        wm.stubFor(
            get(urlPathMatching("/unstable-network"))
                .willReturn(
                    WireMock.ok()
                        .withHeader("Content-Type", "application/json")
                        .withJsonBody(Json.node(Json.write(Map.of("hello", "world"))))
                )
        );
    }
}
