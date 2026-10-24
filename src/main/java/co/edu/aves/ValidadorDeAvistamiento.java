package co.edu.aves;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class ValidadorDeAvistamiento {

    static final int ESPECIE_MIN = 3;
    static final int ESPECIE_MAX = 50;
    private static final Pattern FORMATO_FECHA = Pattern.compile("\\d{4}-\\d{2}-\\d{2}");

    private final Reloj reloj;

    public ValidadorDeAvistamiento(Reloj reloj) {
        this.reloj = reloj;
    }

    public void validar(Avistamiento a) {
        validarEspecie(a.getEspecie());
        validarFecha(a.getFecha());
        validarNoVacio("lugar", a.getLugar());
        validarNoVacio("observador", a.getObservador());
    }

    private void validarEspecie(String especie) {
        validarNoVacio("especie", especie);
        int largo = especie.strip().length();
        if (largo < ESPECIE_MIN || largo > ESPECIE_MAX) {
            throw new AvistamientoInvalidoException("especie",
                    "La especie debe tener entre " + ESPECIE_MIN + " y " + ESPECIE_MAX + " caracteres");
        }
    }

    private void validarFecha(String fecha) {
        validarNoVacio("fecha", fecha);
        if (!FORMATO_FECHA.matcher(fecha).matches()) {
            throw new AvistamientoInvalidoException("fecha", "La fecha debe tener formato AAAA-MM-DD");
        }
        LocalDate valor;
        try {
            valor = LocalDate.parse(fecha);
        } catch (DateTimeParseException e) {
            throw new AvistamientoInvalidoException("fecha", "La fecha no es una fecha real del calendario");
        }
        if (valor.isAfter(reloj.hoy())) {
            throw new AvistamientoInvalidoException("fecha", "La fecha no puede ser posterior a hoy");
        }
    }

    private void validarNoVacio(String campo, String valor) {
        if (valor == null || valor.isBlank()) {
            throw new AvistamientoInvalidoException(campo, "El campo " + campo + " no puede estar vacío");
        }
    }
}
