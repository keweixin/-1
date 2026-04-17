package com.bylw.service;

import com.bylw.entity.Food;
import java.util.List;

public interface ExpiryReminderService {
    List<Food> getExpiringFoods(Integer userId, int hoursAhead);
}
