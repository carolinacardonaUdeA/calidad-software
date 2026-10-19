package com.udea.calidad.calculadora;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.udea.calidad.calculadora.exception.UnsupportedOperatorException;
import com.udea.calidad.calculadora.history.CalculationHistory;
import com.udea.calidad.calculadora.operation.BinaryOperation;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * PRUEBAS AVANZADAS DE DISENO (SOLID).
 *
 * <p>Estas pruebas demuestran con evidencia ejecutable los principios OCP y LSP:</p>
 * <ul>
 *   <li>OCP: se agrega un operador nuevo (modulo) SIN tocar el codigo de Calculator.</li>
 *   <li>LSP: todas las operaciones son sustituibles por su abstraccion.</li>
 *   <li>DIP: dos calculadoras con historiales independientes no comparten estado.</li>
 * </ul>
 */
@DisplayName("Calculadora - Principios SOLID verificables")
class CalculatorSolidOcpLspTest {

    /** Operacion NUEVA creada solo para la prueba: nunca modificamos Calculator para soportarla. */
    private static final class Modulo implements BinaryOperation {

        @Override
        public String getSymbol() {
            return "%";
        }

        @Override
        public double apply(double left, double right) {
            return left % right;
        }
    }

    private static final double DELTA = 1e-9;

    @Test
    @DisplayName("OCP: nueva operacion registrada en tiempo de ejecucion sin modificar la clase")
    void nuevaOperacionSinModificarCalculator() {
        // Arrange: calculadora estandar + extension por composicion
        Calculator calculator = new Calculator(new CalculationHistory());

        // Act: EXTENDER el sistema registrando una operacion nueva
        calculator.register(new Modulo());
        double actual = calculator.execute("%", 10.0d, 3.0d);

        // Assert: el nuevo operador funciona y los originales siguen intactos
        assertEquals(1.0d, actual, DELTA);
        assertEquals(7.0d, calculator.execute("+", 3.0d, 4.0d), DELTA);
    }

    @Test
    @DisplayName("LSP: toda operacion binaria responde al mismo contrato via su abstraccion")
    void todasLasOperacionesSonSustituiblesPorSuAbstraccion() {
        Calculator calculator = new Calculator(new CalculationHistory());

        Set<String> simbolos = calculator.binarySymbols();
        assertEquals(Set.of("+", "-", "*", "/", "^"), simbolos);

        // Cada implementacion se usa SOLO a traves de la interfaz comun
        simbolos.forEach(symbol ->
                assertDoesNotThrow(() -> calculator.execute(symbol, 6.0d, 3.0d),
                        "La operacion " + symbol + " debe cumplir el contrato binario"));
    }

    @Test
    @DisplayName("DIP: historial inyectado; dos calculadores no comparten estado")
    void historialInyectadoPorConstructor() {
        // Arrange: cada calculadora recibe SU PROPIA dependencia (abstraccion concreta)
        CalculationHistory historialA = new CalculationHistory();
        CalculationHistory historialB = new CalculationHistory();
        Calculator calculadoraA = new Calculator(historialA);
        Calculator calculadoraB = new Calculator(historialB);

        // Act: solo A ejecuta operaciones
        calculadoraA.execute("+", 1.0d, 1.0d);

        // Assert: B permanece aislada (bajo acoplamiento)
        assertEquals(1, calculadoraA.historySize());
        assertEquals(0, calculadoraB.historySize());
    }

    @Test
    @DisplayName("Operador no registrado sigue fallando incluso tras extender con otro")
    void extensionNoAlcanceOperadoresNoRegistrados() {
        Calculator calculator = new Calculator(new CalculationHistory());
        calculator.register(new Modulo());

        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperatorException.class,
                () -> calculator.execute("&", 1.0d, 1.0d));
    }
}
