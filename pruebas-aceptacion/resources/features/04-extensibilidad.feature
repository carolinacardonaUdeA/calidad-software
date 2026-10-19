@motor @ocp
Feature: Nuevas operaciones sin modificar el codigo existente (principio Abierto/Cerrado)
  Como equipo de desarrollo
  Queremos agregar operaciones nuevas registrandolas
  Para extender la calculadora sin riesgo de romper lo que ya funciona

  Background:
    * def Driver = Java.type(pkg + '.CalculatorDriver')
    * def calc = new Driver()

  Scenario: Antes de registrar el modulo, el operador % no existe
    * calc.calculate('%', 10, 3)
    * match calc.errorType() == 'UnsupportedOperatorException'

  Scenario Outline: Despues de registrar el modulo, <a> % <b> da <esperado>
    * calc.registerModulo()
    * calc.calculate('%', <a>, <b>)
    * match calc.errorType() == null
    * match calc.result() == '<esperado>'

    Examples:
      | a   | b | esperado |
      | 10  | 3 | 1        |
      | 7.5 | 2 | 1.5      |
      | 8   | 4 | 0        |

  Scenario: Registrar una operacion nueva no altera las existentes
    * calc.registerModulo()
    * match calc.binarySymbols() contains only ['+', '-', '*', '/', '^', '%']
    * calc.calculate('+', 2, 3)
    * match calc.result() == '5'
    * calc.calculate('/', 1, 0)
    * match calc.errorType() == 'DivisionByZeroException'
