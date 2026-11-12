package co.edu.aves.aplicacion.servicio;

import co.edu.aves.aplicacion.dominio.Avistamiento;
import co.edu.aves.aplicacion.puerto.salida.RepositorioDeAvistamientos;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Fake: implementación real y funcional del puerto de salida, pero en memoria. Asigna ids como
 * lo haría la base de datos y guarda copias, para que modificar un objeto afuera no cambie lo guardado.
 */
class RepositorioEnMemoria implements RepositorioDeAvistamientos {

    private final Map<Long, Avistamiento> guardados = new LinkedHashMap<>();
    private long siguienteId = 1;

    @Override
    public List<Avistamiento> listarTodos() {
        List<Avistamiento> copia = new ArrayList<>();
        guardados.values().forEach(a -> copia.add(copiar(a)));
        return copia;
    }

    @Override
    public Optional<Avistamiento> buscarPorId(Long id) {
        return Optional.ofNullable(guardados.get(id)).map(RepositorioEnMemoria::copiar);
    }

    @Override
    public boolean existePorId(Long id) {
        return guardados.containsKey(id);
    }

    @Override
    public Avistamiento guardar(Avistamiento avistamiento) {
        Avistamiento aGuardar = copiar(avistamiento);
        if (aGuardar.getId() == null) {
            aGuardar.setId(siguienteId++);
        }
        guardados.put(aGuardar.getId(), aGuardar);
        return copiar(aGuardar);
    }

    @Override
    public void eliminarPorId(Long id) {
        guardados.remove(id);
    }

    int cantidad() {
        return guardados.size();
    }

    private static Avistamiento copiar(Avistamiento o) {
        Avistamiento c = new Avistamiento();
        c.setId(o.getId());
        c.setEspecie(o.getEspecie());
        c.setLugar(o.getLugar());
        c.setFecha(o.getFecha());
        c.setObservador(o.getObservador());
        return c;
    }
}
