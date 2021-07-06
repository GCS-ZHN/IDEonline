#!/bin/bash
# Author: Zhang Hongning
# Email : zhang.h.n@foxmail.com
# This script is essential for init this docker container.
# Please not modify it unless it's necessary. 
set -e
trap "exit" TERM ## deal with SIGTERM as init PID
ROOT=`dirname $0`
USER=$1
CMD1="jupyter-lab  \
    --ip 0.0.0.0   \
    --port 8888    \
    --allow-root   \
    --no-browser   \
    --notebook-dir /public/home/$USER \
    --LabApp.password='' \
    --LabApp.token='' \
    --LabApp.base_url='/jupyter/' \
    --LabApp.allow_origin=* \
    1>/root/jupyter-lab.log 2>&1 \
    "
CMD2="code-server  \
    --host 0.0.0.0 \
    --port 8067    \
    --locale zh-cn \
    --auth none \
    --user-data-dir /public/home/$USER/.vscode-server \
    --extensions-dir /public/home/$USER/.local/share/code-server/extensions \
    --config /public/home/$USER/.config/code-server/config.yaml \
    1>/root/code-server.log 2>&1 \
    "
$ROOT/dip "$CMD1" "$CMD2"