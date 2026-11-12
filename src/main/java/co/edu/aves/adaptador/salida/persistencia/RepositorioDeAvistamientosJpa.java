package co.edu.aves.adaptador.salida.persistencia;

import co.edu.aves.aplicacion.dominio.Avistamiento;
import co.edu.aves.aplicacion.puerto.salida.RepositorioDeAvistamientos;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/**
 * Adaptador de salida (lado de la base de datos): implementa el puerto RepositorioDeAvistamientos
 * con JPA y traduce entre el modelo del dominio y el modelo de persistencia.
 */
@Component
class RepositorioDeAvistamientosJpa implements RepositorioDeAvistamientos {

    private final AvistamientoJpaRepository jpa;

    RepositorioDeAvistamientosJpa(AvistamientoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public List<Avistamiento> listarTodos() {
        return jpa.findAll().stream().map(RepositorioDeAvistamientosJpa::aDominio).toList();
    }

    @Override
    public Optional<Avistamiento> buscarPorId(Long id) {
        return jpa.findById(id).map(RepositorioDeAvistamientosJpa::aDominio);
    }

    @Override
    public boolean existePorId(Long id) {
        return jpa.existsById(id);
    }

    @Override
    public Avistamiento guardar(Avistamiento avistamiento) {
        return aDominio(jpa.save(aJpa(avistamiento)));
    }

    @Override
    public void eliminarPorId(Long id) {
        jpa.deleteById(id);
    }

    private static Avistamiento aDominio(AvistamientoJpa e) {
        Avistamiento a = new Avistamiento();
        a.setId(e.getId());
        a.setEspecie(e.getEspecie());
        a.setLugar(e.getLugar());
        a.setFecha(e.getFecha());
        a.setObservador(e.getObservador());
        return a;
    }

    private static AvistamientoJpa aJpa(Avistamiento a) {
        AvistamientoJpa e = new AvistamientoJpa();
        e.setId(a.getId());
        e.setEspecie(a.getEspecie());
        e.setLugar(a.getLugar());
        e.setFecha(a.getFecha());
        e.setObservador(a.getObservador());
        return e;
    }
}
