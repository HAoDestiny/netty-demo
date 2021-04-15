package com.ghost.netty.handler;

import cn.hutool.extra.spring.SpringUtil;
import com.ghost.netty.client.NettyClient;
import com.ghost.netty.protobuf.MessageBase;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Date;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
// 为了多个handler可以被多个channel安全地共享，也就是保证线程安全
@ChannelHandler.Sharable
public class NettyClientHandler extends ChannelInboundHandlerAdapter {

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
        log.info("客户端建立连接时间: " + new Date() + ", 地址:" + ctx.channel().remoteAddress());
        super.channelActive(ctx);
    }

    /**
     * 关闭连接时
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("客户端地址: "+ ctx.channel().localAddress() + ", 已关闭channelInactive");
        //如果运行过程中服务端挂了,执行重连机制 10秒后进行重连
        log.info("客户端地址: "+ ctx.channel().localAddress() + "2秒后进行重连");
        ctx.channel().eventLoop().schedule(() -> SpringUtil.getBean(NettyClient.class).start(), 2L, TimeUnit.SECONDS);
        super.channelInactive(ctx);
    }

    /**
     * 超时处理 心跳请求处理 每4秒发送一次心跳请求; 发送心跳间隔时间应小于服务端读入的空闲检测时间
     * 重连配置 new IdleStateHandler(0, 20, 0, TimeUnit.SECONDS)
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            // 如果写通道处于空闲状态,就发送心跳命令
            if (IdleState.WRITER_IDLE.equals(event.state())) {
                log.info("已经20秒没有向服务端发送信息了");

                log.info("开始向服务端发送心跳");
                //发送心跳消息，并在发送失败时关闭该连接
                ctx.writeAndFlush(
                        MessageBase.Message
                        .newBuilder()
                        .setCmd(MessageBase.Message.CommandType.HEARTBEAT_REQUEST)
                        .setContent("client-Heartbeat")
                        .build())
                        .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
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
        log.info("第" + count.get() + "次" + ", 接收服务端的消息:" + msg);
        try {
            // 如果不是protobuf类型的数据
            if (! (msg instanceof MessageBase.Message)) {
                log.info("未知数据!" + msg);
                return;
            }

            MessageBase.Message message = (MessageBase.Message) msg;
            if (message.getCmdValue() == MessageBase.Message.CommandType.NORMAL_VALUE) {
                log.info("服务端业务信息: requestId=" + message.getRequestId() + ", content=" + message.getContent());
            } else if(message.getCmdValue() == MessageBase.Message.CommandType.HEARTBEAT_RESPONSE_VALUE) {
                log.info("接受到服务端发送的心跳: " + message.getContent());
            } else {
                log.info("未知命令: " + message.getCmdValue());
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
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