
package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioClientException;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import com.isil.edu.pe.spedidos.service.AnulacionPedidoService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@Service
public class AnulacionPedidoServiceImpl implements AnulacionPedidoService {

    private final PedidoRepository pedidoRepository;
    private final AnulacionPedidoRepository anulacionRepository;
    private final DevolucionStockClient devolucionStockClient;

    public AnulacionPedidoServiceImpl(
            PedidoRepository pedidoRepository,
            AnulacionPedidoRepository anulacionRepository,
            DevolucionStockClient devolucionStockClient) {

        this.pedidoRepository = pedidoRepository;
        this.anulacionRepository = anulacionRepository;
        this.devolucionStockClient = devolucionStockClient;
    }

    @Override
    @Transactional
    public AnulacionPedido anularPedido(Long pedidoId, String motivo) {

        if (pedidoId == null || pedidoId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "El ID del pedido debe ser válido."
            );
        }

        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "El pedido no existe."
                ));

        if (pedido.getEstado() == PedidoEstado.ANULADO
                || anulacionRepository.existsByPedidoId(pedidoId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "El pedido ya fue anulado."
            );
        }

        DevolucionStockResponse respuesta;

        try {
            respuesta = devolucionStockClient.devolverStock(
                    pedido.getProductoCodigo(),
                    pedido.getCantidad()
            );
        } catch (InventarioClientException e) {
            if (e.getStatus() >= 500) {
                throw new InventarioNoDisponibleException(
                        "El servicio de inventario presentó un error.",
                        e
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Inventario rechazó la devolución: " + e.getMessage(),
                    e
            );
        }

        if (respuesta == null
                || respuesta.cantidadDevuelta() != pedido.getCantidad()
                || !pedido.getProductoCodigo().equals(respuesta.codigo())) {

            throw new InventarioNoDisponibleException(
                    "La respuesta de inventario es inválida.",
                    new IllegalStateException("Respuesta inconsistente")
            );
        }

        AnulacionPedido anulacion = new AnulacionPedido(
                pedido.getId(),
                pedido.getProductoCodigo(),
                pedido.getCantidad(),
                motivo
        );

        pedido.anular();

        AnulacionPedido guardada = anulacionRepository.save(anulacion);
        pedidoRepository.save(pedido);

        return guardada;
    }

    @Override
    @Transactional(readOnly = true)
    public List<AnulacionPedido> listarAnulaciones() {
        return anulacionRepository.findAll();
    }
}
