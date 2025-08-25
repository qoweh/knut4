# Test Cases (Summary)

## Authentication
- Signup success (new username)
- Signup duplicate username -> 400
- Login success returns JWT
- Login invalid password -> 401

## Recommendation
- Basic recommend returns menu list (LLM disabled) with empty places when no API key
- Recommend with moods & budget persists history
- Retry latest history reproduces new recommendation
- Share existing history returns token
- Get shared recommendation by token returns stored snapshot

## Preferences
- Upsert new preference (allergies/dislikes arrays)
- Fetch existing preference
- Recommendation filters items containing allergy/dislike substrings

## LLM
- LLM enabled prompt build returns structured JSON
- Timeout triggers fallback stub path
- Malformed JSON from LLM gracefully skipped

## Map Provider
- NaverMapProvider gracefully handles missing credentials (empty list)
- Distance calculation within expected tolerance (Haversine)

## Error Paths
- Share with other user's history -> 404
- Retry without any history -> 404
- Shared token not found -> 404
