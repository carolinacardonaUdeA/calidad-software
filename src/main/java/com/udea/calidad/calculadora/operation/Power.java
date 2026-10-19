package com.udea.calidad.calculadora.operation;

/** Operacion de potencia: base elevada al exponente indicado (SRP). */
public final class Power implements BinaryOperation {

    @Override
    public String getSymbol() {
        return "^";
    }

    @Override
    public double apply(double base, double exponent) {
        return Math.pow(base, exponent);
    }
}
