# Core Concepts

## Target

**Meaning:** The central domain object. A Target represents a website (or section of a website) that the British Library intends to archive.

Each Target has:
- One or more **FieldUrls** (seed URLs for crawling)
- A **scope** (how deep to crawl: `root`, `subdomains`, etc.)
- A **crawl frequency** (`DAILY`, `WEEKLY`, `MONTHLY`, etc.)
- Links to Collections, Subjects, Tags, Licences, and QA Issues

**Where it appears:** `app/models/Target.java`, `app/controllers/TargetController.java`, `conf/evolutions/default/1.sql`

---

## Legal Deposit Scope

**Meaning:** Whether a Target falls under the UK Non-Print Legal Deposit (NPLD) regulations, giving the British Library the right to crawl it without explicit permission.

The scope engine in `Scope.java` evaluates multiple criteria in order:

1. Manual curator overrides (UK hosting, postal address, professional judgement, correspondence)
2. TLD matching (`.uk`, `.scot`, `.wales`, `.cymru`, `.london`)
3. GeoIP lookup (MaxMind GeoLite2 — is the IP in GB?)
4. WHOIS lookup (is the registrant UK-based?)

A Target is also in scope if a crawl **Licence** has been granted, even without NPLD criteria.

**Where it appears:** `app/uk/bl/scope/Scope.java`, `app/models/Target.java` (`isTopLevelDomain()`, `isUkHosting()`, `isUkRegistration()`, `checkManualScope()`, `checkLicense()`)

---

## Crawl Permission / Licence

**Meaning:** A formal permission or licence granted by a website owner to the British Library, authorising crawling and/or publication of their site.

- A **CrawlPermission** records the workflow state (request sent, agreed, refused) and links to a **ContactPerson** and a **MailTemplate**.
- A **License** (note: mixed UK/US spelling in the codebase) records the actual agreed licence terms.
- The licence workflow uses **token-based URLs** sent to site owners so they can view or accept a licence without logging in.

**Where it appears:** `app/models/CrawlPermission.java`, `app/models/License.java`, `app/controllers/LicenseController.java`, `app/controllers/CrawlPermissionController.java`

---

## Collection

**Meaning:** A named grouping of Targets, used to organise the archive thematically (e.g. "UK Government Websites", "News Sites").

Collections can be hierarchical (a Collection can have sub-collections). They are linked to Targets in a many-to-many relationship.

**Where it appears:** `app/models/Collection.java`, `app/controllers/CollectionController.java`

---

## Subject

**Meaning:** A topic classification applied to Targets, based on the FAST (Faceted Application of Subject Terminology) vocabulary.

- `Subject` is a full FAST subject node with hierarchy.
- `FastSubject` is a lighter, importable version loaded from `conf/fast-subjects.yml`.

**Where it appears:** `app/models/Subject.java`, `app/models/FastSubject.java`, `conf/fast-subjects.yml`, `app/controllers/SubjectController.java`

---

## Instance

**Meaning:** A specific archived capture of a Target — a snapshot at a particular point in time. Instances are linked to crawl records and carry QA status.

**Where it appears:** `app/models/Instance.java`, `app/controllers/InstanceController.java`

---

## QA (Quality Assurance)

**Meaning:** The review process applied to crawled Instances to determine whether they are suitable for publication.

QA statuses include:
- `PASSED_PUBLISH_NO_ACTION_REQUIRED`
- `FAILED_DO_NOT_PUBLISH`
- `FAILED_PASS_TO_ENGINEER`
- `RECRAWL_REQUESTED`
- `ISSUE_NOTED`

QA Issues (e.g. "Missing content", "Robot blocks") can be attached to Targets.

**Where it appears:** `app/models/QaIssue.java`, `app/controllers/QAController.java`, `app/uk/bl/Const.java` (`QAStatusType`)

---

## Watched Target

**Meaning:** A Target that is actively monitored for new archived documents. The nightly `CrawlActor` iterates all WatchedTargets, checks Wayback for new captures since the last known timestamp, and harvests any new documents.

**Where it appears:** `app/models/WatchedTarget.java`, `app/controllers/WatchedTargets.java`, `app/uk/bl/crawling/CrawlActor.java`

---

## Document

**Meaning:** A harvested file (PDF, ebook, ejournal) discovered within a crawled Watched Target. Documents are converted to HTML (via pdftohtmlEX) and checked for duplicates using content hashes. Ebooks and ejournals can be submitted as SIPs (Submission Information Packages) to the Digital Library System.

**Where it appears:** `app/models/Document.java`, `app/controllers/Documents.java`, `app/uk/bl/crawling/CrawlActor.java`, `app/controllers/DocumentSIPController.java`

---

## Evolution

**Meaning:** Play Framework's incremental database migration mechanism. Each numbered `.sql` file in `conf/evolutions/default/` contains an `# --- !Ups` and `# --- !Downs` section, applied in order to bring the schema up to date. In production they are typically disabled and applied manually.

**Where it appears:** `conf/evolutions/default/1.sql` through `7.sql`

---

## CrawlActor

**Meaning:** An Akka `UntypedActor` that runs on a 24-hour schedule (triggered at midnight). It processes all WatchedTargets, retrieves new captures from Wayback, stores harvested Documents, and then schedules PDF-to-HTML conversion one hour later.

**Where it appears:** `app/uk/bl/crawling/CrawlActor.java`, `app/Global.java`

---

## DDHAPT

**Meaning:** Document and Data Harvesting and Processing Tool — the subsystem responsible for harvesting ebooks and ejournals from the web archive and submitting them as SIPs to the Digital Library System. Controlled by the `enableDDHAPT` / `ddhapt.submission.enabled` config flags.

**Where it appears:** `conf/application.conf`, `conf/docker.conf`, `app/controllers/DocumentSIPController.java`

---

## Taxonomy

**Meaning:** A general-purpose hierarchical classification system used within W3ACT for organising collections, licences, and QA issue types. `TaxonomyType` defines the kind of taxonomy; `Taxonomy` records the nodes.

**Where it appears:** `app/models/Taxonomy.java`, `app/models/TaxonomyType.java`, `app/controllers/TaxonomyController.java`
