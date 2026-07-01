package com.ms.Comunity.Service;

import com.ms.Comunity.Client.UserClient;
import com.ms.Comunity.DTOs.CommunityCreateDTO;
import com.ms.Comunity.DTOs.CommunityJoinDTO;
import com.ms.Comunity.DTOs.CommunityResponseDTO;
import com.ms.Comunity.DTOs.UserDTO;
import com.ms.Comunity.Model.CommunityModel;
import com.ms.Comunity.Repository.CommunityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private CommunityModel mockComm;
    private UserDTO mockUser;

    @BeforeEach
    void setUp() {
        mockComm = CommunityModel.builder()
                .id(1L)
                .name("Java Fans")
                .description("Desc")
                .creatorId(10L)
                .communityAccess("1234")
                .memberIds(new ArrayList<>(List.of(10L)))
                .memberCount(1)
                .createdAt(LocalDateTime.now())
                .build();
        mockUser = new UserDTO(10L, "creator", "Alias");
    }

    @Test
    void testCreateCommunity_Success() {
        CommunityCreateDTO req = new CommunityCreateDTO("Java Fans", "Desc", "1234");

        Mockito.when(communityRepository.existsByName("Java Fans")).thenReturn(false);
        Mockito.when(communityRepository.save(any(CommunityModel.class))).thenReturn(mockComm);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(10L), anyString())).thenReturn(mockUser);

        CommunityResponseDTO res = communityService.createCommunity(req, 10L, "token");

        assertEquals("Java Fans", res.getName());
        assertEquals("creator", res.getCreator().getUsername());
    }

    @Test
    void testCreateCommunity_AlreadyExists() {
        CommunityCreateDTO req = new CommunityCreateDTO("Java Fans", "Desc", "1234");
        Mockito.when(communityRepository.existsByName("Java Fans")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> {
            communityService.createCommunity(req, 10L, "token");
        });
    }

    @Test
    void testJoinCommunity_Success() {
        CommunityJoinDTO req = new CommunityJoinDTO("1234");
        Mockito.when(communityRepository.findById(1L)).thenReturn(Optional.of(mockComm));
        Mockito.when(communityRepository.save(any(CommunityModel.class))).thenReturn(mockComm);
        Mockito.when(userClient.obtenerUsuarioPorId(eq(10L), anyString())).thenReturn(mockUser);

        CommunityResponseDTO res = communityService.joinCommunity(1L, req, 20L, "token");

        assertTrue(mockComm.getMemberIds().contains(20L));
        assertEquals(2, mockComm.getMemberCount());
    }

    @Test
    void testJoinCommunity_WrongAccessCode() {
        CommunityJoinDTO req = new CommunityJoinDTO("wrong");
        Mockito.when(communityRepository.findById(1L)).thenReturn(Optional.of(mockComm));

        Exception ex = assertThrows(RuntimeException.class, () -> {
            communityService.joinCommunity(1L, req, 20L, "token");
        });
        assertTrue(ex.getMessage().contains("incorrecto"));
    }

    @Test
    void testJoinCommunity_AlreadyMember() {
        CommunityJoinDTO req = new CommunityJoinDTO("1234");
        Mockito.when(communityRepository.findById(1L)).thenReturn(Optional.of(mockComm));

        assertThrows(RuntimeException.class, () -> {
            communityService.joinCommunity(1L, req, 10L, "token"); // El 10L ya fue seteado como miembro al crear
        });
    }

    @Test
    void testGetAllCommunities() {
        Mockito.when(communityRepository.findAll()).thenReturn(List.of(mockComm));
        Mockito.when(userClient.obtenerUsuarioPorId(eq(10L), anyString())).thenReturn(mockUser);

        List<CommunityResponseDTO> res = communityService.getAllCommunities("token");

        assertEquals(1, res.size());
    }
}
