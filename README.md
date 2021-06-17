# Spring OAuth2 Utils

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

Solving this problem was done in two ways.

First, the only time that a backend application calls the OAuth2 Auth Server directly is to download the JWK. Therefore, the hostname for the auth server set via configuration would contain the k8s hostname, and that's it.

Second, the auth code redirect problems can be solved via Nginx redirects. The auth code flow is a UI-based flow, and a UI app using nginx solves all the hostname problems. Rather than relying on multiple hostnames, the nginx configuration can be setup with the k8s hostname for a proxy_pass directive. Likewise, during development, the webpack dev server would have a similar proxy setup.

Since nginx will handle proxying the request to the auth server, the only hostname required is the UI app's hostname, since that will be a part of many of the redirect requests. To deal with the varying hostname problem mentioned before, the Origin header will be relied on. All the configuration will simply include URIs that are appended to the hostname for doing the redirects.

More explicit details will be shown in the configuration guide.

## Documentation

1. <a href="/docs/INTEGRATING.md">How to Integrate This Library</a>
1. <a href="/docs/CONFIGURING.md">How to Configure This Library</a>
1. <a href="/docs/USING.md">How to Use Authentication</a>
