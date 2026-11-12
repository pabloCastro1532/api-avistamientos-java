package co.edu.aves.aplicacion.servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import co.edu.aves.aplicacion.dominio.Avistamiento;
import co.edu.aves.aplicacion.dominio.AvistamientoInvalidoException;
import co.edu.aves.aplicacion.dominio.ValidadorDeAvistamiento;
import co.edu.aves.aplicacion.puerto.salida.Reloj;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Pruebas unitarias de los casos de uso, sin Spring ni base de datos: el servicio habla solo con
 * puertos, así que se le conectan dobles. El repositorio es un fake en memoria y el reloj es un
 * stub (lambda), porque Reloj tiene un solo método.
 */
class ServicioDeAvistamientosTest {

    private static final LocalDate HOY = LocalDate.of(2026, 11, 11);

    private RepositorioEnMemoria repositorio;
    private ServicioDeAvistamientos servicio;

    @BeforeEach
    void prepararServicio() {
        Reloj relojFijo = () -> HOY;
        repositorio = new RepositorioEnMemoria();
        servicio = new ServicioDeAvistamientos(repositorio, new ValidadorDeAvistamiento(relojFijo));
    }

    private static Avistamiento avistamiento(String especie, String lugar, String fecha, String observador) {
        Avistamiento a = new Avistamiento();
        a.setEspecie(especie);
        a.setLugar(lugar);
        a.setFecha(fecha);
        a.setObservador(observador);
        return a;
    }

    private static Avistamiento colibri() {
        return avistamiento("colibri", "Chicaque", "2026-10-11", "Pablo");
    }

    // ---------- crear ----------

    @Test
    void crear_conDatosValidos_guardaYDevuelveElAvistamientoConId() {
        // Arrange
        Avistamiento nuevo = colibri();
        // Act
        Avistamiento creado = servicio.crear(nuevo);
        // Assert
        assertEquals(1L, creado.getId());
        assertEquals("colibri", creado.getEspecie());
        assertEquals(1, repositorio.cantidad());
    }

    @Test
    void crear_conIdEnviadoPorElCliente_loIgnoraYUsaElDelRepositorio() {
        // Arrange
        Avistamiento nuevo = colibri();
        nuevo.setId(99L);
        // Act
        Avistamiento creado = servicio.crear(nuevo);
        // Assert
        assertNotEquals(99L, creado.getId());
        assertFalse(repositorio.existePorId(99L));
    }

    @Test
    void crear_conDatosInvalidos_lanzaExcepcionYNoGuardaNada() {
        // Arrange
        Avistamiento invalido = avistamiento("ab", "Chicaque", "2026-10-11", "Pablo");
        // Act
        AvistamientoInvalidoException e =
                assertThrows(AvistamientoInvalidoException.class, () -> servicio.crear(invalido));
        // Assert
        assertEquals("especie", e.getCampo());
        assertEquals(0, repositorio.cantidad());
    }

    @Test
    void crear_conFechaDeManana_seRechazaSegunElRelojInyectado() {
        // Arrange
        Avistamiento deManana = avistamiento("colibri", "Chicaque", "2026-11-12", "Pablo");
        // Act
        AvistamientoInvalidoException e =
                assertThrows(AvistamientoInvalidoException.class, () -> servicio.crear(deManana));
        // Assert
        assertEquals("fecha", e.getCampo());
        assertEquals(0, repositorio.cantidad());
    }

    // ---------- listar y resumen ----------

    @Test
    void listar_sinDatos_devuelveListaVacia() {
        // Arrange: el repositorio está vacío
        // Act
        List<Avistamiento> lista = servicio.listar();
        // Assert
        assertTrue(lista.isEmpty());
    }

    @Test
    void listar_conDosAvistamientos_devuelveLosDos() {
        // Arrange
        servicio.crear(colibri());
        servicio.crear(avistamiento("tucan", "Bosque", "2026-10-12", "Ana"));
        // Act
        List<Avistamiento> lista = servicio.listar();
        // Assert
        assertEquals(2, lista.size());
    }

