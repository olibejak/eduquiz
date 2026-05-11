# Logging Guidelines

This service uses Lombok `@Slf4j` with simple key-value style messages to keep logs searchable.

## Levels

- `INFO`: business actions that change state (create, update, delete, favorite toggle)
- `DEBUG`: request boundaries, search branches, mapping/orchestration details
- `WARN`: expected but undesirable flows (access denied, invalid ownership, missing resources)
- `ERROR`: unhandled exceptions and server-side failures

## Key Naming And Order

Use stable lower-camel-case keys and keep ordering consistent:

- Actor first: `userId`, `authorId`
- Entity IDs next: `deckId`, `questionId`
- Query/paging context last: `keyword`, `authorName`, `page`, `size`

Examples:

```text
Create deck request userId={}, title={}
Update question request userId={}, deckId={}, questionId={}
Search decks by title keyword={}, page={}, size={}
Ownership verification failed userId={}, deckId={}, userRole={}
```

## Security And Privacy

- Never log raw tokens, passwords, or request bodies with sensitive content.
- JWT logs may include non-sensitive identifiers (for example `subject`, `userId`, `userRole`).
- Prefer IDs over personal data when possible.

## Avoid Duplicate Error Logs

`GlobalExceptionHandler` is the central place for exception logging. Avoid re-logging the same exception stack trace in lower layers unless you are adding unique context.

