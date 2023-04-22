# parenthesin/components
Common components used on the Parenthesin's service templates.

# Components

## [str/parenthesin/components/config/aero.clj](config/aero)
Reads aero config file on `resources/config.edn`.  
Gets the current [profile](https://github.com/juxt/aero#profile) on enviroment var `SYSTEM_ENV`
### Libraries
- [aero](https://github.com/juxt/aero)

## [str/parenthesin/components/db/jdbc_hikari.clj](db/jdbc-hikari)
Depends on config component to read [connection info data](test/resources/config.edn#L3)
### Libraries
- [next-jdbc](https://github.com/seancorfield/next-jdbc)
- [hikaricp](https://github.com/brettwooldridge/HikariCP)

## [str/parenthesin/components/http/clj-http.clj](http/clj-http)
Has some [mock implementations](test/unit/parenthesin/components/http/clj_http_test.clj) for tests
### Libraries
- [clj-http](https://github.com/dakrone/clj-http)

## [str/parenthesin/components/router/reitit_malli.clj](router/reitit-malli)
Has some presets, handlers and configs to use malli as input/output validations for routes
### Libraries
- [reitit](https://github.com/metosin/reitit)
- [malli](https://github.com/metosin/malli)

## [str/parenthesin/components/router/reitit_malli.clj](router/reitit-malli)
Has some presets, handlers and configs to use schema as input/output validations for routes
### Libraries
- [reitit](https://github.com/metosin/reitit)
- [schema](https://github.com/plumatic/schema)

## [str/parenthesin/components/server/reitit_pedestal_jetty.clj](server/reitit-pedestal-jetty)
Depends on one of each type of the components [config, db, http, router] and starts a webserver with all components injected in the http context.  
Gets the web port from the config file [config file](test/resources/config.edn#L3) or the enviroment var `PORT`
### Libraries
- [reitit](https://github.com/metosin/reitit)
- [pedestal](https://github.com/pedestal/pedestal)

# Helpers

## [str/parenthesin/helpers/state_flow/server/pedestal.clj](helpers/state-flow/server/pedestal)
Extract `io.pedestat.http/service-fn` frow state-flow context and calls `io.pedestat.test/response-for` to simulate and http request on the system server.

## [str/parenthesin/helpers/state_flow/db.clj](helpers/state-flow/db)
Exposes function to direclty execute sql commands on the state-flow context db. 

## [str/parenthesin/helpers/state_flow/http.clj](helpers/state-flow/http)
Exposes functions to set/get http mock state.

## [str/parenthesin/helpers/logs.clj](helpers/logs)
Setup function with preset appender and nice macro to log over `timbre/log!`

## [str/parenthesin/helpers/malli.clj](helpers/malli)
Function to start / stop instrumentation as `clojure.test/use-fixtures`

## [str/parenthesin/helpers/malli.clj](helpers/malli)
Wrapper over migratus to create an CLI based API.  
Depends on aero and jdbc to read and connect to the database.

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
  clj -X:build :lib cc.delboni/helix-scratch :version '"0.1.0"'
  # Deploy
  env CLOJARS_USERNAME=username CLOJARS_PASSWORD=clojars-token clj -X:deploy :lib delboni/helix-scratch :version '"0.1.0"'
```

# Features

## System
- [schema](https://github.com/plumatic/schema) Types and Schemas
- [component](https://github.com/stuartsierra/component) System Lifecycle and Dependencies
- [pedestal](https://github.com/pedestal/pedestal) Http Server
- [reitit](https://github.com/metosin/reitit) Http Routes System 
- [clj-http](https://github.com/dakrone/clj-http) Http Client
- [cheshire](https://github.com/dakrone/cheshire) JSON encoding
- [aero](https://github.com/juxt/aero) Configuration file and enviroment variables manager
- [timbre](https://github.com/ptaoussanis/timbre) Logging library
- [next-jdbc](https://github.com/seancorfield/next-jdbc) JDBC-based layer to access databases
- [hikaricp](https://github.com/brettwooldridge/HikariCP) A solid, high-performance, JDBC connection pool at last
- [honeysql](https://github.com/seancorfield/honeysql) SQL as Clojure data structures
- [depstar](https://github.com/seancorfield/depstar) Generates Uberjars for releases

## Tests & Checks
- [kaocha](https://github.com/lambdaisland/kaocha) Test runner
- [kaocha-cloverage](https://github.com/lambdaisland/kaocha-cloverage) Kaocha plugin for code coverage reports
- [schema-generators](https://github.com/plumatic/schema-generators) Data generation and generative testing
- [state-flow](https://github.com/nubank/state-flow) Testing framework for integration tests
- [matcher-combinators](https://github.com/nubank/matcher-combinators) Assertions in data structures
- [pg-embedded-clj](https://github.com/Bigsy/pg-embedded-clj) Embedded PostgreSQL for integration tests
- [clojure-lsp](https://github.com/clojure-lsp/clojure-lsp/) Code Format, Namespace Check and Diagnosis

# License

Copyright © 2023 Rafael Delboni

This is free and unencumbered software released into the public domain.
For more information, please refer to http://unlicense.org
