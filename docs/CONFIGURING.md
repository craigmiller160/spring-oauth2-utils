# How to Configure This Library

To flexibly support various applications, this library is very configuration driven. A handful of configuration properties control everything that this library does.

## Logging

Some of the features here have log output. To make sure that output is visible, add the following to the `application.yml` of the project consuming this library. Be sure to set the level to what you want.
                                           
```
logging:
    level:
        io.craigmiller160.oauth2: INFO
```

## Connecting to Auth Server

The application needs to connect to the Auth Server when it first starts up. This is done to download the JWK information for validating the tokens.

In production, this property should have the k8s service name for the auth server.

```
oauth2:
    auth-server-host: https://localhost:7003
```

## Client Information

The OAuth2 Auth Server has certain information about this application as a "client". That information needs to be provided via properties.

```
oauth2:
    client-name: auth-management-service
    client-key: a4cc4fef-564e-44c1-82af-45572f124c1a
    client-secret: 1566aadf-800f-4a9d-9828-6a77426a53b5
```

## Token Cookie

When using the auth code flow, the access token will be stored in a cookie. The name, path, and max age of this cookie needs to be set up here. Keep in mind that all access will have an expiration for the token itself, and every time it is refreshed a new cookie will be issued with a fresh expiration. However, it is good for security purposes to have an expiration on the cookie.

```
oauth2:
    cookie-name: auth-management-token
    cookie-path: /prod-path-prefix
    cookie-max-age-secs: 86400
```

## Redirects

The auth code flow contains several redirects.

1. Redirect to the auth server login page.
1. Redirect to the server application (consumer of this library) to provide the auth code.
1. Redirect back to the UI after the fact.

As was clarified in the README, due to issues with various IP addresses for the various applications, all of these redirects will be done using the UI itself. The webpack dev server (development) or nginx (production) will need to be configured properly for this. There is documentation on how to do that. To configure each of these requests, the following properties must be configured:

### Proxy Base for Auth Server

The base URI that triggers the proxying to the auth server must be provided via a property. Any URI that starts with this prefix should be redirected to the OAuth2 Auth Server. It will be used for the first redirect to the login page:

```
oauth2:
    auth-login-base-uri: "/oauth2"
```

### Redirect Back to Server App With Auth Code

Once the login has succeeded, the user is redirected back to the server application (the consumer of this library) in a request that contains the authorization code. While the end of the URI is always the same `/oauth/authcode/code`, this must be provided because of the proxy element to it. A prefix or other proxy strategy should be used to ensure that this URI is redirected to the server app.

```
oauth2:
    auth-code-redirect-uri: "/api/oauth/authcode/code"
```

### Post Authentication Redirect

Once authentication is fully successful, it's time to redirect back to the application. In most cases, this may just be a `/`, taking the user to the homepage of the UI app.

```
oauth2:
    post-auth-redirect: "/"
```

## Insecure Paths

Not all paths may need to be secured for authenticated users only. There are several paths, such as the ones for the auth code flow, that are insecure by default. However, if more paths need to be excluded from the authentication, they can easily be added by a comma-separated list of glob patterns with this property:

```
oauth2:
    insecure-paths: /path/**,/path2/**
```

## Auth Code Wait Time

After the user first attempts to login, there needs to be a limit on how long the application will wait for a successful login. By default this is 10 minutes, however it can be customized with this property:

```
oauth2:
    auth-code-wait-mins: 10
``` 

## Disabling CSRF

Some apps won't need CSRF protection, such as if the only requests being served come from other server-side applications. To disable csrf, use the following property:

```
oauth2:
    disable-csrf: true
```