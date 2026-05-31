# Important Files

## Entrypoints

| File / Directory | Role | Why it matters |
|---|---|---|
| `app/Global.java` | Application lifecycle hook | Runs on startup: triggers data import, ensures the `closed` role exists, schedules the nightly CrawlActor |
| `conf/routes` | HTTP routing table | Defines every URL the app serves; the authoritative map of all features |
| `app/controllers/ApplicationController.java` | Login / logout / home | The only controller that is not secured; owns the authentication flow |

## Configuration

| File / Directory | Role | Why it matters |
|---|---|---|
| `conf/application.conf` | Base configuration | Default values for database, mail, Wayback, AMQP, logging |
| `conf/docker.conf` | Docker/production config | Reads all sensitive values from environment variables; used by the production container |
| `conf/prod.conf.template` | Production config template | Static template for non-Docker deployments; shows expected production settings |
| `conf/dev.conf` / `conf/dev-on-docker.conf` | Dev overrides | Used when running the app locally against a live or Dockerised database |
| `conf/evolutions/default/` | Database schema | Play Evolutions SQL scripts (1.sql–7.sql); defines the full relational schema |

## Core Application Logic

| File / Directory | Role | Why it matters |
|---|---|---|
| `app/models/Target.java` | Central domain entity | Represents a website to be archived; links to URLs, collections, subjects, licences, QA issues |
| `app/models/` | All domain entities | Ebean-mapped JPA entities for every concept in the system |
| `app/controllers/TargetController.java` | Target CRUD + import | Manages creation, editing, bulk import, and licence workflow for Targets |
| `app/controllers/QAController.java` | QA dashboard | Lists targets needing quality assurance; filters by collection and QA issue type |
| `app/controllers/APIController.java` | JSON REST API | Exposes Target data and accepts updates from crawlers; uses cookie-based auth |
| `app/uk/bl/scope/Scope.java` | Legal deposit scope engine | Determines UK in-scope status via TLD, GeoIP, WHOIS, and manual curator flags |
| `app/uk/bl/crawling/CrawlActor.java` | Background document harvester | Akka actor; runs nightly to fetch documents from Wayback for watched targets |
| `app/uk/bl/crawling/Crawler.java` | Wayback document crawler | Fetches and parses captured pages from the Wayback archive |
| `app/uk/bl/db/DataImport.java` | Seed / test data loader | Inserts initial permissions, roles, organisations, users, and targets from YAML fixtures |
| `app/uk/bl/Const.java` | Global constants | Centralised constants for field names, enums (crawl frequency, QA status, scope types) |

## External Integrations

| File / Directory | Role | Why it matters |
|---|---|---|
| `app/controllers/WaybackController.java` | Wayback + CDX query | Queries the Wayback CDX server to check whether a URL has been archived |
| `app/controllers/LicenseController.java` | Licence workflow | Handles owner-facing licence forms (token-based, unauthenticated) and internal licence management |
| `app/uk/bl/scope/EmailHelper.java` | SMTP email | Sends licence request and crawl permission emails to site owners |
| `app/uk/bl/scope/Scope.java` | GeoIP + WHOIS | Uses MaxMind GeoLite2 and JRuby WHOIS to resolve domain scope automatically |
| `GeoLite2-City.mmdb` | GeoIP database | Binary database bundled with the application for IP-to-country lookups |
| `app/uk/bl/export/` | Data export utilities | Exports target lists and Wayback data; supports malformed-URL audits |

## UI / Templates / Static Assets

| File / Directory | Role | Why it matters |
|---|---|---|
| `app/views/` | Twirl HTML templates | One subdirectory per domain area; `main.scala.html` and `header.scala.html` are the layout wrappers |
| `public/` | Static assets (JS, CSS, images) | Served under `/assets/`; includes jQuery, Bootstrap, and app-specific scripts |
| `conf/messages.en` / `conf/messages.cy` | i18n message bundles | English and Welsh translations for UI labels |

## Tests and Fixtures

| File / Directory | Role | Why it matters |
|---|---|---|
| `test/` | All test sources | JUnit unit tests plus Cucumber BDD tests |
| `test/bdd/features/` | Gherkin feature files | Human-readable acceptance scenarios for URL checking, exporting, and TLD validation |
| `conf/testdata-*.yml` | YAML fixture data | Seed data (users, targets, collections, organisations) loaded by `DataImport` during tests |

## Build / Deployment

| File / Directory | Role | Why it matters |
|---|---|---|
| `build.sbt` | sbt build definition | Declares all dependencies, compiler settings, and test options |
| `Dockerfile` | Container image | Two-stage build: compiles with Activator, packages onto a slim JRE image |
| `docker-compose.yml` | Local multi-service stack | Runs W3ACT + PostgreSQL + optional pg_restore together for integration testing |
