@ui @happy-path
Feature: Uso de la calculadora grafica con el teclado en pantalla
  Como usuario de la interfaz grafica
  Quiero presionar teclas como en una calculadora real
  Para ver en pantalla el numero que escribo y el resultado de mis operaciones

  Background:
    * def Driver = Java.type(pkg + '.PresenterDriver')
    * def ui = new Driver()

  @smoke
  Scenario: La pantalla inicia en cero
    * match ui.display() == '0'
    * match ui.hint() == ''
    * match ui.isError() == false

  Scenario Outline: Secuencia de teclas "<teclas>" deja <pantalla> en pantalla
    * ui.press('<teclas>')
    * match ui.display() == '<pantalla>'
    * match ui.isError() == false

    Examples:
      | teclas        | pantalla           |
      | 2 + 3 =       | 5                  |
      | 12 + 30 =     | 42                 |
      | 9 - 4 =       | 5                  |
      | 4 - 9 =       | -5                 |
      | 6 * 7 =       | 42                 |
      | 8 / 4 =       | 2                  |
      | 1 / 3 =       | 0.3333333333333333 |
      | 2 ^ 10 =      | 1024               |
      | 9 sqrt        | 3                  |
      | 1.5 + 2.25 =  | 3.75               |
      | 2 + 3 + 4 =   | 9                  |
      | 5 + 3 = + 2 = | 10                 |
      | 5 + * 3 =     | 15                 |

  Scenario: Al completar una operacion con = no queda ninguna operacion pendiente
    * ui.press('2 + 3 =')
    * match ui.hint() == ''

  Scenario Outline: El indicador de operacion pendiente muestra "<pista>" tras "<teclas>"
    * ui.press('<teclas>')
    * match ui.display() == '<pantalla>'
    * match ui.hint() == '<pista>'

    Examples:
      | teclas  | pantalla | pista |
      | 7 +     | 7        | 7 +   |
      | 2 + 3 * | 5        | 5 ×   |
      | 8 /     | 8        | 8 ÷   |
      | 4 ^     | 4        | 4 ^   |

  Scenario Outline: Edicion de numeros con "<teclas>" muestra <pantalla>
    * ui.press('<teclas>')
    * match ui.display() == '<pantalla>'

    Examples:
      | teclas   | pantalla |
      | 123 back | 12       |
      | 5 back   | 0        |
      | back     | 0        |
      | .5       | 0.5      |
      | 1.5.5    | 1.55     |
      | 007      | 7        |
      | 3 + C    | 0        |
      | =        | 0        |

  Scenario: Las operaciones exitosas aparecen en el historial en vivo
    * ui.press('2 + 3 =')
    * ui.press('9 sqrt')
    * match ui.historySize() == 2
    * match ui.historyLines() == ['  2.0 + 3.0 = 5.0', '  sqrt(9.0) = 3.0']
