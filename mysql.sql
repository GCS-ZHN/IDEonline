--
-- Copyright © 2021 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
--
-- Licensed under the Apache License, Version 2.0 (thie "License");
-- You may not use this file except in compliance with the license.
-- You may obtain a copy of the License at
--
--       http://wwww.apache.org/licenses/LICENSE-2.0
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language govering permissions and
-- limitations under the License.
--

# USE idrb_platform;
# ALTER TABLE idrb_platform.account ADD role VARCHAR(8) NOT NULL DEFAULT 'N';
# ALTER TABLE idrb_platform.account DROP role;

# UPDATE idrb_platform.account SET role='M' WHERE username='zhanghn';
# UPDATE idrb_platform.account SET role='R' WHERE username='root';
# ALTER TABLE idrb_platform.account ADD owner VARCHAR(8) NOT NULL;
/* UPDATE idrb_platform.account SET owner='root' WHERE username='root';
UPDATE idrb_platform.account SET owner='张洪宁' WHERE username='zhanghn';
UPDATE idrb_platform.account SET owner='刘金' WHERE username='liujin';
UPDATE idrb_platform.account SET owner='郑玲燕' WHERE username='zhengly';
UPDATE idrb_platform.account SET owner='夏伟琪' WHERE username='xiawq';
UPDATE idrb_platform.account SET owner='曹端华' WHERE username='bodao';
UPDATE idrb_platform.account SET owner='张滢' WHERE username='zhangying';
UPDATE idrb_platform.account SET owner='王云霞' WHERE username='wangyx';
UPDATE idrb_platform.account SET owner='李丰成' WHERE username='lifengcheng';
UPDATE idrb_platform.account SET owner='牟敏杰' WHERE username='moumj';
UPDATE idrb_platform.account SET owner='张瀚毓' WHERE username='zhanghy';
UPDATE idrb_platform.account SET owner='周莹' WHERE username='zhouy';
UPDATE idrb_platform.account SET owner='路明坤' WHERE username='lumk';
UPDATE idrb_platform.account SET owner='测试用户' WHERE username='dockerTest'; */
# ALTER TABLE idrb_platform.account ADD create_stamp TIMESTAMP NOT NULL DEFAULT NOW();
# UPDATE idrb_platform.account SET create_stamp=date("1997-12-30")
# UPDATE idrb_platform.account SET create_stamp=time("12:30:00")
# UPDATE idrb_platform.account SET create_stamp=str_to_date('2021-04-05 4:30:00', '%Y-%m-%d %h:%i:%s');
# ALTER TABLE idrb_platform.account ADD last_login_stamp TIMESTAMP NOT NULL DEFAULT NOW();
# UPDATE idrb_platform.account SET address='zhanghy@zju.edu.cn' WHERE username='zhanghy';
# UPDATE idrb_platform.account SET address='caodh@zju.edu.cn' WHERE username='bodao';
# SELECT * FROM idrb_platform.account LIMIT 2
UPDATE idrb_platform.account SET role='manager' WHERE role='M';
UPDATE idrb_platform.account SET role='root' WHERE role='R';
UPDATE idrb_platform.account SET role='normal' WHERE role='N';