
package com.isil.edu.pe.spedidos.repository;

import com.isil.edu.pe.spedidos.entity.AnulacionPedido;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnulacionPedidoRepository
        extends JpaRepository<AnulacionPedido, Long> {

    boolean existsByPedidoId(Long pedidoId);

}
