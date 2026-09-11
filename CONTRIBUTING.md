# Contributing to BattleForge API

These conventions apply to every contributor, human or AI agent. Read them before touching the code.

## The rule that overrides everything else

**Only the developer commits and pushes.** No agent, script or automation ever runs `git commit`,
`git push`, or opens and merges a pull request. An agent may create branches and GitHub Issues, and
must finish a unit of work by handing over: the target branch, the changed files, and a ready-to-use
commit message. The developer reviews it, commits and pushes.

An agent must not start the next task while the previous one sits uncommitted without saying so.

## Branching (GitFlow)

| Branch | Purpose |
|---|---|
| `main` | Release history. Receives merges from `develop` at release time. |
| `develop` | Integration branch. All feature work starts here and returns here. |
| `feature/<issue>-short-description` | One branch per Issue, cut from `develop`. |

```bash
git switch develop
git switch -c feature/12-damage-calculator
```

## Commits

[Conventional Commits](https://www.conventionalcommits.org/), one commit per coherent unit of work.

```
<type>(<optional scope>): <imperative summary in English>
```

Types in use: `feat`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `ci`, `perf`.

```
feat(battle): resolve turn order by priority then effective speed
test(damage): cover STAB, 4x weakness and burn multipliers
```

Everything in the repository is written in English: code, class and method names, commit messages,
branch names and documentation.

## Pull requests

PRs target `develop` and reference their Issue with `Closes #N` in the description. Fill in the
template. CI must be green before merge.

## Definition of done

A unit of work is done when all of these hold:

- `./mvnw verify` is green, including the Testcontainers-backed `*IT` tests
- new behaviour is covered by tests; domain logic is covered thoroughly
- no secrets in the repository; `.env.example` documents any new variable
- simulated or hardcoded data exists in tests only, never in `dev` or `prod`
- ArchUnit rules still pass (they are the architecture, not a suggestion)

## Code conventions

- Java 17, Spring Boot 3.5. No new technology without justifying it and asking first.
- Constructor injection only. Field `@Autowired` is not used.
- Controllers are thin: input validation and delegation, zero business logic.
- Request and response DTOs are `record`s. JPA entities are never exposed by a controller.
- Errors are returned as RFC 7807 `ProblemDetail` through the global handler.
- Files above roughly 250 lines, or long methods, get split.
- Prefer the simple solution. Look for existing code before writing something similar.
- Comment the *why*, not the *what*.
- `Random` is injected, never instantiated inline, so tests can pin a seed.

## Tests

- Unit tests end in `Test` and run under Surefire, with no Docker requirement.
- Integration tests end in `IT`, run under Failsafe during `verify`, and use Testcontainers.
- Local runs of `*IT` need Docker running.
