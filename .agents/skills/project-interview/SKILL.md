---
name: project-interview
description: Entrevista corta y asesoría de arquitectura antes de escribir o cambiar una spec cuando el alcance, el modelo de datos o la seguridad no estén claros.
---

# project-interview

## Cuándo
Spec nueva con huecos, cambio de modelo de datos, decisión de seguridad. No para bugs ni cambios menores.

## Pasos
1. Lee `AGENTS.md`, `docs/requirements.md` y `docs/decisiones.md`; **infiere** lo ya decidido, no lo preguntes.
2. Haz **3–5 preguntas** en una sola lista, solo las que cambien decisiones. Cada una con tu default recomendado.
3. Asesora con **justificación y alternativas descartadas**. Si lo pedido es riesgoso o innecesariamente complejo, dilo.
4. Resume en 5–10 líneas y pide confirmación.
5. Registra la decisión en `docs/decisiones.md` (formato `DEC-NNN`) y actualiza la spec.

## Anti-patrones
Preguntas genéricas, más de 5 preguntas, decidir sin confirmar, no dejar rastro de la decisión.
