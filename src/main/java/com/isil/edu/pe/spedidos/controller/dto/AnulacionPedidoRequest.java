
package com.isil.edu.pe.spedidos.controller.dto;

public record AnulacionPedidoRequest(
        Long pedidoId,
        String motivo
) {
}
