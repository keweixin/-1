package com.bylw.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("recipe_favorite")
public class RecipeFavorite {

    @TableId(type = IdType.AUTO)
    private Integer favoriteId;

    private Integer userId;
    private Integer recipeId;
    private LocalDateTime createTime;
}
