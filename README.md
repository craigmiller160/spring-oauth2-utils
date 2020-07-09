# OAuth2 Utils

This is a collection of utility code to support Spring Boot applications that need to interact with my OAuth2 Auth Server.

## Setup

### Adding to Project

To add this library to a project, first (obviously) add the dependency. Since this will likely not be deployed to Maven Central, the repo will have to be pulled down and installed locally using `mvn clean install`. Then, simply add it to the `pom.xml` of the project that wants to use it.

After this, it is necessary to configure that project to pull in the Spring Beans from this library. This is done by adding an OauthUtilsConfig class to that project, which should look something like this:

```
@Configuration
@ComponentScan(basePackages = [
    "io.craigmiller160.oauth2"
])
class OauthUtilsConfig
```

Keep in mind that the basePackages value can be more fine-tuned if you don't want to pull in all the classes from this library.

### Database Integration

To support token refreshing, this library will store refresh tokens in a database table. This is done generically using Spring Data JPA. There is a file, `sql/schema.sql` that contains the definition of the `app_refresh_tokens` table. This table must be created in the database/schema that the application will be using.

In order for your application to execute the JPA code, it needs to be explicitly configured. The default auto-scanning Spring Boot provides will not be enough, a JpaConfig class needs to be provided in order to properly manage the JPA code in this library:

```
@Configuration
@EnableJpaRepositories(basePackages = [
    "io.craigmiller160.oauth2.repository"
])
@EntityScan(basePackages = [
    "io.craigmiller160.oauth2.entity"
])
class JpaConfig
```

One final thing about the above code example: If you use it as-is, it will break your application. This is because once you start explicitly configuring JPA scanning, it must be configured to scan all JPA classes. This means that any packages containing JPA code in the application consuming this library must be included in the above configuration.

### Security Configuration

The application's Spring Security configuration will need to be setup properly for this library. First the `/authcode/**` path must be without security so that authentication can be performed. Second, the JWT request filter must be added so that all other requests can be properly validated based on the Auth Server's access token.

```
@Configuration
@EnableWebSecurity
class WebSecurityConfig (
        private val jwtFilterConfigurer: JwtValidationFilterConfigurer
) : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity?) {
        http?.let {
            http
                    .requiresChannel().anyRequest().requiresSecure()
                    .and()
                    .authorizeRequests()
                    .antMatchers("/authcode/**").permitAll()
                    .anyRequest().fullyAuthenticated()
                    .and()
                    .apply(jwtFilterConfigurer)
        }
    }

}
```

### TLS Certificate

The TLS public key from the Auth Server will need to be added to a TrustStore in the consuming application.

### Logging

Some of the features here have log output. To make sure that output is visible, add the following to the `application.yml` of the project consuming this library. Be sure to set the level to what you want.
                                           
```
logging:
    level:
        io.craigmiller160.oauth2: INFO
```

## How It Works

The following are instructions of how to properly utilize this library with the application to integrate it with the SSO Auth Server. Each step of the way will reference properties that must be included in the `application.yml` in order for each feature to work.

### Auth Server Awareness

Your application needs to know where the Auth Server is running. This is done through this property:

```
oauth2:
    auth-server-host: https://localhost:7003
```

### Client/User DB Entries

The client and all users need to be added to the database. This can be done through the Auth Management UI, or with direct database queries.

After the client is setup in the Auth Server DB, client information needs to be added to the application via properties:

```
oauth2:
    client-name: auth-management-service
    client-key: a4cc4fef-564e-44c1-82af-45572f124c1a
    client-secret: 1566aadf-800f-4a9d-9828-6a77426a53b5
```

### Token Validation - Bearer vs Cookie

The JWT token from the Auth Server can be delivered to secure APIs in two ways:

1. Bearer Token - The access token is provided in the Authorization header of the HTTP request.
1. Cookie - The access token is provided as a Cookie tied to the application's domain.

If the JWT token is provided in both ways, the Bearer Token will take precedence.

To configure the cookie, two properties need to be provided:

```
oauth2:
    cookie-name: auth-management-token
    cookie-max-age-secs: 86400
```

### Authentication


1. Properties
1. Auth Server DB entry
1. APIs
1. Move auth user to this lib
