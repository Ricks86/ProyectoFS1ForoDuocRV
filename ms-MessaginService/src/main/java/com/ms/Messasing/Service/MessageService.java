package com.ms.Messasing.Service;

import com.ms.Messasing.Client.NotificationClient;
import com.ms.Messasing.Client.UserClient;
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

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserClient userClient;
    private final NotificationClient notificationClient;

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

        dispararNotificacion(receptorDto.getId(), emisorDto.getId(), request.getContenido(), mensajeGuardado.getId());

        return construirMessageResponse(mensajeGuardado, emisorDto, receptorDto);
    }

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

    private UserDTO obtenerUsuarioPorId(Long userId, String token) {
        try {
            return userClient.obtenerUsuarioPorId(userId, token);
        } catch (Exception e) {
            log.warn("Error al buscar ms-User por ID [{}]: {}", userId, e.getMessage());
            return new UserDTO(userId, "Usuario Temporal", "Alias No Disponible");
        }
    }

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
