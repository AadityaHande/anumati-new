# Anumati Frontend

Production-oriented Next.js interface for Anumati, the regulatory intelligence and approval-readiness platform.

## Stack

- Next.js App Router
- React + TypeScript
- CSS with a small internal design system
- Spring Boot backend integration via `NEXT_PUBLIC_API_URL`

## Routes

- `/` product site
- `/app` workspace
- `/app/profiles/new` business profile creation
- `/app/profiles/[id]` business profile detail
- `/privacy` privacy policy
- `/terms` terms and conditions

## Environment

Copy `.env.example` to `.env.local` and set the backend API URL and final site URL.

`NEXT_PUBLIC_SITE_URL` should be the real custom domain when the domain is connected.

## Run

```bash
npm install
npm run dev
```

Build:

```bash
npm run build
```

Type-check:

```bash
npm run typecheck
```

## Domain

The app is domain-neutral. Connect the chosen domain at the hosting provider and set `NEXT_PUBLIC_SITE_URL` to the canonical HTTPS origin. No domain is hardcoded into product content.

## Product UI rules

Do not add fabricated testimonials, customer counts, metrics, reviews, AI badges, generated stock photography, cursor effects, excessive motion, emoji as UI icons, or claims that are not supported by product data.
