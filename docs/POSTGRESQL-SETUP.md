# PostgreSQL Setup for Anumati

Anumati uses PostgreSQL as its system of record. **Do not create application tables manually.** Flyway creates and upgrades the schema when the backend starts.

## 1. Install PostgreSQL

Install PostgreSQL and keep the server running on the default local port:

- Host: `localhost`
- Port: `5432`

The prototype currently connects to database `anumati` using user `anumati`.

## 2. Create the local user and database in pgAdmin

Open pgAdmin and connect to your local PostgreSQL server.

Open **Query Tool** and run:

```sql
CREATE USER anumati WITH PASSWORD 'anumati-local';
CREATE DATABASE anumati OWNER anumati;
```

If the user already exists, do not run `CREATE USER` again. Use:

```sql
ALTER USER anumati WITH PASSWORD 'anumati-local';
```

If the database already exists, leave it in place.

## 3. Optional: verify the owner

In pgAdmin, the database should show:

- Database: `anumati`
- Owner: `anumati`

A matching connection can use:

| Field | Value |
|---|---|
| Host | `localhost` |
| Port | `5432` |
| Database | `anumati` |
| Username | `anumati` |
| Password | `anumati-local` |

## 4. Do not create tables manually

Start the backend with:

```cmd
02-BACKEND-BUILD-AND-START.cmd
```

Flyway validates and applies migrations automatically.

On the current prototype this creates `flyway_schema_history` and applies the numbered migrations. The backend log should end the migration phase with a message similar to:

```text
Successfully applied ... migrations to schema "public"
```

## 5. Verify the schema after the first start

In pgAdmin Query Tool:

```sql
SELECT version, description, success
FROM flyway_schema_history
ORDER BY installed_rank;
```

You should see a sequence of successful migrations.

Check the seeded reference profiles:

```sql
SELECT id, business_name, sector, district, business_stage, version_number
FROM business_profiles
ORDER BY business_name;
```

Check configured document requirements:

```sql
SELECT a.code, a.name, r.category, r.document_name, r.mandatory
FROM approval_document_requirements r
JOIN approvals a ON a.id = r.approval_id
WHERE r.active = true
ORDER BY a.code, r.document_name;
```

## 6. Important demo detail

`Reference Manufacturing Unit` is the seeded document-readiness scenario. Its MPCB approvals have verified document requirements, so its Documents page shows categories after analysis.

`Reference Food Processing Unit` is the second sector-neutral scenario. Its seeded FSSAI rule demonstrates applicability, but the current migration does **not** fabricate a document checklist for FSSAI. Therefore its Documents category dropdown can legitimately be empty.

For the video, use `Reference Manufacturing Unit` for the evidence/document part of the journey and use `Reference Food Processing Unit` only as the short sector-neutral second example.

## 7. PostgreSQL 18 warning

The current local run succeeded against PostgreSQL 18.6, but the installed Flyway version reports that its tested support ends at PostgreSQL 17. This is currently a warning, not a startup failure. Do not change the working local database solely because of that warning unless you need stricter version alignment.
> Local document storage uses the `local` Spring profile so the demo does not require an S3 account.
