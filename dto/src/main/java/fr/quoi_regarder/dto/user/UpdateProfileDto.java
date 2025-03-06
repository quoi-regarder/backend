package fr.quoi_regarder.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileDto implements Serializable {
    private String username;
    private String firstName;
    private String lastName;
    private String avatarUrl;
}
