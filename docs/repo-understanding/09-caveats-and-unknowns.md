# Caveats and Unknowns

## Missing Context

### Drupal Integration
`conf/application.conf` contains `drupal_user` and `drupal_password` fields, and `Const.java` has Drupal-specific URL constants (e.g. `URL_STR = "http://www.webarchive.org.uk/act/node.json?type="`). The codebase appears to have been migrated away from a previous Drupal-based system. It is unclear whether any live Drupal connection is still used in production.

### Secret Server
`conf/docker.conf` references `secret_server_user` and `secret_server_password` settings. There is no code in the repository that clearly shows what these credentials are used for. This appears to be an external credential store, but its role is unconfirmed.

### PII Endpoint
`conf/docker.conf` includes a `pii_url` setting pointing to a PII (Personally Identifiable Information) detection service. The `Pii.java` controller exists but its integration path is not fully documented in the codebase.

### OutbackCDX
The `README.md` mentions that OutbackCDX is not included in the Docker Compose setup and that some features will work slowly without it. The exact relationship between the CDX server endpoint in config and OutbackCDX is not defined in code.

---

## Ambiguous Naming

### "License" vs "Licence"
The codebase uses both spellings inconsistently. `LicenseController` and `LicencesController` are separate controllers managing different aspects of the same domain concept. `models/License.java` is the entity; `models/Licence.java` (if it exists) may be a different object. A new developer should verify which controller handles which workflow before making changes.

### DDHAPT
The acronym "DDHAPT" (Document and Data Harvesting and Processing Tool) is used throughout config and controllers but is not explained inline in the code. Its exact boundaries — what it includes vs. what the general `CrawlActor` handles — are not always clear.

### `application.activeCrawling`
This config flag controls whether the nightly CrawlActor is scheduled. It is not set in `application.conf` (defaults to null/false) and is only mentioned in `Global.java`. It is unclear how it is set in production environments that are not containerised.

---

## External Systems Not Present in the Repo

- **Heritrix / crawl infrastructure** — the crawlers that actually fetch web content are not in this repo. W3ACT manages *what* to crawl; the crawlers read from the API and write back results.
- **Wayback / UKWA playback** — the Wayback Machine is a separate service; W3ACT only queries it.
- **OutbackCDX** — the CDX index server is external.
- **Monitrix / ELK / Kibana** — linked as a dashboard URL; not part of this system.
- **DLS (Digital Library System)** — the downstream repository that receives SIP packages from the document harvester.

---

## Behaviour Inferred from Tests but Not Clearly Visible in Implementation

- The BDD feature `url_check.feature` implies a URL existence-check endpoint (e.g. "check if URL is in the DB"). The exact route and controller method are not immediately obvious from the route table.
- `ExportAuthTest.java` suggests the API has authentication requirements for the export endpoints, but the precise auth flow tested is not documented in the controller comments.

---

## Behaviour Visible in Code but Not Covered by Tests

- The nightly **CrawlActor** and document harvesting pipeline have no automated tests.
- The **licence email workflow** (token generation, email delivery, owner-facing form) has no tests.
- **PDF-to-HTML conversion** and duplicate detection (`compareHashes.sh`) are untested at the Java level.
- The **RabbitMQ publish path** (pushing crawl jobs to the queue) in `TargetController` is not tested.
- The **Welsh language** (`messages.cy`) support has no tests.

---

## Questions for Maintainers

- Is the Drupal connection (`drupal_user`, `drupal_password`) still active in any environment?
- What does the Secret Server integration do, and is it required for the app to start?
- Is `application.activeCrawling` enabled in any current production deployment?
- What is the current production config file used (Docker or non-Docker)? Is `prod.conf.template` still in use?
- Is `LicencesController` (plural) still needed, or has it been superseded by `LicenseController`?
- Are there any live Wayback / CDX environments the app is currently connected to, and do their API formats match the current query code?
- What version of MaxMind GeoLite2 is the bundled `GeoLite2-City.mmdb` — and when was it last updated?
