package org.gcszhn.system.watch;

import java.util.EventObject;

import org.gcszhn.system.service.obj.User;
import org.gcszhn.system.service.obj.User.UserAction;

/**
 * 用户事件处理
 * @author Zhang.H.N
 * @version 1.0
 */
public class UserEvent extends EventObject {
    private static final long serialVersionUID = 658767311654342711L;
    /**用户动作 */
    private UserAction userAction;
    /**
     * 用户事件构造方法
     * @param source 事件源用户
     * @param userAction 用户动作
     */
    public UserEvent(User source, UserAction userAction) {
        super(source);
        this.userAction = userAction;
    }
    /**
     * 获取事件源用户
     * @return 用户对象
     */
    public User getUser() {
        return (User)getSource();
    }
    /**
     * 获取用户动作
     * @return 用户动作枚举类型
     */
    public UserAction getUserAction() {
        return this.userAction;
    }
}