/*
 * Copyright © 2021 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 *
 * Licensed under the Apache License, Version 2.0 (thie "License");
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at
 *
 *       http://wwww.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language govering permissions and
 * limitations under the License.
 */
package org.gcszhn.server.http;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import org.apache.logging.log4j.Level;
import org.gcszhn.server.tools.ResponseResult.JobResult;
import org.gcszhn.system.log.AppLog;
import org.gcszhn.system.log.HttpRequestLog;
import org.gcszhn.system.service.docker.DockerService;
import org.gcszhn.system.service.user.User;
import org.gcszhn.system.service.user.UserJob;
import org.gcszhn.system.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 后台任务请求控制器
 * @author Zhang.H.N
 * @version 1.2
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
    @GetMapping("/submitjob")
    public JobResult doSubmitJob(
        @RequestParam String stdoutf, 
        @RequestParam String tmout,
        @RequestParam String cmd,
        @RequestParam String args,
        @RequestParam String unit
        ) {
        HttpRequestLog.log(request);
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

                    //命令的处理
                    cmd = cmd.strip();
                    String[] argArray = args.strip().split("\\s+");
                    String[] cmds = new String[argArray.length + 1];
                    cmds[0] = cmd;
                    for (int i = 1; i < cmds.length; i++) {
                        cmds[i] = argArray[i-1];
                    }

                    /**用户任务实例 */
                    UserJob userJob = new UserJob();
                    Date createTime = new Date();
                    userJob.setId("background"+createTime.getTime());
                    userJob.setHost(user.getAliveNode().getHost());
                    userJob.setCmds(cmds);
                    userJob.setAccount(user.getAccount());
                    userJob.setCreatedTime(createTime);
                    userJob.setStdoutfile(stdoutf.equals("")?"default":stdoutf);
                    userJob.setTimeout(timeout);
                    userJob.setTimeOutUnit(timeOutUnit);
                    AppLog.printMessage("Prepared for start a background job");
                    /**启动异步任务 */
                    userService.startUserJob(userJob);
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
    @GetMapping("/terminatejob")
    public JobResult doTerminateJob(@RequestParam String jobid) {
        HttpRequestLog.log(request);
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
                    userService.stopUserJob(userJob);
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
    /**
     * 获取该用户所有节点下的任务
     * @return 节点对象
     */
    @GetMapping("/joblist")
    public JSONArray doJobList() {
        HttpRequestLog.log(request);
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
                        jsonObject.put("node", userJob.getHost());
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