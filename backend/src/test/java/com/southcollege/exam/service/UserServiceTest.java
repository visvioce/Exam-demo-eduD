package com.southcollege.exam.service;

import com.southcollege.exam.dto.response.LoginResponse;
import com.southcollege.exam.dto.response.UserResponse;
import com.southcollege.exam.entity.User;
import com.southcollege.exam.enums.UserStatusEnum;
import com.southcollege.exam.exception.BusinessException;
import com.southcollege.exam.utils.JwtUtil;
import com.southcollege.exam.utils.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 用户服务测试
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private JwtUtil jwtUtil;

    @Spy
    @InjectMocks
    private UserService userService;

    private User testUser;
    private String encryptedPassword;

    @BeforeEach
    void setUp() {
        encryptedPassword = PasswordUtil.encrypt("password123");
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword(encryptedPassword);
        testUser.setNickname("Test User");
        testUser.setRole("STUDENT");
        testUser.setStatus(UserStatusEnum.ACTIVE);
    }

    @Test
    void testLogin_Success() {
        // Given
        doReturn(testUser).when(userService).getByUsername("testuser");
        when(jwtUtil.generateToken(1L, "testuser", "STUDENT")).thenReturn("mock-token");

        // When
        LoginResponse response = userService.login("testuser", "password123");

        // Then
        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
        assertEquals("testuser", response.getUser().getUsername());
    }

    @Test
    void testLogin_UserNotFound() {
        // Given
        doReturn(null).when(userService).getByUsername("nonexistent");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.login("nonexistent", "password");
        });
    }

    @Test
    void testLogin_WrongPassword() {
        // Given
        doReturn(testUser).when(userService).getByUsername("testuser");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.login("testuser", "wrongpassword");
        });
    }

    @Test
    void testLogin_UserDisabled() {
        // Given
        testUser.setStatus(UserStatusEnum.INACTIVE);
        doReturn(testUser).when(userService).getByUsername("testuser");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.login("testuser", "password123");
        });
    }

    @Test
    void testRegister_Success() {
        // Given
        doReturn(null).when(userService).getByUsername("newuser");
        doReturn(true).when(userService).save(any(User.class));

        // When
        UserResponse response = userService.register("newuser", "password123", "New User", "STUDENT");

        // Then
        assertNotNull(response);
        assertEquals("newuser", response.getUsername());
        assertEquals("New User", response.getNickname());
    }

    @Test
    void testRegister_UsernameExists() {
        // Given
        doReturn(testUser).when(userService).getByUsername("testuser");

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.register("testuser", "password", "Test", "STUDENT");
        });
    }

    @Test
    void testChangePassword_Success() {
        // Given
        doReturn(testUser).when(userService).getById(1L);
        doReturn(true).when(userService).updateById(any(User.class));

        // When
        userService.changePassword(1L, "password123", "newpassword123");

        // Then - no exception thrown
        verify(userService).updateById(any(User.class));
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        // Given
        doReturn(testUser).when(userService).getById(1L);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.changePassword(1L, "wrongpassword", "newpassword123");
        });
    }

    @Test
    void testChangePassword_UserNotFound() {
        // Given
        doReturn(null).when(userService).getById(999L);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.changePassword(999L, "oldpass", "newpass");
        });
    }

    @Test
    void testGetCurrentUser_Success() {
        // Given
        doReturn(testUser).when(userService).getById(1L);

        // When
        UserResponse response = userService.getCurrentUser(1L);

        // Then
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
    }

    @Test
    void testGetCurrentUser_UserNotFound() {
        // Given
        doReturn(null).when(userService).getById(999L);

        // When & Then
        assertThrows(BusinessException.class, () -> {
            userService.getCurrentUser(999L);
        });
    }
}
