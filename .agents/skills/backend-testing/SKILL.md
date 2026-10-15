---
name: backend-testing
description: Estrategia de tests del backend con JUnit 5, Mockito, AssertJ y JaCoCo (cobertura >= 80 %). Úsalo al escribir o revisar tests Java.
---

# backend-testing

## Pirámide
- **Unit** (mayoría): motor de descuentos, servicios, reglas. JUnit 5 + Mockito (`@ExtendWith(MockitoExtension.class)`) + AssertJ. Sin Spring ni BD.
- **Slice web**: `@WebMvcTest` + spring-security-test: validación, mapeo de errores a `ProblemDetail`, 401/403.
- **Integración**: `@SpringBootTest`/`@DataJpaTest` sobre **H2 en memoria** con las migraciones Flyway reales (esquema y seed): repositorios, `@Version`, concurrencia de stock, flujo checkout completo.

## Reglas
- Nombre `method_condition_expectedResult`; Arrange-Act-Assert; un concepto por test.
- Mockea solo fronteras (repositorios, `Clock`); no mockees el objeto bajo prueba.
- `Clock` fijo en tests de cupones (vigencia). `BigDecimal` con `isEqualByComparingTo`.
- Tests del motor: parametrizados desde la matriz del skill `discount-test-matrix`.
- Concurrencia: `ExecutorService` + `CountDownLatch`, N hilos comprando la última unidad; aserción: exactamente 1 orden y stock 0, con timeout.
- JaCoCo `check` LINE y BRANCH ≥ 0.80 (BUNDLE) en `verify`; excluye solo `*Application` y config declarativa. Prioriza **ramas** del motor y de stock.
- Pregúntate: ¿el test falla si cambias `>` por `>=` en el umbral de $100?

## Comando
`cd apps/backend && ./mvnw verify` → reporte en `target/site/jacoco/index.html`.

## Anti-patrones
Tests sin asserts, `@SpringBootTest` para todo, `Thread.sleep`, esperados calculados con el código bajo prueba, tests acoplados por orden.
