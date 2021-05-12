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

`git push`

用于将本地仓库同步到远程仓库

- 相同分支名   git push <远程主机名> <本地分支名>:<远程分支名>
- 不同分支名   git push <远程主机名> <本地分支名>


# 参考文献
[1] [如何使用git命令行上传项目到github](https://blog.csdn.net/majinggogogo/article/details/81152938)