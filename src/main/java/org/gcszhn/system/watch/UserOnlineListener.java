package org.gcszhn.system.watch;

import java.util.HashMap;
import java.util.List;

import javax.servlet.http.HttpSession;

/**
 * 用户在线情况监听器
 * @author Zhang.H.N
 * @version 1.0
 */
public class UserOnlineListener implements UserListener {
    /**在线用户统计 */
    private static HashMap <String, List<HttpSession>> onlineUsers = new HashMap<>();
    @Override
    public void userRegister(UserEvent ue) {
        synchronized(onlineUsers) {

        }
    }

    @Override
    public void userCancel(UserEvent ue) {
        synchronized(onlineUsers) {

        }
    }

    @Override
    public void userLogin(UserEvent ue) {
        synchronized(onlineUsers) {

        }
    }

    @Override
    public void userLogout(UserEvent ue) {
        synchronized(onlineUsers) {

        }
    }
}