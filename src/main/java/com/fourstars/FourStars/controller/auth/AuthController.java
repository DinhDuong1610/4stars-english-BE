package com.fourstars.FourStars.controller.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fourstars.FourStars.domain.User;
import com.fourstars.FourStars.domain.request.auth.ForgotPasswordRequestDTO;
import com.fourstars.FourStars.domain.request.auth.GoogleLoginRequestDTO;
import com.fourstars.FourStars.domain.request.auth.RegisterRequestDTO;
import com.fourstars.FourStars.domain.request.auth.ReqLoginDTO;
import com.fourstars.FourStars.domain.request.auth.ResetPasswordRequestDTO;
import com.fourstars.FourStars.domain.response.auth.ResLoginDTO;
import com.fourstars.FourStars.domain.response.user.UserResponseDTO;
import com.fourstars.FourStars.service.UserService;
import com.fourstars.FourStars.util.SecurityUtil;
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
import java.util.Optional;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Authentication API", description = "APIs for user login, registration, and token management")
public class AuthController {
        private final AuthenticationManagerBuilder authenticationManagerBuilder;
        private final AuthenticationManager authenticationManager;
        private final SecurityUtil securityUtil;
        private final UserService userService;
        private final PasswordEncoder passwordEncoder;

        @Value("${fourstars.jwt.refresh-token-validity-in-seconds}")
        private long refreshTokenExpiration;

        public AuthController(AuthenticationManagerBuilder authenticationManagerBuilder,
                        AuthenticationManager authenticationManager,
                        SecurityUtil securityUtil,
                        UserService userService,
                        PasswordEncoder passwordEncoder) {
                this.authenticationManagerBuilder = authenticationManagerBuilder;
                this.authenticationManager = authenticationManager;
                this.securityUtil = securityUtil;
                this.userService = userService;
                this.passwordEncoder = passwordEncoder;
        }

