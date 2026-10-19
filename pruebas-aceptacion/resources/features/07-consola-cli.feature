@cli
Feature: Calculadora en consola (modo --cli)
  Como usuario de la terminal
  Quiero escribir comandos como "+ 2 3" y leer la respuesta
  Para usar la calculadora sin interfaz grafica

  Background:
    * def Driver = Java.type(pkg + '.CliDriver')
    * def cli = new Driver()

  @smoke @happy-path
  Scenario: Al iniciar muestra el banner y la ayuda
    * cli.run('q')
    * match cli.banner() contains 'Calculadora CLI'
    * match cli.banner() contains 'Operadores disponibles'
    * match cli.banner() contains 'Comandos: h=historial, ?=ayuda, q=salir'

  @happy-path
  Scenario Outline: El comando "<comando>" responde "<respuesta>"
    * def out = cli.run('<comando>;q')
    * match out[0] == '<respuesta>'
    * match out[1] == 'Hasta luego.'

    Examples:
      | comando   | respuesta |
      | + 2 3     | = 5.0     |
      | - 10 4    | = 6.0     |
      | * 6 7     | = 42.0    |
      | / 10 4    | = 2.5     |
      | ^ 2 10    | = 1024.0  |
      | sqrt 9    | = 3.0     |
      | + -2 -3   | = -5.0    |
      | * 1.5 4   | = 6.0     |

  @exceptional-path
  Scenario Outline: El comando invalido "<comando>" muestra un error y la consola sigue viva
    * def out = cli.run('<comando>;+ 1 1;q')
    * match out[0] contains '<error>'
    * match out[0] contains '[ERROR]'
    * match out[1] == '= 2.0'
    * match out[2] == 'Hasta luego.'

    Examples:
      | comando   | error                    |
      | / 5 0    | dividir entre cero        |
      | sqrt -4  | raiz cuadrada real        |
      | % 2 3    | Operador no soportado     |
      | SQRT 9   | Operador no soportado     |
      | + a 3    | Numero invalido           |
      | + 1      | Operador no soportado     |
      | + 1 2 3  | Formato invalido          |
      | hola     | Formato invalido          |

  @historial
  Scenario: El comando h muestra el historial vacio
    * def out = cli.run('h;q')
    * match out[0] == '(historial vacio)'

  @historial
  Scenario Outline: El comando <alias> muestra las operaciones realizadas
    * def out = cli.run('+ 2 3;sqrt 9;<alias>;q')
    * match out contains '  2.0 + 3.0 = 5.0'
    * match out contains '  sqrt(9.0) = 3.0'

    Examples:
      | alias     |
      | h         |
      | historial |
      | H         |

  @historial @exceptional-path
  Scenario: Los errores no aparecen en el historial de la consola
    * def out = cli.run('/ 1 0;h;q')
    * match out[0] contains '[ERROR]'
    * match out[1] == '(historial vacio)'

  Scenario Outline: El comando <alias> vuelve a mostrar la ayuda
    * def out = cli.run('<alias>;q')
    * match out contains 'Operadores disponibles:'

    Examples:
      | alias |
      | ?     |
      | ayuda |

  Scenario Outline: El comando <alias> cierra la consola
    * def out = cli.run('<alias>')
    * match out == ['Hasta luego.']

    Examples:
      | alias |
      | q     |
      | salir |
      | Q     |
      | SALIR |

  Scenario: Si la entrada termina sin "q" la consola se cierra sola
    * def out = cli.run('+ 1 1')
    * match out == ['= 2.0', 'Hasta luego.']

  Scenario: Las lineas vacias se ignoran
    * def out = cli.run(';;+ 1 1;;q')
    * match out == ['= 2.0', 'Hasta luego.']
