# netty-demo
## 目录结构
```
netty-demo     
├── ghost-common          // 公共模块 
├── ghost-protobuf        // 编码模块
├── ghost-netty          
│       └── ghost-netty-client                        // netty客户端
│       └── ghost-netty-server                    	  // netty服务端
│       └── ghost-netty-webSocket-server              // netty-webSocket服务端
				└── html							  // netty-webSocket客户端
├──pom.xml                // 公共依赖
```

#### Netty客户端 
+ 自定义空闲检测机制(写操作空闲)
+ 重连机制
+ 通信编码使用[Google Protobuf](https://developers.google.cn/protocol-buffers/ "Google Protobuf")进行通信 - 与服务端保持一致

#### Netty服务端
+ 自定义空闲检测机制(读操作空闲)
+ 通信编码使用[Google Protobuf](https://developers.google.cn/protocol-buffers/ "Google Protobuf")进行通信 - 与客户端保持一致

#### Netty-WebSocket客户端
+ JavaScript原生webSocket连接

#### Netty-WebSocket服务端
+ 自定义空闲检测机制(读写操作空闲)
+ ChannelGroup群发信息
+ 本地缓存channelId，用于单独向channel发送信息
+ 注册WebSocket连接
	+ 手动注册 - 客户端与服务端首次会以FullHttpRequest方式进行握手
	+ 自动注册 - 在每个channel建立连接进行Initializer时添加addLast进行注册

#### Google Protobuf编译java(支持多种语言编译 [具体](https://developers.google.cn/protocol-buffers/))
```shell script
protoc -I=$SRC_DIR --java_out=$DST_DIR $SRC_DIR/addressbook.proto
```
