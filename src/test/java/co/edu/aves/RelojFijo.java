package co.edu.aves;

import java.time.LocalDate;

/** Stub de Reloj: siempre responde la misma fecha, para que las pruebas no dependan del día en que se corren. */
class RelojFijo implements Reloj {

    private final LocalDate fecha;

    RelojFijo(LocalDate fecha) {
        this.fecha = fecha;
    }

    @Override
    public LocalDate hoy() {
        return fecha;
    }
}
