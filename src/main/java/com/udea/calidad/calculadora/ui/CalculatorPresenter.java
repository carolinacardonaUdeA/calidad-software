package com.udea.calidad.calculadora.ui;

import com.udea.calidad.calculadora.Calculator;
import com.udea.calidad.calculadora.exception.DivisionByZeroException;
import com.udea.calidad.calculadora.exception.NegativeSquareRootException;
import java.util.List;
import java.util.Objects;

/**
 * Logica de presentacion de la calculadora grafica (patron MVP: Presenter).
 *
 * <p>Toda la logica de interaccion esta AQUI y NO en la vista Swing, de modo
 * que es 100% testeable unitariamente sin abrir una ventana (SRP + DIP).
 * La vista solo dibuja el estado que este presenter publica.</p>
 */
public class CalculatorPresenter {

    /** Estado observable e inmutable que la vista dibuja en cada refresco. */
    public record UiState(String display, String hint, boolean error) {
    }

    private final Calculator calculator;

    private String current = "0";          // texto mostrado en pantalla
    private Double pendingLeft = null;     // primer operando de operacion binaria
    private String pendingSymbol = null;   // simbolo pendiente (+, -, *, /, ^)
    private boolean startNewEntry = true;  // el proximo digito reemplaza la pantalla
    private boolean errorState = false;    // ultimo resultado fue un error

    public CalculatorPresenter(Calculator calculator) {
        this.calculator = Objects.requireNonNull(calculator);
    }

    // ------------------------------------------------------------ entradas --

    public void inputDigit(String digit) {
        if (errorState) {
            clear();
        }
        if (startNewEntry || "0".equals(current)) {
            current = digit;
            startNewEntry = false;
        } else {
            current += digit;
        }
    }

    public void inputDot() {
        if (errorState) {
            clear();
        }
        if (startNewEntry) {
            current = "0.";
            startNewEntry = false;
        } else if (!current.contains(".")) {
            current += ".";
        }
    }

    /** Selecciona una operacion binaria (+, -, *, /, ^), encadenando si ya habia una. */
    public void setOperation(String symbol) {
        if (errorState) {
            clear();
        }
        if (pendingSymbol != null && !startNewEntry && pendingLeft != null) {
            resolvePending();               // 2 + 3 [x] -> resuelve 5 y queda 5 x _
            if (errorState) {
                return;
            }
        }
        if (pendingLeft == null) {
            pendingLeft = Double.parseDouble(current); // siempre valido por construccion
        }
        pendingSymbol = symbol;             // presionar dos veces solo cambia el operador
        startNewEntry = true;
    }

    /** Ejecuta la operacion pendiente (=). */
    public void equalsOperation() {
        if (errorState) {
            clear();
            return;
        }
        if (pendingSymbol == null || pendingLeft == null) {
            return;                         // = sin operacion no hace nada
        }
        resolvePending();
    }

    /** Ejecuta inmediatamente una operacion unaria (sqrt). */
    public void applyUnary(String symbol) {
        if (errorState) {
            clear();
        }
        try {
            double value = Double.parseDouble(current);
            double result = calculator.execute(symbol, value);
            current = format(result);
            pendingLeft = null;
            pendingSymbol = null;
            startNewEntry = true;
        } catch (DivisionByZeroException | NegativeSquareRootException e) {
            setError(shortMessage(e)); // excepciones de negocio SI son alcanzables
        }
    }

    public void backspace() {
        if (errorState) {
            clear();
            return;
        }
        if (!startNewEntry && current.length() > 1) {
            current = current.substring(0, current.length() - 1);
            if (current.isEmpty() || "-".equals(current)) {
                current = "0";
            }
        } else {
            current = "0";
            startNewEntry = true;
        }
    }

    public void clear() {
        current = "0";
        pendingLeft = null;
        pendingSymbol = null;
        startNewEntry = true;
        errorState = false;
    }

    // ------------------------------------------------------------- estado --

    public UiState getState() {
        String hint = "";
        if (pendingSymbol != null && pendingLeft != null) {
            hint = format(pendingLeft) + " " + pretty(pendingSymbol);
        }
        return new UiState(current, hint, errorState);
    }

    public List<String> historyLines() {
        return calculator.getHistory().stream()
                .map(entry -> "  " + entry)
                .toList();
    }

    public int historySize() {
        return calculator.historySize();
    }

    // ------------------------------------------------------------ internos --

    private void resolvePending() {
        try {
            double right = Double.parseDouble(current);
            double result = calculator.execute(pendingSymbol, pendingLeft, right);
            current = format(result);
            pendingLeft = null;
            pendingSymbol = null;
            startNewEntry = true;
            // Un resultado infinito o NaN es un estado excepcional visible:
            // la vista lo pinta en rojo y exige reinicio (estilo iOS).
            errorState = !Double.isFinite(result);
        } catch (DivisionByZeroException | NegativeSquareRootException e) {
            setError(shortMessage(e));
        } catch (NumberFormatException e) {
            setError("Número inválido");
        }
    }

    private void setError(String message) {
        current = "⚠ " + message;
        pendingLeft = null;
        pendingSymbol = null;
        startNewEntry = true;
        errorState = true;
    }

    private String shortMessage(RuntimeException e) {
        if (e instanceof DivisionByZeroException) {
            return "División entre cero";
        }
        return "Raíz de número negativo"; // NegativeSquareRootException
    }

    private String format(double value) {
        if (Double.isNaN(value)) {
            return "No definido";
        }
        if (Double.isInfinite(value)) {
            return value > 0 ? "∞" : "-∞";
        }
        if (Math.abs(value) < 1e15d && value == Math.rint(value)) {
            return String.valueOf((long) value);
        }
        return Double.toString(value);
    }

    private String pretty(String symbol) {
        return switch (symbol) {
            case "*" -> "×";
            case "/" -> "÷";
            case "sqrt" -> "√";
            default -> symbol;
        };
    }
}
