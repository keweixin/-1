package com.bylw.service;

import com.bylw.entity.FoodProfile;
import com.bylw.dto.FoodProfileDTO;

public interface FoodProfileService {
    FoodProfile getProfile(Integer userId);
    FoodProfile saveOrUpdate(FoodProfileDTO dto);
}
