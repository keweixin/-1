package com.bylw.common;

/**
 * 系统常量定义
 */
public class Constants {

    // 角色
    public static final String ROLE_ADMIN = "admin";
    public static final String ROLE_USER = "user";

    // 订单状态
    public static final String ORDER_STATUS_PENDING = "待支付";
    public static final String ORDER_STATUS_PAID = "待接单";
    public static final String ORDER_STATUS_ACCEPTED = "待配送";
    public static final String ORDER_STATUS_DELIVERING = "配送中";
    public static final String ORDER_STATUS_COMPLETED = "已完成";
    public static final String ORDER_STATUS_CANCELLED = "已取消";
    public static final String ORDER_STATUS_REFUNDED = "已退款";

    // 支付状态
    public static final String PAY_STATUS_UNPAID = "未支付";
    public static final String PAY_STATUS_PAID = "已支付";

    // 配送状态
    public static final String DELIVERY_STATUS_PENDING = "待分配";
    public static final String DELIVERY_STATUS_ACCEPTED = "待取货";
    public static final String DELIVERY_STATUS_DELIVERING = "配送中";
    public static final String DELIVERY_STATUS_COMPLETED = "已完成";
    public static final String DELIVERY_STATUS_REJECTED = "已拒绝";

    // 售后申诉类型
    public static final String REASON_QUALITY = "质量问题";
    public static final String REASON_DELAY = "配送延迟";
    public static final String REASON_DAMAGE = "包装破损";
    public static final String REASON_OTHER = "其他";

    // 售后处理状态
    public static final String HANDLE_STATUS_PENDING = "待处理";
    public static final String HANDLE_STATUS_APPROVED = "已通过";
    public static final String HANDLE_STATUS_REJECTED = "已驳回";

    // 社区帖子审核状态
    public static final String AUDIT_STATUS_PENDING = "待审核";
    public static final String AUDIT_STATUS_APPROVED = "已通过";
    public static final String AUDIT_STATUS_REJECTED = "已驳回";

    // 积分变动类型
    public static final String CHANGE_TYPE_EARN = "earn";
    public static final String CHANGE_TYPE_SPEND = "spend";

    // 积分来源类型
    public static final String SOURCE_SIGN = "每日签到";
    public static final String SOURCE_POST = "发布帖子";
    public static final String SOURCE_ORDER = "订单奖励";
    public static final String SOURCE_REVIEW = "商品评价";
    public static final String SOURCE_EXCHANGE = "积分兑换";
    public static final String SOURCE_ENV_REWARD = "环保积分奖励";  // 减少食品浪费，每完成一单按购买数量奖励环保积分
    public static final String SOURCE_POINTS_DEDUCT = "积分抵扣";

    // 推荐目标类型
    public static final String TARGET_TYPE_FOOD = "food";
    public static final String TARGET_TYPE_ARTICLE = "article";
    public static final String TARGET_TYPE_RECIPE = "recipe";

    // 行为类型
    public static final String BEHAVIOR_VIEW = "view";
    public static final String BEHAVIOR_FAVORITE = "favorite";
    public static final String BEHAVIOR_LIKE = "like";
    public static final String BEHAVIOR_PURCHASE = "purchase";
    public static final String BEHAVIOR_COMMENT = "comment";

    // ========== 业务规则常量（魔法数字提取）==========

    /** 积分兑换比例：1元 = 100积分 */
    public static final int POINTS_PER_YUAN = 100;

    /** 签到单次奖励积分 */
    public static final int SIGN_IN_POINTS = 10;

    /** 环保积分奖励：每件食品奖励积分 */
    public static final int ENV_POINTS_PER_ITEM = 1;

    /** 环保积分奖励：单次上限（防止刷积分） */
    public static final int ENV_POINTS_PER_ORDER_MAX = 100;

    /** 订单超时时间（分钟），超时未支付自动取消 */
    public static final int ORDER_TIMEOUT_MINUTES = 30;

    /** 定时任务执行间隔（毫秒），每分钟检查一次超时订单 */
    public static final long TASK_INTERVAL_MS = 60_000L;
}
