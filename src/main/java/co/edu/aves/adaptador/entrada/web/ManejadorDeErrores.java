package co.edu.aves.adaptador.entrada.web;

import co.edu.aves.aplicacion.dominio.AvistamientoInvalidoException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Parte del adaptador web: traduce la excepción del dominio a una respuesta HTTP 400. */
@RestControllerAdvice
public class ManejadorDeErrores {

    @ExceptionHandler(AvistamientoInvalidoException.class)
    public ResponseEntity<Map<String, String>> manejar(AvistamientoInvalidoException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("campo", e.getCampo(), "mensaje", e.getMessage()));
    }
}
