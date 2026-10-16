
package com.isil.edu.pe.spedidos.client;

import com.isil.edu.pe.spedidos.client.dto.DevolucionStockRequest;
import com.isil.edu.pe.spedidos.client.dto.DevolucionStockResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

@Component
public class DevolucionStockRestClient implements DevolucionStockClient {

    private final RestClient restClient;
    private final String baseUrl;

    public DevolucionStockRestClient(
            @Qualifier("inventarioHttpClient") RestClient restClient,
            @Value("${inventario.api.base-url}") String baseUrl) {

        this.restClient = restClient;
        this.baseUrl = baseUrl;
    }

    @Override
    public DevolucionStockResponse devolverStock(String codigo, int cantidad) {

        DevolucionStockRequest request =
                new DevolucionStockRequest(cantidad);

        try {
            DevolucionStockResponse respuesta = restClient
                    .post()
                    .uri(baseUrl + "/productos/codigo/{codigo}/devoluciones", codigo)
                    .body(request)
                    .retrieve()
                    .body(DevolucionStockResponse.class);

            if (respuesta == null) {
                throw new InventarioNoDisponibleException(
                        "El servicio de inventario devolvió una respuesta vacía.",
                        new IllegalStateException("Respuesta HTTP sin cuerpo")
                );
            }

            return respuesta;

        } catch (RestClientResponseException e) {
            throw new InventarioClientException(
                    e.getStatusCode().value(),
                    "Error al devolver stock. Inventario respondió HTTP "
                            + e.getStatusCode().value()
            );

        } catch (ResourceAccessException e) {
            throw new InventarioNoDisponibleException(
                    "No se pudo conectar con el servicio de inventario.",
                    e
            );
        }
    }
}
