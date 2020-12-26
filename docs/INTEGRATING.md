# How to Integrate This Library

These are the instructions for how to integrate this library into any SpringBoot project.

## TLS Certificate

Because the application using this library will be communicating with the OAuth2 Auth Server, the TLS public key from that application will need to be added to a TrustStore in the application. The `web-utils` project contains code to easily integrate a third party TrustStore into the application once this is done.

## Add Dependency

This library should be installed locally (unless it gets published to a repo at some point). Then it can be added to the pom.xml.

```
<dependency>
    <groupId>io.craigmiller160</groupId>
    <artifactId>oauth2-utils</artifactId>
    <version>${oauth2.utils.version}</version>
</dependency>
```

## Component Scan

The Spring beans in this library need to be scanned and managed by the Spring context. To do this, add a special configuration class with the following code:

```
@Configuration
@ComponentScan(basePackages = [
    "io.craigmiller160.oauth2"
])
class OauthUtilsConfig
```

## Database Integration

To support token refreshing, this library will store the refresh tokens in a database table. This is done using Spring Data JPA.

To set that up, first add the table in `sql/schema.sql` to the database/schema that the application will be using. Don't forget that the SQL file does not have a schema prefix in it.

Then, create a special configuration class for JPA configuration. By default, JPA will scan the local entities/repositories and ignore anything in libraries. The only way to fix this is to manually configure all of the related scanning. The code sample below only includes scanning of the code in this library, make sure to include the related packages from the local project too:

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

## Security Configuration

The security filter that checks for the JWT must be integrated into the Spring Security configuration. Not only is the filter provided, but the ability to configure insecure patterns is also configured. Some patterns are insecure by default, such as the ones to allow auth code authentication, so it is very important that the insecure patterns are set along with adding the filter itself.

Lastly, the session policy must be set properly. Even though this is intended to be used for REST APIs, the CSRF protection requres an active session.

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
                    .antMatchers(*jwtFilterConfigurer.getInsecurePathPatterns()).permitAll()
                    .anyRequest().fullyAuthenticated()
                    .and()
                    .apply(jwtFilterConfigurer)
                    .and()
                    .sessionManagement()
                        .sessionCreationPolicy(SessionCreationPolicy.ALWAYS)
        }
    }

}
```

## Session Configuration

The session requred for CSRF protection has a different timeout handling than the access/refresh tokens. Since access expiration is handled in the token, not the session, we don't want the session to ever expire. This needs to be done by adding this to the `application.yml`:

```
server:
    session:
        timeout: -1
```

## TimeZone Configuration

The JPA & System TimeZones need to be set to UTC. This is to ensure that everything works smoothly when transitioning between local workstation (which is US/Eastern) and Kubernetes (which is UTC). Just by using this library, the system timezone will be set to UTC. However, the application must set the Hibernate timezone to UTC as well:

```
spring:
  jpa:
    properties:
      hibernate.jdbc.time_zone: UTC
```

NOTE: The property must be added EXACTLY as displayed above. IntelliJ may try to format it, do not let it.

## Setup Client/Users

The application using this library is considered to be a "client" in OAuth2 terminology. It must be registered with the Auth Server, and the client key/secret assigned to it should be recorded, especially because the secret is not visible in its raw form after being hashed and saved in the database.

All the users associated with this application also need to be registered with the Auth Server. Existing users can easily have this application added to them.
