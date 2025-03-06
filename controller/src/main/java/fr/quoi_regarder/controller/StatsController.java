package fr.quoi_regarder.controller;

import fr.quoi_regarder.dto.response.ApiResponse;
import fr.quoi_regarder.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
@RequiredArgsConstructor
public class StatsController {
    private final UserRepository userRepository;

    @GetMapping("/user-count")
    public ResponseEntity<ApiResponse<Long>> getUserCount() {
        Long count = userRepository.count();

        return ResponseEntity.ok(
                ApiResponse.success("Total user count retrieved successfully", count, HttpStatus.OK)
        );
    }
}