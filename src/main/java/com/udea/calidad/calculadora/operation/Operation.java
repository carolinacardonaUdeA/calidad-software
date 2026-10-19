package com.udea.calidad.calculadora.operation;

/** Contrato comun de todas las operaciones: expone su simbolo (ISP + DIP). */
public interface Operation {

    /** @return simbolo que identifica la operacion dentro de la calculadora. */
    String getSymbol();
}
