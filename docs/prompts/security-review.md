# Plantilla: revisión de seguridad

```
Revisa la seguridad de {{auth | checkout}} con el skill auth-jwt y docs/security.md.
Comprueba: secretos fuera del repo, tokens no logueados ni en storage, rotación y reutilización del refresh,
401 vs 403, roles y propiedad del recurso (IDOR), validación de input, el checkout recalcula y no confía en totales del cliente,
CORS al origen del front, ProblemDetail sin stack traces.
Entrega hallazgos con severidad, archivo:línea y corrección; añade tests para cada hallazgo corregido.
```
