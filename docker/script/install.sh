#!/bin/bash
function installBaseTool() {
    apt-get -y -q update
    apt-get -y -q install python-software-properties
    apt-get -y -q install software-properties-common
    apt-get -y -q install --assume-yes apt-utils
    apt-get -y -q install openssh-server
    apt-get -y -q install inetutils-ping
    apt-get -y -q install perl
    apt-get -y -q install lsof
    apt-get -y -q install cmake
    apt-get -y -q install unzip
    apt-get -y -q install git
    apt-get -y -q install libgl1-mesa-glx
}
function installGCC() {
    add-apt-repository ppa:ubuntu-toolchain-r/test
    apt-get update
    apt-get install -y gcc-8
    apt-get install -y g++-8

    update-alternatives --install /usr/bin/gcc gcc /usr/bin/gcc-8 100
    update-alternatives --install /usr/bin/g++ g++ /usr/bin/g++-8 100
}
function installPython() {
    packages/Anaconda3-2020.07-Linux-x86_64.sh -b -p /root/anaconda3
    /root/anaconda3/bin/pip install --upgrade pip
    /root/anaconda3/bin/pip install -r .script/requirements.txt -f https://download.pytorch.org/whl/torch_stable.html
}
function installCodeServer() {
    dpkg -i packages/code-server_3.9.3_amd64.deb
    rm -f packages/code-server_3.9.3_amd64.deb
}
function installNodeJs() {
    curl -sL https://deb.nodesource.com/setup_14.x | bash
    apt-get -y -q install nodejs
}
function clear() {
    ln -s /usr/local/cuda/lib64/libcusolver.so /usr/local/cuda/lib64/libcusolver.so.10
    rm -rf packages .script/install.sh .script/requirements.txt
}
function main() {
    cd /root
    installBaseTool
    installGCC
    installNodeJs
    installPython
    installCodeServer
    clear
}

main