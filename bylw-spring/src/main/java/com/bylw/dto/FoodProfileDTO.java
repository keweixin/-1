package com.bylw.dto;

import lombok.Data;

@Data
public class FoodProfileDTO {
    private Integer userId;
    private String tastePreference;
    private String allergenInfo;
    private String chronicDisease;
    private String tabooInfo;
    private String remark;
}