        @Operation(summary = "User Login", description = "Authenticates a user with email and password, returns JWT tokens in response and HttpOnly cookie for refresh token.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Login successful"),
                        @ApiResponse(responseCode = "400", description = "Invalid username or password")
        })
        @PostMapping("/auth/login")
        @ApiMessage("Authenticate user and get tokens")
        public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO loginDTO) {
                UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                                loginDTO.getUsername(), loginDTO.getPassword());

                Authentication authentication;
                try {
                        if (this.authenticationManager != null) {
                                authentication = authenticationManager.authenticate(authenticationToken);
                        } else {
                                authentication = authenticationManagerBuilder.getObject()
                                                .authenticate(authenticationToken);
                        }
                } catch (AuthenticationException e) {
                        throw new ResourceNotFoundException("Invalid username or password.");
                }

                SecurityContextHolder.getContext().setAuthentication(authentication);

                ResLoginDTO res = new ResLoginDTO();
                User currentUser = this.userService.handleGetUsername(loginDTO.getUsername());

                if (currentUser != null) {
                        ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                        currentUser.getId(),
                                        currentUser.getEmail(),
                                        currentUser.getName(),
                                        currentUser.getRole(),
                                        currentUser.getStreakCount(),
                                        currentUser.getPoint(),
                                        currentUser.getBadge());
                        res.setUser(userLogin);
                }

                String accessToken = this.securityUtil.createAccessToken(authentication.getName(), res);
                res.setAccessToken(accessToken);

                String refreshToken = this.securityUtil.createRefreshToken(loginDTO.getUsername(), res);
                this.userService.updateUserToken(refreshToken, loginDTO.getUsername());

                ResponseCookie resCookies = ResponseCookie
                                .from("refresh_token", refreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path("/api/v1/auth")
                                .maxAge(refreshTokenExpiration)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                                .body(res);
        }

        @Operation(summary = "Handle Google OAuth2 Login", description = "Receives the authorization code from frontend, validates it with Google, and returns app-specific JWTs.")
        @PostMapping("/auth/google")
        @ApiMessage("Handle Google OAuth2 login callback")
        public ResponseEntity<ResLoginDTO> loginWithGoogle(@RequestBody GoogleLoginRequestDTO requestDTO)
                        throws Exception {
                ResLoginDTO loginResponse = userService.loginWithGoogle(requestDTO.getCode());

                String refreshToken = this.securityUtil.createRefreshToken(loginResponse.getUser().getEmail(),
                                loginResponse);
                this.userService.updateUserToken(refreshToken, loginResponse.getUser().getEmail());

                ResponseCookie resCookies = ResponseCookie
                                .from("refresh_token", refreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path("/api/v1/auth")
                                .maxAge(refreshTokenExpiration)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, resCookies.toString())
                                .body(loginResponse);
        }

        @Operation(summary = "Get Current User Account", description = "Fetches the profile information of the currently authenticated user based on their token.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Successfully retrieved account info"),
                        @ApiResponse(responseCode = "401", description = "Unauthorized - Token is missing or invalid")
        })
        @GetMapping("/auth/account")
        @PreAuthorize("isAuthenticated()")
        @ApiMessage("Fetch current authenticated user's account")
        public ResponseEntity<ResLoginDTO.UserLogin> getAccount() {
                Optional<String> currentUserLoginOpt = SecurityUtil.getCurrentUserLogin();
                if (currentUserLoginOpt.isEmpty()) {
                        throw new ResourceNotFoundException("User not authenticated.");
                }
                String email = currentUserLoginOpt.get();
                User currentUser = this.userService.handleGetUsername(email);

                if (currentUser == null) {
                        throw new ResourceNotFoundException("User account not found for email: " + email);
                }

                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                currentUser.getId(),
                                currentUser.getEmail(),
                                currentUser.getName(),
                                currentUser.getRole(),
                                currentUser.getStreakCount(),
                                currentUser.getPoint(),
                                currentUser.getBadge());

                return ResponseEntity.ok().body(userLogin);
        }

        @Operation(summary = "Refresh Access Token", description = "Generates a new access token using a valid refresh token from an HttpOnly cookie.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
                        @ApiResponse(responseCode = "400", description = "Refresh token is missing or invalid")
        })
        @PostMapping("/auth/refresh")
        @ApiMessage("Refresh access token using refresh token from cookie")
        public ResponseEntity<ResLoginDTO> getRefreshToken(
                        @CookieValue(name = "refresh_token", required = false) String refreshTokenFromCookie)

                        throws BadRequestException, ResourceNotFoundException {

                if (refreshTokenFromCookie == null || refreshTokenFromCookie.isEmpty()
                                || "error".equals(refreshTokenFromCookie)) {
                        throw new BadRequestException("Refresh token is missing from cookie.");
                }
                Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshTokenFromCookie);
                String email = decodedToken.getSubject();

                User currentUserDB = this.userService.getUserByRefreshTokenAndEmail(refreshTokenFromCookie, email);

                if (currentUserDB == null) {
                        throw new ResourceNotFoundException("Invalid refresh token or user mismatch.");
                }

                UserDetails userDetails = userService.loadUserByUsername(email);
                UsernamePasswordAuthenticationToken newAuthentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(newAuthentication);

                ResLoginDTO res = new ResLoginDTO();
                ResLoginDTO.UserLogin userLogin = new ResLoginDTO.UserLogin(
                                currentUserDB.getId(),
                                currentUserDB.getEmail(),
                                currentUserDB.getName(),
                                currentUserDB.getRole(),
                                currentUserDB.getStreakCount(),
                                currentUserDB.getPoint(),
                                currentUserDB.getBadge());
                res.setUser(userLogin);

                String newAccessToken = this.securityUtil.createAccessToken(newAuthentication.getName(), res);
                res.setAccessToken(newAccessToken);

                String newRefreshToken = this.securityUtil.createRefreshToken(email, res);
                this.userService.updateUserToken(newRefreshToken, email);

                ResponseCookie newResCookies = ResponseCookie
                                .from("refresh_token", newRefreshToken)
                                .httpOnly(true)
                                .secure(true)
                                .path("/api/v1/auth")
                                .maxAge(refreshTokenExpiration)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, newResCookies.toString())
                                .body(res);
        }

        @Operation(summary = "User Logout", description = "Logs out the user by clearing their refresh token and cookie.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "200", description = "Logout successful")
        })
        @PostMapping("/auth/logout")
        @PreAuthorize("isAuthenticated()")
        @ApiMessage("Logout User")
        public ResponseEntity<Void> logout() {
                Optional<String> currentUserLoginOpt = SecurityUtil.getCurrentUserLogin();
                if (currentUserLoginOpt.isPresent()) {
                        String email = currentUserLoginOpt.get();
                        this.userService.updateUserToken(null, email);
                }
                SecurityContextHolder.clearContext();

                ResponseCookie deleteCookie = ResponseCookie
                                .from("refresh_token", "")
                                .httpOnly(true)
                                .secure(true)
                                .path("/api/v1/auth")
                                .maxAge(0)
                                .build();

                return ResponseEntity.ok()
                                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                                .build();
        }

        @Operation(summary = "User Registration", description = "Registers a new user account.")
        @ApiResponses(value = {
                        @ApiResponse(responseCode = "201", description = "User registered successfully"),
                        @ApiResponse(responseCode = "400", description = "Invalid input data"),
                        @ApiResponse(responseCode = "409", description = "Email already exists")
        })
        @PostMapping("/auth/register")
        @ApiMessage("Register a new user")
        public ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO registerDTO)
                        throws DuplicateResourceException, ResourceNotFoundException {
                UserResponseDTO newUserInfo = this.userService.registerNewUser(registerDTO);
                return ResponseEntity.status(HttpStatus.CREATED).body(newUserInfo);
        }

        @Operation(summary = "Request a password reset OTP", description = "Sends a One-Time Password to the user's email if the account exists.")
        @PostMapping("auth/forgot-password")
        @ApiMessage("Password reset OTP has been sent to your email if it exists in our system.")
        public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDTO requestDTO) {
                userService.handleForgotPassword(requestDTO);
                return ResponseEntity.ok().build();
        }

        @Operation(summary = "Reset password using OTP")
        @PostMapping("auth/reset-password")
        @ApiMessage("Password has been successfully reset.")
        public ResponseEntity<Void> resetPassword(@Valid @RequestBody ResetPasswordRequestDTO requestDTO) {
                userService.handleResetPassword(requestDTO);
                return ResponseEntity.ok().build();
        }
}