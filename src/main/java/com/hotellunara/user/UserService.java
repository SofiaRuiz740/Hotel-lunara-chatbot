package com.hotellunara.user;

import com.hotellunara.auth.AuthService;
import com.hotellunara.auth.dto.RegisterRequest;
import com.hotellunara.common.enums.UserRole;
import com.hotellunara.common.dto.PageResponse;
import com.hotellunara.common.exception.ResourceNotFoundException;
import com.hotellunara.user.dto.UserRequestDTO;
import com.hotellunara.user.dto.UserResponseDTO;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public UserResponseDTO getProfile(UUID userId) {
        return userMapper.toResponse(getEntity(userId));
    }

    @Transactional
    public UserResponseDTO updateProfile(UUID userId, UserRequestDTO requestDTO) {
        User user = getEntity(userId);
        userMapper.updateEntity(requestDTO, user);
        if (requestDTO.getIdioma() == null) {
            user.setIdioma(user.getIdioma());
        }
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO changeRole(UUID userId, UserRole role) {
        User user = getEntity(userId);
        user.setRole(role);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO changeStatus(UUID userId, boolean active) {
        User user = getEntity(userId);
        user.setActivo(active);
        return userMapper.toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponseDTO createReceptionist(RegisterRequest request) {
        return authService.registerWithRole(request, UserRole.RECEPTIONIST).getUser();
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponseDTO> getUsersPage(int page, int size) {
        Page<UserResponseDTO> usersPage = userRepository.findAllByOrderByNombreAscApellidoAsc(PageRequest.of(page, size))
                .map(userMapper::toResponse);
        return PageResponse.from(usersPage);
    }

    @Transactional(readOnly = true)
    public User getEntity(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}
