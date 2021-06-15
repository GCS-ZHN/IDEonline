package org.gcszhn;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.velocity.VelocityContext;
import org.gcszhn.system.service.user.User;
import org.gcszhn.system.service.user.UserService;
import org.gcszhn.system.service.velocity.VelocityService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Velocity模板服务单元测试
 * @author Zhang.H.N
 * @version 1.0
 */
public class VelocityServiceTest extends AppTest {
    /**
     * velocity模板引擎测试
     */
    @Autowired
    VelocityService vs;
    @Autowired
    UserService userService;
    @Test
    public void testVelocity() {
        VelocityContext context = new VelocityContext();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        User user = userService.createUser("test", "123456", "test@163.com");
        context.put("user", user);
        context.put("date", sdf.format(new Date()));
        System.out.println(vs.getResult("mail.vm", context));
    }
}