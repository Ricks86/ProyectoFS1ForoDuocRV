package com.ms.Messasing.Service;

import com.ms.Messasing.Model.Message;
import com.ms.Messasing.Model.MessageCreatetDTO;
import com.ms.Messasing.Model.MessageResponseDTO;
import com.ms.Messasing.Model.UserDTO;
import com.ms.Messasing.Repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

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
                .idEmisor(idEmisorLogueado)
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
    public List<UserDTO> obtenerBandejaEntrada(Long idLogueado) {

        List<Long> idsEmisores = messageRepository.findDistinctEmisoresByIdReceptor(idLogueado);

        return idsEmisores.stream()
                .map(idEmisor -> {
                    try {
                        return webClientBuilder.build()
                                .get()
                                .uri("http://localhost:8082/users/{id}", idEmisor)
                                .retrieve()
                                .bodyToMono(UserDTO.class)
                                .block(); // Llamada síncrona por cada remitente
                    } catch (Exception e) {
                        return new UserDTO(idEmisor, "Usuario Temporal", "Alias No Disponible");
                    }
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MessageResponseDTO> obtenerConversacion(Long idLogueado, Long idOtroUsuario) {

        List<Message> mensajes = messageRepository.findConversacionCompleta(idLogueado, idOtroUsuario);

        UserDTO perfilLogueado = obtenerUsuarioPorId(idLogueado);
        UserDTO perfilOtro = obtenerUsuarioPorId(idOtroUsuario);

        return mensajes.stream()
                .map(mensaje -> {
                    UserDTO emisor = (mensaje.getIdEmisor().equals(idLogueado)) ? perfilLogueado : perfilOtro;
                    UserDTO receptor = (mensaje.getIdReceptor().equals(idLogueado)) ? perfilLogueado : perfilOtro;

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
