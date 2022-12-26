# convert-video 音视频转换MP3、MP4服务

本服务用于将各类常见视频文件格式转换为可供在线播放的MP4文件格式，支持回写、回调等。

---

## 说明文档

[![快速开始](https://img.shields.io/badge/%E8%AF%95%E7%94%A8-%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B-blue.svg)](readme.md)
[![详细介绍](https://img.shields.io/badge/%E6%8E%A5%E5%8F%A3-%E8%AF%A6%E7%BB%86%E4%BB%8B%E7%BB%8D-blue.svg)](detail.md)

---

## 特性

* 支持多种文件输入方式：文件路径、http（get）下载、ftp，可扩展
* 支持多种文件格式：asx，asf，mpg，wmv，3gp，mp4，mov，avi，flv。
* 支持多种文件回写方式：文件路径（path）、http协议上传（url）、FTP服务上传（ftp），可扩展。
* 支持转换结果回调
* 支持失败延迟重试

## 依赖

* `jdk8`: 编译、运行环境
* `maven`: 编译打包，只运行`jar`不需要，建议`V3.6.3`以上版本
* `FFmpeg`: 视频转换工具， [官网](http://www.ffmpeg.org/)
* `rabbitMQ`: 重试机制依赖MQ延迟队列，需安装插件 `rabbitmq_delayed_message_exchange`

## 快速启动

1. 使用`mvn clean package -Dmaven.test.skip=true`编译

2. 修改配置`application.yml`：
   
   1. 输出文件所在文件夹:`convert.video.outPutPath`:
   
   > Windows： D:/work/temp
   > 
   > Linux： /work/temp/
   
   2. ffmpeg所在文件夹和文件名: `convert.video.ffmpegPath`:
   
   > Windows： C:/Program Files (x86)/FormatFactory/ffmpeg.exe
   > 
   > Linux： /app/ffmpeg.sh
   
   3. 如需支持失败重试功能，需启用 RabbitMQ 功能
      
      > `spring.rabbitmq.host`: RabbitMQ IP地址， 例：10.3.214.12
      > 
      > `spring.rabbitmq.port`: RabbitMQ 端口号, 例： 5672
      > 
      > `spring.rabbitmq.username`: RabbitMQ 用户名, 例： guest
      > 
      > `spring.rabbitmq.password`: RabbitMQ 用户密码, 例： guest
      > 
      > `spring.rabbitmq.listener.direct.auto-startup`: RabbitMQ 生产者 开关, 例： true | false, true: 标识启用功能
      > 
      > `spring.rabbitmq.listener.simple.auto-startup`: RabbitMQ 消费者 开关, 例： true | false, true: 标识启用功能
      > 
      > `convert.retry.max`: 重试次数（0-8），0标识不重试, 若出现异常情况只记录日志， 大于1（最大8）：标识失败重试的次数, 将会在以下时间重试（5min, 10min, 30min, 1h, 2h, 4h, 8h, 16h），例：3, 标识将在5分钟后进行第一次重试，如果还失败，将在10分钟后（即初次转换15分钟后）进行第二次重试. 如果还失败，将在30分钟后（即初次转换45分钟后）进行第三次重试

3. 确认文件目录结构

```
│  application.yml
│  convertvideo-{版本号}.jar
│  conf（文件夹）
```

5. 以管理员身份运行
   
   > Windows： javaw -jar convertvideo-{版本号}.jar
   > 
   > Linux： nohup java -jar convertvideo-{版本号}.jar &

6. 浏览器访问 `http://{ip}:{端口}` , 返回 **启动成功** 标识项目启动正常

## 常见问题

1. 项目日志在哪里？
   
   运行目录下logs文件夹内

2. 项目启动失败，日志中有`The Tomcat connector configured to listen on port 8080 failed to start. The port may already be in use or the connector may be misconfigured.`的报错
   
   端口被占用，修改`application.yml`中`server.port`, 改为其他端口

3. Windows `...  libx264  ...` 错误
   
   ffmpeg缺少依赖包, 重新下载[全量编译包](https://www.gyan.dev/ffmpeg/builds/ffmpeg-git-full.7z) 替换即可