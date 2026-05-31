package com.github.codesm27.codereview.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepoDto {
    private Long id;
    private String fullName;
    private String description;
    private String url;
    private LocalDateTime createdAt;
}
