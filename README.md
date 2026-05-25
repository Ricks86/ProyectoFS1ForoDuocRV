# ProyectoFS1ForoDuocRV

Este repositorio contiene el backend de un sistema de foro, construido sobre una arquitectura de 10 microservicios independientes.

---
## Aspectos Compartidos y Reglas Arquitectónicas

Para mantener la coherencia en todo el ecosistema, todos los microservicios se adhieren a los siguientes estándares y patrones de diseño:

### 1. Persistencia y migración
Cada microservicio cuenta con su base de datos unica e independiente levantada en dockercompose
* **Motor:** MySQL.
* **Migraciones:** El esquema de base de datos inicial para pruebas es dado por **Flyway** mediante scripts `.sql` versionados.
* **Docker:** Cada servicio es levantado mediante una imagen docker `MySQL 8.0` con su propio puerto

### 2. Comunicación Inter-Servicios (OpenFeign)
Toda comunicación sincrónica entre microservicios se realiza a través de clientes declarativos **OpenFeign**.
* **¿Por qué?** El sistema OpenFeign permite trabajar con llamadas Http síncronicas y asíncronicas, así tener mayor control cuando un método debe continuar con o sin respuesta de un método en otro microservicio

### 3. Seguridad y Autorización (JWT)
El ecosistema es *Stateless*. La identidad y autorización se manejan íntegramente a través de **JSON Web Tokens (JWT)**.
* Los tokens son extraídos en la capa de Controladores y propagados a través de las cabeceras HTTP hacia los servicios internos mediante OpenFeign.
* La suplantación de identidad se previene mediante Autorización Basada en Recursos: la capa lógica valida que el ID extraído del Token coincida con el propietario del recurso a modificar.

### 4. Mitigación de Problemas de Rendimiento (Patrón N+1)
En endpoints de lectura masiva (como *Feeds* o *Bandejas de Entrada*), se implementa un patrón de **Caché Local en Memoria** (`HashMap.computeIfAbsent`). Esto asegura que al renderizar listas con múltiples interacciones de un mismo autor, se realice una única petición a la red por usuario, neutralizando el problema N+1 y protegiendo el ancho de banda del servidor.

### 5. Manejo Centralizado de Excepciones
El código permite la visualización de errores mediante `<ResponseEntity>` y logs de `Slf4j` para una mejor visualización en el **Postman** y terminal de **Spring**
### 6. Trazabilidad y Auditoría 
Todo acción crítica dentro del foro (*Entiéndase crear usuario, modificar usuario, publicar post, registro, etc...*) queda almacenada en el microservicio `ms-audit` en donde cada microservicio dispara un hilo a el microservicio (haciendo uso de la interfaz `UserClient` y la clase `AuditService`)

---
## A continuación desglose de los microservicios

##  1. Microservicio de Autenticación (`ms-AuthService`)

**Puerto por defecto:** `8081`

Este microservicio es la puerta de entrada al ecosistema. Su responsabilidad exclusiva es gestionar las credenciales, encriptar las contraseñas, emitir los Tokens JWT y coordinar la creación del perfil público en el microservicio de usuarios. Ningún otro microservicio tiene acceso a las contraseñas.

###  Estructura de Paquetes y Clases Principales

* **`Client/`**
    * `UserClient`: Interfaz OpenFeign que se comunica de forma interna con `ms-User` para inicializar el perfil del usuario recién registrado.
    * `AuditClient`: Interfaz OpenFeign que envía los eventos de registro o inicio de sesión hacia el `ms-Audit`.
*  **`Configuration/`**
    * `SecurityConfig`: Define la cadena de filtros HTTP. Permite acceso público a los endpoints de registro y login.
    * `PasswordConfig`: Aísla el Bean de `BCryptPasswordEncoder` para evitar dependencias circulares.
*  **`Controller/`**
    * `AuthController`: Expone los endpoints REST.
*  **`Model/`** (Entidades y DTOs)
    * `UserAuth`: Entidad JPA mapeada a la tabla `user_auth`.
    * `RegisterRequestDTO` / `LoginRequestDTO`: Clases de entrada.
    * `RegisterResponseDTO` / `TokenResponseDTO`: Contratos de salida.
*  **`Security/`**
    * `JwtService` y`JwtUtil`: Lógica de generación, firma y expiración del JSON Web Token.
*  **`Service/`**
    * `AuthService`: Contiene la lógica de negocio.

