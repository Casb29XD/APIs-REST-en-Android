# API REST de medicamentos y farmacias

La aplicación consulta directamente la API REST de Supabase (PostgREST) mediante solicitudes HTTP GET. Supabase devuelve colecciones JSON; la app las convierte a modelos Kotlin y las muestra en Compose. Las consultas usan la clave pública configurada en el proyecto y dependen de que las políticas RLS permitan la lectura anónima.

## Operaciones utilizadas

1. **Listado paginado de medicamentos**  
   `GET /rest/v1/medicamentos?select=*&limit={limit}&offset={offset}&order=nombre_comercial.asc`  
   Usa `limit` y `offset` para cargar páginas conforme el usuario se acerca al final de la lista.

2. **Búsqueda por medicamento**  
   `GET /rest/v1/medicamentos?select=*&or=(nombre_comercial.ilike.*{texto}*,principio_activo.ilike.*{texto}*)&limit={limit}&offset={offset}&order=nombre_comercial.asc`  
   Busca el texto en el nombre comercial o el principio activo. Si hay un laboratorio seleccionado, se agrega `laboratorio=ilike.*{laboratorio}*`.

3. **Filtro por laboratorio**  
   `GET /rest/v1/medicamentos?select=*&laboratorio=ilike.*{laboratorio}*&limit={limit}&offset={offset}&order=nombre_comercial.asc`  
   Filtra el catálogo por laboratorio y conserva la paginación.

4. **Detalle de medicamento e inventario por farmacia**  
   `GET /rest/v1/medicamentos?id=eq.{uuid}&select=*,inventario(*,farmacias(*))`  
   Obtiene un medicamento por UUID y expande sus filas relacionadas de inventario y farmacia.

5. **Medicamentos de una farmacia**  
   `GET /rest/v1/inventario?id_farmacia=eq.{uuid}&select=id_farmacia,stock_actual,precio_unitario,medicamentos(id,nombre_comercial,principio_activo,concentracion,presentacion,laboratorio,imagen_url)`  
   Se ejecuta al tocar el nombre de una farmacia en el detalle. Devuelve los medicamentos asociados a esa sucursal junto con el stock y el precio unitario.

## Respuestas y manejo de errores

Las respuestas de PostgREST son JSON. En lecturas, el éxito normalmente es una lista JSON con estado **200 OK**; si no hay filas se muestra un estado vacío. **204 No Content** también se interpreta como una lista vacía. PostgREST puede responder `200 []` en consultas sin resultados, por lo que ambos casos tienen el mismo mensaje visual: “No se encontraron resultados” o “Esta farmacia no tiene medicamentos registrados”.

| HTTP | Significado | Mensaje/acción en la app |
| --- | --- | --- |
| 200 | Consulta correcta | Se muestran las filas recibidas. |
| 204 | Consulta correcta sin contenido | Se presenta el estado vacío, sin tratarlo como error. |
| 400 | Consulta o filtro inválido | “La consulta enviada a Supabase no es válida.” |
| 401 | Clave API no aceptada | “Supabase rechazó la clave de API.” |
| 403 | Acceso denegado, por ejemplo por RLS | Se solicita revisar permisos y políticas de lectura. |
| 404 | Recurso, tabla o relación no encontrada | Se informa que no se encontró el endpoint, tabla o relación. |
| 409 | Conflicto con los datos | Se informa que la solicitud entra en conflicto con los datos actuales. |
| 429 | Límite de solicitudes alcanzado | Se indica que se debe intentar más tarde. |
| 500–599 | Error del servidor | Se informa del error interno y se puede reintentar. |
| Sin respuesta de red | Dispositivo sin conexión o fallo de transporte | Se pide comprobar la conexión e intentar de nuevo. |

Cuando Supabase envía un cuerpo de error, la app conserva el mensaje genérico y añade el detalle recibido para facilitar el diagnóstico. Los fallos HTTP no se confunden con una respuesta vacía.
