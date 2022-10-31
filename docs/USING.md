# How to Use Authentication

See the `oauth2-utils-core` project for more details on this.

## Tests

The `OAuth2Config` bean will not be autowired in tests (as long as they use the `test` profile). This is because this config class will automatically load the JWK and other configuration, and that needs to be mocked in tests.