@motor @happy-path
Feature: Operaciones aritmeticas basicas de la calculadora
  Como usuario de la calculadora
  Quiero realizar operaciones aritmeticas
  Para obtener resultados correctos y confiables

  Background:
    * def Driver = Java.type(pkg + '.CalculatorDriver')
    * def calc = new Driver()

  @smoke
  Scenario: La calculadora ofrece todas las operaciones del alcance
    * match calc.binarySymbols() contains only ['+', '-', '*', '/', '^']
    * match calc.unarySymbols() == ['sqrt']

  Scenario Outline: Suma de <a> + <b>
    * calc.calculate('+', <a>, <b>)
    * match calc.errorType() == null
    * match calc.result() == '<esperado>'

    Examples:
      | a       | b       | esperado |
      | 2       | 3       | 5        |
      | 0       | 0       | 0        |
      | -4      | 9       | 5        |
      | -7      | -3      | -10      |
      | 0.1     | 0.2     | 0.3      |
      | 1000000 | 2500000 | 3500000  |

  Scenario Outline: Resta de <a> - <b>
    * calc.calculate('-', <a>, <b>)
    * match calc.errorType() == null
    * match calc.result() == '<esperado>'

    Examples:
      | a   | b   | esperado |
      | 10  | 4   | 6        |
      | 3   | 8   | -5       |
      | 0   | 0   | 0        |
      | 2.5 | 0.5 | 2        |

  Scenario Outline: Multiplicacion de <a> * <b>
    * calc.calculate('*', <a>, <b>)
    * match calc.errorType() == null
    * match calc.result() == '<esperado>'

    Examples:
      | a   | b  | esperado |
      | 6   | 7  | 42       |
      | -3  | 5  | -15      |
      | 0   | 99 | 0        |
      | 1.5 | 4  | 6        |
      | -2  | -2 | 4        |

  Scenario Outline: Division de <a> / <b>
    * calc.calculate('/', <a>, <b>)
    * match calc.errorType() == null
    * match calc.result() == '<esperado>'

    Examples:
      | a  | b | esperado     |
      | 10 | 4 | 2.5          |
      | 9  | 3 | 3            |
      | -8 | 2 | -4           |
      | 1  | 3 | 0.3333333333 |
      | 0  | 5 | 0            |

  Scenario Outline: Potencia de <a> ^ <b>
    * calc.calculate('^', <a>, <b>)
    * match calc.errorType() == null
    * match calc.result() == '<esperado>'

    Examples:
      | a  | b   | esperado |
      | 2  | 10  | 1024     |
      | 9  | 0.5 | 3        |
      | 5  | 0   | 1        |
      | 2  | -2  | 0.25     |
      | -2 | 3   | -8       |

  Scenario Outline: Raiz cuadrada de <a>
    * calc.calculateUnary('sqrt', <a>)
    * match calc.errorType() == null
    * match calc.result() == '<esperado>'

    Examples:
      | a    | esperado     |
      | 9    | 3            |
      | 2    | 1.4142135624 |
      | 0    | 0            |
      | 144  | 12           |
      | 0.25 | 0.5          |

  Scenario: Varias operaciones seguidas conservan resultados independientes
    * calc.calculate('+', 2, 3)
    * match calc.result() == '5'
    * calc.calculate('*', 4, 5)
    * match calc.result() == '20'
    * calc.calculateUnary('sqrt', 16)
    * match calc.result() == '4'
