
package com.isil.edu.pe.spedidos.client;

import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;

public interface DevolucionStockClient {

    DevolucionStockResponse devolverStock(String codigo, int cantidad);

}
