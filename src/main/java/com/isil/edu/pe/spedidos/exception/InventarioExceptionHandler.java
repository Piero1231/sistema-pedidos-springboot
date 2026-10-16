
package com.isil.edu.pe.spedidos.exception;

import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class InventarioExceptionHandler {

    @ExceptionHandler(InventarioNoDisponibleException.class)
    public ResponseEntity<Map<String, Object>> manejarInventarioNoDisponible(
            InventarioNoDisponibleException e) {

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of(
                        "status", 503,
                        "error", "Service Unavailable",
                        "message", e.getMessage()
                ));
    }
}
