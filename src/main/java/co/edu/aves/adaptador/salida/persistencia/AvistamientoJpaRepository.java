package co.edu.aves.adaptador.salida.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

/** Interfaz de Spring Data: queda dentro del adaptador, el núcleo nunca la ve. */
public interface AvistamientoJpaRepository extends JpaRepository<AvistamientoJpa, Long> {
}