###  Lógica de Funcionamiento 

**`register(RegisterRequestDTO)`**
1.  Verifica en la base de datos local que el `username` o `email` no existan.
2.  Encripta la contraseña usando BCrypt y guarda la entidad `UserAuth`.
3.  **Comunicación Externa:** Extrae el ID autogenerado, construye un mapa de datos y utiliza `UserClient` para hacer una petición asíncrona hacia `ms-User` solicitando la creación del perfil público.
4.  Retorna un `RegisterResponseDTO` confirmando el éxito de la operación.

**`login(LoginRequestDTO)`**
1.  Busca al usuario por su `username`.
2.  Utiliza BCrypt para comparar la contraseña plana enviada con el hash guardado en la base de datos.
3.  Si coinciden, llama a `JwtService` para generar un Token que incluye el ID del usuario en su *Payload*.
4.  Retorna un `TokenResponseDTO` que el frontend deberá guardar para futuras peticiones.

---

###  Guía de Endpoints (Pruebas en Postman)

#### 1. Registrar un Nuevo Usuario
Crea una credencial y desencadena la inicialización del perfil.

* **URL:** `POST http://localhost:8081/api/auth/register`
* **Body (JSON):**
    ```json
    {
      "nombreUser": "Usuario_ejemplo",
      "email": "correo@Ejemplo.com",
      "password": "Contraseña_ejemplo3"
    }
    ```

#### 2. Iniciar Sesión (Login)
Valida credenciales y devuelve el Token JWT necesario para navegar por el foro.

* **URL:** `POST http://localhost:8081/api/auth/login`
* **Body (JSON):**
    ```json
    {
      "nombreUser": "Usuario_ejemplo",
      "password": "Contraseña_ejemplo3"
    }
    ```

##  2. Microservicio de Usuarios (`ms-User`)

**Puerto por defecto:** `8082`

Este microservicio gestiona la identidad pública y social dentro del foro.

###  Estructura de Paquetes y Clases 

*  **`Client/`**
    * `AuditClient`: Interfaz OpenFeign encargada de enviar un rastro al `ms-Audit` cada vez que un usuario modifica su perfil público.
*  **`Controller/`**
    * `UserController`: Expone los endpoints REST.
*  **`Model/`** * `UserModel`: Entidad JPA mapeada a la tabla `users`.
    * `UserInitDTO`: Objeto estricto y validado para la creación inicial desde `ms-Auth`.
    * `UserUpdateDTO`: DTO que encapsula los campos permitidos para modificación (bio y avatar).
    * `UserProfileDTO`: DTO de salida para no exponer datos sensibles (como IDs internos) al cliente externo.
*  **`Security/`**
    * `SecurityConfig`: Configuración estricta que permite el paso libre a `/api/users/init` (para que `ms-Auth` pueda comunicarse) y exige autenticación para el resto.
    * `JwtUtil`: Herramienta para extraer el `userId` del token firmado.
*  **`Service/`**
    * `UserService`: Contiene la lógica de negocio.

###  Lógica de Funcionamiento

**`createInitialProfile(UserInitDTO)`**
1. Es invocado internamente (vía HTTP) por el `ms-Auth` milisegundos después de un registro exitoso.
2. Recibe el `authId`, `username` y `email`.
3. Crea un `UserModel` inicializándolo con reputación 0 y lo guarda en la base de datos de usuarios.

**`updateProfile(username, UserUpdateDTO, idUsuarioLogueado)`**
1. Busca al usuario en la base de datos utilizando el `username` proporcionado en la URL.
2. **Barrera de Seguridad:** Compara el `authId` del usuario encontrado con el `idUsuarioLogueado` extraído del Token JWT.
3. Actualiza la biografía y/o el avatar, guarda los cambios y retorna el perfil actualizado.

**`obtenerUsuarioDtoPorId(Long authId)` / `obtenerUsuarioDtoPorUsername(String username)`**
1. Métodos de uso interno para el ecosistema. Son consumidos vía Feign por `ms-Post`, `ms-Comment` y `ms-Messagin`.
2. Su característica arquitectónica clave es que **siempre devuelven el `authId`** en lugar del ID autoincremental local, asegurando la sincronización de identidades en todo el foro.

---

###  Guía de Endpoints 

#### 1. Inicializar Perfil (Uso Interno)
Llamado por `ms-Auth` para crear el perfil público.

