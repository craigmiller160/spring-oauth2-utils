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

TODO fill this out
