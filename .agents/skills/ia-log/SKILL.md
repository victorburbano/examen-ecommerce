---
name: ia-log
description: Mantiene docs/ia.md (skills, sub-agentes, bitácora de co-creación y correcciones a la IA). Úsalo al cerrar cada spec o cuando corrijas una sugerencia de la IA.
---

# ia-log

La prueba exige `docs/ia.md` con: (1) skills/prompts propios, (2) sub-agentes con rol y reglas, (3) bitácora de co-creación con **% de código asistido** y **mínimo dos correcciones concretas** a sugerencias de la IA. **Solo hechos reales**: nunca inventes entradas ni correcciones.

## Cuándo registrar
- Al cerrar cada spec (prompt clave usado y resultado).
- En cuanto rechaces o cambies una sugerencia de la IA, aunque el spec siga abierto.

## Formato
### Bitácora de prompts
`| # | Fecha | Spec | Objetivo | Prompt (enlace a specs/NNN/prompt.md) | Resultado | Verificación humana |`

### Correcciones a la IA
```
#### N. <título corto>
- **Sugerencia original**: …
- **Problema técnico**: violación SOLID (cuál) / ineficiencia (qué) / tipado / test mal planteado
- **Corrección aplicada**: …
- **Evidencia**: commit / test / archivo
```

### Porcentaje de código asistido
Calcúlalo de forma reproducible, no a ojo, y documenta el método:
`git log --format='%H %(trailers:key=Co-Authored-By,valueonly)' ` para commits asistidos y `git diff --shortstat` por spec. Indica además qué lógica crítica escribió o reescribió el humano.

## Checklist
- [ ] Prompt clave y fecha registrados. [ ] Cada corrección con evidencia. [ ] Sin entradas inventadas.
