package com.bylw.dto;

import lombok.Data;
import java.util.List;

@Data
public class RecommendResultDTO {
    private List<FoodDTO> foods;
    private Integer candidateCount;
    private Integer healthFilteredCount;
    private Integer tasteMatchedCount;
    private Integer finalCount;
    private Boolean coldStart;
}
