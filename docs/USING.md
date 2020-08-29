# How to Use Authentication

Once fully integrated and configured, the consuming application will expose several important APIs to allow the authentication to take place. This will detail how they work.

## Auth Code APIs

The auth code flow requires two separate API endpoints to support the operation.

### Generate Login URL

```
POST /oauth/authcode/login
```

All redirects are done using the client UI's proxy settings. This `POST` request takes the Origin header and combines it with static and configurable values to generate the full redirect URL to the auth server login page. Again, it will use the client UI as a host and rely on proxy redirection to get there.

After calling this API, the response will contain the URL. The UI should then explicitly redirect to the target.

```
{
    "url": "https://localhost:7003/login/path?various=query&param=values"
}
```

### Receive Authorization Code

```
GET /oauth2/authcode/code
```

After the auth code login has finished successfully, the OAuth2 Auth Server will call this API and provide the authorization code via a query parameter. When this API finishes successfully, it will redirect the user again to the post-auth redirect path. As always, all of these paths use the UI app as the host and rely on proxying to go to the backend.

## Logout

```
GET /oauth/logout
```

Logout will clear the token cookie so the user can no longer stay logged in.

## Get User Details

```
GET /oauth/user
```

This endpoint will return details on the currently authenticated user. This includes the user's basic information and any access roles they currently have.
