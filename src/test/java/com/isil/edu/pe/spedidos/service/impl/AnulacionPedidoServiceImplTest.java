
package com.isil.edu.pe.spedidos.service.impl;

import com.isil.edu.pe.spedidos.client.DevolucionStockClient;
import com.isil.edu.pe.spedidos.client.InventarioNoDisponibleException;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import com.isil.edu.pe.spedidos.entity.Pedido;
import com.isil.edu.pe.spedidos.entity.PedidoEstado;
import com.isil.edu.pe.spedidos.repository.AnulacionPedidoRepository;
import com.isil.edu.pe.spedidos.repository.PedidoRepository;
import java.math.BigDecimal;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnulacionPedidoServiceImplTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private AnulacionPedidoRepository anulacionRepository;

    @Mock
    private DevolucionStockClient devolucionStockClient;

    @InjectMocks
    private AnulacionPedidoServiceImpl anulacionService;

    private Pedido pedido;

    @BeforeEach
    void configurar() {
        pedido = new Pedido(
                "Cliente Prueba",
                "LAP-001",
                "Laptop",
                new BigDecimal("2500.00"),
                2,
                new BigDecimal("5000.00")
        );

        pedido.setId(1L);
    }

    @Test
    void debeAnularPedidoCorrectamente() {
        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        when(anulacionRepository.existsByPedidoId(1L))
                .thenReturn(false);

        when(devolucionStockClient.devolverStock("LAP-001", 2))
                .thenReturn(new DevolucionStockResponse(
                        "LAP-001", 2, 10
                ));

        when(anulacionRepository.save(any(AnulacionPedido.class)))
                .thenAnswer(invocacion -> invocacion.getArgument(0));

        AnulacionPedido resultado =
                anulacionService.anularPedido(
                        1L, "Solicitud del cliente"
                );

        assertNotNull(resultado);
        assertEquals(1L, resultado.getPedidoId());
        assertEquals(2, resultado.getCantidadDevuelta());
        assertEquals(PedidoEstado.ANULADO, pedido.getEstado());

        verify(devolucionStockClient)
                .devolverStock("LAP-001", 2);

        verify(anulacionRepository)
                .save(any(AnulacionPedido.class));

        verify(pedidoRepository).save(pedido);
    }

    @Test
    void noDebePermitirAnulacionDuplicada() {
        pedido.setEstado(PedidoEstado.ANULADO);

        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        assertThrows(
                ResponseStatusException.class,
                () -> anulacionService.anularPedido(
                        1L, "Segunda anulación"
                )
        );

        verifyNoInteractions(devolucionStockClient);
        verify(anulacionRepository, never())
                .save(any(AnulacionPedido.class));
    }

    @Test
    void noDebeAnularSiInventarioFalla() {
        when(pedidoRepository.findById(1L))
                .thenReturn(Optional.of(pedido));

        when(anulacionRepository.existsByPedidoId(1L))
                .thenReturn(false);

        when(devolucionStockClient.devolverStock("LAP-001", 2))
                .thenThrow(new InventarioNoDisponibleException(
                        "Inventario no disponible",
                        new RuntimeException("Error de conexión")
                ));

        assertThrows(
                InventarioNoDisponibleException.class,
                () -> anulacionService.anularPedido(
                        1L, "Prueba de error"
                )
        );

        assertEquals(PedidoEstado.REGISTRADO, pedido.getEstado());

        verify(anulacionRepository, never())
                .save(any(AnulacionPedido.class));

        verify(pedidoRepository, never())
                .save(any(Pedido.class));
    }
}
