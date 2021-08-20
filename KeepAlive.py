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

import time
import requests
import warnings
import logging
import argparse

warnings.filterwarnings("ignore")
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(name)s - %(levelname)s - %(message)s")

class KeepAlive(object):
    """
    用于保持IDEonline后台在线状态的python脚本
    """
    def __init__(self, cookieValue:str,  domain:str,  scheme="https",  sec=250) -> None:
        """
        Args:
            cookieValue    用户登录后的cookie值，从浏览器获取
            sec                      时间间隔，单位为秒
            scheme            协议类型
            domain            目标域名
        """
        self.__logger = logging.getLogger(KeepAlive.__name__)
        self.__scheme=scheme
        self.__domain=domain
        self.__uri="/backend/keepalive"
        self.__cookieValue =  cookieValue
        self.__session = requests.Session()
        self.__session.cookies.set("JSESSIONID",  cookieValue,  path="/", domain=self.__domain)
        self.__sec = sec
        self.__logger.info("创建会话")

    def run(self):
        while True:
            response = self.__session.get(f"{self.__scheme}://{self.__domain}{self.__uri}",  verify=False)
            content = response.json()
            if content["status"] == 0:
                self.__logger.info("保持登录成功")
            else:
                self.__logger.error(content["info"])
            time.sleep(self.__sec)

    def close(self):
        self.__logger.info("关闭会话")
        self.__session.close()

    __del__ = close

if __name__=="__main__":
    try:
        parser  = argparse.ArgumentParser("该脚本用于IDEonline在线保持，适用于浏览器失焦不运行JS的情况")  
        parser.add_argument("domain", type=str, help="请求的域名")
        parser.add_argument("cookie", type=str, help="从浏览器获取的当前cookie值JSESSIONID")
        parser.add_argument("--scheme", default="https", type=str,  help= "应用层协议类型")
        parser.add_argument("--step", default=5, type=int, help="发送时间间隔")
        args = parser.parse_args()
        keepAlive = KeepAlive(args.cookie,  args.domain,  args.scheme,  args.step)
        keepAlive.run()
    except KeyboardInterrupt:
        print()
        del keepAlive