* **URL:** `POST http://localhost:8082/api/users/init`
* **Body (JSON):**
    ```json
    {
      "authId": 15,
      "username": "usuario ejemplo",
      "email": "correo@test.com"
    }
    ```

#### 2. Obtener Perfil Público
Consulta los datos públicos de un usuario (Biografía, Reputación, etc.).

* **URL:** `GET http://localhost:8082/api/users/perfil/{username}`
* **Body:** Ninguno.

#### 3. Actualizar Perfil
Modifica la biografía o el avatar del usuario autenticado.

* **URL:** `PUT http://localhost:8082/api/users/actualizar/{username}`
* **Ejemplo:** `PUT http://localhost:8082/api/users/actualizar/DuckyProtocol`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`
* **Body (JSON):**
    ```json
    {
      "bio": "bio de ejemplo",
      "avatarUrl": "link avatar ejemplo"
    }
    ```

##  3. Microservicio de Posts (`ms-PostService`)

**Puerto por defecto:** `8083`

Este microservicio es el motor de contenido principal del foro.

###  Estructura de Paquetes y Clases Principales

*  **`Client/`**
    * `UserClient`: Interfaz OpenFeign que consulta de manera segura los datos del autor (`UserDTO`) por ID o Username en el `ms-User`.
*  **`Controller/`**
    * `PostController`: Expone los endpoints de la API de contenidos.
*  **`Model/`**
    * `Post`: Entidad JPA que mapea la tabla `posts` 
    * `PostCreateDTO`: Valida los requerimientos mínimos de entrada de un nuevo post
    * `PostResponseDTO` / `PostFeedDTO`: Estructuras de salida.
*  **`Repository/`**
    * `PostRepository`: Abstracción de datos con consultas avanzadas para aplicación correcta de logica de negocio en el *service*
*  **`Security/`**
    * `SecurityConfig`: Configuración que restringe todo el árbol de rutas de posts, exigiendo un Token JWT válido para cualquier operación de lectura o escritura.
    * `JwtUtil`: Utilidad encargada de desencriptar el token para identificar al usuario activo.
*  **`Service/`**
    * `PostService`: Lógica de negocio. 
    * `AuditService`: Despacha registros de trazabilidad hacia el `ms-Audit`.

###  Lógica de Funcionamiento

**`crearPost(PostCreateDTO, idUsuarioLogueado, token)`**
1. Recibe el cuerpo de la publicación validado y el ID del usuario extraído de forma segura desde el controlador.
2. Instancia y persiste localmente la entidad `Post`.
3. Invoca sincrónicamente al `ms-User` usando `UserClient` enviando el token de autorización para adjuntar la identidad del autor en la respuesta.

**`getFeedPaginado(Pageable, token)` / `obtenerPostsCreadosAntesDe(...)`**
1. Recupera un lote ordenado de publicaciones desde la base de datos local.
2. **(Caché Local):** Instancia un `HashMap<Long, UserDTO>` temporal en memoria para la duración de la transacción.

---

###  Guía de Endpoints (Pruebas en Postman)

#### 1. Crear una Nueva Publicación
Publica un post dentro de una comunidad específica.

* **URL:** `POST http://localhost:8083/api/posts`
* * **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`
* **Body (JSON):**
    ```json
    {
      "titulo": "titulo ejemplo",
      "contenido": "contenido ejemplo",
      "idComunidad": 1
    }
    ```

#### 2. Obtener el Feed Global Paginado
Recupera las publicaciones de todo el foro ordenadas cronológicamente.

* **URL:** `GET http://localhost:8083/api/posts/feed?page=0&size=10`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`
* **Body:** Ninguno.

#### 3. Filtrar Publicaciones por Username
Obtiene todo el historial de posts escritos por un usuario específico a partir de su alias textual.

