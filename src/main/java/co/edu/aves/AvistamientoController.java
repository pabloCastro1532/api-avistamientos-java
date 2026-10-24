package co.edu.aves;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/avistamientos")
public class AvistamientoController {

    private final AvistamientoRepository repo;
    private final ValidadorDeAvistamiento validador;

    public AvistamientoController(AvistamientoRepository repo, ValidadorDeAvistamiento validador) {
        this.repo = repo;
        this.validador = validador;
    }

    // GET /avistamientos -> 200 con la lista
    @GetMapping
    public List<Avistamiento> listar() {
        return repo.findAll();
    }

    // GET /avistamientos/resumen -> 200 con el conteo por especie (bonus)
    @GetMapping("/resumen")
    public Map<String, Long> resumen() {
        return repo.findAll().stream().collect(
                Collectors.groupingBy(Avistamiento::getEspecie, TreeMap::new, Collectors.counting()));
    }

    // GET /avistamientos/{id} -> 200 o 404
    @GetMapping("/{id}")
    public ResponseEntity<Avistamiento> ver(@PathVariable Long id) {
        return repo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /avistamientos -> 201 o 400
    @PostMapping
    public ResponseEntity<Avistamiento> crear(@RequestBody Avistamiento nuevo) {
        validador.validar(nuevo);
        nuevo.setId(null);
        Avistamiento guardado = repo.save(nuevo);
        return ResponseEntity
                .created(URI.create("/avistamientos/" + guardado.getId()))
                .body(guardado);
    }

    // PUT /avistamientos/{id} -> 200, 404 o 400 (se valida primero: 400 antes que 404)
    @PutMapping("/{id}")
    public ResponseEntity<Avistamiento> actualizar(@PathVariable Long id,
                                                   @RequestBody Avistamiento datos) {
        validador.validar(datos);
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        datos.setId(id);
        return ResponseEntity.ok(repo.save(datos));
    }

    // DELETE /avistamientos/{id} -> 204 o 404
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        if (!repo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        repo.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}