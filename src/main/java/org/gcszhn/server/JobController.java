package org.gcszhn.server;

import java.io.PipedOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.logging.log4j.Level;
import org.gcszhn.server.ResponseResult.JobResult;
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
     * @param cmd 后台命令
     * @param args 命令参数
     * @return 提交状态
     */
    @GetMapping("/submit")
    public JobResult doSubmitJob(
        @RequestParam String stdoutf, 
        @RequestParam String tmout,
        @RequestParam String cmd,
        @RequestParam String args,
        @RequestParam String unit
        ) {
        
        JobResult res = new JobResult();
        try {
            HttpSession session = request.getSession(false);
            if (session !=null && !cmd.equals("")) {
                User user = (User) session.getAttribute("user");
                if (user != null) {
                    //超时时间配置
                    long timeout = 1;
                    if(!tmout.equals("")) timeout = Long.parseLong(tmout);
                    TimeUnit timeOutUnit = TimeUnit.HOURS;
                    switch(unit) {
                        case "s": timeOutUnit = TimeUnit.SECONDS;break;
                        case "m": timeOutUnit = TimeUnit.MINUTES;break;
                        case "h": timeOutUnit = TimeUnit.HOURS;break;
                    }


                    DockerNode dockerNode = dockerService.getDockerNodeByHost(
                        user.getAliveNode().getHost());
                    /**用户任务实例 */
                    UserJob userJob = new UserJob();
                    Date createTime = new Date();
                    userJob.setId("background"+createTime.getTime());
                    userJob.setCmd(args.equals("")?cmd:cmd+" "+args);
                    userJob.setUser(user);
                    userJob.setCreatedTime(createTime);
                    userJob.setStdin(new PipedOutputStream());
                    userJob.setStdoutfile(stdoutf);
                    userJob.setTimeout(timeout);
                    userJob.setTimeOutUnit(timeOutUnit);
                    AppLog.printMessage("Prepared for start a background job");
                    /**启动异步任务 */
                    userService.startAsyncJob(user, dockerNode, userJob);
                    res.setStatus(0);
                    res.setJobId(userJob.getId());
                    res.setJobstatus("running");
                } 
            }
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            res.setStatus(1);
        }
        return res;
    }

    /**
     * 处理终止任务的请求
     * @param jobid 要求主动终止的任务ID
     * @return 任务状态
     */
    @GetMapping("/terminate")
    public JobResult doTerminateJob(@RequestParam String jobid) {
        JobResult res = new JobResult();
        try {
            HttpSession session = request.getSession(false);
            User user;
            if (session != null && (user=(User)session.getAttribute("user"))!= null) {
                UserJob userJob = userService.getUserBackgroundJob(user.getAccount(), jobid);
                if (userJob == null) {
                    res.setStatus(-1);
                    res.setJobstatus("No job found");
                } else {
                    //发送^C关闭信号，不一定关闭
                    userJob.close();
                    //移除在线列表
                    userService.removeUserBackgroundJob(user.getAccount(), userJob);
                    res.setStatus(0);
                    res.setJobstatus("exit");
                    res.setJobId(jobid);
                }
            }
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
            res.setStatus(1);
        }
        return res;
    }

    @GetMapping("/joblist")
    public JSONArray doJobList() {
        JSONArray arrayList = new JSONArray();
        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                User user = (User) session.getAttribute("user");
                if (user != null) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    userService.getUserJobSet(user.getAccount()).forEach((UserJob userJob)->{
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("jobid", userJob.getId());
                        jsonObject.put("cmd", userJob.getCmd());
                        jsonObject.put("stdoutf", userJob.getStdoutfile());
                        jsonObject.put("created", sdf.format(userJob.getCreatedTime()));
                        arrayList.add(jsonObject);
                    });
                }
            }
        } catch (Exception e) {
            AppLog.printMessage(null, e, Level.ERROR);
        }
        return arrayList;
    }
}