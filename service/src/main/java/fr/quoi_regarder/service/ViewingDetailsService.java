package fr.quoi_regarder.service;

import fr.quoi_regarder.dto.ViewingDetailsDto;
import fr.quoi_regarder.entity.ViewingDetails;
import fr.quoi_regarder.exception.exceptions.EntityNotExistsException;
import fr.quoi_regarder.mapper.ViewingDetailsMapper;
import fr.quoi_regarder.repository.ViewingDetailsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ViewingDetailsService {
    private final ViewingDetailsRepository viewingDetailsRepository;
    private final ViewingDetailsMapper viewingDetailsMapper;

    public ViewingDetailsDto getViewingDetails(UUID userId, Long contentId) {
        return viewingDetailsRepository.findByUserIdAndContextId(userId, contentId)
                .map(viewingDetailsMapper::toDto)
                .orElse(null);
    }

    @Transactional
    public ViewingDetailsDto addViewingDetails(UUID userId, ViewingDetailsDto viewingDetailsDto) {
        ViewingDetails existingViewingDetails = viewingDetailsRepository
                .findByUserIdAndContextId(userId, viewingDetailsDto.getContextId())
                .orElseGet(() -> null);

        if (existingViewingDetails != null) {
            updateViewingDetails(existingViewingDetails, viewingDetailsDto);
        } else {
            existingViewingDetails = viewingDetailsMapper.toEntity(viewingDetailsDto);
        }

        existingViewingDetails = viewingDetailsRepository.save(existingViewingDetails);

        return viewingDetailsMapper.toDto(existingViewingDetails);
    }

    private void updateViewingDetails(ViewingDetails existingViewingDetails, ViewingDetailsDto viewingDetailsDto) {
        existingViewingDetails.setPlatformId(viewingDetailsDto.getPlatformId());
        existingViewingDetails.setRating(viewingDetailsDto.getRating());
        existingViewingDetails.setLiked(viewingDetailsDto.getLiked());
        existingViewingDetails.setEmotion(viewingDetailsDto.getEmotion());
        existingViewingDetails.setContextType(viewingDetailsDto.getContextType());
        existingViewingDetails.setContextId(viewingDetailsDto.getContextId());
    }


    @Transactional
    public ViewingDetailsDto updateViewingDetails(UUID userId, Long contentId, ViewingDetailsDto viewingDetailsDto) {
        var existingViewingDetails = viewingDetailsRepository.findByUserIdAndContextId(userId, contentId)
                .orElseThrow(() -> new EntityNotExistsException(ViewingDetails.class,
                        "(userId: " + userId + " and contentId: " + contentId + ")"));

        var updatedViewingDetails = viewingDetailsMapper.toEntity(viewingDetailsDto);
        updatedViewingDetails.setId(existingViewingDetails.getId());

        updatedViewingDetails = viewingDetailsRepository.save(updatedViewingDetails);

        return viewingDetailsMapper.toDto(updatedViewingDetails);
    }

    @Transactional
    public void deleteViewingDetails(UUID userId, Long contentId) {
        viewingDetailsRepository.deleteByUserIdAndContextId(userId, contentId);
    }
}
