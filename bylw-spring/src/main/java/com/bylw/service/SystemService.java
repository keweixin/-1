package com.bylw.service;

import com.bylw.entity.*;

import java.util.List;

public interface SystemService {
    List<Notice> getNotices();
    Notice publishNotice(Notice notice);
    Notice updateNotice(Notice notice);
    boolean deleteNotice(Integer noticeId);

    List<Banner> getBanners();
    Banner saveBanner(Banner banner);
    Banner updateBanner(Banner banner);
    boolean deleteBanner(Integer bannerId);

    List<FriendLink> getFriendLinks();
    FriendLink saveFriendLink(FriendLink link);
    FriendLink updateFriendLink(FriendLink link);
    boolean deleteFriendLink(Integer linkId);

    AboutUs getAboutUs();
    AboutUs updateAboutUs(AboutUs aboutUs);
}
