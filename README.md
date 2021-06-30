<h1 style="text-align: center">IDEonline后台管理系统</h1>
<div style="text-align: center">

![AUR](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)
![star](https://gitee.com/GCSZHN/IDRBonline/badge/star.svg?theme=white)
![GitHub stars](https://img.shields.io/github/stars/GCS-ZHN/IDRBonline.svg?style=social&label=Stars)
![GitHub forks](https://img.shields.io/github/forks/GCS-ZHN/IDRBonline.svg?style=social&label=Fork)

</div>
这是一个基于Spring Boot开发的IDRB实验室在线平台IDEonline的后台管理系统。涉及与redis、mysql、docker等交互。整个平台的核心部分是各内网服务器节点上的docker容器部署。利用mysql持久化用户信息，利用nginx进行动态反向代理和负载均衡，利用redis+lua实现nginx与java spring后台的会话共享。利用Vue+nodeJS+webpack搭建前端访问页面。

# 应用启动
`java -jar IDEonline-***.jar --server.port=8080`

相应启动参数可以如`--server.port=8080`这样进行指定，也可以将资源文件内配置文件修改后重新打包。

# 优化计划
- 数据持久化
  - 持久化用户会话
  - 持久化用户任务
    - 需要保证恢复的任务能够正常终止
    - 恢复时需要检测后台任务还在不在
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
- 解决选择默认进入jupyter时不时产生bad gateway的问题。
  - 当前依据vscode进行启动测试，jupyter不一定启动完成
  - 给启动设置timeout，否则会一直等待

