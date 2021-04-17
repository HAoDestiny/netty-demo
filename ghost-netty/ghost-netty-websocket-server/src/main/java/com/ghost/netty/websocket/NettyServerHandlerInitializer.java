package com.ghost.netty.websocket;

import cn.hutool.core.util.StrUtil;
import com.ghost.netty.handler.NettyWebSocketServerHandler;
import com.ghost.netty.handler.NettyWebSocketServerIdleStateHandler;
import com.ghost.netty.protobuf.MessageBase;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @author ghost
 * @create 2021/4/15 5:43 下午
 */

@Slf4j
public class NettyServerHandlerInitializer extends ChannelInitializer<Channel> {

    /**
     * 每个channel连接时都需要进行初始化配置
     * @param channel
     * @throws Exception
     */
    protected void initChannel(Channel channel) throws Exception {

        ChannelPipeline pipeline = channel.pipeline();

        // 空闲检测
        // 入参说明: 读超时时间、写超时时间、所有类型的超时时间、时间格式
        //pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(new NettyWebSocketServerIdleStateHandler());

        // websocket配置
        // 设置log监听器，并且日志级别为debug，方便观察运行流程
        pipeline.addLast(new LoggingHandler("DEBUG"));

        //websocket协议本身是基于http协议的，所以这边也要使用http解编码器
        pipeline.addLast(new HttpServerCodec());
        //以块的方式来写的处理器
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(8192));

        // 自动注册webSocket(常规连接无参数携带 ws://localhost:{port}/{websocketPath})
        // 如果需要获取连接Url(ws://localhost:{port}/webSocket?key=value)后面的参数即Uri 需要在首次握手时通过WebSocketServerHandshakerFactory进行手动注册, 首次握手时会以FullHttpRequest的类型返回
        pipeline.addLast(new WebSocketServerProtocolHandler("/webSocket", "WebSocket", true, 65536 * 10));

        //普通添加编解码
        //pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
        //pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));

        //业务逻辑实现类
        pipeline.addLast(new NettyWebSocketServerHandler());

        log.info("Netty WebSocket Server ChannelInitializer Success!");
    }
}
