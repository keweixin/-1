package com.bylw.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class FoodDTO {
    private Integer foodId;
    private Integer merchantId;
    private String merchantName;
    private Integer categoryId;
    private String categoryName;
    private String foodName;
    private BigDecimal originPrice;
    private BigDecimal discountPrice;
    private Integer stockQty;
    private LocalDateTime expireDate;
    private String nutritionDesc;
    private String tasteDesc;
    private String suitablePeople;
    private String allergenInfo;
    private String coverImg;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private Double matchScore;
    private Double cfScore;
    private Double featureScore;
    private String matchReason;
    private List<String> matchedTags;
}
