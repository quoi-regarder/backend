package fr.quoi_regarder.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class BaseEntityDto implements Serializable {
    private UUID id;
    private Date createdAt;
    private Date updatedAt;
}