package co.edu.aves;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

class ValidadorDeAvistamientoTest {

    // "Hoy" fijo: el RelojFijo es un stub, así las pruebas dan igual hoy que dentro de un mes.
    private static final LocalDate HOY = LocalDate.of(2026, 10, 23);

    private ValidadorDeAvistamiento validador;

    @BeforeEach
    void prepararValidador() {
        validador = new ValidadorDeAvistamiento(new RelojFijo(HOY));
    }

    private static Avistamiento avistamientoValido() {
        Avistamiento a = new Avistamiento();
        a.setEspecie("colibri");
        a.setLugar("Chicaque");
        a.setFecha("2026-10-11");
        a.setObservador("Pablo");
        return a;
    }

    // ---------- Especie: partición (corta / válida / larga / vacía) y límites 2-3 y 50-51 ----------

    @Test
    void avistamientoCompletoYValido_noLanzaExcepcion() {
        // Arrange
        Avistamiento a = avistamientoValido();
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        assertDoesNotThrow(validar);
    }

    @Test
    void especieDe2Caracteres_seRechazaEnElCampoEspecie() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setEspecie("ab");
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        AvistamientoInvalidoException e = assertThrows(AvistamientoInvalidoException.class, validar);
        assertEquals("especie", e.getCampo());
        assertEquals("La especie debe tener entre 3 y 50 caracteres", e.getMessage());
    }

    @Test
    void especieDe3Caracteres_seAcepta() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setEspecie("abc");
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        assertDoesNotThrow(validar);
    }

    @Test
    void especieDe50Caracteres_seAcepta() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setEspecie("a".repeat(50));
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        assertDoesNotThrow(validar);
    }

    @Test
    void especieDe51Caracteres_seRechazaEnElCampoEspecie() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setEspecie("a".repeat(51));
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        AvistamientoInvalidoException e = assertThrows(AvistamientoInvalidoException.class, validar);
        assertEquals("especie", e.getCampo());
    }

    @Test
    void especieSoloConEspacios_seRechazaEnElCampoEspecie() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setEspecie("     ");
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        AvistamientoInvalidoException e = assertThrows(AvistamientoInvalidoException.class, validar);
        assertEquals("especie", e.getCampo());
    }

    @Test
    void especieNula_seRechazaEnElCampoEspecie() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setEspecie(null);
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        AvistamientoInvalidoException e = assertThrows(AvistamientoInvalidoException.class, validar);
        assertEquals("especie", e.getCampo());
    }

    // ---------- Fecha: partición (pasada / hoy / futura / formato / inexistente / vacía), límite hoy-mañana ----------

    @Test
    void fechaDeHoy_seAcepta() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setFecha(HOY.toString());
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        assertDoesNotThrow(validar);
    }

    @Test
    void fechaDeAyer_seAcepta() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setFecha(HOY.minusDays(1).toString());
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        assertDoesNotThrow(validar);
    }

    @Test
    void fechaDeManiana_seRechazaEnElCampoFecha() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setFecha(HOY.plusDays(1).toString());
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        AvistamientoInvalidoException e = assertThrows(AvistamientoInvalidoException.class, validar);
        assertEquals("fecha", e.getCampo());
        assertEquals("La fecha no puede ser posterior a hoy", e.getMessage());
    }

    @Test
    void fechaConFormatoDiaMesAnio_seRechazaEnElCampoFecha() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setFecha("11/10/2026");
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        AvistamientoInvalidoException e = assertThrows(AvistamientoInvalidoException.class, validar);
        assertEquals("fecha", e.getCampo());
        assertEquals("La fecha debe tener formato AAAA-MM-DD", e.getMessage());
    }

    @Test
    void fechaQueNoExisteEnElCalendario_seRechazaEnElCampoFecha() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setFecha("2026-02-31");
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        AvistamientoInvalidoException e = assertThrows(AvistamientoInvalidoException.class, validar);
        assertEquals("fecha", e.getCampo());
    }

    @Test
    void fechaVacia_seRechazaEnElCampoFecha() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setFecha("");
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        AvistamientoInvalidoException e = assertThrows(AvistamientoInvalidoException.class, validar);
        assertEquals("fecha", e.getCampo());
    }

    // ---------- Lugar y observador: no vacíos ----------

    @Test
    void lugarVacio_seRechazaEnElCampoLugar() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setLugar("   ");
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        AvistamientoInvalidoException e = assertThrows(AvistamientoInvalidoException.class, validar);
        assertEquals("lugar", e.getCampo());
    }

    @Test
    void observadorNulo_seRechazaEnElCampoObservador() {
        // Arrange
        Avistamiento a = avistamientoValido();
        a.setObservador(null);
        // Act
        Executable validar = () -> validador.validar(a);
        // Assert
        AvistamientoInvalidoException e = assertThrows(AvistamientoInvalidoException.class, validar);
        assertEquals("observador", e.getCampo());
    }
}
