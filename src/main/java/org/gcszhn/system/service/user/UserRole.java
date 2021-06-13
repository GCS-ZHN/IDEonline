package org.gcszhn.system.service.user;

/**
 * 用户角色的枚举
 * @author Zhang.H.N
 * @version 1.0
 */
public enum UserRole {
    /**root用户、管理员用户、普通用户 */
    root("root", 0), manager("manager", 1), normal("normal", 10);
    /**用户ID */
    private final String id;
    /**权限等级，非负数，越小权限越大 */
    private final int privilege;
    /**
     * 构造方法
     * @param id 用户ID
     * @param privilege 权限等级
     */
    UserRole(String id, int privilege) {
        this.id = id;
        this.privilege = privilege<0?0:privilege;
    }
    /**
     * 权限比较，判断是否具有管理对方的权限
     * @param oth 其他待比较角色
     * @return true代表拥有更高的权限，为上级
     */
    public boolean hasPrivilegeTo(UserRole oth) {
        return this.privilege < oth.privilege;
    }
    /**
     * 获取角色ID
     * @return 角色ID
     */
    public String toString() {
        return this.id;
    }
    
}