# Task: wire Green API WhatsApp alerts into alert-service

**For:** Danai (zoonotic-disease-service owner), continuing on his own PC.
**Context for whoever picks this up (including a fresh Claude Code session with
no memory of prior discussion):** this file is self-contained. Read it fully
before touching any code.

## Background

DPDMS's alert-service already sends real email alerts (SMTP/Gmail) whenever a
supervisor approves an incident that meets that hazard's alerting criteria.
WhatsApp was scaffolded using Meta's official WhatsApp Business Cloud API
(`WhatsAppCloudNotifier.java`), but that path needs Meta business verification
and template approval, so it was never actually activated
(`WHATSAPP_ENABLED=false`).

Danai has separately gotten WhatsApp delivery working using **Green API**
(green-api.com) — an unofficial gateway that pairs to a normal WhatsApp number
by scanning a QR code, no Meta approval needed. The brief allows "the WhatsApp
Business Cloud API **or a comparable gateway**", so Green API is a legitimate
swap-in, not a shortcut around the requirement.

The repo was recently migrated from Thymeleaf to a React + Vite frontend
(`frontend/`), and every service's old web pages were deleted. None of that
touched `alert-service`'s actual notification logic (the `notify/` package
below) — only its web layer, which no longer exists. Make sure you're
working from current `main`, not an older Thymeleaf-era checkout, before
starting.

## Before you start: don't lose your existing Green API work

**Do not just `git pull` or delete/re-clone the project.** If you have
uncommitted local changes (your working Green API code, possibly sitting on
top of the old Thymeleaf `alert-service` pages), pulling `main` directly can
either refuse to apply (conflicts with the incoming file deletions) or, if
you force it, silently wipe out your WhatsApp work. Do this instead, in
order, from inside `dpdms/`:

```
git status
```
See what's actually changed on your machine first.

```
git checkout -b my-whatsapp-work
git add -A
git commit -m "wip: green api whatsapp"
```
This safely snapshots your current work on its own branch — nothing on it
can be lost no matter what happens to `main` next.

```
git checkout main
git pull
```
Now `main` catches up cleanly: the old Thymeleaf files are gone,
`frontend/` is present, and your WhatsApp work is safely parked on
`my-whatsapp-work`, untouched.

Only after that: come back to `my-whatsapp-work` (or just look at the diff
via `git diff main my-whatsapp-work`) to pull out the actual Green API
request logic, and follow the steps below to drop it into the current
`alert-service` structure.

## How alert-service dispatches alerts (read these files first)

- `alert-service/src/main/java/zw/ac/uz/dpdms/alert/notify/AlertNotifier.java`
  — the interface every channel implements: `channelName()`, `isEnabled()`,
  `recipients()`, `displayRecipient(String)` (for masking in logs),
  `send(recipient, subject, messageText)` returning a `DeliveryResult`.
- `alert-service/src/main/java/zw/ac/uz/dpdms/alert/notify/EmailNotifier.java`
  — the cleanest reference implementation to copy the shape of.
- `alert-service/src/main/java/zw/ac/uz/dpdms/alert/notify/WhatsAppCloudNotifier.java`
  — the existing (disabled) Meta implementation. Same interface, same error
  handling pattern (`RestClientResponseException` → `DeliveryResult(FAILED, ...)`),
  same phone-number masking in `displayRecipient`. **Do not delete this file**
  — leave it as an alternative, just keep it disabled.
- `alert-service/src/main/java/zw/ac/uz/dpdms/alert/config/EmailProperties.java`
  and `WhatsAppProperties.java` — `@ConfigurationProperties` records bound
  from `application.yml`. Follow this exact pattern for the new config.
- `alert-service/src/main/java/zw/ac/uz/dpdms/alert/service/AlertService.java`
  — orchestrates dispatch. **You should not need to change this file at all**
  — Spring auto-discovers every `AlertNotifier` bean via constructor
  injection (`List<AlertNotifier> notifiers`), so a new `@Component`
  implementing the interface is picked up automatically.
- `alert-service/src/main/resources/application.yml` — see the existing
  `dpdms.whatsapp:` block for the config-binding pattern to mirror.

## What to build

1. **`GreenApiProperties.java`** in `alert-service/.../config/`, mirroring
   `EmailProperties.java`:
   ```java
   @ConfigurationProperties(prefix = "dpdms.greenapi")
   public record GreenApiProperties(
           boolean enabled,
           String instanceId,
           String apiTokenInstance,
           List<String> recipients
   ) {
       public List<String> recipientsOrEmpty() {
           return recipients == null ? List.of()
                   : recipients.stream().map(String::trim).filter(r -> !r.isEmpty()).toList();
       }
   }
   ```

