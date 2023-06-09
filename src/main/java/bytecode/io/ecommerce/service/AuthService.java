package bytecode.io.ecommerce.service;

import bytecode.io.ecommerce.dto.AuthenticationResponse;
import bytecode.io.ecommerce.dto.LoginRequest;
import bytecode.io.ecommerce.dto.RefreshTokenRequest;
import bytecode.io.ecommerce.dto.UserDto;
import bytecode.io.ecommerce.exception.SpringECommerceException;
import bytecode.io.ecommerce.model.NotificationEmail;
import bytecode.io.ecommerce.model.User;
import bytecode.io.ecommerce.model.VerifyToken;
import bytecode.io.ecommerce.repository.UserRepository;
import bytecode.io.ecommerce.repository.VerifyTokenRepository;
import bytecode.io.ecommerce.security.JwtProvider;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final VerifyTokenRepository verifyTokenRepository;
    private final MailService mailService;
    private final UserRepository userRepository;
    private final JwtProvider jwtProvider;
    private final AuthenticationManager authenticationManager;
    private final RefreshTokenService refreshTokenService;

    public void signUp(UserDto userDto) {

        User user = new User();
        user.setUsername(userDto.getUsername());
        user.setEmail(userDto.getEmail());
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setCreated(Instant.now());
        user.setEnabled(false);

        userRepository.save(user);

        String token = generateVerificationToken(user);
        mailService.sendEmail(new NotificationEmail("Please Activate your Account",
                user.getEmail(), "Thank you for signing up to Shopify, " +
                "please click on the below url to activate your account : " +
                "http://localhost:8081/api/auth/accountVerification/" + token));
    }

    private String generateVerificationToken(User user) {

        String token = UUID.randomUUID().toString();
        VerifyToken verificationToken = new VerifyToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);

        verifyTokenRepository.save(verificationToken);
        return token;
    }

    private void fetchUserAndEnable(VerifyToken verifyToken) {
        String username = verifyToken.getUser().getUsername();
        User user = userRepository.findByUsername(username).orElseThrow(() -> new SpringECommerceException("User not found with name " + username));
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void verifyAccount(String token) {
        Optional<VerifyToken> verificationToken = verifyTokenRepository.findByToken(token);
        fetchUserAndEnable(verificationToken.orElseThrow(() -> new SpringECommerceException("Invalid Token")));
    }

    public AuthenticationResponse login(LoginRequest loginRequest) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(),
                loginRequest.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authenticate);
        String token = jwtProvider.generateToken(authenticate);
        return AuthenticationResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenService.generateRefreshToken().getToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .username(loginRequest.getUsername())
                .build();
    }

    public AuthenticationResponse refreshToken(RefreshTokenRequest refreshTokenRequest) {
        refreshTokenService.validateRefreshToken(refreshTokenRequest.getRefreshToken());
        String token = jwtProvider.generateTokenWithUserName(refreshTokenRequest.getUsername());
        return AuthenticationResponse.builder()
                .authenticationToken(token)
                .refreshToken(refreshTokenRequest.getRefreshToken())
                .expiresAt(Instant.now().plusMillis(jwtProvider.getJwtExpirationInMillis()))
                .username(refreshTokenRequest.getUsername())
                .build();
    }
}
