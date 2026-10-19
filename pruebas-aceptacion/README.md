# Pruebas de aceptación (Karate)

Pruebas de aceptación de la calculadora escritas en **Gherkin** y ejecutadas con **Karate 1.5.1** sobre JUnit 5.
La aplicación no expone una API HTTP, así que Karate invoca tres *drivers* Java que representan sus entradas reales:

| Driver | Entrada real | Features |
|---|---|---|
| `CalculatorDriver` | `Calculator` (motor) | 01, 02, 03, 04 |
| `PresenterDriver`  | `CalculatorPresenter` (lógica de la UI, sin ventana) | 05, 06 |
| `CliDriver`        | `Main --cli` (consola real) | 07 |

## Ejecutar

```bash
./gradlew acceptanceTest                 # solo aceptación
./gradlew check                          # unitarias + aceptación
open build/karate-reports/karate-summary.html
```

Ejecutar solo un subconjunto por etiqueta (`@smoke`, `@happy-path`, `@exceptional-path`, `@motor`, `@ui`, `@cli`, `@historial`, `@ocp`):

```bash
./gradlew acceptanceTest -Dkarate.options="--tags @smoke"
```

## Estructura

```
pruebas-aceptacion/
├── java/com/udea/calidad/calculadora/acceptance/   # drivers + runner (AcceptanceTest)
└── resources/
    ├── karate-config.js
    └── features/                                    # 7 features · 44 escenarios · 130 ejecuciones
```

## Notas

- El runner usa `.parallel(1)`: `CliDriver` redirige `System.in/out`, recursos globales.
- Los resultados numéricos se comparan como texto (hasta 10 decimales) para evitar ambigüedades de punto flotante.
- Para agregar un caso, agrega una fila a la tabla `Examples` del feature correspondiente.
