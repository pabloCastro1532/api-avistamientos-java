package co.edu.aves.aplicacion.puerto.salida;

import co.edu.aves.aplicacion.dominio.Avistamiento;
import java.util.List;
import java.util.Optional;

/** Puerto de salida: lo que el núcleo necesita para guardar y consultar avistamientos. */
public interface RepositorioDeAvistamientos {

    List<Avistamiento> listarTodos();

    Optional<Avistamiento> buscarPorId(Long id);

    boolean existePorId(Long id);

    Avistamiento guardar(Avistamiento avistamiento);

    void eliminarPorId(Long id);
}
