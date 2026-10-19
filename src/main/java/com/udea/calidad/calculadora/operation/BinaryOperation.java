package com.udea.calidad.calculadora.operation;

/**
 * Operacion binaria: recibe dos operandos (izquierda y derecha) y produce un resultado.
 *
 * <p>Toda implementacion debe ser sustituible por otra sin alterar el comportamiento
 * del cliente (<b>SOLID - Liskov Substitution Principle</b>): mismo contrato de entrada,
 * mismo tipo de salida y excepciones documentadas para los casos excepcionales.</p>
 */
public interface BinaryOperation extends Operation {

    /**
     * Aplica la operacion sobre dos operandos.
     *
     * @param left  operando izquierdo
     * @param right operando derecho
     * @return resultado numerico de la operacion
     */
    double apply(double left, double right);
}
