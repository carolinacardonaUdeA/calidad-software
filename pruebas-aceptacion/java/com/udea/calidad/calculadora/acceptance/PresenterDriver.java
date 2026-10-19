package com.udea.calidad.calculadora.acceptance;

import com.udea.calidad.calculadora.Calculator;
import com.udea.calidad.calculadora.history.CalculationHistory;
import com.udea.calidad.calculadora.ui.CalculatorPresenter;

import java.util.List;

/**
 * Simula a una persona usando la calculadora grafica: recibe una secuencia de
 * teclas y la entrega al {@link CalculatorPresenter} (la misma logica que usa la
 * ventana Swing). Asi se prueba el flujo completo de usuario sin abrir ventanas.
 *
 * <p>Teclas validas (separadas por espacio): digitos (puede ir varios juntos,
 * p. ej. {@code 12}), {@code .}, {@code + - * / ^}, {@code =}, {@code sqrt},
 * {@code back} (borrar) y {@code C} (limpiar por completo).</p>
 */
public class PresenterDriver {

    private final Calculator calculator = new Calculator(new CalculationHistory());
    private CalculatorPresenter presenter = new CalculatorPresenter(calculator);

    public void press(String keys) {
        for (String token : keys.trim().split("\\s+")) {
            pressToken(token);
        }
    }

    /** Reinicia el presenter (conserva el historial del motor, como la app real). */
    public void restart() {
        presenter = new CalculatorPresenter(calculator);
    }

    public String display() {
        return presenter.getState().display();
    }

    public String hint() {
        return presenter.getState().hint();
    }

    public boolean isError() {
        return presenter.getState().error();
    }

    public int historySize() {
        return presenter.historySize();
    }

    public List<String> historyLines() {
        return presenter.historyLines();
    }

    private void pressToken(String token) {
        switch (token) {
            case "+", "-", "*", "/", "^" -> presenter.setOperation(token);
            case "=" -> presenter.equalsOperation();
            case "sqrt" -> presenter.applyUnary("sqrt");
            case "back" -> presenter.backspace();
            case "C" -> presenter.clear();
            default -> typeNumber(token);
        }
    }

    private void typeNumber(String token) {
        for (char c : token.toCharArray()) {
            if (c == '.') {
                presenter.inputDot();
            } else if (Character.isDigit(c)) {
                presenter.inputDigit(String.valueOf(c));
            } else {
                throw new IllegalArgumentException("Tecla no reconocida: '" + token + "'");
            }
        }
    }
}
