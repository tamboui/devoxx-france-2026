# devoxx-france-2026

TamboUI / Devoxx France 2026 presentation deck.

## Fastest way to render locally

```bash
./jbang adoc2reveal.java
```

That generates `index.html` next to `index.adoc`.

## Live watch + serve mode with a tui

```bash
./jbang adoc2reveal.java --serve
```

That will:
- render the deck
- watch the source files
- serve the output with Java's built-in HTTP server
- print the local URL in the terminal

Useful hotkeys in TUI mode:
- `r` re-render now
- `w` toggle watch
- `s` toggle serve
- `o` open browser
- `h` toggle help
- `c` clear log
- `q` quit

## GitHub Pages

A workflow is available at `.github/workflows/pages.yml`.
It renders the deck on push to `main` and publishes the generated site to GitHub Pages.
