package org.example.dto;

import lombok.*;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ValidationResponseDto {
    
    private boolean valid;
    private String message;
}