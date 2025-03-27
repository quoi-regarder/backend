package fr.quoi_regarder.dto.user;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileDto implements Serializable {
    @Size(max = 255)
    private String username;
    @Size(max = 255)
    private String firstName;
    @Size(max = 255)
    private String lastName;
    @Size(max = 255)
    private String avatarUrl;
}