* **URL:** `GET http://localhost:8083/api/posts/usuario/{username}`
* **Ejemplo:** `GET http://localhost:8083/api/posts/usuario/DuckyProtocol`
* * **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`
* **Body:** Ninguno.

##  4. Microservicio de Comentarios (`ms-CommentService`)

**Puerto por defecto:** `8084`

Este microservicio gestiona las interacciones de texto que los usuarios realizan sobre las publicaciones. Al igual que el servicio de Posts, no guarda información redundante del autor; almacena únicamente la relación (IDs).
###  Estructura de Paquetes y Clases Principales

*  **`Client/`**
    * `UserClient`: Cliente OpenFeign que requiere el envío del Token JWT para consultar al `ms-User`.
    * `AuditClient`: Cliente OpenFeign para despachar registros de actividad al `ms-Audit`.
*  **`Controller/`**
    * `CommentController`: Intercepta las peticiones REST, extrae el identificador del usuario desde el Token JWT.
*  **`Model/`**
    * `Comment`: Entidad JPA persistida en la tabla `comments` 
    * `CommentCreateDTO`: Objeto de transferencia de entrada que asegura que ningún comentario ingrese vacío a la base de datos.
    * `CommentResponseDTO`: DTO de salida que unifica los datos del comentario con el objeto `UserDTO` del autor.
*  **`Repository/`**
    * `CommentRepository`: Interfaz JPA que incluye métodos de búsqueda ordenados cronológicamente por `postId`.
*  **`Security/`**
    * `SecurityConfig`: Configuración que restringe todo el árbol de rutas de posts, exigiendo un Token JWT válido para cualquier operación de lectura o escritura.
    * `JwtUtil`: Utilidad encargada de desencriptar el token para identificar al usuario activo.
* **`Service/`**
    * `CommentService`: lógica de negocio.

###  Lógica de Funcionamiento 

**`crearComentario(CommentCreateDTO, userIdLogueado, token)`**
1. Recibe el texto del comentario y el ID del post al que pertenece.
2. Construye y guarda la entidad `Comment` en la base de datos local.
3. Llama a un método privado (`obtenerAutor`) que envuelve la petición Feign hacia `ms-User`

**`obtenerComentariosPorPostId(postId, token)`**
1. Busca todos los comentarios asociados a un post específico.
2. **(Caché Local):** Si un post tiene 50 comentarios escritos por 3 personas, el sistema creará un `HashMap<Long, UserDTO>` temporal.

---

###  Guía de Endpoints (Pruebas en Postman)

#### 1. Publicar un Comentario
Agrega una respuesta a una publicación existente.

* **URL:** `POST http://localhost:8084/api/comments`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`
* **Body (JSON):**
    ```json
    {
      "postId": 1,
      "content": "Comentario ejemplo"
    }
    ```

#### 2. Obtener Comentarios de un Post
Recupera el hilo completo de respuestas de una publicación específica.

* **URL:** `GET http://localhost:8084/api/comments/post/{postId}`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`
* **Body:** Ninguno.

##  5. Microservicio de Mensajería Privada (`ms-MessaginService`)

**Puerto por defecto:** `8085`

Este microservicio regula la comunicación directa y privada entre los usuarios del ecosistema. Al igual que las capas de contenido público (posts y comentarios), resuelve de forma dinámica las identidades de los remitentes y destinatarios consumiendo el `ms-User`.

###  Estructura de Paquetes y Clases Principales

*  **`Client/`**
    * `UserClient`: Cliente OpenFeign que requiere el envío del Token JWT para consultar al `ms-User`.
    * `AuditClient`: Cliente OpenFeign para despachar registros de actividad al `ms-Audit`.
*  **`Controller/`**
    * `MessageController`: Expone los endpoints. Extrae el ID del emisor desde el JWT.
*  **`Model/`**
    * `Message`: Entidad JPA ligada a la tabla `messages`.
    * `MessageCreatetDTO`: Estructura de entrada validada para la composición del cuerpo del mensaje.
    * `MessageResponseDTO`: Modelo de salida con los objetos `UserDTO` completos de emisor y receptor.
    * `BandejaItemDTO`: la bandeja de entrada (Último mensaje, contador de no leídos y datos del contacto).
*  **`Repository/`**
    * `MessageRepository`: Contiene consultas personalizadas en JPQL para agrupar las conversaciones y calcular los mensajes pendientes de lectura.
*  **`Security/`**
    * `SecurityConfig`: Configuración que restringe todo el árbol de rutas de posts, exigiendo un Token JWT válido para cualquier operación de lectura o escritura.
    * `JwtUtil`: Utilidad encargada de desencriptar el token para identificar al usuario activo. 
* **`Service/`**
    * `MessageService`: Lógica de negocio.

###  Lógica de Funcionamiento 

**`enviarMensajePorUsername(usernameReceptor, MessageCreatetDTO, idEmisorLogueado, token)`**
1. Recibe el texto del mensaje y el alias del destinatario.
2. Consulta sincrónicamente al `ms-User` para obtener el ID real del receptor a partir de su username.
3. Guarda el registro local con el estado `leido = false` y dispara un log estructurado asíncrono hacia el servicio de auditoría.

**`obtenerBandejaEntrada(idLogueado, token)`**
1. Ejecuta una consulta agregada que agrupa los mensajes donde el usuario participa, extrayendo el conteo de pendientes y la última fecha de interacción.
2. **(Caché Local):** Al recuperar la lista de contactos de la bandeja de entrada, utiliza un `HashMap<Long, UserDTO>` interno. Si un usuario tiene múltiples mensajes pendientes del mismo contacto, el servicio solo viaja una vez por HTTP a buscar su perfil.

**`obtenerConversacion(idLogueado, otroUsuario, token)`**
1. Busca el ID del contacto en el servicio de usuarios.
2. Ejecuta un query de mutación masiva que cambia el estado `leido = true` a todos los mensajes recibidos de ese contacto específico antes de desplegar el historial.
3. Retorna la conversación completa en orden cronológico ascendente.

---

###  Guía de Endpoints (Pruebas en Postman)

#### 1. Enviar Mensaje Privado
Despacha un mensaje a un usuario utilizando su nombre de cuenta.

* **URL:** `POST http://localhost:8085/api/messages/enviar/{usernameReceptor}`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`
* **Body (JSON):**
    ```json
    {
      "contenido": "mensaje ejemplo"
    }
    ```

