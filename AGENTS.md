# AGENTS.md

Monorepo with independent subprojects. Each has its own `Makefile` and most have their own `AGENTS.md`. **Read the subproject `AGENTS.md` before working there.**

| Dir | What | Toolchain | Details |
|---|---|---|---|
| `kotlin/` | Spring Boot API (Postgres, Flyway) | Gradle, Kotlin | `kotlin/AGENTS.md` |
| `angular/` | SPA | Angular, yarn, Jest (not Karma) | `angular/AGENTS.md` |
| `go/{imagine,healthbells,remindabot}` | Spin-off tools | 3 separate Go modules, no `go.work` | `go/*/AGENTS.md` |
| `terraform/` | AWS infra | **OpenTofu (`tofu`)**, not `terraform` | `terraform/AGENTS.md` |
| `docs/` | Antora docs (`antora-playbook.yml` at root) | AsciiDoc | |
| `tools/` | Local docker-compose helpers (mock OIDC, sonar, grafana) | | |

## Gotchas
- Run commands from inside the subproject dir. Root `Makefile` targets mostly delegate via `make -C <dir>`; root `api-build`/`build` point at a nonexistent `api/` dir and are broken.
- Subproject Makefiles fail at parse time if local, gitignored config is missing:
  - `kotlin/`: needs `~/.angkor/.env` and `kotlin/config/application.properties` (from `.tmpl`).
  - `angular/`: needs `src/assets/window-env.js` (template: `src/assets/window-env.tmpl.js`).
  - `terraform/`: needs `terraform.tfvars` / `terraform-backend.tf` from `.tmpl` and `AWS_PROFILE`.
  - If they are missing, call the underlying tools directly (`./gradlew`, `yarn`, `go test`) and don't commit those files.
- Kotlin: `make test` / full `./gradlew test` needs a running Postgres (`angkor_test` DB, settings in gitignored `src/test/resources/application-test.properties`). DB-free unit tests only: `./gradlew test --tests "*UT"` (same as `make ut`, and what CI runs for dependabot).
- Kotlin DB changes: add a Flyway migration in `kotlin/src/main/resources/db/migration` (`V<major>.<minor>__Name.sql`, continue the latest number). Test-only seed data goes in `db/testdata` (`V99.x`) and is loaded only by the `test` profile.
- Go: no root module, so run `go test ./...` inside each tool dir. `go/Makefile` `test`/`build` loop over all three; binaries land in `go/dist/`.
- CI (`.github/workflows/*.yml`) is path-filtered per subproject; images go to ghcr.io only on `main`. Commit messages containing `skip ci` skip the kotlin/angular builds.
- Releases: root `make release` uses `semtag` plus `tofu apply -target=module.release`, then pushes a git tag. Don't run it unless asked.
