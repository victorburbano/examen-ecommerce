# Plantilla: corregir un bug

```
Bug: {{síntoma}} · Pasos para reproducir: {{pasos}} · Esperado: {{esperado}} · Real: {{real}}
1) Escribe primero un test que falle y reproduzca el bug (skill backend-testing o frontend-testing-jest).
2) Encuentra la causa raíz (no parchees el síntoma) y corrígela con el cambio mínimo.
3) Verifica que el test pasa y que no baja la cobertura. Si el bug nace de una sugerencia previa de la IA, regístralo con ia-log.
Si el bug indica que la spec era ambigua, corrige la spec y anótalo en docs/decisiones.md.
```