#### 2. Obtener Bandeja de Entrada
Lista los canales activos de chat del usuario autenticado con contadores de mensajes pendientes.

* **URL:** `GET http://localhost:8085/api/messages/bandeja`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`
* **Body:** Ninguno.

#### 3. Consultar Historial de Conversación
Recupera el chat completo con un usuario específico y marca los mensajes entrantes como leídos automáticamente.

* **URL:** `GET http://localhost:8085/api/messages/conversacion/{otroUsuario}`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`
* **Body:** Ninguno.

## 6. Microservicio de Notificaciones (`ms-Notification`)

**Puerto por defecto:** `8086`

Este microservicio gestiona el sistema de alertas del ecosistema. Su responsabilidad es capturar eventos del sistema (como un nuevo "Like" o un comentario) y transformarlos en notificaciones persistentes que el usuario puede consultar. Es un servicio diseñado para ser consumido tanto internamente por otros microservicios como externamente por el frontend.

###  Estructura de Paquetes y Clases Principales

*  **`Client/`**
   * `UserClient`: Cliente OpenFeign para resolver la identidad del emisor (sender) en tiempo real.
   * `AuditClient`: Cliente Feign para registrar las acciones críticas de lectura de alertas.
*  **`Controller/`**
   * `NotificationController`: Expone endpoints de gestión de alertas.
*  **`Model/`**
   * `NotificationModel`: Entidad persistida en la tabla `notifications`
   * `NotificationCreateDTO`: DTO de entrada. Define la estructura mínima para crear una alerta.
   * `NotificationResponseDTO`: Estructura de salida enriquecida con los datos del perfil del emisor.
*  **`Repository/`**
   * `NotificationRepository`: Métodos de acceso a datos utilizando `recipient_id` (Long) para garantizar la integridad y rendimiento.
*  **`Security/`**
   * `SecurityConfig` y `JwtUtil`: Aseguran que el usuario solo pueda acceder a sus propias notificaciones.
*  **`Service/`**
   * `NotificationService`: Implementa la lógica de persistencia, la resolución de identidades y el patrón de caché local.

###  Lógica de Funcionamiento (Métodos Clave)

**`createNotification(NotificationCreateDTO, serviceOrigin)`**
1. Recibe el DTO y el origen (cabecera `X-Service-Origin`) que disparó la alerta.
2. Construye la entidad `NotificationModel` asignando el estado `is_read = false` por defecto.
3. Persiste el registro y registra un log de auditoría.

**`getUserNotifications(recipientId, token)`**
1. Recupera la lista de notificaciones ordenadas por fecha descendente.
2. **Mitigación N+1:** Utiliza un `HashMap<Long, UserDTO>` como caché local. Si el usuario tiene múltiples notificaciones del mismo remitente, evita realizar consultas HTTP redundantes al `ms-User` reutilizando la información del emisor previamente resuelta.

