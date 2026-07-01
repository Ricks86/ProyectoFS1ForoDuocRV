package com.ms.Interaction.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.Interaction.DTOs.InteractionRequestDTO;
import com.ms.Interaction.DTOs.InteractionResponseDTO;
import com.ms.Interaction.Security.JwtUtil;
import com.ms.Interaction.Service.AuditService;
import com.ms.Interaction.Service.InteractionService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(InteractionController.class)
@AutoConfigureMockMvc(addFilters = false)
public class InteractionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private InteractionService interactionService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuditService auditoriaService;

    @Test
    void testToggleVote() throws Exception {

        InteractionRequestDTO request = InteractionRequestDTO.builder()
                .entityType("POST")
                .entityId(1L)
                .voteType("UPVOTE")
                .build();

        InteractionResponseDTO response = InteractionResponseDTO.builder()
                .status("VOTE_ADDED")
                .entityType("POST")
                .entityId(1L)
                .voteType("UPVOTE")
                .build();

        Mockito.when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        Mockito.when(interactionService.toggleVote(any(InteractionRequestDTO.class), any(Long.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/interactions/vote")
                .header("Authorization", "Bearer token-falso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void testGetVotesByEntity() throws Exception {

        Mockito.when(interactionService.getVotesForEntity(anyString(), any(Long.class), anyString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/interactions/entity/POST/1")
                .header("Authorization", "Bearer token-falso"))
                .andExpect(status().is2xxSuccessful());
    }
}
