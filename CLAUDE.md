@AGENTS.md

## Notas solo para Claude Code
- Los skills se descubren desde `.claude/skills/` (symlinks hacia `.agents/skills/`). Si faltan: `scripts/sync-agent-links.ps1` (Windows, Modo desarrollador) o `.sh`.
- Los sub-agentes viven en `.claude/agents/` (`code-auditor`, `coverage-guard`). Invócalos al cerrar cada spec.
- Trabajo por spec: pide el plan corto y espera el OK antes de escribir código.
