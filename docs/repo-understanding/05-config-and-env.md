# Configuration and Environment

## Config File Hierarchy

W3ACT uses Play's layered configuration. The active file is selected at startup with `-Dconfig.file=<path>`.

| Config file | Used when | Notes |
|---|---|---|
| `conf/application.conf` | Default / development | Hardcoded DB URL (`******127.0.0.1/w3act`); evolutions enabled |
| `conf/dev.conf` | Local dev | Minimal overrides on top of `application.conf` |
| `conf/dev-on-docker.conf` | Dev against Dockerised DB | Adjusts DB URL to Docker network |
| `conf/travis.conf` | CI (Travis) | Uses H2 in-memory database |
| `conf/docker.conf` | Production container | All sensitive values read from environment variables (see below) |
| `conf/prod.conf.template` | Non-Docker production | Template; requires manual population before deployment |

---

## Key Settings

### Application Identity

| Setting | Default | Effect |
|---|---|---|
| `application.name` | `w3act` | App name shown in UI |
| `application.context` | `/act` | URL prefix for all routes |
| `application.version` | `DEV` | Shown in the UI footer |
| `application.secret` | (hardcoded in dev) | Play's HMAC secret — **must be changed in production** |
| `application.langs` | `en,cy` | Supported UI languages: English and Welsh |

### Database

| Setting | Default | Effect |
|---|---|---|
| `db.default.driver` | `org.postgresql.Driver` | JDBC driver class |
| `db.default.url` | `******127.0.0.1/w3act` | JDBC URL |
| `evolutionplugin` | `enabled` | Whether schema evolutions run on startup |
| `applyEvolutions.default` | `true` | Auto-apply pending UP evolutions |
| `applyDownEvolutions.default` | `false` | Auto-apply DOWN evolutions (**destructive — keep false in prod**) |

### External Services

| Setting | Default | Effect |
|---|---|---|
| `application.wayback.url` | `http://crawler03.bl.uk:8081/wayback` | Wayback playback base URL |
| `application.cdxserver.endpoint` | `http://192.168.45.21:8080/data-heritrix` | CDX index query endpoint |
| `application.pdftohtmlex.url` | `http://192.168.99.100:5000/convert?url=` | PDF conversion service URL |
| `application.access.resolver.url` | (prod only) | Global access resolver for archived URLs |
| `application.monitrix.url` | (prod only) | Kibana/Monitrix dashboard URL |
| `queue_host` / `queue_port` | `crawler03.bl.uk` / `5672` | RabbitMQ connection details |
| `queue_name` / `routing_key` / `exchange_name` | `w3actqueue` / `w3actroutingkey` / `w3actexchange` | AMQP queue configuration |

### Mail

| Setting | Default | Effect |
|---|---|---|
| `host` | `juno.bl.uk` | SMTP server hostname |
| `port` | `25` | SMTP port |
| `from` | `web-archivist@bl.uk` | Sender address for all outgoing mail |
| `mail.user` / `mail.password` | `""` | SMTP credentials (empty = no auth) |

### Feature Flags

| Setting | Default | Effect |
|---|---|---|
| `application.data.import` | (not set) | If `true`, inserts seed/test data from YAML fixtures on startup |
| `application.activeCrawling` | (not set) | If `true`, schedules the nightly CrawlActor |
| `enableDDHAPT` | `true` (dev), `false` (prod template) | Enables the document harvesting subsystem |
| `ddhapt.submission.enabled` | `true` (Docker env) | Whether harvested documents are submitted as SIPs |
| `use.accounts` | `true` (dev), `false` (prod template) | Controls whether user account management is active |
| `admin.default.email` | `wa-sysadm@bl.uk` | Default system admin email |

### Document Storage (DDHAPT)

| Setting | Default | Effect |
|---|---|---|
| `dls.documents.ebook.sip.dir` | `/opt/data/w3act/ebooks` | Output directory for ebook SIPs |
| `dls.documents.ejournal.sip.dir` | `/opt/data/w3act/ejournals` | Output directory for ejournal SIPs |
| `dls.documents.sip.copy.dir` | `/opt/data/dls_sips_copy` | Copy destination for submitted SIPs |

---

## Environment Variables (Docker / `docker.conf`)

The Docker config reads all sensitive and environment-specific values from environment variables. These must be set before the container starts.

| Environment variable | Required | Maps to |
|---|---|---|
| `APPLICATION_SECRET` | Yes | `application.secret` |
| `DB_DRIVER` | Yes | `db.default.driver` |
| `DB_URI` | Yes | `db.default.url` |
| `SMTP_SERVER` | Yes | `host` |
| `WAYBACK_URL` | Yes | `application.wayback.url` |
| `CDXSERVER_ENDPOINT` | Yes | `application.cdxserver.endpoint` |
| `PDFTOHTMLEX_URI` | Yes | `application.pdftohtmlex.url` |
| `AMQP_HOST` | Yes | `queue_host` |
| `SERVER_NAME` | Yes | `server_name` |
| `ACCESS_RESOLVER_URI` | Yes | `application.access.resolver.url` |
| `PII_URI` | Yes | `pii_url` |
| `SECRET_SERVER_USER` / `SECRET_SERVER_PASSWORD` | Yes | `secret_server_user` / `secret_server_password` |
| `USE_TEST_DATA` | Yes | `application.data.import` |
| `ENABLE_DDHAPT` | Yes | `enableDDHAPT` |
| `DDHAPT_SUBMISSION_ENABLED` | Yes (Docker default: `true`) | `ddhapt.submission.enabled` |
| `ENABLE_EVOLUTIONS` | No | `evolutionplugin` |
| `APPLY_EVOLUTIONS` | No | `applyEvolutions.default` |
| `APPLY_DOWN_EVOLUTIONS` | No | `applyDownEvolutions.default` — **dangerous** |
| `MONITRIX_URI` | No | `application.monitrix.url` |
| `APPLICATION_NAVBAR_COLOR` | No | `application.navbar.background-color` |
