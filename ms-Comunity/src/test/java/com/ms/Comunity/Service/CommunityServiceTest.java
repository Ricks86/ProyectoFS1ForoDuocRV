package com.ms.Comunity.Service;

import com.ms.Comunity.Client.UserClient;
import com.ms.Comunity.DTOs.CommunityCreateDTO;
import com.ms.Comunity.DTOs.CommunityJoinDTO;
import com.ms.Comunity.DTOs.CommunityResponseDTO;
import com.ms.Comunity.Model.CommunityModel;
import com.ms.Comunity.Repository.CommunityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CommunityServiceTest {

    @Mock
    private CommunityRepository communityRepository;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private CommunityService communityService;

    @Test
    void testCreateCommunity() {
        CommunityCreateDTO req = new CommunityCreateDTO();
        req.setName("Comunidad Test");
        req.setDescription("Desc");
        req.setCommunityAccess("pass123");

        Mockito.when(communityRepository.existsByName(anyString())).thenReturn(false);

        CommunityModel saved = new CommunityModel();
        saved.setId(1L);
        saved.setName("Comunidad Test");
        saved.setMemberIds(new ArrayList<>());

        Mockito.when(communityRepository.save(any(CommunityModel.class))).thenReturn(saved);

        CommunityResponseDTO result = communityService.createCommunity(req, 1L, "token");

        assertNotNull(result);
        assertEquals("Comunidad Test", result.getName());
    }

    @Test
    void testJoinCommunity() {
        CommunityJoinDTO req = new CommunityJoinDTO();
        req.setAccessCode("pass123");

        CommunityModel comm = new CommunityModel();
        comm.setId(1L);
        comm.setCommunityAccess("pass123");
        comm.setMemberCount(1);
        comm.setMemberIds(new ArrayList<>());

        Mockito.when(communityRepository.findById(anyLong())).thenReturn(Optional.of(comm));
        Mockito.when(communityRepository.save(any(CommunityModel.class))).thenReturn(comm);

        CommunityResponseDTO result = communityService.joinCommunity(1L, req, 2L, "token");

        assertNotNull(result);
    }

    @Test
    void testGetAllCommunities() {
        Mockito.when(communityRepository.findAll()).thenReturn(Collections.emptyList());

        List<CommunityResponseDTO> result = communityService.getAllCommunities("token");

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

}
