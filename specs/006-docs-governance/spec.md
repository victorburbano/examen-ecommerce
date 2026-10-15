# Spec 006 — Documentación y gobernanza de IA

## Objetivo
Cerrar los entregables evaluables: `docs/arquitectura.md`, `docs/ia.md`, README final y guion de la demo de 7 minutos, todo con hechos reales del repo.

## Criterios de aceptación
- `docs/arquitectura.md` responde: por qué ese stack y estructura; trade-offs asumidos (con enlace a `DEC-*`); cómo se aísla el motor de persistencia y controllers; **≥ 2 patrones descritos y ubicados en el código** (Strategy, Pipeline, Factory, Port/Adapter, Observer). Diagramas Mermaid actualizados.
- `docs/ia.md` cumple el enunciado: (1) ≥ 1 skill/prompt propio descrito con ejemplo real (`discount-test-matrix`), (2) ≥ 1 sub-agente con rol y reglas y un hallazgo real, (3) bitácora con **% de código asistido calculado y su método** y **≥ 2 correcciones reales** con evidencia (commit o test). **Si no hubo correcciones reales, se dice y se revisa el código con `code-auditor` hasta encontrarlas; nunca se inventan.**
- README: requisitos, instalación, variables de entorno, usuarios seed, cupones seed, comandos de back y front (run, test, cobertura), cómo ver el reporte de JaCoCo, estructura del monorepo y enlaces a docs y specs.
- `docs/demo.md`: guion de 7 minutos (catálogo → carrito → quote con `WELCOME2026` → `MEGA40` con banner → stock insuficiente → compra y orden persistida → vista ADMIN) y bloque de preguntas probables del jurado con respuestas.
- Evidencia de cobertura ≥ 80 % de back y front incluida (comandos y cifras reales).
- Historial de commits: uno por spec, descriptivos.

## Fuera de alcance
Código de negocio nuevo (solo correcciones menores detectadas en la auditoría final).
