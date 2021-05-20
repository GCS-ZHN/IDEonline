<h1 style="text-align: center">IDEonline后台管理系统</h1>
<div style="text-align: center">

[![AUR](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://github.com/GCS-ZHN/IDRBonline/blob/master/LICENSE)
[![star](https://gitee.com/GCSZHN/IDRBonline/badge/star.svg?theme=white)](https://gitee.com/elunez/eladmin)
[![GitHub stars](https://img.shields.io/github/stars/GCS-ZHN/IDRBonline.svg?style=social&label=Stars)](https://github.com/elunez/eladmin)
[![GitHub forks](https://img.shields.io/github/forks/GCS-ZHN/IDRBonline.svg?style=social&label=Fork)](https://github.com/elunez/eladmin)

</div>
这是一个基于Spring Boot和Spring MVC开发的IDRB实验室在线平台IDEonline的后台管理系统。涉及nginx、mysql、redis和docker等。

# 优化计划
- 容器闲置超时，自动关闭容器。下次访问时重新启动。
- 优化Nginx+Lua+Redis的访问。
- 搭建管理员平台。
- 搭建帮助。