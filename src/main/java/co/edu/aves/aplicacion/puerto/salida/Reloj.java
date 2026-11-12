package co.edu.aves.aplicacion.puerto.salida;

import java.time.LocalDate;

/** Puerto de salida: lo que el núcleo necesita saber del mundo exterior sobre la fecha de hoy. */
public interface Reloj {
    LocalDate hoy();
}
