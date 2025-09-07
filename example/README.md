# How to use

1. Build the JAR that is needed. Assuming you are in this file's directory:

    ```bash
   $ cd ../ && ./gradlew clean build
   ```
   
2. Copy the build artifact into `example/extensions`. We want the standalone jar for docker:

    ```bash
   $ cp build/libs/wiremock-extension-unstable-network-standalone-dd8bcf6.dirty.jar example/extensions/wiremock-extension-unstable-network-standalone.jar
   ```

3. Run the container, for example

    ```bash
   $ docker build . -t wm:ext && docker run -p 8080:8080 wm:ext
   ```

By default the extension have 25% chance of failing request, you can chance this by doing curl to the admin endpoint:

```bash
$ curl -X POST http://localhost:8080/__admin/settings \
    -H 'Content-Type: application/json' \
    -d '{"extended": {"unstableNetworkDefinitionTargets": [{"method": "GET", "path": "/hello-world"}], "unstableNetworkDefinitionChance": 0.5}}'
```

The above command will make `GET /hello-world` have 50% chance of failing connection.