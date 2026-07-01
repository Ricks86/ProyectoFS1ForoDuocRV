package com.ms.Interaction.Service;

import com.ms.Interaction.Client.CommentClient;
import com.ms.Interaction.Client.NotificationClient;
import com.ms.Interaction.Client.PostClient;
import com.ms.Interaction.Client.UserClient;
import com.ms.Interaction.DTOs.InteractionRequestDTO;
import com.ms.Interaction.DTOs.InteractionResponseDTO;
import com.ms.Interaction.Model.InteractionModel;
import com.ms.Interaction.Repository.InteractionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
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

    @Test
    void testToggleVote_NewVote() {
        InteractionRequestDTO req = new InteractionRequestDTO();
        req.setEntityId(1L);
        req.setEntityType("POST");
        req.setVoteType("UPVOTE");

        Mockito.when(interactionRepository.findByEntityIdAndUserIdAndEntityType(anyLong(), anyLong(), anyString()))
                .thenReturn(Optional.empty());

        InteractionModel saved = new InteractionModel();
        saved.setVoteType("UPVOTE");
        Mockito.when(interactionRepository.save(any(InteractionModel.class))).thenReturn(saved);

        InteractionResponseDTO result = interactionService.toggleVote(req, 1L, "token");

        assertNotNull(result);
        assertEquals("VOTE_ADDED", result.getStatus());
    }

    @Test
    void testGetVotesForEntity() {
        Mockito.when(interactionRepository.findByEntityIdAndEntityType(anyLong(), anyString()))
                .thenReturn(Collections.emptyList());

        List<InteractionResponseDTO> result = interactionService.getVotesForEntity("POST", 1L, "token");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
