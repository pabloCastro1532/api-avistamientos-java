package co.edu.aves.adaptador.salida.reloj;

import co.edu.aves.aplicacion.puerto.salida.Reloj;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

/** Adaptador de salida: implementa el puerto Reloj con el reloj real del sistema. */
@Component
public class RelojSistema implements Reloj {
    @Override
    public LocalDate hoy() {
        return LocalDate.now();
    }
}
