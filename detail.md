**Convert MP4 Service** 

---

## 说明文档

[![快速开始](https://img.shields.io/badge/%E8%AF%95%E7%94%A8-%E5%BF%AB%E9%80%9F%E5%BC%80%E5%A7%8B-blue.svg)](readme.md)
[![详细介绍](https://img.shields.io/badge/%E6%8E%A5%E5%8F%A3-%E8%AF%A6%E7%BB%86%E4%BB%8B%E7%BB%8D-blue.svg)](detail.md)

---

**视频转换MP4服务**

# 简介

本服务用于将各类常见视频文件格式转换为可供在线播放的MP4文件格式。

本服务依赖于FFmpeg，需要先行在系统中安装此应用。

FFmpeg的官方网址：http://www.ffmpeg.org/

FFMpeg下载页面：**Windows 下载 ___full.7z版本**

1. https://github.com/BtbN/FFmpeg-Builds/releases
2. https://www.gyan.dev/ffmpeg/builds/
3. https://www.gyan.dev/ffmpeg/builds/ffmpeg-git-full.7z

## FFMpeg 安装

### windows

[最新完整版下载地址](https://www.gyan.dev/ffmpeg/builds/ffmpeg-git-full.7z)

下载解压即可。

### linux

[下载地址](http://www.ffmpeg.org/releases/ffmpeg-5.0.1.tar.gz)

```shell
# 解压
tar -zxvf ffmpeg-5.0.1.tar.gz
# 更新 asm
yum install yasm.x86_64 -y
# 配置
./configure --enable-ffplay --enable-ffserver
# 编译
make;make install
# 安装 （安装目录： /usr/local/bin/）
./configure --prefix=PREFIX

# 测试
ffmpeg -i test.map4 test.avi
```

ubuntu

```shell
sudo add-apt-repository universe
sudo apt update
sudo apt install ffmpeg
```

# 配置说明

本服务的所有配置信息均在于jar包同级文件夹中的application.yml中，默认内容如下：

```yml
# Tomcat
server:
  # 端口号
  port: 8080
  tomcat:
    uri-encoding: UTF-8
    threads:
      max: 1000
      min-spare: 30
    # 超时时间
    connection-timeout: 5000

#开放端点用于SpringBoot Admin的监控
management:
  health:
    rabbit:
      # 是否检测rabbit
      enabled: false
  endpoints:
    web:
      exposure:
        # 开放端点用于SpringBoot Admin的监控
        include: '*'

spring:
  boot:
    admin:
      client:
        url: http://localhost:9999
  # RabbitMQ设置
  rabbitmq:
    # 访问地址
    host: 127.0.0.1
    # 端口
    port: 5672
    # 用户名
    username: guest
    # 密码
    password: guest
    # 监听设置
    listener:
      # 生产者
      direct:
        # 自动启动开关
        auto-startup: false
      # 消费者
      simple:
        # 自动启动开关
        auto-startup: false

  application:
    # 应用名称。如果启用nacos，此值必填
    name: com.thinkdifferent.convertvideo
  cloud:
    # Nacos的配置。
    # 如果启用Nacos服务作为配置中心，
    # 则此部分之后的内容均可以在Nacos配置中心中管理，
    # 不必在此配置文件中维护。
    nacos:
      config:
        # 配置服务地址
        server-addr: 127.0.0.1:8848
        # 启用状态
        enabled: false
      discovery:
        # 服务发现服务地址
        server-addr: 127.0.0.1:8848
        # 启用状态
        enabled: false


# log4j2设置
logging:
  level:
    root: info
    com.thinkdifferent: debug
    de.codecentric.boot.admin.client: error
  file:
    name: logs/application.log

# 线程设置参数 #######
ThreadPool:
  # 核心线程数10：线程池创建时候初始化的线程数
  CorePoolSize: 10
  # 最大线程数20：线程池最大的线程数，只有在缓冲队列满了之后才会申请超过核心线程数的线程
  MaxPoolSize: 20
  # 缓冲队列200：用来缓冲执行任务的队列
  QueueCapacity: 200
  # 保持活动时间60秒
  KeepAliveSeconds: 60
  # 允许线程的空闲时间60秒：当超过了核心线程出之外的线程在空闲时间到达之后会被销毁
  AwaitTerminationSeconds: 60


# 本服务设置
convert:
  # 重试功能需启用MQ才有效
  retry:
    # 最大重试次数（0-8）, 0标识不重试, 若异常情况只记录日志， 大于1（最大8）：标识失败进行重试的次数, 将会在以下时间重试（5min, 10min, 30min, 1h, 2h, 4h, 8h, 16h）
    max: 1
  video:
    # 接收的输入文件存储的临时文件夹
    inPutTempPath: E:/application/ConvertVideo/input/
    # 输出文件所在文件夹
    outPutPath: E:/application/ConvertVideo/output/
    # ffmpeg相关设置
    ffmpeg:
      # ffmpeg所在文件夹和文件名
      file: C:/Program Files (x86)/FormatFactory/ffmpeg.exe
      # 可以使用的CPU进程数量。设置为0，则使用全部CPU资源
      threads: 0
      # 视频编码格式。设置为空，则默认使用libx264编码
      videoCode:
      # 帧率。设置为0，则使用原视频的帧率
      fps: 0
      # 分辨率。如：640*480。设置为空，则使用原视频的分辨率
      resolution:
      # 音频编码格式。设置为空，则默认使用aac编码
      audioCode:
      # 图片水印设置
      picMark:
        # 水印图片位置。不使用默认水印，可将此值设置为空。
        picFile: d:/logo.png
        # 水印参数。第一个参数表示水印距离视频左边的距离，第二个参数表示水印距离视频上边的距离，第三个参数 为1，表示支持透明水印
        param: 30:10:1
      # 文字水印设置
      textMark:
        # 字体文件位置。不使用默认水印，可将此值设置为空。
        fontFile: simhei.ttf
        # 水印文字内容
        text: XX公司
        # 水印横坐标
        localX: 100
        # 水印纵坐标
        localY: 10
        # 文字大小
        fontSize: 24
        # 文字颜色
        fontColor: yellow
```

可以根据服务器的实际情况进行修改。

重点需要修改的内容：

- Nacos服务设置：设置是否启用、服务地址和端口。
- 线程参数设置：需要根据实际硬件的承载能力，调整线程池的大小。
- RabbitMQ设置：根据实际软件部署情况，控制是否启用RabbitMQ；如果启用RabbitMQ，一定要根据服务的配置情况修改地址、端口、用户名、密码等信息。
- 本服务设置：根据本服务所在服务器的实际情况，修改路径和FFmpeg的路径、文件名、转换参数等设置。

# 使用说明

本服务提供REST接口供外部系统调用，提供了直接转换接口和通过MQ异步转换的接口。

转换接口URL：[http://host:port/api/convert]()

接口调用方式：POST

请求头参数：

| key   | 说明               | 示例                              |
| ----- | ---------------- | ------------------------------- |
| token | ecology ssoToken | OEIHGFE29L24J94U24FLKJLFOEU2U33 |

传入参数形式：JSON

示例转换MP4，传入参数：

```json
{
    "inputType": "path",
    "inputFile": "D:/cvtest/001.MOV",
    "outPutFileName": "001-online",
    "params": {
        "threads": 2,
        "videoCode": "libx264",
        "fps": 15,
        "resolution": "640*480",
        "audioCode": "aac",
        "picMark": {
            "picFile": "d:/cvtest/watermark.png",
            "overlay": "500:500",
            "scale": "100:100"
        }
    },
    "writeBackType": "path",
    "writeBack": {
        "path": "D:/cvtest/"
    },
    "callBackURL": "http://1234.com/callback.do"
}
```

视频截图JPG，传入参数示例：

```json
{
    "inputType": "path",
    "inputFile": "D:/cvtest/001.MOV",
    "jpgFileName": "out01",
    "params": {
        "time": "00:01:00"
    },
    "writeBackType": "path",
    "writeBack": {
        "path": "D:/cvtest/"
    },
    "callBackURL": "http://1234.com/callback.do"
}
```

以下分块解释传入参数每部分的内容。

## 输入信息

系统支持本地文件路径输入（path）和http协议的url文件下载输入（url）。

当使用文件路径输入时，配置示例如下：

```json
    "inputType": "path",
    "inputFile": "D:/cvtest/001.MOV",
```

- inputType：必填，值为“path”。
- inputFile：必填，值为需转换的视频文件（输入文件）在当前服务器中的路径和文件名。

当使用url文件下载输入时，配置示例如下：

```json
    "inputType": "url",
    "inputFile": "http://localhost/file/1.mov",
    "inputFileType": "mov",
```

- inputType：必填，值为“url”。
- inputFile：必填，值为需转换的文件（输入文件）在Web服务中的URL地址。
- inputFileType：“inputType”为“url”时，必填。值为url链接的文件的扩展名。

当使用`ftp`时，配置示例如下：

```json
    "inputType": "ftp",
    // 有密码配置
    "inputFile": "ftp://ftptest:zx@192.168.0.102/archives/ftptest/tjTest/test.mov"
    // 无密码配置
    // "inputFile":"ftp://192.168.0.102/archives/ftptest/tjTest/test.mov"
```

- inputType：必填，值为“ftp”。
- inputFile：必填，值为需转换的音视频文件（输入文件）在FTP服务中的地址,兼容用户密码。

## 输出信息(转换MP4/MP3)

可以设置输出的MP4/MP3文件的文件名（无扩展名），如下：

```json
    "outPutFileName": "001-online",
```

- outPutFileName：转换MP4/MP3时，必填，为文件生成后的文件名（无扩展名），系统会自动根据源文件的类型判断，视频转为MP4，音频转为MP3。

本例中，即转换后生成名为 001-online.mp4 的文件。

（FFmpeg支持amr, 3gp, 3gpp, aac, ape, aif, au, mid, wma, wav, ra, rm, rmx, vqf, ogg格式转换为MP3）



## 输出信息(截图JPG)

可以设置截图的JPG文件的文件名（无扩展名），如下：

```json
    "jpgFileName": "out01",
```

- jpgFileName：截图JPG时，必填，为JPG文件生成后的文件名（无扩展名）。

本例中，即截图后生成名为 out01.JPG 的文件。

## 转换MP4参数

系统支持传入转换参数。如果相关参数没有传入，则系统首先从配置文件中获取。如果配置文件中设置为空或0，则自动取默认值。

当需要设置转换参数时，需要设置“params”。示例如下：

```json
    "params": {
        "threads": 2,
        "videoCode": "libx264",
        "fps": 15,
        "resolution": "640*480",
        "audioCode": "aac",
        "picMark": {
            "picFile": "d:/cvtest/watermark.png",
            "overlay": "500:500",
            "scale": "100:100"
        },
        "textMark": {
            "fontFile": "C:/Windows/Fonts/simhei.ttf",
            "text": "XX公司",
            "localX": 500,
            "localY": 500,
            "fontSize": 50,
            "fontColor": "yellow"
        },
        "custom":"default"
    },
```

- threads：非必填，可以使用的CPU进程数量。设置为0，则使用全部CPU资源。
- videoCode：非必填，视频编码格式。设置为空，则默认使用libx264编码。
- fps：非必填，帧率。设置为0，则使用原视频的帧率。
- resolution：非必填，分辨率。如：640*480。设置为空，则使用原视频的分辨率。
  - 注意：加图片水印时，不能使用此参数！否则转换出来的视频黑屏！！
- audioCode：非必填，音频编码格式。设置为空，则默认使用aac编码。
- picMark：非必填，图片水印。如果需要设置图片水印，则设置此项。
  - 注意：
    - 需要预先调整好图片水印的尺寸，加水印时不能调整尺寸。
    - 水印最好是透明底色的PNG。
    - 图片水印不能与文字水印同时使用！！
  - picFile：必填，水印图片位置。
  - overlay：必填，水印参数。第一个参数表示水印距离视频左边的距离，第二个参数表示水印距离视频上边的距离，第三个参数 为1，表示支持透明水印。
  - scale：非必填，水印缩放。参数就是缩放后的width和height。
- textMark：非必填，文字水印。如果需要设置文字水印，则设置此项。
  - fontFile：必填，字体文件位置。
  - text：必填，水印文字内容。
  - localX：必填，水印横坐标。
  - localY：必填，水印纵坐标。
  - fontSize：必填，文字大小。
  - fontColor：必填，文字颜色。
- custom：自定义命令行。如果设置此参数，则前述参数均失效。此处设置的为命令行配置项名称，命令行内容在“conf/CustomLine.xml”文件中配置。

## 截图JPG参数

系统支持传入截图参数，需要设置“params”。示例如下：

```json
    "params": {
        "time": "00:01:00"
    },
```

- time：必填，在视频中截图的时间点。

如果需要生成缩略图，则需要添加如下内容。缩略图输出固定使用“jpg”格式。

设定缩略图边长：

```json
    "params": {
        "time": "00:01:00",
        "thumbnail": {
            "width" : 200,
            "height": 400
        }
    },
```

- width：非必填（width和height可只填其一，也可都填），缩略图的宽度像素值。不填写height值，则根据宽度自动按原比例计算高度。
- height：非必填（width和height可只填其一，也可都填），缩略图的高度像素值。不填写width值，则根据高度自动按原比例计算宽度。

或设定缩略图比例：

```json
    "params": {
        "time": "00:01:00",
        "thumbnail": {
            "scale"  : 0.5,
            "quality": 0.9
        }
    },
```

- scale：必填，图片缩放比例。

- quality：必填，为缩略图压缩比。

## 回写信息

MP4文件生成后，需要回写到业务系统，此处即设置将MP4文件以何种方式，回写到何处。

本服务支持以下回写方式：文件路径（path）、http协议上传（url）、FTP服务上传（ftp）、Ecology接口回写（ecology）。

当使用文件路径方式回写时，配置如下：

```json
    "writeBackType": "path",
    "writeBack": {
        "path": "D:/cvtest/"
    },
```

- writeBackType：必填，值为“path”。
- writeBack：必填。JSON对象，path方式中，key为“path”，value为MP4文件回写的路径。

当使用http协议上传方式回写时，配置如下：

```json
    "writeBackType": "url",
    "writeBack": {
        "url": "http://localhost/uploadfile.do"
    },
    "writeBackHeaders": {
        "Authorization": "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0"
    },
```

- writeBackType：必填，值为“url”。
- writeBack：必填。JSON对象，url方式中，key为“url”，value为业务系统提供的文件上传接口API地址。
- writeBackHeaders：非必填。如果Web服务器访问时需要设置请求头或Token认证，则需要在此处设置请求头的内容；否则此处可不添加。

当使用FTP服务上传方式回写时，配置如下：

```json
    "writeBackType": "ftp",
    "writeBack": {
         "passive": "false",
        "host": "ftp://localhost",
         "port": "21",
         "username": "guest",
         "password": "guest",
         "filepath": "/2021/10/"
    },
```

- writeBackType：必填，值为“ftp”。
- writeBack：必填。JSON对象。
  - passive：是否是被动模式。true/false
  - host：ftp服务的访问地址。
  - port：ftp服务的访问端口。
  - username：ftp服务的用户名。
  - password：ftp服务的密码。
  - filepath：文件所在的路径。

当使用Ecology接口（ecology）方式回写时，配置如下：

```json
    "writeBackType": "ecology",
    "writeBack": {
         "address": "http://10.115.92.26",
         "api": "/api/doc/upload/uploadFile2Doc",
         "category": "123",
         "appId": "EEAA5436-7577-4BE0-8C6C-89E9D88805EA"
    }
```

- writeBackType：必填，值为“ecology”。
- writeBack：必填。JSON对象。
  - address：ecology服务的访问地址
  - api：文件上传接口的api地址
  - category：Ecology中存储此类文件的“文档目录”的ID
  - appId: ecology系统发放的授权许可证(appid)

## 回调信息

业务系统可以提供一个GET方式的回调接口，在视频文件转换、回写完毕后，本服务可以调用此接口，传回处理的状态。

```json
    "callBackURL": "http://10.11.12.13/callback.do",
    "callBackHeaders": {
        "Authorization": "Bearer da3efcbf-0845-4fe3-8aba-ee040be542c0"
    },
```

- callBackURL：回调接口的URL。回调接口需要接收两个参数：
  - file：处理后的文件名。本例为“001-online.mp4”（如果使用ecology方式回写，则此处回传上传接口返回的id）。
  - flag：处理后的状态，值为：success 或 error。
- callBackHeaders：如果回调接口需要在请求头中加入认证信息等，可以在此处设置请求头的参数和值。

接口url示例：

```
http://1234.com/callback.do?file=001-online&flag=success
```

## 返回信息

接口返回信息示例如下：

```json
{
  "flag": "success",
  "message": "Convert Video to MP4 success."
}
```

- flag：处理状态。success，成功；error，错误，失败。
- message：返回接口消息。

# 代码结构说明

本项目所有代码均在  com.thinkdifferent.convertvideo 之下，包含如下内容：

- config
  - ConvertVideoConfig：本服务自有配置读取。
  - RabbitMQConfig：RabbitMQ服务配置读取。
- consumer
  - ConvertVideoConsumer：MQ消费者，消费队列中传入的JSON参数，执行任务（Task）。
- controller
  - ConvertVideo：REST接口，提供直接转换接口，和调用MQ异步转换的接口。
- service
  - ConvertVideoService：视频文件格式转换、文件回写上传、接口回调等核心逻辑处理。
  - RabbitMQService：将JSON消息加入到队列中的服务层处理。
- task
  - Task：异步多线程任务，供MQ消费者调用，最大限度的提升并行能力。
- utils
  - ConvertVideoUtils：调用FFmpeg进行视频格式转换的工具类。
  - PrintStream：将FFmpeg返回的内容实时输出到控制台。