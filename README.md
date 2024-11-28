# parenthesin/components
[![Clojars Project](https://img.shields.io/clojars/v/com.github.parenthesin/components.svg)](https://clojars.org/com.github.parenthesin/components)

Common components used on the Parenthesin's service templates.

# Components

## [config/aero](src/parenthesin/components/config/aero.clj)
Reads aero config file on `resources/config.edn`.  
Gets the current [profile](https://github.com/juxt/aero#profile) on enviroment var `SYSTEM_ENV`
### Libraries
- [aero](https://github.com/juxt/aero)

## [db/jdbc-hikari](src/parenthesin/components/db/jdbc_hikari.clj)
Depends on config component to read [connection info data](test/resources/config.edn#L3)
### Libraries
- [next-jdbc](https://github.com/seancorfield/next-jdbc)
- [hikaricp](https://github.com/brettwooldridge/HikariCP)
### In addition, you will need to add dependencies for the JDBC drivers you wish to use for whatever databases you are using. For example:
MySQL: *com.mysql/mysql-connector-j {:mvn/version "[latest-version](https://mvnrepository.com/artifact/com.mysql/mysql-connector-j)"}*  
PostgreSQL: *org.postgresql/postgresql {:mvn/version "[latest-version](https://mvnrepository.com/artifact/org.postgresql/postgresql)"}*  
Microsoft SQL Server: *com.microsoft.sqlserver/mssql-jdbc {:mvn/version "[latest-version](https://mvnrepository.com/artifact/com.microsoft.sqlserver/mssql-jdbc)"}*  
Sqlite: *org.xerial/sqlite-jdbc {:mvn/version "[latest-version](https://mvnrepository.com/artifact/org.xerial/sqlite-jdbc)"}*  
**(always search for latest version)**

## [http/clj-http](src/parenthesin/components/http/clj_http.clj)
Has some [mock implementations](test/unit/parenthesin/components/http/clj_http_test.clj) for tests
### Libraries
- [clj-http](https://github.com/dakrone/clj-http)

## [router/reitit-malli](src/parenthesin/components/router/reitit_malli.clj)
Has some presets, handlers and configs to use malli as input/output validations for routes
### Libraries
- [reitit](https://github.com/metosin/reitit)
- [malli](https://github.com/metosin/malli)

## [router/reitit-schema](src/parenthesin/components/router/reitit_schema.clj)
Has some presets, handlers and configs to use schema as input/output validations for routes
### Libraries
- [reitit](https://github.com/metosin/reitit)
- [schema](https://github.com/plumatic/schema)

## [server/reitit-pedestal-jetty](src/parenthesin/components/server/reitit_pedestal_jetty.clj)
Depends on one of each type of the components [config, db, http, router] and starts a webserver with all components injected in the http context.  
Gets the web port from the config file [config file](test/resources/config.edn#L3) or the enviroment var `PORT`
### Libraries
- [reitit](https://github.com/metosin/reitit)
- [pedestal](https://github.com/pedestal/pedestal)

# Helpers

## [helpers/state-flow/server/pedestal](src/parenthesin/helpers/state_flow/server/pedestal.clj)
Extract `io.pedestat.http/service-fn` from state-flow context and calls `io.pedestat.test/response-for` to simulate and http request on the system server.  
*Check [system integration tests](./test/integration/parenthesin/schema/system_test.clj#L79) to see how to use this function.*

## [helpers/state-flow/db](src/parenthesin/helpers/state_flow/db.clj)
Exposes function to direclty execute sql commands on the state-flow context db.  
*Check [db integration tests](test/integration/parenthesin/db/jdbc_hikari_test.clj) to see how to use these functions.*

## [helpers/state-flow/http](src/parenthesin/helpers/state_flow/http.clj)
Exposes functions to set/get http mock state.  
*Check [http integration tests](test/integration/parenthesin/http/clj_http_test.clj) to see how to use these functions.*

## [helpers/logs](src/parenthesin/helpers/logs.clj)
Setup function with preset appender and nice macro to log over `timbre/log!`

## [helpers/malli](src/parenthesin/helpers/malli.clj)
Function to start / stop instrumentation as `clojure.test/use-fixtures`

## [helpers/migrations](src/parenthesin/helpers/migrations.clj)
Wrapper over migratus to create an CLI based API.  
Depends on aero and jdbc to read and connect to the database.

# Projects using this library
These projects are using the library, they can be used as templates or source of documentation of how use the components.
- [parenthesin/microservice-boilerplate](https://github.com/parenthesin/microservice-boilerplate)
- [parenthesin/microservice-boilerplate-malli](https://github.com/parenthesin/microservice-boilerplate-malli)

# Contributing

## Tests
To run unit tests inside `./test/unit`
```bash
clj -M:test :unit
```
To run integration tests inside `./test/integration`
```bash
clj -M:test :integration
```
To run all tests inside `./test`
```bash
clj -M:test
```
To generate a coverage report 
```bash
clj -M:test --plugin kaocha.plugin/cloverage
```

## Lint and format

```bash
clj -M:clojure-lsp format
clj -M:clojure-lsp clean-ns
clj -M:clojure-lsp diagnostics
```

## Build / Deploy

```bash
  # Build
  clojure -T:build jar :version '"0.1.0"'
  # Deploy
  env CLOJARS_USERNAME=username CLOJARS_PASSWORD=clojars-token clojure -T:build deploy :version '"0.1.0"'
```

# Features

## System
- [schema](https://github.com/plumatic/schema) Types and Schemas
- [malli](https://github.com/metosin/malli) High-performance Data-Driven Data Specification Library for Clojure/Script. 
- [component](https://github.com/stuartsierra/component) System Lifecycle and Dependencies
- [pedestal](https://github.com/pedestal/pedestal) Http Server
- [reitit](https://github.com/metosin/reitit) Http Routes System 
- [clj-http](https://github.com/dakrone/clj-http) Http Client
- [cheshire](https://github.com/dakrone/cheshire) JSON encoding
- [aero](https://github.com/juxt/aero) Configuration file and enviroment variables manager
- [timbre](https://github.com/ptaoussanis/timbre) Logging library
- [next-jdbc](https://github.com/seancorfield/next-jdbc) JDBC-based layer to access databases
- [hikaricp](https://github.com/brettwooldridge/HikariCP) A solid, high-performance, JDBC connection pool at last
- [tools.build](https://github.com/clojure/tools.build) Clojure builds as Clojure programs 
- [deps-deploy](https://github.com/slipset/deps-deploy) A Clojure library to deploy your stuff to clojars

## Tests & Checks
- [kaocha](https://github.com/lambdaisland/kaocha) Test runner
- [kaocha-cloverage](https://github.com/lambdaisland/kaocha-cloverage) Kaocha plugin for code coverage reports
- [schema-generators](https://github.com/plumatic/schema-generators) Data generation and generative testing
- [state-flow](https://github.com/nubank/state-flow) Testing framework for integration tests
- [matcher-combinators](https://github.com/nubank/matcher-combinators) Assertions in data structures
- [pg-embedded-clj](https://github.com/Bigsy/pg-embedded-clj) Embedded PostgreSQL for integration tests
- [clojure-lsp](https://github.com/clojure-lsp/clojure-lsp/) Code Format, Namespace Check and Diagnosis

# Documentation

If you want to know more about the components in general and each component or helper implementation, check the [documentation](doc/intro.md).
Contributions and suggestions are welcome.

# License

Copyright © 2023 Parenthesin

This is free and unencumbered software released into the public domain.
For more information, please refer to http://unlicense.org
