package com.ghost.netty.server;

import com.ghost.netty.handler.NettyServerHandler;
import com.ghost.netty.handler.NettyServerIdleStateHandler;
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
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author ghost
 * @create 2021/4/15 5:43 下午
 */

@Slf4j
public class NettyServerHandlerInitializer extends ChannelInitializer<Channel> {

    protected void initChannel(Channel channel) throws Exception {

        ChannelPipeline pipeline = channel.pipeline();

        // 空闲检测
        // 入参说明: 读超时时间、写超时时间、所有类型的超时时间、时间格式
        //pipeline.addLast(new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS));
        pipeline.addLast(new NettyServerIdleStateHandler());

        // 解码和编码，应和客户端一致
        // 传输的协议 Protobuf
        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast(new ProtobufDecoder(MessageBase.Message.getDefaultInstance()));
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast(new ProtobufEncoder());

        // websocket配置
        // 设置log监听器，并且日志级别为debug，方便观察运行流程
        //pipeline.addLast(new LoggingHandler("DEBUG"));

        ////websocket协议本身是基于http协议的，所以这边也要使用http解编码器
        //pipeline.addLast(new HttpServerCodec());
        ////以块的方式来写的处理器
        //pipeline.addLast(new ChunkedWriteHandler());
        //pipeline.addLast(new HttpObjectAggregator(8192));
        //pipeline.addLast(new WebSocketServerProtocolHandler("/ws", "MessageBase", true, 65536 * 10));

        //普通添加编解码
        //pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
        //pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));

        //业务逻辑实现类
        pipeline.addLast(new NettyServerHandler());

        log.info("Netty Server ChannelInitializer Success!");
    }
}
