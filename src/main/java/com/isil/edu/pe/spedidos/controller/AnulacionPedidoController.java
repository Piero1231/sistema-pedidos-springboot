
package com.isil.edu.pe.spedidos.controller;

import com.isil.edu.pe.spedidos.controller.dto.AnulacionPedidoRequest;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/anulaciones")
public class AnulacionPedidoController {

    private final AnulacionPedidoService anulacionPedidoService;

    public AnulacionPedidoController(
            AnulacionPedidoService anulacionPedidoService) {
        this.anulacionPedidoService = anulacionPedidoService;
    }

    @PostMapping
    public ResponseEntity<AnulacionPedido> anularPedido(
            @RequestBody AnulacionPedidoRequest request) {

        AnulacionPedido anulacion =
                anulacionPedidoService.anularPedido(
                        request.pedidoId(),
                        request.motivo()
                );

        return ResponseEntity
                .created(URI.create(
                        "/sistema-pedidos/api/v1/anulaciones/"
                                + anulacion.getId()
                ))
                .body(anulacion);
    }

    @GetMapping
    public ResponseEntity<List<AnulacionPedido>> listarAnulaciones() {

        return ResponseEntity.ok(
                anulacionPedidoService.listarAnulaciones()
        );
    }
}
