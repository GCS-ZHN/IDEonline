package org.gcszhn.system.service.obj;

import lombok.Data;


/**
 * 用户任务实例
 * @author Zhang.H.N
 * @version 1.0
 */
@Data
public class UserJob {
    /**任务ID */
    String id;
    /**任务命令 */
    String cmd;
    /**任务状态 */
    int status;
    /**任务所有者 */
    User user;
    /**任务标准输入 */
    String stdinfile;
    /**任务标准输出 */
    String stdoutfile;
    /**任务超时 */
    long timeout;
}