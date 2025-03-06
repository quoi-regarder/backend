package fr.quoi_regarder.dto;

import fr.quoi_regarder.commons.enums.TokenType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenDto implements Serializable {
    private Long id;
    private Long userId;
    private TokenType type;
    private String token;
    private Date expiresAt;
    private Date createdAt;
    private Date updatedAt;
}