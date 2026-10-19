package com.udea.calidad.calculadora.operation;

import com.udea.calidad.calculadora.exception.NegativeSquareRootException;

/**
 * Operacion de raiz cuadrada (unaria).
 *
 * <p>Caso excepcional documentado: raiz de numero negativo lanza
 * {@link NegativeSquareRootException}.</p>
 */
public final class SquareRoot implements UnaryOperation {

    @Override
    public String getSymbol() {
        return "sqrt";
    }

    @Override
    public double apply(double operand) {
        if (operand < 0.0d) {
            throw new NegativeSquareRootException(
                "No existe raiz cuadrada real de un numero negativo (operando=" + operand + ").");
        }
        return Math.sqrt(operand);
    }
}
