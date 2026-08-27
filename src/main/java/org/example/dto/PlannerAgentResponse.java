package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlannerAgentResponse {
    private String llmResponse;
    private String summary;
    private boolean summaryGenerated;
}
