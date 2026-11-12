package co.edu.aves.aplicacion.servicio;

import co.edu.aves.aplicacion.dominio.Avistamiento;
import co.edu.aves.aplicacion.dominio.ValidadorDeAvistamiento;
import co.edu.aves.aplicacion.puerto.entrada.GestionDeAvistamientos;
import co.edu.aves.aplicacion.puerto.salida.RepositorioDeAvistamientos;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Implementa los casos de uso. Habla solo con puertos (interfaces): no sabe si detrás hay
 * JPA, memoria u otra cosa, ni si lo llaman por HTTP, por consola o desde una prueba.
 */
public class ServicioDeAvistamientos implements GestionDeAvistamientos {

    private final RepositorioDeAvistamientos repositorio;
    private final ValidadorDeAvistamiento validador;

    public ServicioDeAvistamientos(RepositorioDeAvistamientos repositorio, ValidadorDeAvistamiento validador) {
        this.repositorio = repositorio;
        this.validador = validador;
    }

    @Override
    public List<Avistamiento> listar() {
        return repositorio.listarTodos();
    }

    @Override
    public Map<String, Long> resumen() {
        return repositorio.listarTodos().stream().collect(
                Collectors.groupingBy(Avistamiento::getEspecie, TreeMap::new, Collectors.counting()));
    }

    @Override
    public Optional<Avistamiento> ver(Long id) {
        return repositorio.buscarPorId(id);
    }

    @Override
    public Avistamiento crear(Avistamiento nuevo) {
        validador.validar(nuevo);
        nuevo.setId(null);
        return repositorio.guardar(nuevo);
    }

    @Override
    public Optional<Avistamiento> actualizar(Long id, Avistamiento datos) {
        validador.validar(datos);
        if (!repositorio.existePorId(id)) {
            return Optional.empty();
        }
        datos.setId(id);
        return Optional.of(repositorio.guardar(datos));
    }

    @Override
    public boolean eliminar(Long id) {
        if (!repositorio.existePorId(id)) {
            return false;
        }
        repositorio.eliminarPorId(id);
        return true;
    }
}
