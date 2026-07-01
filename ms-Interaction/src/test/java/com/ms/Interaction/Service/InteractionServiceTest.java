package com.ms.Interaction.Service;

import com.ms.Interaction.Client.CommentClient;
import com.ms.Interaction.Client.NotificationClient;
import com.ms.Interaction.Client.PostClient;
import com.ms.Interaction.Client.UserClient;
import com.ms.Interaction.DTOs.InteractionRequestDTO;
import com.ms.Interaction.DTOs.InteractionResponseDTO;
import com.ms.Interaction.DTOs.NotificationCreateDTO;
import com.ms.Interaction.DTOs.UserDTO;
import com.ms.Interaction.Model.InteractionModel;
import com.ms.Interaction.Repository.InteractionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class InteractionServiceTest {

    @Mock
    private InteractionRepository interactionRepository;
    @Mock
    private UserClient userClient;
    @Mock
    private PostClient postClient;
    @Mock
    private NotificationClient notificationClient;
    @Mock
    private CommentClient commentClient;

    @InjectMocks
    private InteractionService interactionService;

    private InteractionModel mockVote;
    private UserDTO mockUserDTO;

    @BeforeEach
    void setUp() {
        mockVote = InteractionModel.builder()
                .id(1L)
                .userId(1L)
                .entityId(10L)
                .entityType("POST")
                .voteType("UPVOTE")
                .build();
        mockUserDTO = new UserDTO(1L, "testuser", "Alias");
    }

    @Test
    void testToggleVote_NewVote_TriggersNotification() {
        InteractionRequestDTO req = new InteractionRequestDTO("POST", 10L, "UPVOTE");

        Mockito.when(interactionRepository.findByEntityIdAndUserIdAndEntityType(10L, 1L, "POST")).thenReturn(Optional.empty());
        Mockito.when(userClient.obtenerUsuarioPorId(eq(1L), anyString())).thenReturn(mockUserDTO);
        Mockito.when(postClient.getAuthorIdByPostId(10L, "token")).thenReturn(2L);

        InteractionResponseDTO res = interactionService.toggleVote(req, 1L, "token");

        assertEquals("VOTE_ADDED", res.getStatus());
        Mockito.verify(interactionRepository, Mockito.times(1)).save(any(InteractionModel.class));
        Mockito.verify(notificationClient, Mockito.times(1)).enviarNotificacion(any(NotificationCreateDTO.class), eq("ms-Interaction"));
    }

    @Test
    void testToggleVote_RemoveExistingVote() {
        InteractionRequestDTO req = new InteractionRequestDTO("POST", 10L, "UPVOTE");
        Mockito.when(interactionRepository.findByEntityIdAndUserIdAndEntityType(10L, 1L, "POST")).thenReturn(Optional.of(mockVote));

        InteractionResponseDTO res = interactionService.toggleVote(req, 1L, "token");

        assertEquals("VOTE_REMOVED", res.getStatus());
        Mockito.verify(interactionRepository, Mockito.times(1)).delete(mockVote);
    }

    @Test
    void testToggleVote_UpdateExistingVote() {
        InteractionRequestDTO req = new InteractionRequestDTO("POST", 10L, "DOWNVOTE"); // Cambia de UP a DOWN
        Mockito.when(interactionRepository.findByEntityIdAndUserIdAndEntityType(10L, 1L, "POST")).thenReturn(Optional.of(mockVote));
        Mockito.when(userClient.obtenerUsuarioPorId(eq(1L), anyString())).thenReturn(mockUserDTO);

        InteractionResponseDTO res = interactionService.toggleVote(req, 1L, "token");

        assertEquals("VOTE_UPDATED", res.getStatus());
        assertEquals("DOWNVOTE", mockVote.getVoteType());
        Mockito.verify(interactionRepository, Mockito.times(1)).save(mockVote);
    }

    @Test
    void testGetVotesForEntity() {
        Mockito.when(interactionRepository.findByEntityIdAndEntityType(10L, "POST")).thenReturn(List.of(mockVote));
        Mockito.when(userClient.obtenerUsuarioPorId(eq(1L), anyString())).thenReturn(mockUserDTO);

        List<InteractionResponseDTO> res = interactionService.getVotesForEntity("POST", 10L, "token");

        assertFalse(res.isEmpty());
        assertEquals("UPVOTE", res.get(0).getVoteType());
        assertEquals("testuser", res.get(0).getUser().getUsername());
    }

    @Test
    void testObtenerUsuario_FallbackException() {
        InteractionRequestDTO req = new InteractionRequestDTO("POST", 10L, "DOWNVOTE");
        Mockito.when(interactionRepository.findByEntityIdAndUserIdAndEntityType(10L, 1L, "POST")).thenReturn(Optional.of(mockVote));
        Mockito.when(userClient.obtenerUsuarioPorId(eq(1L), anyString())).thenThrow(new RuntimeException("Error User"));

        InteractionResponseDTO res = interactionService.toggleVote(req, 1L, "token");

        assertEquals("Usuario Desconocido", res.getUser().getUsername());
    }
}
