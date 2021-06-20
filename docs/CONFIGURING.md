# How to Configure This Library

To flexibly support various applications, this library is very configuration driven. A handful of configuration properties control everything that this library does.

## Property Location

All properties should be found in a regular Spring application properties/yaml file. Also, environment variables with the same keys will override the properties file.

## Standard OAuth2 Properties

This project supports all the properties from `oauth2-utils-core`. Please see that project for details about its properties.

## Logging

Some of the features here have log output. To make sure that output is visible, add the following to the `application.yml` of the project consuming this library. Be sure to set the level to what you want.
                                           
```
logging:
    level:
        io.craigmiller160.spring.oauth2: INFO
```

## Disabling CSRF

Some apps won't need CSRF protection, such as if the only requests being served come from other server-side applications. To disable csrf, use the following property:

```
oauth2:
    disable-csrf: true
```