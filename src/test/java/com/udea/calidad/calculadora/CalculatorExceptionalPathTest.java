package com.udea.calidad.calculadora;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.udea.calidad.calculadora.exception.DivisionByZeroException;
import com.udea.calidad.calculadora.exception.NegativeSquareRootException;
import com.udea.calidad.calculadora.exception.UnsupportedOperatorException;
import com.udea.calidad.calculadora.history.CalculationHistory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * EXCEPTIONAL PATH: verifica el comportamiento ante entradas invalidas,
 * condiciones de borde y estados no permitidos.
 *
 * <p>Buenas practicas verificadas aqui:</p>
 * <ul>
 *   <li>Se lanza la excepcion CORRECTA para cada condicion (assertThrows).</li>
 *   <li>El mensaje es claro y accionable.</li>
 *   <li>El sistema queda en estado consistente despues del fallo
 *       (el historial NO registra operaciones fallidas).</li>
 * </ul>
 */
@DisplayName("Calculadora - Exceptional Path (flujos de error)")
class CalculatorExceptionalPathTest {

    private static final double DELTA = 1e-9;

    private Calculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new Calculator(new CalculationHistory());
    }

    @Test
    @DisplayName("Division entre cero lanza DivisionByZeroException")
    void divisionEntreCeroLanzaExcepcion() {
        // Arrange: operacion valida pero divisor en cero
        double dividendo = 10.0d;

        // Act + Assert: se espera la excepcion especifica, no una generica
        DivisionByZeroException exception = assertThrows(
                DivisionByZeroException.class,
                () -> calculator.execute("/", dividendo, 0.0d));

        assertTrue(exception.getMessage().contains("cero"),
                "El mensaje debe explicar la causa: " + exception.getMessage());
    }

    @Test
    @DisplayName("Division entre cero negativo tambien lanza DivisionByZeroException")
    void divisionEntreCeroNegativoLanzaExcepcion() {
        assertThrows(DivisionByZeroException.class,
                () -> calculator.execute("/", 5.0d, -0.0d));
    }

    @Test
    @DisplayName("Raiz cuadrada de numero negativo lanza NegativeSquareRootException")
    void raizDeNegativoLanzaExcepcion() {
        NegativeSquareRootException exception = assertThrows(
                NegativeSquareRootException.class,
                () -> calculator.execute("sqrt", -9.0d));

        assertTrue(exception.getMessage().contains("-9.0"),
                "El mensaje debe incluir el valor invalido: " + exception.getMessage());
    }

    @Test
    @DisplayName("Operador binario no registrado lanza UnsupportedOperatorException")
    void operadorBinarioNoSoportado() {
        UnsupportedOperatorException exception = assertThrows(
                UnsupportedOperatorException.class,
                () -> calculator.execute("%", 10.0d, 3.0d));

        assertEquals("Operador no soportado: '%'.", exception.getMessage());
    }

    @Test
    @DisplayName("Operador unario usado con firma binaria lanza UnsupportedOperatorException")
    void operadorUnarioConFirmaBinariaFalla() {
        assertThrows(UnsupportedOperatorException.class,
                () -> calculator.execute("sqrt", 4.0d, 2.0d));
    }

    @Test
    @DisplayName("El historial NO registra operaciones fallidas (estado consistente)")
    void historialNoRegistraOperacionesFallidas() {
        // Arrange: historial limpio
        int expectedSizeAfterFailure = 0;

        // Act: intentar dos operaciones que fallan
        try {
            calculator.execute("/", 1.0d, 0.0d);
        } catch (DivisionByZeroException ignored) {
            // se documenta la captura: la prueba verifica el estado posterior
        }
        try {
            calculator.execute("sqrt", -4.0d);
        } catch (NegativeSquareRootException ignored) {
            // idem
        }

        // Assert: ninguna operacion fallo quedo registrada
        assertEquals(expectedSizeAfterFailure, calculator.historySize());
    }

    @Test
    @DisplayName("Tras un error, la calculadora sigue funcionando (recuperacion)")
    void calculadoraRecuperaEstadoDespuesDeError() {
        // Arrange
        double expected = 7.0d;

        // Act: provocar un error y luego ejecutar una operacion valida
        assertThrows(DivisionByZeroException.class, () -> calculator.execute("/", 8.0d, 0.0d));
        double actual = assertDoesNotThrow(() -> calculator.execute("+", 3.0d, 4.0d));

        // Assert
        assertEquals(expected, actual, DELTA);
        assertEquals(1, calculator.historySize(), "Solo la operacion exitosa queda registrada");
    }
}
