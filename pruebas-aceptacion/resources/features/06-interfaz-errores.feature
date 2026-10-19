@ui @exceptional-path
Feature: Errores visibles y recuperacion en la calculadora grafica
  Como usuario de la interfaz grafica
  Quiero ver un mensaje claro cuando hago algo invalido
  Para entender que paso y poder continuar sin cerrar la aplicacion

  Background:
    * def Driver = Java.type(pkg + '.PresenterDriver')
    * def ui = new Driver()

  @smoke
  Scenario Outline: La secuencia "<teclas>" muestra el error "<mensaje>"
    * ui.press('<teclas>')
    * match ui.isError() == true
    * match ui.display() contains '<mensaje>'
    * match ui.hint() == ''

    Examples:
      | teclas        | mensaje                |
      | 5 / 0 =       | División entre cero    |
      | 0 / 0 =       | División entre cero    |
      | 4 - 9 = sqrt  | Raíz de número negativo |
      | 10 ^ 400 =    | ∞                      |

  Scenario Outline: Tras un error, la tecla "<recuperacion>" permite continuar
    * ui.press('5 / 0 =')
    * match ui.isError() == true
    * ui.press('<recuperacion>')
    * match ui.isError() == false
    * match ui.display() == '<pantalla>'

    Examples:
      | recuperacion | pantalla |
      | 7            | 7        |
      | C            | 0        |
      | =            | 0        |
      | back         | 0        |
      | .            | 0.       |

  Scenario: Despues de recuperarse de un error se puede calcular normalmente
    * ui.press('5 / 0 =')
    * ui.press('C')
    * ui.press('6 * 7 =')
    * match ui.display() == '42'
    * match ui.isError() == false

  Scenario: Los errores no se registran en el historial
    * ui.press('5 / 0 =')
    * ui.press('C')
    * ui.press('4 - 9 = sqrt')
    * match ui.historySize() == 1
    * match ui.historyLines() == ['  4.0 - 9.0 = -5.0']