    @Test
    void resumen_cuentaPorEspecieYLasOrdenaAlfabeticamente() {
        // Arrange
        servicio.crear(avistamiento("tucan", "Bosque", "2026-10-12", "Ana"));
        servicio.crear(colibri());
        servicio.crear(avistamiento("colibri", "Parque", "2026-10-13", "Luis"));
        // Act
        Map<String, Long> resumen = servicio.resumen();
        // Assert
        assertEquals(List.of("colibri", "tucan"), List.copyOf(resumen.keySet()));
        assertEquals(2L, resumen.get("colibri"));
        assertEquals(1L, resumen.get("tucan"));
    }

    // ---------- ver ----------

    @Test
    void ver_idExistente_devuelveElAvistamiento() {
        // Arrange
        Avistamiento creado = servicio.crear(colibri());
        // Act
        Optional<Avistamiento> encontrado = servicio.ver(creado.getId());
        // Assert
        assertTrue(encontrado.isPresent());
        assertEquals("Chicaque", encontrado.get().getLugar());
    }

    @Test
    void ver_idInexistente_devuelveVacio() {
        // Arrange: el repositorio está vacío
        // Act
        Optional<Avistamiento> encontrado = servicio.ver(9999L);
        // Assert
        assertTrue(encontrado.isEmpty());
    }

    // ---------- actualizar ----------

    @Test
    void actualizar_idExistenteYDatosValidos_reemplazaLosDatosConservandoElId() {
        // Arrange
        Avistamiento creado = servicio.crear(colibri());
        Avistamiento cambios = avistamiento("tucan", "Bosque", "2026-10-12", "Ana");
        // Act
        Optional<Avistamiento> actualizado = servicio.actualizar(creado.getId(), cambios);
        // Assert
        assertTrue(actualizado.isPresent());
        assertEquals(creado.getId(), actualizado.get().getId());
        assertEquals("tucan", servicio.ver(creado.getId()).orElseThrow().getEspecie());
        assertEquals(1, repositorio.cantidad());
    }

    @Test
    void actualizar_idInexistente_devuelveVacioYNoCreaNada() {
        // Arrange
        Avistamiento cambios = colibri();
        // Act
        Optional<Avistamiento> actualizado = servicio.actualizar(9999L, cambios);
        // Assert
        assertTrue(actualizado.isEmpty());
        assertEquals(0, repositorio.cantidad());
    }

    @Test
    void actualizar_datosInvalidosEnIdInexistente_lanzaExcepcionAntesDeBuscar() {
        // Arrange: el id no existe, pero los datos son inválidos (400 antes que 404)
        Avistamiento invalido = avistamiento("ab", "Chicaque", "2026-10-11", "Pablo");
        // Act + Assert
        assertThrows(AvistamientoInvalidoException.class, () -> servicio.actualizar(9999L, invalido));
    }

    @Test
    void actualizar_datosInvalidos_noCambiaElRegistroGuardado() {
        // Arrange
        Avistamiento creado = servicio.crear(colibri());
        Avistamiento invalido = avistamiento("ab", "Bosque", "2026-10-12", "Ana");
        // Act
        assertThrows(AvistamientoInvalidoException.class, () -> servicio.actualizar(creado.getId(), invalido));
        // Assert
        assertEquals("colibri", servicio.ver(creado.getId()).orElseThrow().getEspecie());
    }

    // ---------- eliminar ----------

    @Test
    void eliminar_idExistente_devuelveTrueYYaNoSeEncuentra() {
        // Arrange
        Avistamiento creado = servicio.crear(colibri());
        // Act
        boolean eliminado = servicio.eliminar(creado.getId());
        // Assert
        assertTrue(eliminado);
        assertTrue(servicio.ver(creado.getId()).isEmpty());
    }

    @Test
    void eliminar_idInexistente_devuelveFalseYNoTocaLoDemas() {
        // Arrange
        servicio.crear(colibri());
        // Act
        boolean eliminado = servicio.eliminar(9999L);
        // Assert
        assertFalse(eliminado);
        assertEquals(1, repositorio.cantidad());
    }
}
