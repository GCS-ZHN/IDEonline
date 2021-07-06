# Docker后台容器
IDEonline是基于docker容器的多用户隔离平台。本目录提供了Dockerfile，用户可以通过

`docker build <本目录> -t <自定义标签>`

来构建docker镜像。
# 依赖
## 镜像依赖
Dockerfile的基于的`nvidia/cuda:11.1.1-cudnn-ubuntu16.04`镜像源自[nvidia-docker](https://github.com/NVIDIA/nvidia-docker)或[nvidia-container](https://gitlab.com/nvidia/container-images/cuda/-/tree/master/)，请先安装。
## 软件依赖
### code-server
code-server是vscode的网页版本，也是本后台管理系统的核心提供之一。本后台管理系统使用版本为模认版本为3.9.3，即`code-server_3.9.3_amd64.deb`，请从[cdr/code-server](https://github.com/cdr/code-server)下载所需版本，若文件名不同，请修改`script/install.sh`。下载文件默认需要放在`thirdPackage`子文件夹下。
### anaconda
anaconda是python的打包，本后台管理系统默认使用anaconda自带的jupyterlab。需要手动去[anaconda官网](https://anaconda.org/)下载对应的shell安装包，即`Anaconda3-2020.07-Linux-x86_64.sh`。要求同code-server。
