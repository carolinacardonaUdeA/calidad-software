# Calculadora de Calidad — Java + Gradle + JUnit 5

Proyecto de caso de estudio para **Ingeniería de Calidad de Software**: calculadora
completa con **interfaz gráfica atractiva (Swing, tema oscuro)** y consola, diseñada
con principios **SOLID** y verificada con **87 pruebas unitarias** (simples y
avanzadas) que aplican el **patrón AAA**, cubren **Happy Path** y **Exceptional
Path**, e incluyen **Performance Unit Testing** y verificación de cobertura con JaCoCo.

## Requisitos

- JDK 17 o superior (verificado con Java 21 LTS)
- No requiere Gradle instalado (incluye Gradle Wrapper)

## Cómo ejecutar cada cosa

| Qué | Comando | Resultado |
|---|---|---|
| **Abrir la calculadora gráfica** | `./gradlew run` | Ventana Swing tema oscuro (por defecto) |
| Usar la versión de consola | `./gradlew run --args="--cli"` | CLI: escribe `+ 2 3`, `sqrt 9`, `h`, `q` |
| Ejecutar todas las pruebas | `./gradlew test` | Consola con resultado por prueba |
| Reporte visual de pruebas | `./gradlew test` y luego abrir `build/reports/tests/test/index.html` | HTML navegable |
| Cobertura de código | `./gradlew jacocoTestReport` | `build/reports/jacoco/test/html/index.html` |
| Validar cobertura mínima 90% | `./gradlew jacocoTestCoverageVerification` | Falla si cobertura < 90% |
| Todo en un comando | `./gradlew test jacocoTestReport jacocoTestCoverageVerification` | Pruebas + cobertura + umbral |
| Regenerar documento DOCX | `python3 docs/generar_docx.py` | `docs/Patrones_y_Buenas_Practicas_QA_Calculadora.docx` |

> En Windows usa `gradlew.bat` en lugar de `./gradlew`.

## Estructura del proyecto

```
src/main/java/com/udea/calidad/calculadora/
├── Calculator.java                  # Orquestador (SRP, OCP, DIP)
├── Main.java                        # Entrada: GUI por defecto, consola con --cli
├── ui/
│   ├── CalculatorFrame.java         # Vista Swing (tema oscuro, MVP: sin lógica)
│   └── CalculatorPresenter.java     # Lógica de la UI, 100% testeable (MVP)
├── operation/
│   ├── Operation.java               # Interfaz base (ISP)
│   ├── BinaryOperation.java         # Contrato binario (+,-,*,/,^)
│   ├── UnaryOperation.java          # Contrato unario (sqrt)
│   ├── Addition.java … Power.java   # Implementaciones (SRP, LSP)
│   └── SquareRoot.java              # Raíz cuadrada (unaria)
├── exception/
│   ├── DivisionByZeroException.java       # Exceptional Path
│   ├── NegativeSquareRootException.java   # Exceptional Path
│   └── UnsupportedOperatorException.java  # Exceptional Path
└── history/
    ├── CalculationHistory.java      # Historial thread-safe FIFO (SRP)
    └── HistoryEntry.java            # Registro inmutable

src/test/java/com/udea/calidad/calculadora/
├── CalculatorHappyPathTest.java     # AAA explícito + ciclo de vida JUnit
├── CalculatorExceptionalPathTest.java # assertThrows, estado post-fallo
├── CalculatorParameterizedTest.java # @CsvSource, @MethodSource, @TestFactory
├── CalculatorPerformanceTest.java   # @Timeout, @RepeatedTest, p95, ops/s
├── CalculationHistoryTest.java      # SRP aislado, assertIterableEquals
└── CalculatorSolidOcpLspTest.java   # Evidencia ejecutable de OCP/LSP/DIP
docs/
├── generar_docx.py                  # Generador del documento DOCX (python-docx)
├── Patrones_y_Buenas_Practicas_QA_Calculadora.docx  # Documento para exposición
└── INFORME_CALIDAD.md               # Informe del ingeniero de calidad
```

## Conceptos aplicados → dónde verificarlos

| Concepto | Evidencia |
|---|---|
| Patrón AAA | Comentarios `// Arrange // Act // Assert` en `CalculatorHappyPathTest` |
| Happy Path | Suite HP-01..HP-07 + tablas parametrizadas (39 casos positivos) |
| Exceptional Path | Suite EX-01..EX-07 con excepciones de negocio propias |
| Performance Unit Test | `CalculatorPerformanceTest`: presupuestos @Timeout, warm-up JIT, throughput ~10M ops/s |
| SOLID | Arquitectura de paquetes + pruebas ejecutables OCP/LSP/DIP |

## Métricas de la última ejecución

- **87 pruebas**: 87 exitosas, 0 fallos, 0 omitidas (~2.3 s)
- **Cobertura JaCoCo**: instrucciones 93.3% · ramas 77.6% · líneas 91.7%
- **Rendimiento**: ~10.18M ops/s · media ~45 ms por lote de 500k operaciones

### Cómo usar la interfaz gráfica

`./gradlew run` abre una ventana con:

- **Tema oscuro** con botones de colores por función (números gris oscuro,
  operaciones naranja, `=` verde, funciones grises)
- **Pantalla grande** con pista de operación pendiente (`2 +`)
- **Historial en vivo** de todas las operaciones exitosas
- **Errores amigables** en rojo: división entre cero, raíz negativa, ∞/NaN
- Teclado numérico completo: dígitos, `.`, `⌫`, `C`, `+ − × ÷ ^ √ =`

La lógica de esta UI está en `CalculatorPresenter` (patrón MVP) y tiene su propia
suite de 22 pruebas unitarias — la vista Swing es solo un adaptador visual.

## Pruebas de aceptación y calidad estática

| Qué | Comando | Resultado |
|---|---|---|
| Pruebas de aceptación (Karate) | `./gradlew acceptanceTest` | `build/karate-reports/karate-summary.html` (ver `pruebas-aceptacion/README.md`) |
| Pruebas + aceptación + cobertura | `./gradlew clean test acceptanceTest jacocoTestReport` | Cobertura combinada en `build/reports/jacoco/test/html/index.html` |
| Análisis en SonarCloud | `./gradlew clean test acceptanceTest jacocoTestReport sonar` | Requiere `SONAR_TOKEN`, `SONAR_ORGANIZATION`, `SONAR_PROJECT_KEY` |
| Crear Quality Gate y Quality Profile | `./sonar/setup-sonarcloud.sh` | Ver `docs/Guia_Paso_a_Paso_Karate_SonarCloud.docx` |

CI: copia `sonar/sonarcloud.yml` a `.github/workflows/sonarcloud.yml`.
Documentos: `docs/Documento_Socializacion_Calidad_Calculadora.docx`, `docs/Guia_Paso_a_Paso_Karate_SonarCloud.docx` y `docs/Presentacion_Calidad_Calculadora.pptx`.
