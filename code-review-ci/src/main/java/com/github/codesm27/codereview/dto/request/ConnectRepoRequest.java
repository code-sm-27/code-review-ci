package com.github.codesm27.codereview.dto.request;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;

@Data
public class ConnectRepoRequest {
    @NotBlank
    private String fullName;
    private String description;
    @NotBlank
    private String url;
}
