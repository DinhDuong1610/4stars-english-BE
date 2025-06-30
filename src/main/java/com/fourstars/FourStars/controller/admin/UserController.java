package com.fourstars.FourStars.controller.admin;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
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
import org.springframework.web.bind.annotation.RequestParam;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@PreAuthorize("hasAuthority('ROLE_ADMIN')")
@RequestMapping("/api/v1/admin/users")
@Tag(name = "Admin - User Management API", description = "APIs for Administrators to manage users")
public class UserController {

        private final UserService userService;

        public UserController(UserService userService) {
                this.userService = userService;
        }

        @Operation(summary = "Create a new user", description = "Allows an admin to create a new user account with a specific role.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "User created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid user data provided"),
                        @ApiResponse(responseCode = "403", description = "Access denied"),
                        @ApiResponse(responseCode = "409", description = "Email already exists")
        })
        @PostMapping
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @ApiMessage("Create a new user")
        public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody CreateUserRequestDTO createUserRequestDTO)
                        throws DuplicateResourceException, ResourceNotFoundException {
                UserResponseDTO createdUser = userService.createUser(createUserRequestDTO);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        }

        @Operation(summary = "Create multiple users in bulk", description = "Creates a list of new user accounts. The entire operation is transactional.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "All users created successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid data in the list"),
                        @ApiResponse(responseCode = "409", description = "One or more emails already exist")
        })
        @PostMapping("/bulk")
        @ApiMessage("Create a list of users")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        public ResponseEntity<List<UserResponseDTO>> createBulkUsers(
                        @Valid @RequestBody List<CreateUserRequestDTO> userList) throws DuplicateResourceException {
                List<UserResponseDTO> createdUsers = userService.createBulkUsers(userList);
                return ResponseEntity.status(HttpStatus.CREATED).body(createdUsers);
        }

        @Operation(summary = "Get a user by ID", description = "Retrieves the details of a specific user by their ID.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved user"),
                        @ApiResponse(responseCode = "403", description = "Access denied"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @ApiMessage("Fetch a user by their ID")
        public ResponseEntity<UserResponseDTO> getUserById(@PathVariable long id) throws ResourceNotFoundException {
                UserResponseDTO user = userService.fetchUserById(id);
                return ResponseEntity.ok(user);
        }

        @Operation(summary = "Update an existing user", description = "Allows an admin to update a user's details, role, or status.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "User updated successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid data or ID mismatch"),
                        @ApiResponse(responseCode = "403", description = "Access denied"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
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

        @Operation(summary = "Delete a user", description = "Permanently deletes a user account. This action cannot be undone.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "204", description = "User deleted successfully"),
                        @ApiResponse(responseCode = "403", description = "Access denied"),
                        @ApiResponse(responseCode = "404", description = "User not found")
        })
        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @ApiMessage("Delete a user")
        public ResponseEntity<Void> deleteUser(@PathVariable long id) throws ResourceNotFoundException {
                userService.deleteUser(id);
                return ResponseEntity.noContent().build();
        }

        @Operation(summary = "Get all users with pagination", description = "Retrieves a paginated list of all user accounts.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved user list"),
                        @ApiResponse(responseCode = "403", description = "Access denied")
        })
        @GetMapping
        @PreAuthorize("hasAuthority('ROLE_ADMIN')")
        @ApiMessage("Fetch all users with pagination")
        public ResponseEntity<ResultPaginationDTO<UserResponseDTO>> getAllUsers(
                        Pageable pageable,
                        @RequestParam(name = "name", required = false) String name,
                        @RequestParam(name = "email", required = false) String email,
                        @RequestParam(name = "role", required = false) String role,
                        @RequestParam(name = "active", required = false) Boolean active,
                        @RequestParam(name = "startCreatedAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startCreatedAt,
                        @RequestParam(name = "endCreatedAt", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endCreatedAt) {
                ResultPaginationDTO<UserResponseDTO> result = userService.fetchAllUsers(pageable, name, email, active,
                                role,
                                startCreatedAt, endCreatedAt);
                return ResponseEntity.ok(result);
        }
}
