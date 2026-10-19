package com.udea.calidad.calculadora.operation;

/** Operacion de resta. Responsabilidad unica: calcular la diferencia entre dos operandos (SRP). */
public final class Subtraction implements BinaryOperation {

    @Override
    public String getSymbol() {
        return "-";
    }

    @Override
    public double apply(double left, double right) {
        return left - right;
    }
}
