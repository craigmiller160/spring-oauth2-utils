# OAuth2 Utils

This is a collection of utility code to support Spring Boot applications that need to interact with my OAuth2 Auth Server. The goal is to be able to add this library to a project and with minimal code and a few configuration properties have the application fully integrated with the auth server.

## Basic Concept

An application that uses this library will do the following:

1. When it starts, it will download the JWK from the OAuth2 Auth Server. This will allow it to validate the JWTs issued by that server.
1. All secured requests will validate the presence of a JWT issued by the OAuth2 Auth Server. The token details will be exposed via the SecurityContext.
1. API endpoints will exist to allow the Authorization Code Flow, so a client application can be redirected to and from the OAuth2 Auth Server so the client can log in there.
1. Access tokens will be checked from either a cookie (for auth code flow) or the Authorization header.
1. Refresh tokens provided by the initial authentication (auth code flow only) will be automatically used when the access token expires. 

## PSA: Multiple Hostname Strategy

### The Problem

A challenge was discovered while creating the OAuth2 Auth Server: how to handle multiple hostnames for the authentication code flow? To use an auth code flow, multiple redirects between the client, server, and auth server applications occur. This was smooth sailing during local development, but when deploying to my production environment a problem occurred.

The core of it is there is no custom DNS at the time of writing this readme. That means that all the hostnames are just simple IP addresses. The problem then arises from inconsistency. So an application will have a k8s service name for the hostname when making requests within the cluster (`auth-management-service-service`), a localhost IP address when on my desktop (`localhost`), a router IP address when accessing over my LAN (`192.168.0.5`), and a public IP address when accessing out in the world (`123.456.7.8`). Due to the lack of a DNS solution, all of these different hosts need to work.

### The Solution

Solving this problem was done two few ways.

First, the only time that a backend application calls the OAuth2 Auth Server directly is to download the JWK. Therefore, the hostname for the auth server set via configuration would contain the k8s hostname, and that's it.

Second, the auth code redirect problems can be solved via Nginx redirects. The auth code flow is a UI-based flow, and a UI app using nginx solves all the hostname problems. Rather than relying on multiple hostnames, the nginx configuration can be setup with the k8s hostname for a proxy_pass directive. Likewise, during development, the webpack dev server would have a similar proxy setup.

Since nginx will handle proxying the request to the auth server, the only hostname required is the UI app's hostname, since that will be a part of many of the redirect requests. To deal with the varying hostname problem mentioned before, the Origin header will be relied on. All the configuration will simply include URIs that are appended to the hostname for doing the redirects.

More explicit details will be shown in the configuration guide.

## Documentation

1. <a href="/docs/INTEGRATING.md">How to Integrate This Library</a>
1. <a href="/docs/CONFIGURING.md">How to Configure This Library</a>




TODO must include nginx documentation





### Authentication

A user can authenticate with any allowable grant type via the Auth Server, and then use the JWT as a Bearer token to query the APIs. However, the primary advantage of this library is the out-of-the-box support for the Authorization Code Grant Type, which allows for delegating user logins directly to the Auth Server.

This is done through two API endpoints that are exposed by the application importing this library. As long as the classes from this library are scanned by the consuming application, these endpoints will exist;

`/oauth2/authcode/login` - This endpoint will create a well-formed redirect request to the Auth Server. The 302 response will contain the necessary parameters for the Auth Server to setup the correct authentication page.

`/oauth2/authcode/code` - This is the endpoint that will be called after authentication. Assuming the user successfully authenticates, the Auth Server will redirect them to this endpoint, along with the Authorization Code. This library will then use that Authorization Code to request Access/Refresh tokens from the Auth Server, and store the Access Token in a cookie. Finally, it will redirect to  whatever page the user should land on post-authentication. This is all driven by the following properties:

```
oauth2:
    auth-code-redirect-uri: https://localhost:3000/api/oauth/authcode/code
    post-auth-redirect: https://localhost:3000
```

The first one tells the Auth Server how to redirect to the `/authcode/code` endpoint. The second one is where the user should be redirected to once the authentication is finished.

### Authentication Without DNS Hostnames

Due to the limitations of running this in my house without a custom DNS server, there is a need to support a variety of IP addresses. Localhost, LAN, and Public IPs all need to be supported. To get around this, here is the recommended approach.

First, rely on the UI app to do the redirects. The UI can proxy everything using the kubernetes service names, thus removing IPs from the equation. The problem then only becomes making sure all the redirects can point to the UI no matter what IP is being used.

To do this, a special property has been provided that makes all the redirects rely on the "origin" HTTP header for the hostname. The origin should be set by the UI app automatically on every request, thus allowing the redirects to be performed with the various IP addresses. When using the "origin" header, all the other URI properties are simply URI paths that get appended to that origin:

```
oauth2:
    use-origin-for-redirect: true
    auth-code-redirect-uri: /api/oauth/authcode/code
    post-auth-redirect: /
```

Also, keep in mind that the Auth Server will validate the Redirect URIs, so make sure the database is set with the URIs from every possible origin host.

Lastly, one more property is needed: the internal auth server hostname. When the application starts up, it needs to be able to immediately query the auth server to load the JWK, so it needs the kubernetes service hostname. However, when doing the authentication request, it needs to have the request be proxied by the UI app. Therefore, the base auth server host property should be a URI appended to the request origin that will proxy to the actual auth server. The internal property should be the kubernetes hostname that can be called directly by the backend app to load the JWK.

```
oauth2:
    auth-server-host: /oauth2
    internal-auth-server-host: https://sso-oauth2-server
```

If not set, `internal-auth-server-host` will default to the value of `auth-server-host`.

### Logout

Logout has an endpoint as well: `/oauth/logout`. This will clear the cookie and delete the refresh token so no more authenticated calls will be possible.

### Get User Details

There is an endpoint to get details on the authenticated user. This is useful for adding those details to the UI in some way. It is `/oauth/user`.

### Adding Insecure Paths

By default, all requests to the application will be secure, other than the few that need to be open to allow for initial authentication. If there is a need to add more paths to be insecure, the following property allows it. It accepts a comma-separated list of Ant Matching patterns:

```
oauth2:
    insecure-paths: /path/**,/path2/**
```
### Auth Code Expiration

If there is too long of a delay between the initial request to the OAuth2 Server for an auth code and the response, the application will reject it. This defaults to 10 minutes. The number of minutes to wait can be configured with the following property:

```
oauth2:
    auth-code-wait-mins: 10
```