**`markAsRead(notificationId, idUsuarioLogueado)`**
1. Busca la notificación por ID.
2. **Barrera de Seguridad:** Compara el `recipient_id` de la notificación con el `idUsuarioLogueado` (extraído del token). Si no coinciden, lanza una excepción de acceso denegado (403), evitando que un usuario marque notificaciones ajenas como leídas.

---

###  Guía de Endpoints (Pruebas en Postman)

#### 1. Crear Notificación (Uso Interno)
Endpoint consumido por otros microservicios (Feign).

* **URL:** `POST http://localhost:8086/api/notifications`
   * `X-Service-Origin: ms-service`
* **Body (JSON):**
    ```json
    {
      "recipientId": 15,
      "senderId": 2,
      "type": "LIKE",
      "message": "A ejemplo le gustó tu publicación.",
      "relatedId": 101
    }
    ```

#### 2. Obtener Notificaciones Propias
Devuelve todas las alertas pendientes y leídas del usuario logueado.

* **URL:** `GET http://localhost:8086/api/notifications/mis-notificaciones`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`

#### 3. Marcar como Leída
Cambia el estado de una notificación específica.

* **URL:** `PUT http://localhost:8086/api/notifications/{id}/read`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`

## 7. Microservicio de Comunidades (`ms-Community`)

**Puerto por defecto:** `8087` 

Este microservicio gestiona la creación, listado y membresía de espacios temáticos (comunidades) dentro del foro e implementa un modelo de privacidad mediante códigos de acceso (`communityAccess`).

###  Estructura de Paquetes y Clases Principales

*  **`Client/`**
   * `UserClient`: Cliente OpenFeign para validar y enriquecer los datos públicos de los creadores de cada comunidad.
   * `AuditClient`: Cliente Feign para registrar de manera inmutable la creación de comunidades y la unión de nuevos miembros.
*  **`Controller/`**
   * `CommunityController`: Punto de entrada protegido. Extrae el AuthID numérico (`Long`) desde el JWT, bloqueando cualquier intento de suplantación de identidad mediante inyección en el JSON.
*  **`Model/`**
   * `CommunityModel`: Entidad principal persistida en la tabla `communities`. Incluye una colección secundaria `community_members` (`@ElementCollection`) para gestionar la lista de usuarios unidos.
   * `CommunityCreateDTO` / `CommunityJoinDTO`: Objetos de transferencia que validan estrictamente las entradas del usuario (nombres, descripciones, y códigos de acceso).
   * `CommunityResponseDTO`: DTO de salida que excluye datos sensibles (como el código de acceso) e incluye el perfil renderizado del usuario creador.
*  **`Repository/`**
   * `CommunityRepository`: Interfaz JPA estándar conectada a la base de datos provisionada por Flyway.
*  **`Service/`**
   * `CommunityService`: Lógica de negocio.

###  Lógica de Funcionamiento 

1. **Auto-Membresía:** Al crear una comunidad, el sistema inyecta automáticamente el ID del creador en la tabla `community_members` y establece el contador en 1.
2. **Control de Acceso Cerrado:** Para que un nuevo usuario ingrese a una comunidad mediante el endpoint `/join`, debe proporcionar un código de acceso exacto. El servicio verifica que el código coincida y que el usuario no sea ya miembro para evitar duplicidad.
3. **Lectura rápida:** Al consultar todas las comunidades disponibles (`getAllCommunities`), el servicio utiliza un mapa (`HashMap`) para almacenar en memoria RAM los perfiles de creadores ya consultados. Esto asegura que, si un mismo administrador creó 10 comunidades, solo se ejecute 1 llamada HTTP hacia `ms-User`.

---

###  Guía de Endpoints (Pruebas en Postman)

#### 1. Crear una Comunidad Privada
Establece un nuevo espacio. El ID del creador se extrae de forma transparente desde el Token.

* **URL:** `POST http://localhost:8087/api/communities`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`
* **Body (JSON):**
    ```json
    {
      "name": "Desarrolladores Spring Boot",
      "description": "Comunidad exclusiva para discutir arquitectura y microservicios.",
      "communityAccess": "SPRING2026"
    }
    ```

#### 2. Unirse a una Comunidad
Permite a un usuario autenticado ingresar a una comunidad utilizando su código secreto.

* **URL:** `POST http://localhost:8087/api/communities/{communityId}/join`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`
* **Body (JSON):**
    ```json
    {
      "communityAccess": "SPRING2026"
    }
    ```

#### 3. Listar Todas las Comunidades
Devuelve el catálogo de comunidades.

