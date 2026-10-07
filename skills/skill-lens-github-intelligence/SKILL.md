---
name: skill-lens-github-intelligence
description: >-
  Governs GitHub REST API interaction, canonical username normalization, rate-limit resilience,
  public repository discovery, latest 30 commits extraction, evidence graph building, and local caching.
  Use when connecting student GitHub profiles, retrieving repository metadata, analyzing commits, or handling GitHub API boundaries.
---

# SkillLens GitHub Intelligence

This skill defines the technical standards, API contracts, resilience protocols, and evidence collection workflows for analyzing a student's public GitHub footprint.

---

## 1. Input Normalization & Canonical Identity

The student supplies either a bare username or a profile URL:
- `deepak-rakshit`
- `@deepak-rakshit`
- `https://github.com/deepak-rakshit`
- `https://github.com/deepak-rakshit/`
- `https://github.com/deepak-rakshit?tab=repositories`

### Normalization Protocol:
1. Strip leading and trailing whitespace.
2. Strip leading `@` symbols if present.
3. Parse URLs:
   - Must match domain `github.com` (ignore `www.`).
   - Extract the first path segment as the username.
   - Strip trailing slashes, query parameters, and fragments.
   - Reject unrelated domains, repository deep links (unless extracting owner), or invalid URI schemes.
4. Validate username characters: ASCII alphanumeric and single hyphens (cannot begin or end with a hyphen, max 39 characters).
5. Normalize to lowercase for local indexing while preserving canonical casing from the GitHub profile API.

---

## 2. GitHubClient Architecture & Resilience

All GitHub API communication must flow through a centralized, isolated `GitHubClient` component. Never scatter raw HTTP requests across application controllers or services.

### Operational Modes:
- **Unauthenticated Mode:** Used by default for public repositories (60 requests/hour IP limit).
- **Optional Authenticated Mode:** Uses user-configured `GITHUB_TOKEN` (5,000 requests/hour limit).
- **Credential Storage:** Tokens must NEVER be hardcoded. They are loaded from OS-backed secure storage or optional environment variables for development.

### HTTP Error & Rate-Limit Handling:
- **HTTP 403 / 429 Rate Limiting:**
  - Inspect `X-RateLimit-Remaining`, `X-RateLimit-Reset`, and `Retry-After` headers.
  - Calculate sleep duration until reset timestamp. If wait time exceeds 10 seconds in interactive UI flows, fail gracefully and prompt the user to configure a personal access token.
- **HTTP 404 Not Found:**
  - Distinguish between non-existent user and private/missing repository.
  - Present clear student-facing explanation without crashing.
- **HTTP 5xx Server Errors:**
  - Apply exponential backoff with jitter (initial backoff: 500ms, multiplier: 2.0, max attempts: 3).

---

## 3. Local Response Caching

To prevent redundant API queries and respect rate limits:
- Store all raw GitHub API JSON payloads in an embedded local cache (`github_cache` table).
- Cache key: `SHA256(canonical_url + params + auth_hash)`.
- Use HTTP `ETag` and `If-None-Match` headers for conditional requests. If GitHub returns `304 Not Modified`, reuse cached records without consuming rate limit quotas.
- Include a manual "Refresh GitHub Data" button in the UI allowing user-initiated cache invalidation.

---

## 4. Repository Inventory & Relevance Scoring

### Inventory Collection:
Retrieve all public repositories for the user via pagination (`per_page=100`):
- Metadata collected: `id`, `name`, `full_name`, `description`, `html_url`, `default_branch`, `language`, `fork`, `archived`, `size`, `stargazers_count`, `forks_count`, `created_at`, `updated_at`, `pushed_at`.
- Tag repository flags: `is_fork`, `is_archived`, `is_empty`, `is_inactive`.
- **Do not delete forks or archived repos silently;** preserve them with clear tags for transparency.

### Role-Based Relevance Scoring (0.0 to 1.0):
Deterministic relevance determines which projects qualify for deep commit analysis:
- **Language Alignment:** Primary and secondary language match against target role profile (e.g. TypeScript for Frontend, Java/Go/Python for Backend).
- **Topics & Descriptions:** Keyword matches on GitHub topics and repository description.
- **README & Tech Stack:** Extracted dependency files (e.g. `package.json`, `pom.xml`, `requirements.txt`).
- **Recency & Activity:** Commits pushed within the last 12 months score higher than stale repositories.
- **Originality:** Original repositories receive full weight; forks receive a discount unless accompanied by original commits.

---

## 5. Commit Evidence Budget (Latest 30 Commits)

For each relevant repository:
1. Fetch up to **30 latest commits** on the repository's default branch.
2. Metadata persisted per commit:
   - `sha`, `commit_message`, `author_name`, `author_email`, `author_date`, `committer_date`, `html_url`, `verified_status`.
3. Filter commits by student identity:
   - Match author email or username to the student's profile.
   - Ignore pure automated bot commits (e.g. dependabot, formatting bots).
4. **Selective Deep Inspection:**
   - Maintain an API request budget (max 5 deep commit calls per high-relevance repository).
   - Retrieve file patches (`files_changed`, `additions`, `deletions`, file extensions) only for commits modifying core source files to establish concrete syntax and library usage evidence.

---

## 6. Prompt Injection Defense & Data Isolation

All GitHub-derived content (README, commit messages, issue titles, bio) is **untrusted external data**:
- Never interpolate raw GitHub text into system prompts as executable instructions.
- Enclose untrusted data within strict delimiter blocks (e.g. `<UNTRUSTED_GITHUB_DATA>...</UNTRUSTED_GITHUB_DATA>`) with explicit model directives to ignore embedded instructions.
- Sanitize HTML and script tags prior to rendering in JavaFX UI views.
