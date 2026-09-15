package com.carlos.commons.utils;

public class ValoresNumerico {

    public static void validarLongPositivo(Long numero, String mensaje) {
        validarNumeroRequerido(numero);

        if (numero < 0)
            throw new IllegalArgumentException(mensaje);
    }

    public static void validarRangoShort(Short numero, short min, short max, String mensaje) {
        validarNumeroRequerido(numero);

        if (numero < min || numero > max)
            throw new IllegalArgumentException(mensaje);
    }

    public static void validarRangoDouble(Double numero, double min, double max, String mensaje) {
        validarNumeroRequerido(numero);

        if (numero < min || numero > max)
            throw new IllegalArgumentException(mensaje);
    }

    private static void validarNumeroRequerido(Number numero) {
        if (numero == null)
            throw new IllegalArgumentException("El numero es requerido");
    }
}