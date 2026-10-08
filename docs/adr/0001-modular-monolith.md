# ADR 0001: Modular monolith, organized by domain

**Status:** Accepted
**Date:** 2026-10-07

## Context
LedgerSense has distinct concerns (identity, ingestion, categorization, detection,
review, insights, audit) but a single team (one developer), modest data volumes,
and a need for simple deployment.

## Decision
Build one Spring Boot application. Organize packages by business domain
(`ingestion/`, `detection/`, ...) rather than by technical layer (`controller/`,
`service/`, ...). Each domain owns its controllers, services, repositories and DTOs.

## Consequences
- One deployable unit: simple local setup, simple CI/CD, cheap hosting.
- Related code lives together, so changes to a feature stay local.
- Domain boundaries are explicit, so a module could be extracted into its own
  service later if real scaling needs appeared.
- Discipline required: domains must not reach into each other's repositories
  directly; they talk through services.
