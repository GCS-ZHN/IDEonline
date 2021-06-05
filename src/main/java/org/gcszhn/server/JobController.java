package org.gcszhn.server;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


import org.gcszhn.server.ResponseResult.StatusResult;
import org.gcszhn.system.service.DockerService;
import org.gcszhn.system.service.UserService;
import org.gcszhn.system.service.obj.DockerNode;
import org.gcszhn.system.service.obj.User;
import org.gcszhn.system.service.obj.UserJob;
import org.gcszhn.system.service.until.AppLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台任务请求控制器
 * @author Zhang.H.N
 * @version 1.0
 */
@RestController
public class JobController {
    /**Docker服务 */
    @Autowired
    DockerService dockerService;
    /**用户服务 */
    @Autowired
    UserService userService;

    /**web请求对象 */
    @Autowired
    HttpServletRequest request;
    /**
     * 提交后台任务
     * @param stdinf 标准输入文件
     * @param stdoutf 标准输出文件
     * @param timeout 超时时间
     * @param cmd 后台命令字符串
     * @return 提交状态
     */
    @GetMapping("/submit")
    public StatusResult doSubmitJob(
        @RequestParam String stdinf, 
        @RequestParam String stdoutf, 
        @RequestParam String tmout,
        @RequestParam String cmd) {
        
        StatusResult res = new StatusResult();
        res.setStatus(1);

        HttpSession session = request.getSession(false);
        if (session !=null && !cmd.equals("")) {
            User user = (User) session.getAttribute("user");
            if (user != null) {
                //超时时间配置
                long timeout = 1;
                try {
                     if(!tmout.equals("")) timeout = Long.parseLong(tmout);
                } catch (NumberFormatException e) {
                    AppLog.printMessage(e.getMessage());
                    return res;
                }

                DockerNode dockerNode = dockerService.getDockerNodeByHost(
                    user.getAliveNode().getHost());
                /**用户任务实例 */
                UserJob userJob = new UserJob();
                userJob.setStatus(1);
                userJob.setId("background"+userJob.hashCode());
                userJob.setCmd(cmd);
                userJob.setUser(user);
                userJob.setStdinfile(stdinf);
                userJob.setStdoutfile(stdoutf);
                userJob.setTimeout(timeout);
                AppLog.printMessage("Prepared for start a background job");
                /**启动异步任务 */
                userService.startAsyncJob(user, dockerNode, userJob);
                res.setStatus(0);
            } 
        }
        return res;
    }
}