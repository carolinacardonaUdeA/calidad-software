package com.udea.calidad.calculadora.operation;

/**
 * Operacion unaria: recibe un unico operando (ISP - las operaciones de un
 * solo argumento no se ven obligadas a implementar metodos que no usan).
 */
public interface UnaryOperation extends Operation {

    /**
     * Aplica la operacion sobre un operando.
     *
     * @param operand operando
     * @return resultado numerico de la operacion
     */
    double apply(double operand);
}
