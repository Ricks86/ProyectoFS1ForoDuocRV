package com.ms.Messasing.Service;

import com.ms.Messasing.Client.NotificationClient;
import com.ms.Messasing.Client.UserClient;
import com.ms.Messasing.DTOs.*;
import com.ms.Messasing.Model.*;
import com.ms.Messasing.Repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio central para el MS-Messaging.
 * <p>
 * Orquesta la persistencia de mensajes directos, el cálculo de bandejas de entrada.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserClient userClient;
    private final NotificationClient notificationClient;

    /**
     * Envía un mensaje directo a un usuario resolviendo su identidad mediante su username.
     * <p>
     * Valida de forma síncrona la existencia del emisor y el receptor contra ms-User.
     * Tras persistir el mensaje, dispara un evento asíncrono hacia ms-Notification.
     *
     * @param usernameReceptor El nombre de usuario destino.
     * @param request          Objeto DTO con el contenido en texto plano del mensaje.
     * @param idEmisorLogueado Identificador interno extraído del token de quien envía el mensaje.
     * @param token            Token JWT activo para la comunicación entre microservicios.
     * @return                 MessageResponseDTO con la información consolidada de la transacción.
     * @throws RuntimeException Si el username destino no existe en la arquitectura.
     */
    @Transactional
    public MessageResponseDTO enviarMensajePorUsername(String usernameReceptor, MessageCreateDTO request, Long idEmisorLogueado, String token) {

        UserDTO receptorDto = obtenerUsuarioPorUsername(usernameReceptor, token);
        UserDTO emisorDto = obtenerUsuarioPorId(idEmisorLogueado, token);

        Message nuevoMensaje = Message.builder()
                .contenido(request.getContenido())
                .idEmisor(emisorDto.getId())
                .idReceptor(receptorDto.getId())
                .build();

        Message mensajeGuardado = messageRepository.save(nuevoMensaje);

        dispararNotificacion(emisorDto.getId(), receptorDto.getId(), request.getContenido(), mensajeGuardado.getId());

        return construirMessageResponse(mensajeGuardado, emisorDto, receptorDto);
    }

    /**
     * Recupera la bandeja de entrada resumida para el usuario autenticado..
     *
     * @param idLogueado Identificador del dueño de la bandeja de entrada.
     * @param token      Token JWT para autorización cruzada.
     * @return           Lista de BandejaItemDTO conteniendo al último emisor, contadores y fechas.
     */
    @Transactional(readOnly = true)
    public List<BandejaItemDTO> obtenerBandejaEntrada(Long idLogueado, String token) {

        List<Object[]> resultados = messageRepository.getResumenBandeja(idLogueado);

        Map<Long, UserDTO> userCache = new HashMap<>();

        return resultados.stream().map(fila -> {
            Long idEmisor = ((Number) fila[0]).longValue();
            Long sinLeer = ((Number) fila[1]).longValue();
            LocalDateTime ultimaFecha = (LocalDateTime) fila[2];

            UserDTO emisorDto = userCache.computeIfAbsent(idEmisor,
                    id -> obtenerUsuarioPorId(id, token));

            return new BandejaItemDTO(emisorDto, sinLeer, ultimaFecha);
        }).toList();
    }

    /**
     * Recupera el historial completo de un chat privado entre dos usuarios específicos.
     * <p>
     * Ejecuta una actualización masiva en la base de datos para
     * conmutar el estado de todos los mensajes recibidos a "leído: true",
     * asegurando la consistencia de la bandeja de entrada.
     *
     * @param idLogueado  Identificador del usuario que está abriendo la conversación.
     * @param otroUsuario Nombre de usuario (username) de la contraparte.
     * @param token       Token JWT en tránsito.
     * @return            Lista cronológica de MessageResponseDTO.
     */
    @Transactional
    public List<MessageResponseDTO> obtenerConversacion(Long idLogueado, String otroUsuario, String token) {

        UserDTO otroUsuarioDto = obtenerUsuarioPorUsername(otroUsuario, token);
        UserDTO usuarioLog = obtenerUsuarioPorId(idLogueado, token);

        messageRepository.marcarMensajesComoLeidos(otroUsuarioDto.getId(), usuarioLog.getId());

        List<Message> mensajes = messageRepository.findConversacionCompleta(usuarioLog.getId(), otroUsuarioDto.getId());

        return mensajes.stream()
                .map(mensaje -> {
                    UserDTO emisor = (mensaje.getIdEmisor().equals(usuarioLog.getId()) ? usuarioLog : otroUsuarioDto);
                    UserDTO receptor = (mensaje.getIdReceptor().equals(usuarioLog.getId())) ? usuarioLog : otroUsuarioDto;

                    return construirMessageResponse(mensaje, emisor, receptor);
                })
                .toList();
    }

    /**
     * Recupera los datos de un usuario por su ID .
     * <p>
     * Si ms-User no está disponible, retorna un DTO temporal
     * para no interrumpir el flujo de lectura de la bandeja de entrada.
     *
     * @param userId ID a buscar.
     * @param token  Token JWT.
     * @return       Usuario encontrado o un perfil anónimo por defecto.
     */
    private UserDTO obtenerUsuarioPorId(Long userId, String token) {
        try {
            return userClient.obtenerUsuarioPorId(userId, token);
        } catch (Exception e) {
            log.warn("Error al buscar ms-User por ID [{}]: {}", userId, e.getMessage());
            return new UserDTO(userId, "Usuario Temporal", "Alias No Disponible");
        }
    }

    /**
     * Consulta estrictamente la existencia de un usuario mediante su alias.
     * <p>
     * A diferencia de la búsqueda por ID, este método propaga la
     * excepción y aborta la transacción, ya que es lógicamente imposible
     * enviar un mensaje a un destinatario inexistente.
     *
     * @param username Nombre de usuario a resolver.
     * @param token    Token JWT.
     * @return         UserDTO verificado.
     * @throws RuntimeException Si el alias no existe o la red falla.
     */
    private UserDTO obtenerUsuarioPorUsername(String username, String token) {
        try {
            return userClient.obtenerUsuarioPorUsername(username, token);
        } catch (Exception e) {
            log.error("Error crítico: el username [{}] no existe", username);
            throw new RuntimeException("El usuario '@" + username + "' no existe en el sistema.");
        }
    }

    private MessageResponseDTO construirMessageResponse(Message mensaje, UserDTO emisor, UserDTO receptor) {
        return MessageResponseDTO.builder()
                .id(mensaje.getId())
                .contenido(mensaje.getContenido())
                .fechaEnvio(mensaje.getFechaEnvio())
                .leido(mensaje.isLeido())
                .emisor(emisor)
                .receptor(receptor)
                .build();
    }

    /**
     * Orquesta el envío aislado de una alerta por mensaje nuevo.
     * Posee degradación elegante (try-catch) para asegurar que la mensajería funcione
     * incluso si el motor de notificaciones colapsa.
     */
    private void dispararNotificacion(Long senderId, Long recipientId, String content, Long messageId) {
        try {
            if (!senderId.equals(recipientId)) {
                NotificationCreateDTO notif = NotificationCreateDTO.builder()
                        .recipientId(recipientId)
                        .senderId(senderId)
                        .type("MESSAGE")
                        .message("Nuevo mensaje privado: " + content)
                        .relatedId(messageId)
                        .build();

                notificationClient.enviarNotificacion(notif, "ms-MessaginService");
                log.info("Notificación de mensaje enviada al receptor ID [{}]", recipientId);
            }
        } catch (Exception e) {
            log.error("Fallo al enviar notificación de mensaje privado: {}", e.getMessage());
        }
    }
}
