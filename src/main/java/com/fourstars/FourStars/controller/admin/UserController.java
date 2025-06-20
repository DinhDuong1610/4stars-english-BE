package com.fourstars.FourStars.controller.admin;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.request.user.CreateUserRequestDTO;
import com.fourstars.FourStars.domain.request.user.UpdateUserRequestDTO;
import com.fourstars.FourStars.domain.response.ResultPaginationDTO;
import com.fourstars.FourStars.domain.response.user.UserResponseDTO;
import com.fourstars.FourStars.service.UserService;
import com.fourstars.FourStars.util.annotation.ApiMessage;
import com.fourstars.FourStars.util.error.BadRequestException;
import com.fourstars.FourStars.util.error.DuplicateResourceException;
import com.fourstars.FourStars.util.error.ResourceNotFoundException;

import jakarta.validation.Valid;

@RestController
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequestMapping("/api/v1/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Create a new user")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody CreateUserRequestDTO createUserRequestDTO)
            throws DuplicateResourceException, ResourceNotFoundException {
        UserResponseDTO createdUser = userService.createUser(createUserRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch a user by their ID")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable long id) throws ResourceNotFoundException {
        UserResponseDTO user = userService.fetchUserById(id);
        return ResponseEntity.ok(user);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Update an existing user")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable long id,
            @Valid @RequestBody UpdateUserRequestDTO updateUserRequestDTO)
            throws ResourceNotFoundException, DuplicateResourceException, BadRequestException {

        UserResponseDTO updatedUser = userService.updateUser(id, updateUserRequestDTO);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Delete a user")
    public ResponseEntity<Void> deleteUser(@PathVariable long id) throws ResourceNotFoundException {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @ApiMessage("Fetch all users with pagination")
    public ResponseEntity<ResultPaginationDTO<UserResponseDTO>> getAllUsers(Pageable pageable) {
        ResultPaginationDTO<UserResponseDTO> result = userService.fetchAllUsers(pageable);
        return ResponseEntity.ok(result);
    }
}
