<h1 style="text-align: center">IDEonline后台管理系统</h1>
<div style="text-align: center">

[![AUR](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/GCS-ZHN/IDRBonline/blob/master/LICENSE)
[![star](https://gitee.com/GCSZHN/IDRBonline/badge/star.svg?theme=white)](https://gitee.com/elunez/eladmin)
[![GitHub stars](https://img.shields.io/github/stars/GCS-ZHN/IDRBonline.svg?style=social&label=Stars)](https://github.com/elunez/eladmin)
[![GitHub forks](https://img.shields.io/github/forks/GCS-ZHN/IDRBonline.svg?style=social&label=Fork)](https://github.com/elunez/eladmin)

</div>
这是一个基于Spring Boot和Spring MVC开发的IDRB实验室在线平台IDEonline的后台管理系统。涉及nginx、mysql、redis和docker等。

# 优化计划
- 用户登录
  - 登录时将登录时间信息更新至数据库
- 数据持久化
  - 持久化用户会话
  - 持久化用户任务
    - 需要保证恢复的任务能够正常终止
    - 恢复时需要检测后台任务还在不在
    - 将UserJob的User属性改为String的username属性，避免存在一个用户的多个User对象
- 分布式同步
  - 多个节点的在线用户会话同步
  - 多个节点的用户任务列表同步
  - 在多个节点启动后台系统，使用nginx进行负载均衡
- 搭建管理员平台。
  - 平台入口和jupyter、vscode放在一起，侧边栏添加一个管理员平台按钮（仅管理员登录时出现），普通用户试图则登录提示没有权限。
  - 引入管理员权限验证
    - root用户可以登录平台，创建和管理manager用户、普通用户
    - manger用户可以登录平台，创建和管理普通用户，当然可以查看所有用户信息，只是只能修改普通用户
  - 获取所有用户信息
  - 每个用户具体可查看任务列表
- 搭建帮助。

