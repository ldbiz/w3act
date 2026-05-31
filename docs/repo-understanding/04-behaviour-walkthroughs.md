# Behaviour Walkthroughs

## 1. Creating and Scoping a Target

**Trigger:** An archivist navigates to `/act/targets/new` and submits the form.

**Files / modules involved:**
- `app/controllers/TargetController.java`
- `app/models/Target.java`
- `app/models/FieldUrl.java`
- `app/uk/bl/scope/Scope.java`
- `app/views/targets/`

**Flow:**

The archivist enters one or more seed URLs, a title, crawl frequency, and collection/subject associations. On submission, `TargetController` binds the form to a `Target` entity and validates each URL.

Before saving, the scope engine (`Scope.java`) evaluates each URL against legal-deposit criteria:

- Does the TLD match a UK-specific domain (`.uk`, `.scot`, `.wales`, `.cymru`, `.london`)?
- Does GeoIP place the server in GB?
- Does WHOIS identify a UK registrant?

The archivist can also set manual scope flags (UK hosting, postal address, professional judgement, correspondence). These always override the automated checks.

The resulting scope determination is stored on the Target. The Target is then saved to PostgreSQL via Ebean.

**Inputs:** Form fields (title, URLs, scope flags, crawl frequency, collection, subject)

**Outputs:** Persisted `Target` record; scope flags updated; redirect to the target view page

**External calls:** GeoIP lookup (local MaxMind database); WHOIS lookup (JRuby, optional, controlled by `Scope.WHOIS_ENABLED`)

**Tests:** `LicenseInheritanceTest.java`, `UkHostingTest.java`, `UkRegistrationTest.java`, `TopLevelDomainTest.java`, BDD `topleveldomain_check.feature`

---

## 2. Crawl Licence Request Workflow

**Trigger:** An archivist initiates a crawl permission request for a Target that is not in legal-deposit scope.

**Files / modules involved:**
- `app/controllers/LicenseController.java`
- `app/controllers/CrawlPermissionController.java`
- `app/models/CrawlPermission.java`
- `app/models/License.java`
- `app/models/ContactPerson.java`
- `app/models/MailTemplate.java`
- `app/uk/bl/scope/EmailHelper.java`
- `app/views/licence/` and `app/views/crawlpermissions/`

**Flow:**

An archivist creates a `CrawlPermission` record linked to the Target and a `ContactPerson` (the site owner's representative). A `MailTemplate` is selected and an email is sent to the contact containing a unique token URL.

The site owner visits the token URL (e.g. `/act/ukwa/licenseform/:token`) — this route is **unauthenticated**. They see the licence terms and can accept or decline.

On acceptance, the `CrawlPermission` status is updated and the Target is marked as licensed. The licence status propagates to child URLs (subdomains/paths) through the scope inheritance logic in `Target.isInScopeAllOrInheritedWithoutLicense()`.

**Inputs:** Target ID, contact person, mail template, chosen licence

**Outputs:** Email sent to site owner; `CrawlPermission` record created; on acceptance, Target licence status updated

**External calls:** SMTP mail server

**Tests:** `LicenseInheritanceTest.java` (tests scope inheritance after licencing)

---

## 3. QA Review of a Crawled Instance

**Trigger:** A crawl has completed. An archivist visits the QA dashboard at `/act/qa`.

**Files / modules involved:**
- `app/controllers/QAController.java`
- `app/models/Target.java`
- `app/models/Instance.java`
- `app/models/QaIssue.java`
- `app/controllers/WaybackController.java`
- `app/views/qa/`

**Flow:**

The QA dashboard lists Targets filtered by collection and/or QA issue type. For each Target, the archivist can review its crawled Instance by following a link to the Wayback playback URL.

The archivist assigns a QA status (`PASSED_PUBLISH_NO_ACTION_REQUIRED`, `FAILED_DO_NOT_PUBLISH`, `RECRAWL_REQUESTED`, etc.) and optionally flags specific QA issues.

`WaybackController` is used to query the CDX server to confirm whether the URL has been captured and to retrieve the playback URL.

**Inputs:** Target/instance selection; QA status choice; optional issue flags

**Outputs:** Updated QA status on the Instance; flags stored on the Target

**External calls:** CDX Server (Wayback index query)

**Tests:** `QaStatusTest.java`, `QaWaybackTest.java`

---

## 4. JSON API — Crawler Integration

**Trigger:** An external crawler (Heritrix or a management tool) reads or updates a Target via the JSON API.

**Files / modules involved:**
- `app/controllers/APIController.java`
- `app/models/Target.java`
- `conf/routes` (`/act/api/targets/...`)

**Flow:**

The crawler authenticates using a session cookie (obtained via a `POST /act/login` with email/password credentials). It then calls `GET /act/api/targets/:id` to retrieve a Target as JSON, or `PUT /act/api/targets/:id` with a JSON body to update specific fields (e.g. `field_urls`, `uk_postal_address_url`).

`APIController` deserialises the JSON body, applies updates to the Ebean-managed `Target`, and persists the changes. For watched targets, the `isWatched` flag on the response tells the crawler whether to monitor the Target for new documents.

The API also supports listing targets for crawl feeds (`/act/api/targets` with query parameters) — the crawl feed is consumed by the UK Web Archive's crawl scheduler.

**Inputs:** HTTP JSON body; URL path parameters

**Outputs:** JSON representation of Target; HTTP 200/400/404

**External calls:** None (reads/writes PostgreSQL only)

**Tests:** `ExportAuthTest.java`

---

## 5. Nightly Document Harvesting

**Trigger:** Midnight (automatic, Akka scheduler). Can also be triggered manually via `WatchedTargets` controller actions.

**Files / modules involved:**
- `app/uk/bl/crawling/CrawlActor.java`
- `app/uk/bl/crawling/Crawler.java`
- `app/controllers/WatchedTargets.java`
- `app/controllers/Documents.java`
- `app/models/WatchedTarget.java`
- `app/models/Document.java`
- `conf/converter/convertPdfToHtml.sh`

**Flow:**

The CrawlActor wakes at midnight and retrieves all WatchedTargets. For each, it queries the Wayback CDX server for captures newer than the stored `waybackTimestamp`. Newly found captures are passed to `Crawler.crawlForDocuments()`, which fetches the archived page and extracts document links (PDFs, ebooks).

New Document records are saved to the database. If no documents are found, a flag is raised on the Target. After all targets are processed, a `ConvertMessage` is scheduled for one hour later.

During conversion, each Document is passed through `convertPdfToHtml.sh`, which calls the pdftohtmlEX web service. A SHA-256 hash is computed, and `compareHashes.sh` checks for duplicates. Duplicate documents trigger an alert.

**Inputs:** WatchedTargets from the database; Wayback CDX responses

**Outputs:** New `Document` records; updated `waybackTimestamp` on WatchedTarget; alerts for missing or duplicate documents

**External calls:** Wayback CDX Server; pdftohtmlEX HTTP service
