package com.ghost.netty.handler;

import com.ghost.netty.protobuf.MessageBase;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ghost
 * @create 2021/4/15 5:56 下午
 */

@Slf4j
public class NettyServerHandler extends ChannelInboundHandlerAdapter {

    /**
     * 空闲次数
     */
    private AtomicInteger idle_count = new AtomicInteger(1);

    /**
     * 发送次数
     */
    private AtomicInteger count = new AtomicInteger(1);

    /**
     * 建立连接时，发送一条消息
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.info("连接的客户端地址:" + ctx.channel().remoteAddress());
        // google protobuf格式
        ctx.writeAndFlush(MessageBase.Message
                .newBuilder()
                .setCmd(MessageBase.Message.CommandType.NORMAL)
                .setContent("welcome to client netty")
                .setRequestId(UUID.randomUUID().toString())
                .build()
        );
//        ctx.writeAndFlush("welcome to client netty");
        super.channelActive(ctx);
    }

    /**
     * 超时处理 如果30秒没有接受客户端的心跳，就触发; 如果超过两次，则直接关闭;
     * 空闲检测 new IdleStateHandler(30, 0, 0, TimeUnit.SECONDS)
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            // 如果读通道处于空闲状态，说明没有接收到心跳命令
            if (IdleState.READER_IDLE.equals(event.state())) {
                log.info("已经30秒没有接收到客户端的信息了");
                if (idle_count.get() > 1) {
                    log.info("关闭这个不活跃的channel" + ", 地址: " + ctx.channel().localAddress());
                    ctx.channel().close();
                }
                idle_count.getAndIncrement();
            }
        } else {
            super.userEventTriggered(ctx, obj);
        }
    }

    /**
     * 业务逻辑处理
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        log.info("第" + count.get() + "次" + ", 接收客服端的消息:" + msg);
        try {
            if (! (msg instanceof MessageBase.Message)) {
                log.info("未知数据!" + msg);
                return;
            }

            MessageBase.Message message = (MessageBase.Message) msg;
            if (message.getCmdValue() == MessageBase.Message.CommandType.NORMAL_VALUE) {
                log.info("客户端业务信息: requestId=" + message.getRequestId() + ", content=" + message.getContent());
            } else if(message.getCmdValue() == MessageBase.Message.CommandType.HEARTBEAT_REQUEST_VALUE) {
                log.info("接收到客户端: " + ctx.channel().remoteAddress() + "发送的心跳: " + message.getContent());
            } else {
                log.info("未知命令: " + message.getCmdValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            log.debug("final in ======");
            ReferenceCountUtil.release(msg);
        }
        count.getAndIncrement();
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }

}
