# Diagrams

## 1. Runtime Flow

```mermaid
flowchart TD
    A[Application starts] --> B[Load config file]
    B --> C{Evolutions enabled?}
    C -->|Yes| D[Apply DB migrations]
    C -->|No| E[Skip migrations]
    D --> F[DataImport: seed data if configured]
    E --> F
    F --> G[Ensure closed role exists]
    G --> H{activeCrawling enabled?}
    H -->|Yes| I[Schedule CrawlActor at midnight]
    H -->|No| J[Skip actor]
    I --> K[App ready on :9000]
    J --> K

    K --> L[HTTP request arrives]
    L --> M{Session authenticated?}
    M -->|No| N[Redirect to /act/login]
    M -->|Yes| O[Route to Controller]
    O --> P[Ebean query → PostgreSQL]
    P --> Q[Twirl view or JSON response]

    I --> R[Midnight: CrawlActor fires]
    R --> S[Query Wayback CDX per WatchedTarget]
    S --> T[Save new Document records]
    T --> U[+1h: ConvertMessage]
    U --> V[convertPdfToHtml.sh → pdftohtmlEX]
    V --> W[compareHashes.sh → dedup alerts]
```

---

## 2. Module and File Interaction

```mermaid
graph LR
    routes["conf/routes"] --> ctrl["Controllers\napp/controllers/"]
    ctrl --> models["Domain Models\napp/models/"]
    ctrl --> views["Twirl Views\napp/views/"]
    models --> ebean["Ebean ORM"]
    ebean --> pg["PostgreSQL"]
    ctrl --> scope["Scope Engine\nuk.bl.scope.Scope"]
    scope --> geoip["GeoLite2-City.mmdb"]
    scope --> whois["JRuby WHOIS"]
    ctrl --> api["APIController\n/act/api/targets/"]
    api --> models
    ctrl --> email["EmailHelper\nSMTP"]
    ctrl --> wayback["WaybackController\nCDX Server"]
    crawlactor["CrawlActor\nuk.bl.crawling"] --> wayback
    crawlactor --> crawler["Crawler\nuk.bl.crawling"]
    crawler --> models
    crawlactor --> convert["convertPdfToHtml.sh\npdftohtmlEX"]
    global["Global.java"] --> crawlactor
    global --> dataimport["DataImport\nuk.bl.db"]
    dataimport --> fixtures["conf/testdata-*.yml"]
```

---

## 3. Sequence Diagram — Crawl Licence Request Workflow

This is the most complex multi-party behaviour in the system.

```mermaid
sequenceDiagram
    participant A as Archivist
    participant W as W3ACT UI
    participant DB as PostgreSQL
    participant M as SMTP Mail Server
    participant O as Site Owner

    A->>W: Open CrawlPermission form for Target
    W->>DB: Load Target, ContactPerson, MailTemplates
    DB-->>W: Return data
    A->>W: Select ContactPerson, MailTemplate, Licence; submit
    W->>DB: Create CrawlPermission record (status=pending)
    W->>M: Send email to ContactPerson with token URL
    M-->>O: Email delivered

    O->>W: Visit /act/ukwa/licenseform/:token (unauthenticated)
    W->>DB: Look up CrawlPermission by token
    DB-->>W: Return CrawlPermission + Licence terms
    W-->>O: Render licence form

    O->>W: Accept licence (submit form)
    W->>DB: Update CrawlPermission status = agreed
    W->>DB: Update Target licence status
    W-->>O: Show confirmation page

    A->>W: View Target — licence now shown as agreed
```

---

## 4. Scope Determination Flow

How the system decides whether a Target URL is in legal-deposit scope.

```mermaid
flowchart TD
    Start([URL submitted]) --> ManualCheck{Manual curator flags set?}
    ManualCheck -->|UK hosting / postal address / judgement / correspondence| InScope([In Scope — Manual])
    ManualCheck -->|None| LicenceCheck{Licence granted?}
    LicenceCheck -->|Yes| InScope2([In Scope — By Licence])
    LicenceCheck -->|No| TLDCheck{TLD in .uk .scot .wales .cymru .london?}
    TLDCheck -->|Yes| InScope3([In Scope — TLD])
    TLDCheck -->|No| GeoCheck{GeoIP → GB?}
    GeoCheck -->|Yes| InScope4([In Scope — GeoIP])
    GeoCheck -->|No| WhoisCheck{WHOIS → UK registrant?}
    WhoisCheck -->|Yes| InScope5([In Scope — WHOIS])
    WhoisCheck -->|No| OutOfScope([Out of Scope])
```
