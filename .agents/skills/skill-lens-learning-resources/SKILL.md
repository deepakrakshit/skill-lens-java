---
name: skill-lens-learning-resources
description: >-
  Governs learning resource discovery, YouTube Data API integration, curriculum ranking,
  caching, quota management, and graceful offline fallback.
  Use when fetching technical tutorials, discovering YouTube playlists, ranking documentation, or recommending learning materials.
---

# SkillLens Learning Resource Discovery

This skill defines the technical workflows for identifying, ranking, caching, and recommending high-quality learning resources (official documentation, books, and YouTube playlists) tailored to verified student gaps.

---

## 1. Resource Categorization

The resource catalog categorizes materials across four tiers:
1. **Official Documentation & Standards:** Primary source of truth (e.g. docs.oracle.com, spring.io, openjfx.io, MDN).
2. **Authoritative Playlists & Video Series:** Comprehensive multi-part YouTube playlists covering end-to-end architectures.
3. **Reference Textbooks & Books:** Foundational books for deep conceptual understanding.
4. **Hands-On Tutorials & Code Walkthroughs:** Step-by-step implementation guides.

---

## 2. YouTube Data API Integration & Quota Discipline

When an optional `YOUTUBE_API_KEY` is provided:
- **Search Endpoints:**
  - `GET https://www.googleapis.com/youtube/v3/search` with parameters `type=playlist,video`, `part=snippet`, `maxResults=5`, `relevanceLanguage=en`.
- **Quota Conservation Strategy:**
  - YouTube Data API v3 enforces a default daily quota of 10,000 units (a search call consumes 100 units).
  - Cache all YouTube query results in local SQLite (`youtube_resource_cache`) with a 14-day expiration window.
  - Normalize query strings (e.g. `youtube:skill:docker:beginner`) to prevent duplicate outbound calls.

---

## 3. Graceful Fallback (Missing or Unconfigured Key)

The presence of a YouTube API key is **strictly optional**:
- If `YOUTUBE_API_KEY` is absent or unconfigured:
  - The application must continue working without errors.
  - Display informative UI placeholder:
    > *"YouTube recommendations are unavailable until a YouTube API key is configured in Settings. Recommended documentation and curated reading resources are shown below."*
  - The career roadmap and skill gap report remain 100% operational.

---

## 4. Resource Quality Ranking Algorithm

Do NOT select random videos merely because a title contains the skill keyword. Score resources using an explainable index:

$$\text{QualityScore} = w_1 \cdot \text{TopicRelevance} + w_2 \cdot \text{Completeness} + w_3 \cdot \text{Reputation} + w_4 \cdot \text{LevelFit}$$

- **Topic Relevance (0.40):** Exact semantic alignment with the specific gap concept (e.g. *"Spring Boot Transaction Management"* rather than generic *"Java tutorial"*).
- **Completeness (0.25):** Multi-part structured playlists score higher than isolated 5-minute clips.
- **Channel Reputation (0.20):** Established educational publishers and conference channels (e.g. Spring, GOTO Conferences, freeCodeCamp).
- **Level Fit (0.15):** Match resource depth (beginner/intermediate/advanced) to the candidate's current verified proficiency level.
- **Explanatory Justification:** Every recommendation must explain why it was chosen (e.g. *"Recommended because it covers comprehensive database transaction isolation levels required for the Backend Developer role."*).
