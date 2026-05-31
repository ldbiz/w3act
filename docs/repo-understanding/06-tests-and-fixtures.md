# Tests and Fixtures

## Test Framework

- **JUnit 4** — primary unit test framework
- **Cucumber / BDD** (via `info.cukes:cucumber-java` + `cucumber-junit`) — acceptance tests in Gherkin
- **HtmlUnit** — headless browser used in some integration tests
- Play's `fakeApplication()` / `inMemoryDatabase()` helpers for app-context tests

Tests are run with:

```
activator test
# or
play test
```

Parallel execution is disabled (`parallelExecution in Test := false` in `build.sbt`) because tests share a database connection.

---

## Test Files

### Unit Tests (`test/`)

| File | What it tests |
|---|---|
| `ApplicationTest.java` | Smoke test only (`1+1=2`); most URL/scope tests are commented out |
| `LicenseInheritanceTest.java` | Scope inheritance across parent/child URL hierarchies; licensing propagation |
| `QaStatusTest.java` | QA status enum mapping (Wayback issue names → internal status codes) |
| `QaWaybackTest.java` | QA status and Wayback integration helpers |
| `TopLevelDomainTest.java` | TLD-based UK scope detection (`.uk`, `.scot`, etc.) |
| `UkHostingTest.java` | GeoIP-based scope detection |
| `UkRegistrationTest.java` | WHOIS-based UK registrant check |
| `UrlFormatTest.java` | URL normalisation and validation logic |
| `UrlToInstanceTest.java` | URL-to-instance resolution |
| `ValidUrlTest.java` | Input URL validity checks |
| `DateConverterTest.java` | Date parsing and conversion utilities |
| `ExportAuthTest.java` | API authentication and export endpoint checks |
| `IntegrationTest.java` | Full-stack integration test (requires running app) |

### BDD / Cucumber Tests (`test/bdd/`)

| Feature file | What it tests |
|---|---|
| `exporting_targets.feature` | CSV export format and field presence |
| `url_check.feature` | Existence check for URLs in the database |
| `list_category_targets.feature` | Listing targets by category |
| `list_target_categories.feature` | Listing categories for a target |
| `malformed_url.feature` | Detection and handling of malformed URLs |
| `topleveldomain_check.feature` | TLD-based scope checks via BDD |

Step definitions live in `test/bdd/step_definitions/`.

---

## Fixture Data

Fixtures are YAML files in `conf/` loaded by `DataImport` when `application.data.import=true`.

| File | What it provides |
|---|---|
| `testdata-targets.yml` | Sample Targets with associated URLs, collections, and a test user |
| `testdata-accounts.yml` | Test user accounts with roles |
| `testdata-organisations.yml` | Sample organisations |
| `testdata-contact-persons.yml` | Sample contact persons linked to organisations |
| `testdata-tags.yml` | Sample tags |
| `testdata-taxonomies.yml` | Taxonomy nodes (collections, licences, QA types) |
| `testdata-documents.yml` | Sample Document records |
| `testdata-flags.yml` | Sample flag definitions |
| `testdata-mail-templates.yml` | Sample mail templates for crawl permission emails |
| `conf/fast-subjects.yml` | FAST subject vocabulary nodes (always imported, not test-only) |

The `testdata-targets.yml` fixture includes a concrete target (`http://acid.matkelly.com/`) which appears in several BDD scenarios as a known-good URL.

---

## Coverage Notes

- **Scope engine** is well covered: TLD, GeoIP, WHOIS, manual flags, and licence inheritance all have dedicated unit tests.
- **QA status mapping** is unit-tested.
- **API authentication** has a test (`ExportAuthTest`).
- **BDD tests** cover URL existence checks, CSV export format, and TLD scope.
- **The nightly CrawlActor** has no automated tests.
- **The licence email workflow** has no automated tests.
- **Twirl views / UI rendering** are not tested (commented out in `ApplicationTest`).
- Many tests require a running database or Play `fakeApplication()` — pure offline unit tests are limited.