* **URL:** `GET http://localhost:8087/api/communities`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`

##  8. Microservicio de Reportes (`ms-Report`)

**Puerto por defecto:** `8088`

Este microservicio es el pilar de la moderación dentro del ecosistema. Permite a los usuarios denunciar contenido (Posts, Comentarios o Usuarios) que infrinja las normas de la comunidad. Su arquitectura está blindada para evitar que los reportes sean manipulados, garantizando que la identidad del usuario que reporta sea siempre validada mediante su token JWT y nunca basada en entradas de texto manipulables desde el cliente.

###  Estructura de Paquetes y Clases Principales

*  **`Client/`**
   * `UserClient`: Interfaz OpenFeign que resuelve los datos del usuario que reporta (`UserDTO`) para presentar una moderación clara.
   * `AuditClient`: Cliente Feign para dejar constancia inmutable de cada creación de reporte y resolución.
*  **`Controller/`**
   * `ReportController`: Gestiona los endpoints de moderación. Extrae el `reporterId` directamente del Token JWT, eliminando la posibilidad de suplantación en el reporte.
*  **`Model/`**
   * `ReportModel`: Entidad JPA (`reports`). Almacena el `reporter_id`, el tipo de entidad (POST, COMMENT, USER) y su ID relacionado, junto con el estado del ticket.
   * `ReportCreateDTO`: DTO de entrada. Valida que el motivo del reporte no sea nulo.
   * `ReportResponseDTO`: Estructura de salida que unifica el estado del reporte con la información pública del usuario que lo generó.
*  **`Repository/`**
   * `ReportRepository`: Interfaz JPA optimizada para filtrar reportes por estado (`PENDING` vs `RESOLVED`).
*  **`Security/`**
   * `SecurityConfig` y `JwtUtil`: Aseguran que solo usuarios autenticados puedan levantar reportes, y que los endpoints de gestión estén protegidos.
*  **`Service/`**
   * `ReportService`: Orquestador. Implementa el patrón de caché local para evitar saturar al `ms-User` cuando el moderador carga el panel de reportes masivos.

###  Lógica de Funcionamiento

**`createReport(ReportCreateDTO, reporterIdLogueado, token)`**
1. Recibe el objeto con la entidad reportada y el motivo.
2. Asigna el usuario mediante el token al momento de crear el report
3. Asigna por defecto el estado `PENDING`.
4. Dispara un log hacia `ms-Audit` indicando qué entidad está bajo sospecha.

**`getReportsByStatus(status, token)`**
1. Consulta todos los tickets según su estado (ej. "PENDING").
2. Utiliza un `HashMap<Long, UserDTO>` como caché local para resolver los datos de los usuarios reportantes, asegurando que si un usuario ha hecho múltiples reportes, los datos de su perfil solo se consulten una vez vía red.

---

###  Guía de Endpoints (Pruebas en Postman)

#### 1. Crear un Reporte
Permite a un usuario denunciar contenido.

* **URL:** `POST http://localhost:8088/api/reports/create`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`
* **Body (JSON):**
    ```json
    {
      "reportedEntityType": "POST",
      "reportedEntityId": 101,
      "reason": "Contenido ofensivo que rompe las reglas de convivencia."
    }
    ```

#### 2. Cambiar Estado del Reporte
Permite a un moderador marcar un reporte como resuelto.

* **URL:** `PUT http://localhost:8088/api/reports/{id}/resolve`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`

#### 3. Consultar Reportes por Estado
Obtiene la lista de reportes según su estado (ej. "PENDING" para moderación).

* **URL:** `GET http://localhost:8088/api/reports/status/{status}`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`

## 9. Microservicio de Interacciones (`ms-Interaction`)

**Puerto por defecto:** `8089` (o el asignado en tu configuración)

Este microservicio gestiona el sistema de votos, especialmente optimizado

###  Estructura de Paquetes y Clases Principales

*  **`Client/`**
   * `UserClient`: Resuelve la información pública de los usuarios que interactúan.
   * `PostClient`: Recupera el autor de un Post para saber a quién notificar.
   * `NotificationClient`: Dispara alertas al sistema central de notificaciones de forma asíncrona.
   * `AuditClient`: Registra los votos en el libro mayor de auditoría.
*  **`Controller/`**
   * `InteractionController`: Punto de entrada que extrae el AuthID directamente del Token JWT, garantizando que nadie pueda emitir un voto en nombre de otro usuario.
