package com.udea.calidad.calculadora.acceptance;

import com.udea.calidad.calculadora.Calculator;
import com.udea.calidad.calculadora.history.CalculationHistory;
import com.udea.calidad.calculadora.operation.BinaryOperation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Puente entre Karate y el motor de la calculadora (capa "dominio").
 *
 * <p>Karate llama a esta clase con {@code Java.type(...)}. Cada metodo es una accion
 * que un usuario/analista de negocio entiende ("calcular", "limpiar historial").
 * Las excepciones de negocio NO se propagan: se capturan y quedan disponibles via
 * {@link #errorType()} / {@link #errorMessage()} para poder verificar el
 * <i>Exceptional Path</i> desde el Gherkin.</p>
 */
public class CalculatorDriver {

    private final Calculator calculator = new Calculator(new CalculationHistory());
    private String lastResult;
    private String lastErrorType;
    private String lastErrorMessage;

    // ------------------------------------------------------------ acciones --

    /** Ejecuta una operacion binaria (+, -, *, /, ^ o cualquiera registrada). */
    public void calculate(String symbol, double left, double right) {
        reset();
        try {
            lastResult = format(calculator.execute(symbol, left, right));
        } catch (RuntimeException e) {
            captureError(e);
        }
    }

    /** Ejecuta una operacion unaria (sqrt). */
    public void calculateUnary(String symbol, double operand) {
        reset();
        try {
            lastResult = format(calculator.execute(symbol, operand));
        } catch (RuntimeException e) {
            captureError(e);
        }
    }

    public void clearHistory() {
        calculator.clearHistory();
    }

    /** Registra una operacion nueva (modulo) SIN tocar el codigo de Calculator: evidencia de OCP. */
    public void registerModulo() {
        calculator.register(new BinaryOperation() {
            @Override
            public String getSymbol() {
                return "%";
            }

            @Override
            public double apply(double left, double right) {
                return left % right;
            }
        });
    }

    // ------------------------------------------------------------ consultas --

    /** Ultimo resultado como texto estable (hasta 10 decimales, sin ceros sobrantes). */
    public String result() {
        return lastResult;
    }

    /** Nombre simple de la excepcion de negocio lanzada, o null si no hubo error. */
    public String errorType() {
        return lastErrorType;
    }

    public String errorMessage() {
        return lastErrorMessage;
    }

    public int historySize() {
        return calculator.historySize();
    }

    /** Historial en texto, mas antiguo primero (formato de HistoryEntry.toString()). */
    public List<String> historyLines() {
        List<String> lines = new ArrayList<>();
        calculator.getHistory().forEach(entry -> lines.add(entry.toString()));
        return lines;
    }

    public List<String> binarySymbols() {
        return new ArrayList<>(calculator.binarySymbols());
    }

    public List<String> unarySymbols() {
        return new ArrayList<>(calculator.unarySymbols());
    }

    // ------------------------------------------------------------ internos --

    private void reset() {
        lastResult = null;
        lastErrorType = null;
        lastErrorMessage = null;
    }

    private void captureError(RuntimeException e) {
        lastErrorType = e.getClass().getSimpleName();
        lastErrorMessage = e.getMessage();
    }

    static String format(double value) {
        if (!Double.isFinite(value)) {
            return Double.toString(value);
        }
        BigDecimal rounded = new BigDecimal(value).setScale(10, RoundingMode.HALF_UP).stripTrailingZeros();
        return rounded.signum() == 0 ? "0" : rounded.toPlainString();
    }
}
