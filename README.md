
# Sistema de Pedidos — Spring Boot

Proyecto académico desarrollado para el curso de Desarrollo de Aplicaciones Empresariales Avanzado.

Esta aplicación permite gestionar pedidos e incorpora una funcionalidad de anulación que se comunica mediante HTTP con un servicio de inventario desarrollado en Jakarta EE.

## Tecnologías utilizadas

- Java 21
- Spring Boot
- Spring Web
- Spring Data JPA
- Spring RestClient
- H2 Database
- Maven
- JUnit 5
- Mockito
- Postman

## Arquitectura

```text
Cliente / Postman
       |
       | HTTP + JSON
       v
AnulacionPedidoController
       |
       v
AnulacionPedidoServiceImpl
       |
       +----> PedidoRepository
       |           |
       |           v
       |          H2
       |
       +----> AnulacionPedidoRepository
       |           |
       |           v
       |          H2
       |
       +----> DevolucionStockRestClient
                    |
                    | HTTP POST
                    v
             inventario-service
                (Jakarta EE)
                    |
                    v
                SQL Server
```

## Funcionalidades

- Registro y consulta de pedidos.
- Integración HTTP con el servicio de inventario.
- Anulación de pedidos existentes.
- Devolución de unidades al inventario.
- Registro de auditoría de anulaciones.
- Prevención de anulaciones duplicadas.
- Manejo de errores cuando el inventario no está disponible.

## Estados del pedido

Los pedidos utilizan dos estados:

- `REGISTRADO`: pedido activo.
- `ANULADO`: pedido cancelado cuya devolución de stock fue confirmada.

## API de anulaciones

URL base:

`http://localhost:8081/sistema-pedidos/api/v1/anulaciones`

### Registrar una anulación

**Método:** POST

**Endpoint:** `/sistema-pedidos/api/v1/anulaciones`

Body JSON:

```json
{
  "pedidoId": 1,
  "motivo": "Cancelación solicitada por el cliente"
}
```

**Respuesta exitosa:** HTTP 201 Created.

La operación devuelve las unidades al inventario y registra la anulación en la base de datos H2.

### Listar anulaciones

**Método:** GET

**Endpoint:** `/sistema-pedidos/api/v1/anulaciones`

**Respuesta exitosa:** HTTP 200 OK.

Devuelve los registros de anulaciones almacenados.

## Validaciones y manejo de errores

| Situación | Código HTTP |
|---|---|
| Anulación correcta | 201 |
| Pedido inexistente | 404 |
| Pedido ya anulado | 409 |
| Servicio de inventario no disponible | 503 |
| Error de comunicación o respuesta del inventario | 502 o 503, según el caso |

No se registra la anulación local cuando falla la devolución de stock.

## Integración entre aplicaciones

El sistema de pedidos se ejecuta en el puerto `8081` y el servicio de inventario en el puerto `8082`.

La integración utiliza `RestClient` para enviar una solicitud HTTP POST al endpoint:

`/inventario-service/api/inventario/productos/codigo/{codigo}/devoluciones`

La devolución se confirma antes de guardar el estado `ANULADO` y el registro de auditoría.

Las transacciones de H2 y SQL Server son independientes. Por ello, la solución no garantiza atomicidad distribuida si ocurre un fallo después de confirmar la devolución remota.

En un sistema productivo se recomienda implementar idempotencia y mecanismos de recuperación o compensación.

## Pruebas automatizadas

Se implementaron tres pruebas unitarias nuevas utilizando JUnit 5 y Mockito:

1. `debeAnularPedidoCorrectamente()`: verifica la anulación y la devolución de stock.
2. `noDebePermitirAnulacionDuplicada()`: comprueba que un pedido anulado no pueda procesarse nuevamente.
3. `noDebeAnularSiInventarioFalla()`: verifica que no se guarden cambios locales si falla el inventario.

Se mantienen las pruebas originales del proyecto.

Para ejecutar las pruebas:

```bash
mvn clean test
```

También se pueden ejecutar desde IntelliJ IDEA:

`Maven → Lifecycle → test`

## Ejecución

### Requisitos

- JDK 21
- Maven o IntelliJ IDEA con soporte Maven
- Servicio `inventario-service` disponible en el puerto 8082
- Base de datos SQL Server configurada para el servicio de inventario

### Iniciar el sistema

1. Iniciar SQL Server.
2. Iniciar WildFly y desplegar `inventario-service`.
3. Ejecutar la aplicación Spring Boot desde IntelliJ.
4. Verificar que Spring Boot utilice el puerto 8081.
5. Probar los endpoints mediante Postman.

## Evidencias funcionales

Se verificaron los siguientes escenarios:

- Consulta de anulaciones: HTTP 200.
- Anulación válida: HTTP 201.
- Anulación duplicada: HTTP 409.
- Pedido inexistente: HTTP 404.
- Inventario no disponible: HTTP 503.

## Consideraciones finales

La solución demuestra la integración entre Spring Boot y Jakarta EE mediante servicios REST, utilizando bases de datos independientes y validaciones para mantener la consistencia de los pedidos y del inventario.