*  **`Model/`**
   * `InteractionModel`: Entidad polimórfica mapeada a la tabla `votes`. Utiliza una restricción única (`user_id`, `entity_type`, `entity_id`) para evitar votos duplicados.
   * `InteractionRequestDTO`: Recibe el tipo de entidad (ej. "POST") y su ID.
   * `InteractionResponseDTO`: Devuelve el estado de la acción ("VOTE_ADDED" o "VOTE_REMOVED") y los datos del usuario enriquecidos.
*  **`Repository/`**
   * `InteractionRepository`: Ejecuta búsquedas compuestas utilizando índices optimizados en base de datos.
*  **`Service/`**
   * `InteractionService`: Contiene la lógica central. Implementa el patrón "Toggle" (agregar/quitar voto en un solo endpoint), mitigación del problema N+1 mediante caché local, y aislamiento de fallos para las notificaciones.

###  Lógica de Funcionamiento

1. **Patrón Toggle:** Cuando un usuario envía una interacción, el servicio verifica si ya existe. Si no existe, la crea; si ya existe, la elimina. Esto reduce la cantidad de endpoints necesarios en el backend.
2. **Resiliencia:** La emisión de la notificación está encapsulada en un bloque `try-catch`. Si `ms-Notification` está caído, el voto se guarda exitosamente de todos modos y el usuario final no percibe ningún error, protegiendo la experiencia de usuario.
3. **Optimización de Red:** Al listar los likes de un post viral, el servicio almacena temporalmente los perfiles (`UserDTO`) ya consultados en un `HashMap`. Esto evita saturar al `ms-User` con peticiones HTTP repetidas.

---

### Guía de Endpoints (Pruebas en Postman)

#### 1. Emitir o Retirar Voto (Toggle)
Alterna el estado de una interacción sobre una entidad específica.

* **URL:** `POST http://localhost:8089/api/interactions/vote`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`
* **Body (JSON):**
    ```json
    {
      "entityType": "POST",
      "entityId": 1,
      "voteType": "UPVOTE"
    }
    ```

#### 2. Obtener Votos de una Entidad
Recupera la lista completa de interacciones para un Post o Comentario específico.

* **URL:** `GET http://localhost:8089/api/interactions/entity/{entityType}/{entityId}`
* **Autorización:** `Authorization: Bearer <Tu_Token_JWT>`

## ️ 10. Microservicio de Auditoría (`ms-Audit`)

**Puerto por defecto:** `8090`

Encargado de registrar todos las acciones importantes o críticas del sistema.
###  Estructura de Paquetes y Clases Principales

*  **`Controller/`**
    * `AuditController`: Expone un único endpoint de entrada (`/api/audit`) dedicado a recibir los eventos desde la red interna.
*  **`Model/`**
    * `AuditModel`: Entidad JPA persistida en la tabla `audit_logs`.
    * `AuditRequestDTO`: Objeto que los demás microservicios deben construir y enviar para registrar una acción.
*  **`Repository/`**
    * `AuditRepository`: Interfaz JPA básica para la persistencia de los registros.
*  **`Security/`**
    * `SecurityConfig`: Configuración crítica y especializada. A diferencia del resto del ecosistema, este archivo tiene configurado un `.permitAll()` global. 
*  **`Service/`**
    * `AuditService`: Lógica de negocio.

###  Lógica de Funcionamiento 

**`registrarAuditoria(AuditRequestDTO)`**
1. Actúa como un receptor. Toma la petición entrante que contiene los datos en crudo de la transacción (ID del usuario, tipo de acción, microservicio de origen y descripción detallada).
2. Intercepta el DTO e inyecta automáticamente la marca de tiempo exacta del servidor local (`LocalDateTime.now()`).
3. Mapea la información hacia la entidad `AuditModel` almacenando los datos en la base de datos

---

###  Guía de Endpoints (Pruebas en Postman)

#### 1. Registrar un Evento de Auditoría (Uso Interno)
Endpoint consumido por el resto de los microservicios mediante OpenFeign.

* **URL:** `POST http://localhost:8090/api/audit`
* **Body (JSON):**
    ```json
    {
      "usuarioId": 15,
      "accion": "CREATE_POST",
      "recurso": "ms-PostService",
      "detalles": "Post publicado exitosamente con ID [1] y título: 'Mi primer post'"
    }
    ```
