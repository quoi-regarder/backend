package fr.quoi_regarder.security.dto;

import fr.quoi_regarder.commons.enums.ColorModeType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterDto {
    @NotBlank
    @Size(min = 3, max = 255)
    private String username;

    @Size(max = 255)
    private String firstName;

    @Size(max = 255)
    private String lastName;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 8, max = 40)
    @Pattern(
            regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$",
            message = "invalid_password"
    )
    private String password;

    @NotNull
    private String language;

    @NotNull
    private ColorModeType colorMode;
}
