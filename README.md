# BattleForge API

[![CI](https://github.com/JoaoVitorMcz/battleforge-api/actions/workflows/ci.yml/badge.svg)](https://github.com/JoaoVitorMcz/battleforge-api/actions/workflows/ci.yml)
[![codecov](https://codecov.io/gh/JoaoVitorMcz/battleforge-api/branch/develop/graph/badge.svg)](https://codecov.io/gh/JoaoVitorMcz/battleforge-api)

Backend for a turn-based Pokémon battle simulator. **All battle logic lives here**: damage
calculation, turn resolution, status effects, held items and opponent AI. The client
([`battleforge-web`](https://github.com/JoaoVitorMcz/battleforge-web)) only renders the ordered list of
battle events this API returns.

Species, move and item data comes from [PokéAPI](https://pokeapi.co), normalised and cached in
PostgreSQL on first use. The client never talks to PokéAPI.

## Stack

Java 17 · Spring Boot 3.5 · Spring Web, Data JPA, Security (JWT), Validation · PostgreSQL 16 ·
Flyway · springdoc-openapi · Actuator · JUnit 5, Mockito, Testcontainers, WireMock, ArchUnit, JaCoCo · Docker

## Running locally

Requirements: JDK 17 and Docker.

```bash
cp .env.example .env          # then edit the values
docker compose up -d postgres
./mvnw spring-boot:run        # Windows: .\mvnw.cmd spring-boot:run
```

The `dev` profile is the default.

| Endpoint | |
|---|---|
| http://localhost:8080/swagger-ui.html | API documentation |
| http://localhost:8080/actuator/health | Health check |

## Tests

```bash
./mvnw test      # unit tests only, no Docker needed
./mvnw verify    # everything, including Testcontainers integration tests + JaCoCo
```

`./mvnw verify` needs Docker running. The coverage report lands in `target/site/jacoco/index.html`.

## Profiles

| Profile | Use |
|---|---|
| `dev` | Local development. Swagger on, SQL logged, sensible localhost defaults. |
| `test` | Automated tests. Datasource supplied by Testcontainers. |
| `prod` | Deployment. Every value from the environment, Swagger off. |

## Architecture

```
com.battleforge
├── controller/          REST entry points, thin
├── service/             orchestration
├── domain/              the battle engine
│   ├── model/           Pokemon and Move (cached catalog) vs BattlePokemon/BattleSide/BattleState (in-battle)
│   ├── battle/          TurnResolver, DamageCalculator, TypeChart
│   ├── strategy/        move effects (Strategy)
│   ├── item/            held item hooks
│   ├── ai/              opponent decisions, random team generation
│   └── state/           battle phases (State)
├── integration/pokeapi/ anti-corruption layer over PokéAPI
├── repository/          Spring Data JPA
├── dto/                 request/response records
├── security/            JWT
├── exception/           RFC 7807 error handling
└── config/              CORS, OpenAPI
```

Two decisions shape everything else:

**Catalog data and battle instances are different types.** `Pokemon` is immutable cached PokéAPI
data. `BattlePokemon` is one Pokémon inside one battle: current HP, computed stats, four moves with
remaining PP, status, stat boosts. They never mix.

**The engine is modelled for double battles from day one.** A `BattleSide` holds a list of active
slots and every move action carries a target, even though only singles are implemented. No signature
assumes one Pokémon per side.

These are enforced, not just documented: `ArchitectureTest` fails the build if the domain reaches for
Spring, for JPA inside the engine packages, or for the PokéAPI integration layer.

## Privacy (LGPD)

The system stores only username, email and password hash. Registration records which version of the
privacy policy was accepted and when.

- `GET /api/me/data-export` returns everything held about the account as JSON
- `DELETE /api/me` erases personal data and anonymises past battles, preserving history integrity

**External services receiving personal data:** Sentry, in production only, receives stack traces
(configured with `sendDefaultPii=false`). PokéAPI receives no user data whatsoever — it is only
queried for species, move and item names.

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
## Trademark notice

Fan project, non-commercial and not affiliated with Nintendo, Creatures Inc. or GAME FREAK Inc.
Pokémon is a registered trademark of those companies. Game data is provided by
[PokéAPI](https://pokeapi.co).
