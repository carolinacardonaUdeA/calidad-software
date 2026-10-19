@motor @exceptional-path
Feature: Manejo de errores en las operaciones
  Como usuario de la calculadora
  Quiero recibir un error claro cuando una operacion no es valida
  Para no obtener resultados enganosos ni que la aplicacion se caiga

  Background:
    * def Driver = Java.type(pkg + '.CalculatorDriver')
    * def calc = new Driver()

  @smoke
  Scenario: Dividir entre cero es rechazado con un error de negocio
    * calc.calculate('/', 10, 0)
    * match calc.errorType() == 'DivisionByZeroException'
    * match calc.errorMessage() contains 'dividir entre cero'
    * match calc.result() == null

  Scenario Outline: Cualquier dividendo entre cero falla (<dividendo> / 0)
    * calc.calculate('/', <dividendo>, 0)
    * match calc.errorType() == 'DivisionByZeroException'
    * match calc.result() == null

    Examples:
      | dividendo |
      | 1         |
      | -5        |
      | 0         |
      | 0.001     |
      | 1000000   |

  Scenario Outline: La raiz cuadrada de un negativo (<a>) es rechazada
    * calc.calculateUnary('sqrt', <a>)
    * match calc.errorType() == 'NegativeSquareRootException'
    * match calc.errorMessage() contains 'raiz cuadrada real'
    * match calc.result() == null

    Examples:
      | a      |
      | -1     |
      | -4     |
      | -0.001 |
      | -1e6   |

  Scenario Outline: Un operador no registrado (<simbolo>) es rechazado
    * calc.calculate('<simbolo>', 4, 2)
    * match calc.errorType() == 'UnsupportedOperatorException'
    * match calc.errorMessage() contains 'Operador no soportado'

    Examples:
      | simbolo |
      | %       |
      | x       |
      | mod     |
      | SQRT    |
      | sqrt    |

  Scenario: Un operador binario usado como unario es rechazado
    * calc.calculateUnary('+', 4)
    * match calc.errorType() == 'UnsupportedOperatorException'

  Scenario: Un error no deja basura en el historial
    * calc.calculate('/', 10, 0)
    * calc.calculateUnary('sqrt', -9)
    * calc.calculate('%', 1, 2)
    * match calc.historySize() == 0

  Scenario: Despues de un error la calculadora sigue funcionando
    * calc.calculate('/', 10, 0)
    * match calc.errorType() == 'DivisionByZeroException'
    * calc.calculate('+', 2, 3)
    * match calc.errorType() == null
    * match calc.result() == '5'

  Scenario: Una potencia que desborda devuelve infinito sin error
    * calc.calculate('^', 10, 400)
    * match calc.errorType() == null
    * match calc.result() == 'Infinity'

  Scenario: Una potencia sin resultado real devuelve NaN sin error
    * calc.calculate('^', -8, 0.5)
    * match calc.errorType() == null
    * match calc.result() == 'NaN'
