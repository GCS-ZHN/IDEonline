#!/bin/bash
# Copyright © 2021 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
#
# Licensed under the Apache License, Version 2.0 (thie "License");
# You may not use this file except in compliance with the license.
# You may obtain a copy of the License at
#
#       http://wwww.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language govering permissions and
# limitations under the License.
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