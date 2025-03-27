package fr.quoi_regarder.dto.user;

import fr.quoi_regarder.commons.enums.ColorModeType;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProfileDto implements Serializable {
    private UUID userId;
    private String email;
    @Size(min = 3, max = 255)
    private String username;
    @Size(max = 255)
    private String firstName;
    @Size(max = 255)
    private String lastName;
    @Size(max = 255)
    private String avatarUrl;
    private String language;
    private ColorModeType colorMode;
    private Date createdAt;
    private Date updatedAt;
}