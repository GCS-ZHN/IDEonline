FROM nvidia/cuda:11.1.1-cudnn-ubuntu16.04

ENV PATH=/root/anaconda3/bin:/usr/local/bin:$PATH
COPY thirdPackage /root/packages
COPY script /root/.script
WORKDIR /root/
RUN .script/install.sh

ENTRYPOINT ["/root/.script/tini","--","/root/.script/dip.sh"]
CMD [ "zhanghn"]
