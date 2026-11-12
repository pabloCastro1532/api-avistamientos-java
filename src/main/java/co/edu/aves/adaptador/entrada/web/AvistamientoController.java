package co.edu.aves.adaptador.entrada.web;

import co.edu.aves.aplicacion.dominio.Avistamiento;
import co.edu.aves.aplicacion.puerto.entrada.GestionDeAvistamientos;
import java.net.URI;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Adaptador de entrada (lado del usuario). Solo traduce HTTP a llamadas al puerto de entrada
 * y el resultado a códigos de estado. No valida ni guarda: eso es del núcleo.
 */
@RestController
@RequestMapping("/avistamientos")
public class AvistamientoController {

    private final GestionDeAvistamientos casosDeUso;

    public AvistamientoController(GestionDeAvistamientos casosDeUso) {
        this.casosDeUso = casosDeUso;
    }

    // GET /avistamientos -> 200 con la lista
    @GetMapping
    public List<Avistamiento> listar() {
        return casosDeUso.listar();
    }

    // GET /avistamientos/resumen -> 200 con el conteo por especie
    @GetMapping("/resumen")
    public Map<String, Long> resumen() {
        return casosDeUso.resumen();
    }

    // GET /avistamientos/{id} -> 200 o 404
    @GetMapping("/{id}")
    public ResponseEntity<Avistamiento> ver(@PathVariable Long id) {
        return casosDeUso.ver(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /avistamientos -> 201 o 400
    @PostMapping
    public ResponseEntity<Avistamiento> crear(@RequestBody Avistamiento nuevo) {
        Avistamiento guardado = casosDeUso.crear(nuevo);
        return ResponseEntity
                .created(URI.create("/avistamientos/" + guardado.getId()))
                .body(guardado);
    }

    // PUT /avistamientos/{id} -> 200, 404 o 400 (el núcleo valida primero: 400 antes que 404)
    @PutMapping("/{id}")
    public ResponseEntity<Avistamiento> actualizar(@PathVariable Long id,
                                                   @RequestBody Avistamiento datos) {
        return casosDeUso.actualizar(id, datos)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // DELETE /avistamientos/{id} -> 204 o 404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        return casosDeUso.eliminar(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
