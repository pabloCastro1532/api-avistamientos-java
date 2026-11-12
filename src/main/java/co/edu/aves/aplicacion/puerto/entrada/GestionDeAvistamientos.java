package co.edu.aves.aplicacion.puerto.entrada;

import co.edu.aves.aplicacion.dominio.Avistamiento;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Puerto de entrada: los casos de uso que la aplicación ofrece al mundo exterior. */
public interface GestionDeAvistamientos {

    List<Avistamiento> listar();

    Map<String, Long> resumen();

    Optional<Avistamiento> ver(Long id);

    /** @throws co.edu.aves.aplicacion.dominio.AvistamientoInvalidoException si no cumple las reglas */
    Avistamiento crear(Avistamiento nuevo);

    /** Vacío si el id no existe. @throws co.edu.aves.aplicacion.dominio.AvistamientoInvalidoException si los datos no son válidos (se valida primero) */
    Optional<Avistamiento> actualizar(Long id, Avistamiento datos);

    /** @return true si existía y se eliminó; false si el id no existe */
    boolean eliminar(Long id);
}
