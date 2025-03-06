package fr.quoi_regarder.security.controller;

import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.entity.user.User;
import fr.quoi_regarder.mapper.user.UserMapper;
import fr.quoi_regarder.security.dto.LoginResponseDto;
import fr.quoi_regarder.security.dto.SendResetPasswordDto;
import fr.quoi_regarder.security.dto.StoreResetPasswordDto;
import fr.quoi_regarder.security.service.JwtService;
import fr.quoi_regarder.security.service.LoginAttemptService;
import fr.quoi_regarder.security.service.ResetPasswordService;
import fr.quoi_regarder.security.service.TokenServiceSimple;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/reset-password")
@RequiredArgsConstructor
public class ResetPasswordController {
    private final ResetPasswordService resetPasswordService;
    private final JwtService jwtService;
    private final TokenServiceSimple tokenService;
    private final UserMapper userMapper;
    private final LoginAttemptService loginAttemptService;

    @PostMapping("/send")
    public ResponseEntity<ApiResponse<Void>> sendResetPasswordEmail(@RequestBody @Valid SendResetPasswordDto sendResetPasswordDto) throws MessagingException, UnsupportedEncodingException {
        resetPasswordService.sendResetPasswordEmail(sendResetPasswordDto);

        return ResponseEntity.ok(
                ApiResponse.success("Password reset instructions sent to your email", HttpStatus.OK)
        );
    }

    @PostMapping("/store")
    public ResponseEntity<ApiResponse<LoginResponseDto>> storeNewPassword(@RequestBody @Valid StoreResetPasswordDto storeResetPasswordDto) {
        User user = resetPasswordService.storeNewPassword(storeResetPasswordDto);

        String jwtToken = jwtService.generateToken(user);

        tokenService.saveToken(user.getEmail(), jwtToken);

        LoginResponseDto loginResponseDto = new LoginResponseDto();
        loginResponseDto.setToken(jwtToken);
        loginResponseDto.setUser(userMapper.toDto(user));

        loginAttemptService.loginSucceeded();

        return ResponseEntity.ok(
                ApiResponse.success("Password successfully reset and user logged in", loginResponseDto, HttpStatus.OK)
        );
    }
}