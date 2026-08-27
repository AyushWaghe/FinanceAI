package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserCategoriesResponse {
    private Integer categoryId;
    private String title;
}
