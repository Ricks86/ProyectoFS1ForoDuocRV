package com.ms.Messasing.Service;

import com.ms.Messasing.Model.Message;
import com.ms.Messasing.Model.MessageRequestDTO;
import com.ms.Messasing.Model.MessageResponseDTO;
import com.ms.Messasing.Repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final WebClient.Builder webClientBuilder;

    @Transactional
    public MessageResponseDTO enviarMessage(MessageRequestDTO dto) {
        log.info("Enviando Mensaje de Id: {} para Id: {}", dto.getEmisorId(), dto.getReceptorId());

        if (!usuarioExists(dto.getEmisorId()) || !usuarioExists(dto.getReceptorId())) {
            log.error("Emisor/Receptor no existe");
            throw new IllegalArgumentException("El emisor o receptor no existe");
        }

        Message mensaje = Message.builder()
                .contenido(dto.getContenido())
                .idEmisor(dto.getEmisorId().intValue())
                .idReceptor(dto.getReceptorId().intValue())
                .build();

        Message guardado = messageRepository.save(mensaje);
        return mapToResponseDTO(guardado);
    }

    @Transactional(readOnly = true)
    public List<MessageResponseDTO> obtenerMessages(Long userId) {
        log.info("Obteniendo mensajes para el usuario Id: {}", userId);

        return messageRepository.findByIdEmisorOrIdReceptorOrderByFechaEnvioDesc(userId, userId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    private boolean usuarioExists(Long userId) {
        return Boolean.TRUE.equals(webClientBuilder.build()
                .get()
                .uri("http://localhost:8081/api/users/{id}", userId)
                .retrieve()
                .bodyToMono(Boolean.class)
                .block());
    }

    private String getUserName(Long userId) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri("http://localhost:8081/api/users/{id}/name", userId)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();
        } catch (Exception e) {
            return "Usuario Desconocido";
        }
    }

    private MessageResponseDTO mapToResponseDTO(Message mensaje){
        return MessageResponseDTO.builder()
                .id((long) mensaje.getId())
                .contenido(mensaje.getContenido())
                .emisorId((long) mensaje.getIdEmisor())
                .emisorNombre(getUserName((long) mensaje.getIdEmisor()))
                .receptorId((long) mensaje.getIdReceptor())
                .receptorNombre(getUserName((long) mensaje.getIdReceptor()))
                .enviadoEl(mensaje.getFechaEnvio())
                .leido(mensaje.isLeido())
                .build();
    }
}
