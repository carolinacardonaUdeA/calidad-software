package com.udea.calidad.calculadora.exception;

/** Excepcion de negocio: intento de division entre cero (Exceptional Path). */
public class DivisionByZeroException extends RuntimeException {

    public DivisionByZeroException(String message) {
        super(message);
    }
}
