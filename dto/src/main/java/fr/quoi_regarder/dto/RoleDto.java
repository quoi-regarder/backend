package fr.quoi_regarder.dto;

import fr.quoi_regarder.commons.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleDto implements Serializable {
    private Long id;
    private RoleType name;
    private Date createdAt;
    private Date updatedAt;
}