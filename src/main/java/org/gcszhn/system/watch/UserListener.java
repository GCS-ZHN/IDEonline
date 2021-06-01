package org.gcszhn.system.watch;

import java.util.EventListener;

/**
 * 用户监听器
 * @author Zhang.H.N
 * @version 1.0
 */
public interface UserListener extends EventListener {
    /**
     * 用户注册时响应
     * @param ue 用户事件
     */
    public void userRegister(UserEvent ue);
    /**
     * 用户注销时响应
     * @param ue 用户事件
     */
    public void userCancel(UserEvent ue);
    /**
     * 用户登录时响应
     * @param ue 用户事件
     */
    public void userLogin(UserEvent ue);
    /**
     * 用户登出时响应
     * @param ue 用户事件
     */
    public void userLogout(UserEvent ue);
}