2. **`GreenApiNotifier.java`** in `alert-service/.../notify/`, implementing
   `AlertNotifier`:
   - `channelName()` → `"WHATSAPP"` (same channel name as the Meta one is
     fine — only one of the two should ever be `enabled: true` at a time, so
     they won't collide in the delivery log).
   - `isEnabled()` → `props.enabled()`.
   - `recipients()` → `props.recipientsOrEmpty()`.
   - `displayRecipient(String number)` → mask like `WhatsAppCloudNotifier`
     does (last 4 digits only, never log a full phone number).
   - `send(recipient, subject, messageText)` → call Green API:
     ```
     POST https://api.green-api.com/waInstance{instanceId}/sendMessage/{apiTokenInstance}
     Content-Type: application/json
     Body: {"chatId": "<recipient>@c.us", "message": "<messageText>"}
     ```
     Use Spring's `RestClient` exactly like `WhatsAppCloudNotifier` does.
     On success return `DeliveryResult(DeliveryStatus.SENT, "Accepted by Green API")`.
     On a `RestClientResponseException`, log the response body (it explains
     the problem — wrong instance state, invalid number, etc.) and return
     `DeliveryResult(DeliveryStatus.FAILED, <truncated response body>)`.
     Never let `send()` throw — same rule as every other notifier, so one
     bad number can't stop RabbitMQ from acking the message or block other
     channels.
   - Recipient format: Green API expects the number with country code, no
     `+`, no spaces, e.g. `263771234567` → `chatId` becomes
     `263771234567@c.us`. Confirm this matches what Danai already tested.

3. **Config wiring** — add to `alert-service/src/main/resources/application.yml`,
   right after the existing `dpdms.whatsapp:` block:
   ```yaml
   greenapi:
     enabled: ${GREENAPI_ENABLED:false}
     instance-id: ${GREENAPI_INSTANCE_ID:}
     api-token-instance: ${GREENAPI_API_TOKEN:}
     # Comma-separated, international format, no + or spaces: 2637XXXXXXXX
     recipients: ${GREENAPI_RECIPIENTS:}
   ```

4. **Document the env vars** in `dpdms/.env.example` (never put real values
   there), following the existing WhatsApp block's style:
   ```
   # alert-service - WhatsApp via Green API (green-api.com) - unofficial
   # gateway, pairs to a real WhatsApp number via QR code, no Meta business
   # verification needed. Only one of WHATSAPP_ENABLED / GREENAPI_ENABLED
   # should be true at a time.
   GREENAPI_ENABLED=false
   GREENAPI_INSTANCE_ID=replace_with_your_green_api_instance_id
   GREENAPI_API_TOKEN=replace_with_your_green_api_token
   GREENAPI_RECIPIENTS=2637XXXXXXXX,2637YYYYYYYY
   ```

5. **Put your real values in your own local `.env`** (gitignored, never
   committed) — `GREENAPI_ENABLED=true`, your real `instanceId`/token/
   recipients. Never paste these into a chat, a commit, or this file.

6. **Test end to end:**
   - Bring up the backend (`docker-compose up -d`, then
     `discovery-service` → `gateway` → `auth-service` → your hazard
     service (`zoonotic-disease-service`) → `alert-service`).
   - Log in as `zoonotic.recorder` (password `Password123!`) via the
     frontend (`cd frontend && npm install && npm run dev`, then
     `http://localhost:5173`), submit an incident classified as `OUTBREAK`
     (always alerts) or `CLUSTER` with ≥3 confirmed animal cases.
   - Log in as `zoonotic.supervisor`, approve it.
   - Confirm the WhatsApp message actually arrives on the test phone.
   - Log in as `national.viewer` or `provincial.admin`, open the Alerts
     page, confirm the delivery shows `WHATSAPP - SENT` with a masked
     recipient number.

7. **Compile check:** `mvn -q -DskipTests compile` from the `dpdms/` root
   should still build the whole reactor cleanly.

8. **Merge back into `main` and push directly.** This team doesn't use
   feature-branch pull requests — everyone commits straight to `main` —
   so the `my-whatsapp-work` branch from step 2 above was only ever a
   local safety net, not something that gets pushed anywhere on its own:
   ```
   git checkout main
   git pull
   git merge my-whatsapp-work
   mvn -q -DskipTests compile
   git add -A
   git commit -m "Add Green API WhatsApp notifier"
   git push
   ```
   If `git merge` reports conflicts, resolve them (`git status` shows
   which files), `git add` the resolved files, then `git commit` to
   finish the merge before pushing. Once pushed, everyone else
   (including whoever's machine ran the earlier email setup) needs to
   `git pull` before their next session to get this.

## Do not

- Do not touch `AlertService.java`'s dispatch loop — it already handles any
  number of `AlertNotifier` beans generically.
- Do not delete or modify `WhatsAppCloudNotifier.java` — it stays as a
  disabled alternative.
- Do not hardcode the Green API instance ID or token anywhere in Java code
  or `application.yml` — env vars only, exactly like every other credential
  in this project.
- Do not commit your local `.env`.
