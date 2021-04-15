package com.ghost.netty.server;

import com.ghost.netty.handler.NettyServerHandler;
import com.ghost.netty.handler.NettyServerIdleStateHandler;
import com.ghost.netty.protobuf.MessageBase;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.CharsetUtil;

import java.util.concurrent.TimeUnit;

/**
 * @author ghost
 * @create 2021/4/15 5:43 下午
 */
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

        //普通添加编解码
        //pipeline.addLast(new StringDecoder(CharsetUtil.UTF_8));
        //pipeline.addLast(new StringEncoder(CharsetUtil.UTF_8));

        //业务逻辑实现类
        pipeline.addLast(new NettyServerHandler());
    }
}
