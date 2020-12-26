# How to Use Authentication

Once fully integrated and configured, the consuming application will expose several important APIs to allow the authentication to take place. This will detail how they work.

## Handling CSRF Tokens

This library will enforce CSRF protection across the application. This means all modifying calls (GET, POST, PUT, etc) will require a CSRF synchronizer token to be provided. Here is how to work with this:

### Step 1 - Get the CSRF Token

The current, valid CSRF token for the user session can be acquired at any time by making an HTTP GET request with the CSRF Fetch header. The token will be returned in the CSRF response header, and can then be stored. The token will be returned on both a success and failed call.

When the user first reaches the site, be sure to make an HTTP GET request like demonstrated below and store the CSRF token in memory. It should be stored in memory, and not `localStorage` or `sessionStorage` for maximum security.

```
axios.get('/oauth/user', {
    headers: {
        'x-csrf-token': 'fetch'
    }
})
    .then((res) => {
        // CSRF token is at res.headers['x-csrf-token']
    })
    .catch((ex) => {
        // CSRF token is at ex.response.headers['x-csrf-token']
    });
```

### Step 2 - Send the CSRF Token in All Modifying Requests

After this, all modifying requests need to provide the CSRF token in the CSRF header. This can be easily done using an `axios` interceptor:

```
export const addCsrfTokenInterceptor = (config) => {
    const csrfToken = getCsrfTokenFromMemory();
    if (csrfToken && config.method !== 'get') {
        config.headers = {
            ...config.headers,
            ['x-csrf-token']: csrfToken
        };
    }
    return config;
};

axios.interceptors.request.use(addCsrfTokenInterceptor)
```

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

## Important Note About Prefixes

If an app defines a path prefix, then it would be prepended to all the URIs defined here.