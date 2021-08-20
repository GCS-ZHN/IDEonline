/**
 * Copyright © 2021 <a href="mailto:zhang.h.n@foxmail.com">Zhang.H.N</a>.
 *
 * Licensed under the Apache License, Version 2.0 (thie "License");
 * You may not use this file except in compliance with the license.
 * You may obtain a copy of the License at
 *
 *       http://wwww.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language govering permissions and
 * limitations under the License.
 */
#include <string>
#include <pthread.h>
#include <stdlib.h>
#include <iostream>
using namespace std;

/**
 * 执行一条系统命令的独立线程函数
 * cmd 命令字符串
*/
void* threadExc(void* cmd) {
    return new int(system((char*)cmd));
}

/**
 * 该程序是dip命令的源码，若二进制文件无法运行，可以重新编译
*/
int main(int argc, char const *argv[]) {
    if (argc == 1) {
        cerr << "Please input at least one shell command!\n";
        return 0;
    }
    pthread_t tidset[argc - 1];
    for (int i = 1; i< argc; i++) {
        int status = pthread_create(&tidset[i-1], NULL, threadExc, (void*)argv[i]);
        if (status) {
            cerr << "Initialize failed for exec: '" << argv[i] << endl;
        } else {
            cout << "Initialize successfully for exec: '" << argv[i] << endl;
        }
    }
    /*
    线程等待，类似java中多线程的join，若是pthread_exit(NULL)，主线程退出虽然保留了子线程，
    但会变成僵尸进程[dip]<defunct>，而原先使用bash脚本为init进程和dip父进程，bash脚本未能读取dip而先行退出
    导致docker容器无法关闭
    */
    for (int i = 0; i < argc -1; i++) {
        pthread_join(tidset[i], NULL);
    }
    return 0;
}

