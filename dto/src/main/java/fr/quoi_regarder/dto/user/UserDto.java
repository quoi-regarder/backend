package fr.quoi_regarder.dto.user;

import com.fasterxml.jackson.annotation.JsonIgnore;
import fr.quoi_regarder.commons.enums.SocialProviderType;
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
public class UserDto implements Serializable {
    private UUID id;
    private Long roleId;
    @Size(max = 50)
    private String email;
    @JsonIgnore
    @Size(max = 50)
    private String password;
    private boolean isEmailVerified;
    private SocialProviderType socialProvider;
    private Date createdAt;
    private Date updatedAt;
}