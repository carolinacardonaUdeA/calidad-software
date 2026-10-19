package com.udea.calidad.calculadora;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import com.udea.calidad.calculadora.history.CalculationHistory;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * PRUEBAS AVANZADAS - Parametrizadas y dinamicas.
 *
 * <p>Permiten cubrir muchas combinaciones de entrada con un solo metodo de
 * prueba (evita duplicacion y facilita agregar casos limite). Cada fila se
 * reporta como una prueba independiente en el informe de JUnit.</p>
 */
@DisplayName("Calculadora - Pruebas parametrizadas avanzadas")
class CalculatorParameterizedTest {

    private static final double DELTA = 1e-9;

    private final Calculator calculator = new Calculator(new CalculationHistory());

    @ParameterizedTest(name = "{0} + {1} = {2}")
    @DisplayName("Tabla de verificacion de la SUMA")
    @CsvSource({
            "2.0,   3.0,   5.0",     // positivos
            "0.0,   0.0,   0.0",     // neutro aditivo
            "-4.0,  9.0,   5.0",     // negativo + positivo
            "-4.0, -6.0, -10.0",     // ambos negativos
            "2.5,   2.5,   5.0",     // decimales
            "1e10,  1e10,  2e10"     // valores grandes
    })
    void sumaConDiferentesCombinaciones(double a, double b, double expected) {
        assertEquals(expected, calculator.execute("+", a, b), DELTA);
    }

    @ParameterizedTest(name = "{0} - {1} = {2}")
    @DisplayName("Tabla de verificacion de la RESTA")
    @CsvSource({
            "10.0,  4.0,   6.0",
            "4.0,  10.0,  -6.0",
            "5.0,   5.0,   0.0",
            "0.0,   7.0,  -7.0",
            "-3.0, -3.0,   0.0"
    })
    void restaConDiferentesCombinaciones(double a, double b, double expected) {
        assertEquals(expected, calculator.execute("-", a, b), DELTA);
    }

    @ParameterizedTest(name = "{0} * {1} = {2}")
    @DisplayName("Tabla de verificacion de la MULTIPLICACION")
    @CsvSource({
            "3.0,  4.0,  12.0",
            "-2.0, 5.0, -10.0",
            "-3.0,-3.0,   9.0",
            "0.0, 99.0,   0.0",
            "1.5,  2.0,   3.0"
    })
    void multiplicacionConDiferentesCombinaciones(double a, double b, double expected) {
        assertEquals(expected, calculator.execute("*", a, b), DELTA);
    }

    /** Proveedor de casos para la division, incluyendo bordes numericos. */
    static Stream<org.junit.jupiter.params.provider.Arguments> divisionCases() {
        return Stream.of(
                org.junit.jupiter.params.provider.Arguments.of(20.0d, 4.0d, 5.0d),
                org.junit.jupiter.params.provider.Arguments.of(-10.0d, 4.0d, -2.5d),
                org.junit.jupiter.params.provider.Arguments.of(0.0d, 5.0d, 0.0d),
                org.junit.jupiter.params.provider.Arguments.of(1.0d, 3.0d, 1.0d / 3.0d),
                org.junit.jupiter.params.provider.Arguments.of(1e308d, 1e308d, 1.0d),
                org.junit.jupiter.params.provider.Arguments.of(7.0d, 2.0d, 3.5d)
        );
    }

    @ParameterizedTest(name = "{0} / {1} = {2}")
    @MethodSource("divisionCases")
    @DisplayName("Division con casos limite via MethodSource")
    void divisionCasosLimite(double dividendo, double divisor, double expected) {
        assertEquals(expected, calculator.execute("/", dividendo, divisor), DELTA);
    }

    @ParameterizedTest(name = "sqrt({0}) es un valor real no negativo")
    @ValueSource(doubles = {0.0d, 1.0d, 2.25d, 16.0d, 1e6d})
    @DisplayName("Raiz cuadrada de entradas validas retorna valor real no negativo")
    void raizCuadradaDeEntradasValidas(double operand) {
        double actual = calculator.execute("sqrt", operand);
        assertTrue(actual >= 0 && Double.isFinite(actual),
                "sqrt(" + operand + ") debe ser real y no negativo");
        assertEquals(Math.sqrt(operand), actual, DELTA);
    }

    /** PRUEBAS DINAMICAS (@TestFactory): los casos se generan en tiempo de ejecucion. */
    @TestFactory
    @DisplayName("Potencias generadas dinamicamente")
    Stream<DynamicTest> potenciasDinamicas() {
        record CasoPotencia(double base, double exp, double esperado) {
        }
        List<CasoPotencia> casos = List.of(
                new CasoPotencia(2.0d, 8.0d, 256.0d),
                new CasoPotencia(9.0d, 0.5d, 3.0d),
                new CasoPotencia(5.0d, 0.0d, 1.0d),
                new CasoPotencia(-2.0d, 3.0d, -8.0d),
                new CasoPotencia(2.0d, -2.0d, 0.25d));

        return casos.stream().map(caso -> dynamicTest(
                String.format("%.1f ^ %.2f = %.4f", caso.base(), caso.exp(), caso.esperado()),
                () -> assertEquals(caso.esperado(),
                        calculator.execute("^", caso.base(), caso.exp()), DELTA)));
    }
}
