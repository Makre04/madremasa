package com.madremasa.service;

// Error que se produce cuando una operación no cumple una regla de negocio.
// Su mensaje se muestra tal cual al usuario.
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
