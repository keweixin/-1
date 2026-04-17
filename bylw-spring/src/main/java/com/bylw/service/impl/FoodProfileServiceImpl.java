package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bylw.dto.FoodProfileDTO;
import com.bylw.entity.FoodProfile;
import com.bylw.mapper.FoodProfileMapper;
import com.bylw.service.FoodProfileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FoodProfileServiceImpl implements FoodProfileService {

    @Autowired
    private FoodProfileMapper foodProfileMapper;

    @Override
    public FoodProfile getProfile(Integer userId) {
        LambdaQueryWrapper<FoodProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodProfile::getUserId, userId);
        FoodProfile profile = foodProfileMapper.selectOne(wrapper);
        if (profile == null) {
            profile = new FoodProfile();
            profile.setUserId(userId);
            foodProfileMapper.insert(profile);
        }
        return profile;
    }

    @Override
    public FoodProfile saveOrUpdate(FoodProfileDTO dto) {
        LambdaQueryWrapper<FoodProfile> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FoodProfile::getUserId, dto.getUserId());
        FoodProfile profile = foodProfileMapper.selectOne(wrapper);

        if (profile == null) {
            profile = new FoodProfile();
            profile.setUserId(dto.getUserId());
            profile.setTastePreference(dto.getTastePreference());
            profile.setAllergenInfo(dto.getAllergenInfo());
            profile.setChronicDisease(dto.getChronicDisease());
            profile.setTabooInfo(dto.getTabooInfo());
            profile.setRemark(dto.getRemark());
            profile.setUpdateTime(LocalDateTime.now());
            foodProfileMapper.insert(profile);
        } else {
            profile.setTastePreference(dto.getTastePreference());
            profile.setAllergenInfo(dto.getAllergenInfo());
            profile.setChronicDisease(dto.getChronicDisease());
            profile.setTabooInfo(dto.getTabooInfo());
            profile.setRemark(dto.getRemark());
            profile.setUpdateTime(LocalDateTime.now());
            foodProfileMapper.updateById(profile);
        }
        return profile;
    }
}
