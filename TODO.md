## Next Todo: 10

## 9 Repo: Clean up stale branches and old Node (S)

Remote branch feat/update-github-actions-node24 (closed PR #903): git push origin --delete feat/update-github-actions-node24
Local branch material-17 (superseded by #942), check other old local branches (git branch).
nvm uninstall 24.11.0 (default is now 24 -> 24.22.3).

## 8 CI: Merge dependabot PR #917 (setup-node v7, setup-java v6) (S)

Still open after the dependabot cleanup on 2026-11-26, not superseded by #944. Touches angular.yml, docs.yml (setup-node) and kotlin.yml (setup-java), should merge without conflicts.

## 7 CI: Renew SONAR_TOKEN before it expires (S)

New personal SonarCloud token created 2026-11-26 (SonarCloud deletes expired tokens, CI then fails with 'Failed to query JRE metadata'). Set a reminder before expiry, or switch to a Scoped Organization Token if the SonarCloud plan supports it.
Renewal procedure: terraform/modules/secrets_read/README.md (Phase staging /provided -> targeted tofu apply -> synced to GitHub).

## 6 Docs: Fix broken xref to geodata.adoc (S)

Antora build error: docs/modules/ROOT/pages/model/index.adoc links to geodata.adoc#_geographical_hierarchy (target of xref not found). Page still builds.

## 5 Angular UI: Update or remove json-server mock (S)

angular/json-server db.json is outdated compared to the real API (places search etc. differ). yarn mock now runs json-server@0.17.4 via npx (v1 dropped --routes/--middlewares).
Alternative for local UI testing: real backend with 'APP_NOAUTH_USER_ID=<app_user id> make run-noauth' in kotlin/ (see kotlin/AGENTS.md). Decide: refresh mock data or remove mock + mock.sh.

## 4 Angular UI: Replace unmaintained ngx-logger (S)

ngx-logger is stuck on 5.0.12 (no releases for years, peer only rxjs). Still works with Angular 22, but if it breaks: replace with a small own logging service (same API subset: debug/info/warn/error, LoggerTestingModule in specs).

## 3 Angular UI: Reduce initial bundle size (M)

Prod build: initial 3.9 MB vs. 2 MB budget warning (angular.json budgets). Candidates: lazy load map/mapbox-gl and other feature areas via routes, check vendor size.
area-display.component.scss is 13.6 kB vs. 6 kB budget (flag sprite).

## 2 Angular UI: Switch from M2 to Material 3 theme (M)

Theme still uses mat.m2-* (kept on purpose during the update to keep the old look) plus global MDC tweaks in angular/src/styles.scss (lists, cards, form fields, button rows, nav icons).
Optional: try M3 (mat.theme), then re-check which global tweaks are still needed.

## 1 Angular UI: Migrate to standalone components, inject() and OnPush (L)

Angular 22 pinned ChangeDetectionStrategy.Eager on 36 components (22 defaults to OnPush), app still uses NgModules (standalone: false) and constructor injection. Lint rules prefer-standalone, prefer-inject and prefer-on-push-component-change-detection are switched off in angular/eslint.config.js.
Angular migrations: ng generate @angular/core:standalone, @angular/core:inject. OnPush needs care (manual change detection, signals). Re-enable the lint rules afterwards and check all pages visually, esp. the map (mapbox worker, popups) and dialogs.

---
