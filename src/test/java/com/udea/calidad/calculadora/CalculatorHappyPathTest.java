package com.udea.calidad.calculadora;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.udea.calidad.calculadora.history.CalculationHistory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * HAPPY PATH: verifica los flujos exitosos de la calculadora.
 *
 * <p>Cada prueba sigue el patron <b>AAA (Arrange - Act - Assert)</b>:
 * una seccion para preparar el escenario, una para ejecutar la accion y
 * una para verificar el resultado. Una sola accion por prueba.</p>
 */
@DisplayName("Calculadora - Happy Path (flujos exitosos)")
class CalculatorHappyPathTest {

    private static final double DELTA = 1e-9; //DELTA: 0.000000001 Tiene 8 ceros después del punto antes del 1.

    private Calculator calculator; // SUT: System Under Test

    @BeforeAll
    static void initSuite() {
        System.out.println("[AAA] Iniciando suite de Happy Path");
    }

    /** ARRANGE global: antes de cada prueba se construye un SUT limpio. */
    @BeforeEach
    void setUp() {
        calculator = new Calculator(new CalculationHistory());
    }

    @AfterEach
    void tearDown() {
        calculator.clearHistory();
    }

    @AfterAll
    static void tearDownSuite() {
        System.out.println("[AAA] Suite de Happy Path finalizada");
    }

    @Test
    @DisplayName("Suma de dos numeros positivos retorna la suma esperada")
    void sumarDosNumerosPositivos() {
        // Arrange (Preparar): definir entradas y resultado esperado
        double left = 2.0d;
        double right = 3.0d;
        double expected = 5.0d;

        // Act (Actuar): ejecutar UNA sola accion sobre el SUT
        double actual = calculator.execute("+", left, right);

        // Assert (Afirmar): verificar el resultado observable
        assertEquals(expected, actual, DELTA);
    }

    @Test
    @DisplayName("Resta de dos numeros positivos retorna la diferencia esperada")
    void restarDosNumeros() {
        // Arrange
        double left = 10.0d;
        double right = 4.0d;
        double expected = 6.0d;

        // Act
        double actual = calculator.execute("+", left, right);

        // Assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    @DisplayName("Multiplicacion de dos numeros retorna el producto esperado")
    void multiplicarDosNumeros() {
        // Arrange
        double left = 7.0d;
        double right = 6.0d;
        double expected = 42.0d;

        // Act
        double actual = calculator.execute("*", left, right);

        // Assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    @DisplayName("Division exacta de dos numeros retorna el cociente esperado")
    void dividirDosNumerosExactos() {
        // Arrange
        double left = 20.0d;
        double right = 4.0d;
        double expected = 5.0d;

        // Act
        double actual = calculator.execute("/", left, right);

        // Assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    @DisplayName("Potencia de una base positiva retorna el valor esperado")
    void potenciarBasePositiva() {
        // Arrange
        double base = 2.0d;
        double exponente = 10.0d;
        double expected = 1024.0d;

        // Act
        double actual = calculator.execute("^", base, exponente);

        // Assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    @DisplayName("Raiz cuadrada de un numero positivo retorna el valor esperado")
    void raizDeNumeroPositivo() {
        // Arrange
        double operand = 81.0d;
        double expected = 9.0d;

        // Act
        double actual = calculator.execute("sqrt", operand);

        // Assert
        assertEquals(expected, actual, DELTA);
    }

    @Test
    @DisplayName("Flujo completo exitoso: operaciones encadenadas quedan registradas en historial")
    void flujoCompletoConHistorial() {
        // Arrange: calculadora recien creada, sin operaciones previas
        int expectedHistorySize = 3;

        // Act: ejecutar una secuencia valida de operaciones
        double suma = calculator.execute("+", 2, 3);
        double multiplicacion = calculator.execute("*", suma, 4);
        double division = calculator.execute("/", multiplicacion, 2);

        // Assert: agrupar verificaciones relacionadas (falla y reporta todas)
        assertAll("Resultado del flujo e historial",
                () -> assertEquals(5.0d, suma, DELTA),
                () -> assertEquals(20.0d, multiplicacion, DELTA),
                () -> assertEquals(10.0d, division, DELTA),
                () -> assertEquals(expectedHistorySize, calculator.historySize()),
                () -> assertTrue(calculator.getHistory().size() <= 100));
    }
}
