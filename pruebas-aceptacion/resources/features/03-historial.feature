@motor @historial
Feature: Historial de operaciones
  Como usuario de la calculadora
  Quiero ver las operaciones exitosas que realice
  Para revisar y verificar mis calculos

  Background:
    * def Driver = Java.type(pkg + '.CalculatorDriver')
    * def calc = new Driver()

  @smoke
  Scenario: El historial comienza vacio
    * match calc.historySize() == 0
    * match calc.historyLines() == []

  Scenario: Cada operacion exitosa queda registrada en orden
    * calc.calculate('+', 2, 3)
    * calc.calculateUnary('sqrt', 9)
    * calc.calculate('*', 4, 2.5)
    * match calc.historySize() == 3
    * match calc.historyLines() == ['2.0 + 3.0 = 5.0', 'sqrt(9.0) = 3.0', '4.0 * 2.5 = 10.0']

  Scenario: Limpiar el historial lo deja vacio
    * calc.calculate('+', 1, 1)
    * calc.calculate('+', 2, 2)
    * match calc.historySize() == 2
    * calc.clearHistory()
    * match calc.historySize() == 0
    * match calc.historyLines() == []

  Scenario: El historial conserva como maximo 100 operaciones (las mas antiguas se descartan)
    * def fill = function(){ for (var i = 0; i < 105; i++) { calc.calculate('+', i, 1); } }
    * eval fill()
    * match calc.historySize() == 100
    * def lines = calc.historyLines()
    * match lines[0] == '5.0 + 1.0 = 6.0'
    * match lines[99] == '104.0 + 1.0 = 105.0'
