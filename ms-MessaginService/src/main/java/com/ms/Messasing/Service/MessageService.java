package com.ms.Messasing.Service;

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

    @Transactional
    public MessageResponseDTO enviarMensajePorUsername(String usernameReceptor, MessageCreatetDTO request, Long idEmisorLogueado, String token) {

        UserDTO receptorDto = obtenerUsuarioPorUsernameSeguro(usernameReceptor, token);
        UserDTO emisorDto = obtenerUsuarioPorIdSeguro(idEmisorLogueado, token);

        Message nuevoMensaje = Message.builder()
                .contenido(request.getContenido())
                .idEmisor(emisorDto.getId())
                .idReceptor(receptorDto.getId())
                .build();

        Message mensajeGuardado = messageRepository.save(nuevoMensaje);

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
                    id -> obtenerUsuarioPorIdSeguro(id, token));

            return new BandejaItemDTO(emisorDto, sinLeer, ultimaFecha);
        }).toList();
    }

    @Transactional
    public List<MessageResponseDTO> obtenerConversacion(Long idLogueado, String otroUsuario, String token) {

        UserDTO otroUsuarioDto = obtenerUsuarioPorUsernameSeguro(otroUsuario, token);
        UserDTO usuarioLog = obtenerUsuarioPorIdSeguro(idLogueado, token);

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

    private UserDTO obtenerUsuarioPorIdSeguro(Long userId, String token) {
        try {
            return userClient.obtenerUsuarioPorId(userId, token);
        } catch (Exception e) {
            log.warn("Error al buscar ms-User por ID [{}]: {}", userId, e.getMessage());
            return new UserDTO(userId, "Usuario Temporal", "Alias No Disponible");
        }
    }

    private UserDTO obtenerUsuarioPorUsernameSeguro(String username, String token) {
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
}
