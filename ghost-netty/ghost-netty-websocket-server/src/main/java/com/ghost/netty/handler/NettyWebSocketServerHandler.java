package com.ghost.netty.handler;

import cn.hutool.core.date.DateUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.ghost.netty.pool.WebSocketChannelPool;
import com.ghost.netty.protobuf.MessageBase;
import com.ghost.netty.service.ChannelHandlerService;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
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
public class NettyWebSocketServerHandler extends ChannelInboundHandlerAdapter {

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
        log.info("连接的客户端地址: {}, id: {}", ctx.channel().remoteAddress(), ctx.channel().id().asShortText());
        WebSocketChannelPool.addChannel(ctx.channel());
        ctx.writeAndFlush(new TextWebSocketFrame("welcome to client netty webSocket Server" + DateUtil.formatDateTime(DateUtil.date())));
        super.channelActive(ctx);
    }

    /**
     * 连接断开
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.info("连接的客户端地址: {}, id: {}, 已断开", ctx.channel().remoteAddress(), ctx.channel().id().asShortText());
        WebSocketChannelPool.removeChannel(ctx.channel());
        super.channelInactive(ctx);
    }

    /**
     * 超时处理 如果30秒没有接受客户端的心跳，或没有发送消息到客户端就触发; 如果超过两次，则直接关闭;
     * 空闲检测 new IdleStateHandler(0, 0, 30, TimeUnit.SECONDS)
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object obj) throws Exception {
        if (obj instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) obj;
            // 如果读通道和写通道都处于空闲状态，说明没有任何通信
            if (IdleState.ALL_IDLE.equals(event.state())) {
                log.info("已经30秒没有接收或发送信息到客户端{}, id={}的信息了", ctx.channel().remoteAddress(), ctx.channel().id().asShortText());
                if (idle_count.get() > 1) {
                    log.info("关闭这个不活跃的channel, 地址: {}, id: {}", ctx.channel().remoteAddress(), ctx.channel().id().asShortText());
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
        log.info("第{}次, 接收客服端{}, Id={}的消息: {}", count.get(), ctx.channel().remoteAddress(), ctx.channel().id().asShortText(), msg);

        try {

            // webSocket首次唯一一次连接是FullHttpRequest
            if (msg instanceof FullHttpRequest) {
                SpringUtil.getBean(ChannelHandlerService.class).channelHandlerHttpRequest(ctx, (FullHttpRequest) msg);
            }

            // 处理websocket客户端的消息
            else if (msg instanceof WebSocketFrame) {
                SpringUtil.getBean(ChannelHandlerService.class).channelHandlerWebSocketFrame(ctx, (WebSocketFrame) msg);
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
