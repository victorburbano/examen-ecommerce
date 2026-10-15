# Gobernanza de IA

> **Esqueleto (spec 000).** Se llena con hechos reales al cerrar cada spec (skill `ia-log`). Las secciones marcadas *pendiente* no contienen entradas inventadas a propósito.

Asistente usado: Claude Code (Anthropic). Método: Spec-Driven Development, un commit por spec.

## 1. Skills / prompts automatizados
| Skill | Qué automatiza | Dónde |
|---|---|---|
| `spec-driven-development` | Flujo spec → plan → tasks → implementar → verificar → commit | `.agents/skills/spec-driven-development` |
| `discount-engine` | Reglas, orden, redondeo y tope del motor; evita errores típicos (sumar % en vez de cascada) | `.agents/skills/discount-engine` |
| `discount-test-matrix` | Genera la matriz de tests parametrizados del motor con los casos de borde del enunciado; los esperados se calculan a mano | `.agents/skills/discount-test-matrix` |
| Prompts por spec | Un `prompt.md` por spec con alcance, criterios y entrega | `specs/NNN-*/prompt.md` |

Detalle de `discount-test-matrix`: *pendiente* (ejemplo de uso real al cerrar la spec 003).

## 2. Sub-agentes
| Agente | Rol y reglas | Archivo |
|---|---|---|
| `code-auditor` | Solo lectura. Revisa el diff: SOLID, capas, `any`, dinero con `double`, constantes mágicas, N+1, seguridad. Marca **[ia-log]** lo que debe registrarse | `.claude/agents/code-auditor.md` |
| `coverage-guard` | Corre las suites, exige ≥ 80 % LINE/BRANCH y revisa los casos de borde. No baja umbrales ni excluye archivos | `.claude/agents/coverage-guard.md` |

Ejemplos de hallazgos reales de cada agente: *pendiente*.

## 3. Bitácora de co-creación
### Prompts clave
| # | Fecha | Spec | Objetivo | Prompt | Resultado | Verificación humana |
|---|---|---|---|---|---|---|
| 1 | 2026-10-15 | 000 | Entender el enunciado, proponer arquitectura y specs | Conversación de entrevista (Fase 0) | Decisiones DEC-001..011 | El humano corrigió: auth completo, cupones en BD, cap configurable |

### Porcentaje de código asistido
*Pendiente (spec 006).* Método: commits con trailer `Co-Authored-By` y `git diff --shortstat` por spec; se indicará la lógica crítica escrita o reescrita por el humano.

### Correcciones a sugerencias de la IA (mínimo 2, reales)
*Pendiente.* Se registran en cuanto ocurran, con evidencia (commit/test). Formato en el skill `ia-log`.
