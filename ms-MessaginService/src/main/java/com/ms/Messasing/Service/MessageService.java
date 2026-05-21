package com.ms.Messasing.Service;

import com.ms.Messasing.Model.*;
import com.ms.Messasing.Repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public MessageResponseDTO enviarMensajePorUsername(String usernameReceptor, MessageCreatetDTO request, Long idEmisorLogueado) {

        UserDTO receptorDto = obtenerUsuarioPorUsername(usernameReceptor);
        UserDTO emisorDto = obtenerUsuarioPorId(idEmisorLogueado);

        Message nuevoMensaje = Message.builder()
                .contenido(request.getContenido())
                .idEmisor(emisorDto.getId())
                .idReceptor(receptorDto.getId())
                .build();

        Message mensajeGuardado = messageRepository.save(nuevoMensaje);

        return MessageResponseDTO.builder()
                .id(mensajeGuardado.getId())
                .contenido(mensajeGuardado.getContenido())
                .fechaEnvio(mensajeGuardado.getFechaEnvio())
                .leido(mensajeGuardado.isLeido())
                .receptor(receptorDto)
                .emisor(emisorDto)
                .build();
    }

    @Transactional(readOnly = true)
    public List<BandejaItemDTO> obtenerBandejaEntrada(Long idLogueado) {
        UserDTO userlog = obtenerUsuarioPorId(idLogueado);
        List<Object[]> resultados = messageRepository.getResumenBandeja(userlog.getId());

        return resultados.stream().map(fila -> {
            Long idEmisor = ((Number) fila[0]).longValue();
            Long sinLeer = ((Number) fila[1]).longValue();
            LocalDateTime ultimaFecha = (LocalDateTime) fila[2];

            UserDTO emisorDto;
            try {
                emisorDto = webClientBuilder.build()
                        .get()
                        .uri("http://localhost:8082/users/{id}", idEmisor)
                        .retrieve()
                        .bodyToMono(UserDTO.class)
                        .block();
            } catch (Exception e) {
                emisorDto = new UserDTO(idEmisor, "Usuario Desconocido", "N/A");
            }

            return new BandejaItemDTO(emisorDto, sinLeer, ultimaFecha);
        }).toList();
    }

    @Transactional
    public List<MessageResponseDTO> obtenerConversacion(Long idLogueado, String otroUsuario) {

        UserDTO otroUsuarioDto = obtenerUsuarioPorUsername(otroUsuario);
        UserDTO usuarioLog = obtenerUsuarioPorId(idLogueado);

        messageRepository.marcarMensajesComoLeidos(otroUsuarioDto.getId(), usuarioLog.getId());

        List<Message> mensajes = messageRepository.findConversacionCompleta(usuarioLog.getId(), otroUsuarioDto.getId());


        return mensajes.stream()
                .map(mensaje -> {
                    UserDTO emisor = (mensaje.getIdEmisor().equals(usuarioLog.getId()) ? usuarioLog : otroUsuarioDto);
                    UserDTO receptor = (mensaje.getIdReceptor().equals(usuarioLog.getId())) ? usuarioLog : otroUsuarioDto;

                    return MessageResponseDTO.builder()
                            .id(mensaje.getId())
                            .contenido(mensaje.getContenido())
                            .fechaEnvio(mensaje.getFechaEnvio())
                            .leido(mensaje.isLeido())
                            .emisor(emisor)
                            .receptor(receptor)
                            .build();
                })
                .toList();
    }

    private UserDTO obtenerUsuarioPorId(Long userId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8082/users/{id}", userId)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (Exception e) {
            return new UserDTO(userId, "Usuario Temporal", "Alias No Disponible");
        }
    }
    private UserDTO obtenerUsuarioPorUsername(String username) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8082/users/username/{username}", username)
                    .retrieve()
                    .bodyToMono(UserDTO.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("El usuario '@" + username + "' no existe.");
        }
    }
}
