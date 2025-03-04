# OpenAPI Basics

## OpenApi: About

- API description language on top of JSON schema 
- intended for REST and JSON RPC APIs with `JSON` or `XML` payloads
- language independent API specification written in `JSON` or `YML`
- plenty of generators and tools for individual language bindings
- client and server stubs generation for models and api

## OpenApi: Building Blocks

- General info block (title, license, server, tags, ...)
- Operations (HTTP-verb + Path, request and responses, media-types)
- Schemata (named and anonymous models, parameters)
- Security (OAuth 2.0, HTTP BasicAuth, ...)

## OpenApi: Swagger Editor

![Editieren von OpenApi-Dateien in Swagger Editor.](swagger-editor.png){ height=70% }

## A1: Modelliere in OpenAPI

![Klassendiagramm für das Modell "Route".](../aufgaben/a1-uml.png){ height=70% }

# Property-Based-Testing Basics

## PBT: General

![Das Unit-Test Dilemma: "testing can  only find  bugs, not  prove  their absence" (E.W. Dijkstra).](PBT-cartoon.png){ height=70% }

## PBT: Basics

$$ \forall s \in S: P(s) = \top, \qquad P: S \to \{ \top, \bot \} $$ {#eq:pbt-basics}

- $S$: state space of valid instances
- $P$: property to prove, candidates involve [-@bib:pbt:patterns]:
  - invariants,
  - round-trips,
  - symmetry,
  - commutativity,
  - idem-potency, and 
  - induction

# Applying PBT to OpenAPI /-Generator

## Folie 3
