package co.edu.aves.configuracion;

import co.edu.aves.aplicacion.dominio.ValidadorDeAvistamiento;
import co.edu.aves.aplicacion.puerto.entrada.GestionDeAvistamientos;
import co.edu.aves.aplicacion.puerto.salida.Reloj;
import co.edu.aves.aplicacion.puerto.salida.RepositorioDeAvistamientos;
import co.edu.aves.aplicacion.servicio.ServicioDeAvistamientos;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Raíz de composición: el único lugar que conoce el núcleo y Spring a la vez. Crea los objetos
 * del núcleo y les conecta los adaptadores, para que el núcleo no necesite anotaciones.
 */
@Configuration
public class ConfiguracionDeBeans {

    @Bean
    ValidadorDeAvistamiento validadorDeAvistamiento(Reloj reloj) {
        return new ValidadorDeAvistamiento(reloj);
    }

    @Bean
    GestionDeAvistamientos gestionDeAvistamientos(RepositorioDeAvistamientos repositorio,
                                                  ValidadorDeAvistamiento validador) {
        return new ServicioDeAvistamientos(repositorio, validador);
    }
}
