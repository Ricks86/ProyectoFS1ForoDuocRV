package com.ms.Comunity.Controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ms.Comunity.DTOs.CommunityCreateDTO;
import com.ms.Comunity.DTOs.CommunityJoinDTO;
import com.ms.Comunity.DTOs.CommunityResponseDTO;
import com.ms.Comunity.Security.JwtUtil;
import com.ms.Comunity.Service.AuditService;
import com.ms.Comunity.Service.CommunityService;
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

@WebMvcTest(CommunityController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CommunityService communityService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private AuditService auditoriaService;

    @Test
    void TestCreateCommunity() throws Exception {
        CommunityCreateDTO request = CommunityCreateDTO.builder()
                .name("Comunidad Java")
                .description("Descripción de la comunidad")
                .communityAccess("pass123")
                .build();

        CommunityResponseDTO response = new CommunityResponseDTO();

        Mockito.when(jwtUtil.extractUserId(anyString())).thenReturn(1L);
        Mockito.when(communityService.createCommunity(any(CommunityCreateDTO.class), any(Long.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/communities")
                .header("Authorization", "Bearer token-falso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void testJoinCommunity() throws Exception {
        CommunityJoinDTO request = new CommunityJoinDTO();
        request.setAccessCode("pass123");

        CommunityResponseDTO response = new CommunityResponseDTO();

        Mockito.when(jwtUtil.extractUserId(anyString())).thenReturn(2L);
        Mockito.when(communityService.joinCommunity(any(Long.class), any(CommunityJoinDTO.class), any(Long.class), anyString()))
                .thenReturn(response);

        mockMvc.perform(post("/api/communities/1/join")
                .header("Authorization", "Bearer token-falso")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void testGetAllCommunities() throws Exception {
        Mockito.when(communityService.getAllCommunities(anyString()))
                .thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/communities")
                .header("Authorization", "Bearer token-falso"))
                .andExpect(status().is2xxSuccessful());
    }
}
