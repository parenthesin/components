# Contributing

## Workflow

### Start with an issue before writing code

Before writing any code, please create an issue first that describes the problem
you are trying to solve with alternatives that you have considered. A little bit
of prior communication can save a lot of time on coding. Keep the problem as
small as possible. If there are two problems, make two issues. We discuss the
issue and if we reach an agreement on the approach, it's time to move on to a
PR.

### Follow up with a pull request

Post a corresponding PR with the smallest change possible to address the
issue. Then we discuss the PR, make changes as needed and if we reach an
agreement, the PR will be merged.

### Tests

Each bug fix, change or new feature should be tested well to prevent future
regressions.

If possible, tests should use public APIs. If the bug is in private/internal
code, try to trigger it from a public API.

### Force-push

Please do not use `git push --force` on your PR branch for the following
reasons:

- It makes it more difficult for others to contribute to your branch if needed.
- It makes it harder to review incremental commits.
- Links (in e.g. e-mails and notifications) go stale and you're confronted with:
  this code isn't here anymore, when clicking on them.
- Your PR will be squashed anyway.

### Change log
All notable changes to this project will be documented in [CHANGELOG.md](CHANGELOG.md).
This change log follows the conventions of [keepachangelog.com](http://keepachangelog.com/).

## Developing

### Requirements

You need Java (JDK 21+) and Clojure (v1.12.0+) installed in your machine.

### Clone repository

``` shellsession
$ git clone https://github.com/parenthesin/components
```

### Repl

```bash
clj -M:test:nrepl
```

### Lint and Format

```bash
clj -M:clojure-lsp format
clj -M:clojure-lsp clean-ns
clj -M:clojure-lsp diagnostics
```

### Tests
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

### Build / Deploy

```bash
  # Build
  clojure -T:build jar :version '"0.1.0"'
  # Deploy
  env CLOJARS_USERNAME=username CLOJARS_PASSWORD=clojars-token clojure -T:build deploy :version '"0.1.0"'
```

## Design decisions

TODO
