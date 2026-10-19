package com.udea.calidad.calculadora.exception;

/** Excepcion de negocio: raiz cuadrada de un numero negativo (Exceptional Path). */
public class NegativeSquareRootException extends RuntimeException {

    public NegativeSquareRootException(String message) {
        super(message);
    }
}
