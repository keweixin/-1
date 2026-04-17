package com.bylw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.bylw.entity.*;
import com.bylw.mapper.*;
import com.bylw.service.SystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SystemServiceImpl implements SystemService {

    @Autowired
    private NoticeMapper noticeMapper;
    @Autowired
    private BannerMapper bannerMapper;
    @Autowired
    private FriendLinkMapper friendLinkMapper;
    @Autowired
    private AboutUsMapper aboutUsMapper;

    @Override
    public List<Notice> getNotices() {
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, 1)
               .eq(Notice::getDeleted, 0)
               .orderByDesc(Notice::getPublishTime);
        return noticeMapper.selectList(wrapper);
    }

    @Override
    public Notice publishNotice(Notice notice) {
        if (notice.getNoticeId() == null) {
            notice.setPublishTime(LocalDateTime.now());
            notice.setStatus(1);
            notice.setDeleted(0);
            noticeMapper.insert(notice);
        } else {
            noticeMapper.updateById(notice);
        }
        return notice;
    }

    @Override
    public Notice updateNotice(Notice notice) {
        noticeMapper.updateById(notice);
        return notice;
    }

    @Override
    public boolean deleteNotice(Integer noticeId) {
        Notice notice = noticeMapper.selectById(noticeId);
        if (notice != null) {
            notice.setDeleted(1);
            noticeMapper.updateById(notice);
            return true;
        }
        return false;
    }

    @Override
    public List<Banner> getBanners() {
        LambdaQueryWrapper<Banner> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Banner::getStatus, 1)
               .orderByAsc(Banner::getSortNo);
        return bannerMapper.selectList(wrapper);
    }

    @Override
    public Banner saveBanner(Banner banner) {
        if (banner.getBannerId() == null) {
            bannerMapper.insert(banner);
        } else {
            bannerMapper.updateById(banner);
        }
        return banner;
    }

    @Override
    public Banner updateBanner(Banner banner) {
        bannerMapper.updateById(banner);
        return banner;
    }

    @Override
    public boolean deleteBanner(Integer bannerId) {
        return bannerMapper.deleteById(bannerId) > 0;
    }

    @Override
    public List<FriendLink> getFriendLinks() {
        LambdaQueryWrapper<FriendLink> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendLink::getStatus, 1)
               .orderByAsc(FriendLink::getSortNo);
        return friendLinkMapper.selectList(wrapper);
    }

    @Override
    public FriendLink saveFriendLink(FriendLink link) {
        if (link.getLinkId() == null) {
            friendLinkMapper.insert(link);
        } else {
            friendLinkMapper.updateById(link);
        }
        return link;
    }

    @Override
    public FriendLink updateFriendLink(FriendLink link) {
        friendLinkMapper.updateById(link);
        return link;
    }

    @Override
    public boolean deleteFriendLink(Integer linkId) {
        return friendLinkMapper.deleteById(linkId) > 0;
    }

    @Override
    public AboutUs getAboutUs() {
        List<AboutUs> list = aboutUsMapper.selectList(null);
        return list.isEmpty() ? null : list.get(0);
    }

    @Override
    public AboutUs updateAboutUs(AboutUs aboutUs) {
        AboutUs existing = getAboutUs();
        if (existing != null) {
            aboutUs.setAboutId(existing.getAboutId());
            aboutUs.setUpdateTime(LocalDateTime.now());
            aboutUsMapper.updateById(aboutUs);
        } else {
            aboutUs.setUpdateTime(LocalDateTime.now());
            aboutUsMapper.insert(aboutUs);
        }
        return aboutUs;
    }
}
