# netty-demo
#### 客户端 
+ 自定义空闲检测机制(写操作空闲)
+ 重连机制

#### 服务端
+ 自定义空闲检测机制(读操作空闲)

### 通信编码使用Google Protobuf进行通信
https://developers.google.cn/protocol-buffers/

#### protobuf编译
```shell script
protoc -I=$SRC_DIR --java_out=$DST_DIR $SRC_DIR/addressbook.proto
```