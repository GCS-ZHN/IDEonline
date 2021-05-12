# 一、Git命令
`git clone`

用于从远程仓库克隆项目。

- HTTPS  `git clone https://github.com/GCS-ZHN/IDRBonline.git`
- SSH    `git clone git@github.com:GCS-ZHN/IDRBonline.git`

`git init`

在当前文件夹建立git本地仓库，会出现`.git`本地文件夹。

`git add <DIR>`

添加本地文件夹进入仓库。`git init`后需要add才能将内容递交加入仓库。添加本文件夹`git add .`。

`git commit -m <COMMIT>`

`git add`只是将项目加入暂存区，利用commit最终将项目递交到本地仓库。参数为提交的备注评论。

`git commit -am <COMMIT>`可以用于删除。

`git push`

用于将本地仓库同步到远程仓库

- 相同分支名   git push <远程主机名> <本地分支名>:<远程分支名>
- 不同分支名   git push <远程主机名> <本地分支名>

# 二、服务器上自建git仓库
通过`git init`事实上就建立了一个本地当前目录仓库（会产生.git子文件夹），而`git init --bare repo.git`则在当前目录下建立了一个无workspace的仓库，目录名repo.git。

git命令可以基于HTTPS协议或是SSH协议进行访问，且根据url配置自动识别哪种协议。对于自己服务器建立的git仓库，建议就直接使用SSH协议，现成无需配置。例如用户`zhanghn`的`/public/home/zhanghn/repo`是一个git仓库（下面有`.git`子文件夹或者bare模式的配置文件），可以用类ssh格式URL访问。

    git clone zhanghn@10.71.128.68:/public/home/zhanghn/repo

在这里，和ssh一样，倘若配置了ssh密钥免密访问，则不用密码，否则输入用户密码即可。我们与gitee（码云）和github提供的SSH协议URL

    git@gitee.com:GCSZHN/IDRBonline.git
    git@github.com:GCS-ZHN/IDRBonline.git

# 参考文献
[1] [如何使用git命令行上传项目到github](https://blog.csdn.net/majinggogogo/article/details/81152938)