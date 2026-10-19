package com.udea.calidad.calculadora.operation;

import com.udea.calidad.calculadora.exception.DivisionByZeroException;

/**
 * Operacion de division.
 *
 * <p>Responsabilidad unica: dividir dos operandos (SRP).
 * Caso excepcional documentado: divisor cero lanza {@link DivisionByZeroException}.</p>
 */
public final class Division implements BinaryOperation {

    @Override
    public String getSymbol() {
        return "/";
    }

    @Override
    public double apply(double left, double right) {
        if (right == 0.0d) {
            throw new DivisionByZeroException("No es posible dividir entre cero (dividendo=" + left + ").");
        }
        return left / right;
    }
}
