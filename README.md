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

### 🔌 Guía de Endpoints (Pruebas en Postman)

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

### 🔌 Guía de Endpoints (Pruebas en Postman)

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

## ️ 6. Microservicio de Auditoría (`ms-Audit`)

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
