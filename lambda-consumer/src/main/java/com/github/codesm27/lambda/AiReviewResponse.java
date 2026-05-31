package com.github.codesm27.lambda;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class AiReviewResponse {
    private List<Comment> comments;
    private String summary;
    private Integer score;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Comment {
        private String file;
        private Integer line;
        private String severity;
        private String category;
        private String comment;
    }
}
