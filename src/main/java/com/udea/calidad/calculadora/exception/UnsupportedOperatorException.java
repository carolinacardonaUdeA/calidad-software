package com.udea.calidad.calculadora.exception;

/** Excepcion de negocio: el simbolo de operacion solicitado no esta registrado (Exceptional Path). */
public class UnsupportedOperatorException extends RuntimeException {

    public UnsupportedOperatorException(String symbol) {
        super("Operador no soportado: '" + symbol + "'.");
    }
}
