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

### Database Schema

To support token refreshing, this library will store refresh tokens in a database table. This is done generically using Spring Data JPA. There is a file, `sql/schema.sql` that contains the definition of the `app_refresh_tokens` table. This table must be created in the database/schema that the application will be using.

### Logging

Some of the features here have log output. To make sure that output is visible, add the following to the `application.yml` of the project consuming this library. Be sure to set the level to what you want.
                                           
```
logging:
    level:
        io.craigmiller160.oauth2: INFO
```

## Things to Document

1. How to get the repo/entity scan to work for refresh token
1. Logging
1. Schema
1. How to get the classes here scanned. Add this to the other lib too.
1. How to add the filter.
