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
public class ReviewDto {
    private Long id;
    private Long prId;
    private String file;
    private Integer lineNumber;
    private String severity;
    private String category;
    private String comment;
    private LocalDateTime createdAt;
}
