#
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
#

FROM nvidia/cuda:11.1.1-cudnn-ubuntu16.04

ENV PATH=/root/anaconda3/bin:/usr/local/bin:$PATH
COPY thirdPackage /root/packages
COPY script /root/.script
WORKDIR /root/
RUN .script/install.sh

ENTRYPOINT ["/root/.script/tini","--","/root/.script/dip.sh"]
CMD [ "zhanghn"]
