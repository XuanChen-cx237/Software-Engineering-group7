package com.project.util;

/**
 * 流式响应回调接口
 * 用于处理流式API响应
 */
public interface StreamCallback {
    /**
     * 接收流式响应的token
     * @param token 生成的文本片段
     */
    void onToken(String token);

    /**
     * 响应完成时调用
     */
    default void onComplete() {
        // 默认空实现，可由实现类重写
    }

    /**
     * 发生错误时调用
     * @param error 错误信息
     */
    default void onError(String error) {
        // 默认空实现，可由实现类重写
    }
}