package com.bertzzie.wiremock.extensions.template.extensions.models;

import com.github.tomakehurst.wiremock.http.RequestMethod;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import java.util.LinkedHashMap;
import java.util.Objects;

public class HttpRequest {
    private RequestMethod method;
    private String path;

    public static HttpRequest fromLinkedHashMap(LinkedHashMap<String, Object> hashMap) {
        var method = RequestMethod.fromString(hashMap.get("method").toString());
        var path = hashMap.get("path").toString();

        return new HttpRequest(method, path);
    }

    public static HttpRequest fromLoggedRequest(LoggedRequest loggedRequest) {
        return new HttpRequest(
            loggedRequest.getMethod(),
            loggedRequest.getUrl()
        );
    }

    public RequestMethod getMethod() {
        return method;
    }

    public void setMethod(RequestMethod method) {
        this.method = method;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public HttpRequest(RequestMethod method, String path) {
        this.method = method;
        this.path = path;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        HttpRequest that = (HttpRequest) o;
        return Objects.equals(getMethod(), that.getMethod()) && Objects.equals(getPath(), that.getPath());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMethod(), getPath());
    }

    @Override
    public String toString() {
        return "HttpRequest{" +
            "method=" + method +
            ", path='" + path + '\'' +
            '}';
    }
}
