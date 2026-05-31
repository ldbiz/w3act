# W3ACT — Repository Summary

## Purpose

W3ACT (Web Archive Curation Tool) is an annotation and curation management system for web archives, built for the UK Web Archive at the British Library.

It allows archivists to:

- Define and manage **Targets** (websites to be crawled)
- Track whether each Target is in **legal-deposit scope** under UK law
- Manage **crawl permissions** and **licences** from site owners
- Conduct **QA (quality assurance)** on crawled instances
- Discover and harvest **documents** (PDFs, ebooks, ejournals) from web archives
- Organise targets into **Collections** and **Subjects**

## Technology Stack

| Layer | Technology |
|---|---|
| Language | Java 8 |
| Web framework | Play Framework 2.x (Scala build, Java app) |
| ORM | Ebean (JPA-compatible) |
| Database | PostgreSQL (H2 for tests) |
| Templating | Twirl (`.scala.html`) |
| Build | sbt (via Typesafe Activator) |
| Async actors | Akka |
| Queue | RabbitMQ (AMQP) |
| GeoIP | MaxMind GeoLite2 |
| WHOIS | JRuby + `jruby-whois` library |
| Containers | Docker / Docker Compose |
| Testing | JUnit 4, Cucumber/BDD |

## Main Runtime Model

W3ACT is a stateful, session-based web application.

- Runs as a single JVM process on port 9000.
- Serves an HTML UI under the `/act` context path (configurable).
- Exposes a REST-like JSON API at `/act/api/` for machine clients (e.g. crawlers).
- At startup, optionally imports seed data and schedules a background **CrawlActor** via Akka for nightly document harvesting.

## Main External Services / Dependencies

| Service | Role |
|---|---|
| PostgreSQL | Primary data store |
| RabbitMQ (AMQP) | Queues crawl jobs published to crawlers |
| Wayback / CDX Server | Looks up archived captures for QA |
| MaxMind GeoLite2 | IP-to-country geolocation for scope checking |
| WHOIS (JRuby) | Domain registrant lookup for scope checking |
| pdftohtmlEX | PDF-to-HTML conversion for document harvesting |
| SMTP | Email delivery for licence workflows |
| Monitrix / Kibana | Log/monitoring dashboard (linked, not embedded) |

## Main Entry Points

| Entry point | Description |
|---|---|
| `app/Global.java` | Application startup hook (data import, actor scheduling) |
| `conf/routes` | All HTTP routes |
| `app/controllers/ApplicationController.java` | Login, logout, home |
| `app/controllers/TargetController.java` | Core CRUD for Targets |
| `app/controllers/APIController.java` | JSON API for crawler integration |
| `Dockerfile` + `conf/docker.conf` | Containerised production entry point |

## Architecture Summary

The application follows a standard **Play MVC** pattern:

- **Models** (`app/models/`) are Ebean entities mapped to PostgreSQL tables. `Target` is the central domain object, linked to `FieldUrl`, `Collection`, `Subject`, `License`, `CrawlPermission`, and more.
- **Controllers** (`app/controllers/`) handle HTTP requests. Most are secured with session-based authentication via `SecuredController`. A separate `APIController` serves JSON to machine clients using the same session auth.
- **Views** (`app/views/`) are Twirl templates that render HTML pages. Each domain area has its own subdirectory.
- **Scope engine** (`app/uk/bl/scope/Scope.java`) determines whether a Target falls under UK Legal Deposit, using TLD matching, GeoIP, WHOIS, and manual curator overrides.
- **CrawlActor** (`app/uk/bl/crawling/CrawlActor.java`) is an Akka actor that runs nightly, harvesting documents from the Wayback archive for watched targets and converting PDFs to HTML.
- **Database schema** is managed through Play Evolutions (`conf/evolutions/default/`).

Configuration is layered: `application.conf` provides defaults; environment-specific files (`dev.conf`, `docker.conf`, `prod.conf.template`) override them.

## Read These First

| File / Directory | Why |
|---|---|
| `conf/routes` | Complete map of all HTTP endpoints |
| `app/Global.java` | Application startup sequence |
| `app/models/Target.java` | Central domain entity; most features orbit it |
| `app/controllers/TargetController.java` | Core CRUD and licensing workflow for Targets |
| `app/uk/bl/scope/Scope.java` | Legal deposit scope rule engine |
| `app/controllers/APIController.java` | JSON API used by crawlers |
| `conf/application.conf` | Base configuration |
| `conf/docker.conf` | Production/container configuration |
| `conf/evolutions/default/` | Database schema history |
| `app/uk/bl/crawling/CrawlActor.java` | Background document harvesting |
