package co.edu.aves;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

/**
 * Pruebas de integración: levantan la aplicación completa (controlador, validador, JPA, H2 en memoria)
 * y la llaman por HTTP con MockMvc. No hay dobles: se prueba que las piezas funcionan juntas.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AvistamientoApiIntegrationTest {

    private static final int ID_QUE_NO_EXISTE = 9999;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AvistamientoRepository repo;

    // Independencia: cada prueba empieza con la tabla vacía y prepara sus propios datos.
    @BeforeEach
    void limpiarBaseDeDatos() {
        repo.deleteAll();
    }

    private Avistamiento guardar(String especie, String lugar, String fecha, String observador) {
        Avistamiento a = new Avistamiento();
        a.setEspecie(especie);
        a.setLugar(lugar);
        a.setFecha(fecha);
        a.setObservador(observador);
        return repo.save(a);
    }

    private static String json(String especie, String lugar, String fecha, String observador) {
        return "{\"especie\":\"" + especie + "\",\"lugar\":\"" + lugar
                + "\",\"fecha\":\"" + fecha + "\",\"observador\":\"" + observador + "\"}";
    }

    // ---------- GET /avistamientos ----------

    @Test
    void listar_sinDatos_devuelve200YListaVacia() throws Exception {
        // Arrange: la tabla está vacía (@BeforeEach)
        // Act
        ResultActions respuesta = mockMvc.perform(get("/avistamientos"));
        // Assert
        respuesta.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void listar_conDosAvistamientos_devuelve200ConLosDos() throws Exception {
        // Arrange
        guardar("colibri", "Chicaque", "2026-10-11", "Pablo");
        guardar("tucan", "Bosque", "2026-10-12", "Ana");
        // Act
        ResultActions respuesta = mockMvc.perform(get("/avistamientos"));
        // Assert
        respuesta.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.especie=='colibri')]").exists())
                .andExpect(jsonPath("$[?(@.especie=='tucan')]").exists());
    }

    // ---------- GET /avistamientos/{id} ----------

    @Test
    void ver_idExistente_devuelve200ConTodosLosCampos() throws Exception {
        // Arrange
        Avistamiento guardado = guardar("colibri", "Chicaque", "2026-10-11", "Pablo");
        // Act
        ResultActions respuesta = mockMvc.perform(get("/avistamientos/{id}", guardado.getId()));
        // Assert
        respuesta.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId().intValue()))
                .andExpect(jsonPath("$.especie").value("colibri"))
                .andExpect(jsonPath("$.lugar").value("Chicaque"))
                .andExpect(jsonPath("$.fecha").value("2026-10-11"))
                .andExpect(jsonPath("$.observador").value("Pablo"));
    }

    @Test
    void ver_idInexistente_devuelve404() throws Exception {
        // Arrange: la tabla está vacía
        // Act
        ResultActions respuesta = mockMvc.perform(get("/avistamientos/{id}", ID_QUE_NO_EXISTE));
        // Assert
        respuesta.andExpect(status().isNotFound());
    }

    // ---------- POST /avistamientos ----------

    @Test
    void crear_avistamientoValido_devuelve201ConCuerpo_yUnGetPosteriorLoEncuentra() throws Exception {
        // Arrange
        String cuerpo = json("colibri", "Chicaque", "2026-10-11", "Pablo");
        // Act
        ResultActions respuesta = mockMvc.perform(post("/avistamientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
        // Assert: respuesta
        respuesta.andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.especie").value("colibri"))
                .andExpect(jsonPath("$.lugar").value("Chicaque"))
                .andExpect(jsonPath("$.fecha").value("2026-10-11"))
                .andExpect(jsonPath("$.observador").value("Pablo"));
        // Assert: efecto (el nuevo id se puede consultar y quedó una sola fila)
        String ubicacion = respuesta.andReturn().getResponse().getHeader("Location");
        mockMvc.perform(get(ubicacion))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.especie").value("colibri"));
        assertEquals(1, repo.count());
    }

    @Test
    void crear_especieDe2Caracteres_devuelve400ConCampoEspecie_yNoGuarda() throws Exception {
        // Arrange
        String cuerpo = json("ab", "Chicaque", "2026-10-11", "Pablo");
        // Act
        ResultActions respuesta = mockMvc.perform(post("/avistamientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
        // Assert
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("especie"))
                .andExpect(jsonPath("$.mensaje").value("La especie debe tener entre 3 y 50 caracteres"));
        assertEquals(0, repo.count());
    }

    @Test
    void crear_fechaFutura_devuelve400ConCampoFecha_yNoGuarda() throws Exception {
        // Arrange (2999 siempre es futuro, sin depender del día en que se corra la prueba)
        String cuerpo = json("colibri", "Chicaque", "2999-01-01", "Pablo");
        // Act
        ResultActions respuesta = mockMvc.perform(post("/avistamientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
        // Assert
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("fecha"))
                .andExpect(jsonPath("$.mensaje").value("La fecha no puede ser posterior a hoy"));
        assertEquals(0, repo.count());
    }

    @Test
    void crear_conFaltantes_devuelve400ConElPrimerCampoQueFalla() throws Exception {
        // Arrange: solo viene la especie
        String cuerpo = "{\"especie\":\"colibri\"}";
        // Act
        ResultActions respuesta = mockMvc.perform(post("/avistamientos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo));
        // Assert
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("fecha"));
        assertEquals(0, repo.count());
    }

    // ---------- PUT /avistamientos/{id} ----------

    @Test
    void actualizar_idExistente_devuelve200ConLosCambios_yUnGetPosteriorLosVe() throws Exception {
        // Arrange
        Avistamiento guardado = guardar("colibri", "Chicaque", "2026-10-11", "Pablo");
        String cambios = json("colibri", "Bosque", "2026-10-12", "Pablo");
        // Act
        ResultActions respuesta = mockMvc.perform(put("/avistamientos/{id}", guardado.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cambios));
        // Assert: respuesta
        respuesta.andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(guardado.getId().intValue()))
                .andExpect(jsonPath("$.lugar").value("Bosque"))
                .andExpect(jsonPath("$.fecha").value("2026-10-12"));
        // Assert: efecto
        mockMvc.perform(get("/avistamientos/{id}", guardado.getId()))
                .andExpect(jsonPath("$.lugar").value("Bosque"));
        assertEquals(1, repo.count());
    }

    @Test
    void actualizar_datosInvalidos_devuelve400_yNoCambiaElRegistro() throws Exception {
        // Arrange
        Avistamiento guardado = guardar("colibri", "Chicaque", "2026-10-11", "Pablo");
        String cambios = json("ab", "Bosque", "2026-10-12", "Pablo");
        // Act
        ResultActions respuesta = mockMvc.perform(put("/avistamientos/{id}", guardado.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cambios));
        // Assert
        respuesta.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.campo").value("especie"));
        Avistamiento sinCambios = repo.findById(guardado.getId()).orElseThrow();
        assertEquals("colibri", sinCambios.getEspecie());
        assertEquals("Chicaque", sinCambios.getLugar());
    }

    @Test
    void actualizar_idInexistente_devuelve404() throws Exception {
        // Arrange
        String cambios = json("colibri", "Bosque", "2026-10-12", "Pablo");
        // Act
        ResultActions respuesta = mockMvc.perform(put("/avistamientos/{id}", ID_QUE_NO_EXISTE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(cambios));
        // Assert
        respuesta.andExpect(status().isNotFound());
        assertEquals(0, repo.count());
    }

    // ---------- DELETE /avistamientos/{id} ----------

    @Test
    void eliminar_idExistente_devuelve204_yUnGetPosteriorDa404() throws Exception {
        // Arrange
        Avistamiento guardado = guardar("colibri", "Chicaque", "2026-10-11", "Pablo");
        // Act
        ResultActions respuesta = mockMvc.perform(delete("/avistamientos/{id}", guardado.getId()));
        // Assert: respuesta y efecto
        respuesta.andExpect(status().isNoContent());
        mockMvc.perform(get("/avistamientos/{id}", guardado.getId()))
                .andExpect(status().isNotFound());
        assertEquals(0, repo.count());
    }

    @Test
    void eliminar_idInexistente_devuelve404() throws Exception {
        // Arrange
        guardar("colibri", "Chicaque", "2026-10-11", "Pablo");
        // Act
        ResultActions respuesta = mockMvc.perform(delete("/avistamientos/{id}", ID_QUE_NO_EXISTE));
        // Assert: 404 y el registro existente sigue ahí
        respuesta.andExpect(status().isNotFound());
        assertEquals(1, repo.count());
    }

    // ---------- GET /avistamientos/resumen (extra) ----------

    @Test
    void resumen_cuentaLosAvistamientosPorEspecie_devuelve200() throws Exception {
        // Arrange
        guardar("colibri", "Chicaque", "2026-10-11", "Pablo");
        guardar("colibri", "Bosque", "2026-10-12", "Ana");
        guardar("tucan", "Selva", "2026-10-13", "Luis");
        // Act
        ResultActions respuesta = mockMvc.perform(get("/avistamientos/resumen"));
        // Assert
        respuesta.andExpect(status().isOk())
                .andExpect(jsonPath("$.colibri").value(2))
                .andExpect(jsonPath("$.tucan").value(1));
    }
}
