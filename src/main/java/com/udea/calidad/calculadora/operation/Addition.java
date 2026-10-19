package com.udea.calidad.calculadora.operation;

/** Operacion de suma. Responsabilidad unica: sumar dos operandos (SRP). */
public final class Addition implements BinaryOperation {

    @Override
    public String getSymbol() {
        return "+";
    }

    @Override
    public double apply(double left, double right) {
        return left + right;
    }
}
