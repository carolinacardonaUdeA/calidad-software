package com.udea.calidad.calculadora.operation;

/** Operacion de multiplicacion. Responsabilidad unica: producto de dos operandos (SRP). */
public final class Multiplication implements BinaryOperation {

    @Override
    public String getSymbol() {
        return "*";
    }

    @Override
    public double apply(double left, double right) {
        return left * right;
    }
}
