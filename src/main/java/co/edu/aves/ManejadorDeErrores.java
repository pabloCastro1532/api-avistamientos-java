package co.edu.aves;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ManejadorDeErrores {

    @ExceptionHandler(AvistamientoInvalidoException.class)
    public ResponseEntity<Map<String, String>> manejar(AvistamientoInvalidoException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("campo", e.getCampo(), "mensaje", e.getMessage()));
    }
}
