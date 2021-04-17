package com.ghost.netty.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.ghost.netty.pool.WebSocketChannelPool;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;

import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;

/**
 * @author ghost
 * @create 2021/4/18 1:58 上午
 */

@Slf4j
@Service("channelHandlerService")
public class ChannelHandlerService {

    @Value("${netty.port}")
    private Integer port;

    @Value("${netty.host}")
    private String host;

    private WebSocketServerHandshaker handshaker;

    /**
     * webSocket第一次握手
     * @param ctx
     * @param req
     */
    public void channelHandlerHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        log.info("客户端: {}, id: {}, 首次连接握手", ctx.channel().remoteAddress(), ctx.channel().id());
        // 要求Upgrade为websocket，过滤掉get/Post
        if (!req.decoderResult().isSuccess() || (!"websocket".equals(req.headers().get("Upgrade")))) {
            log.info("握手非webSocket模式");
            //若不是websocket方式，则创建BAD_REQUEST的req，返回给客户端
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST));
            return;
        }

        // 手动注册webSocket
//        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
//                StrUtil.format("ws://{}:{}/webSocket"), null, false);
//        handshaker = wsFactory.newHandshaker(req);
//        if (handshaker == null) {
//            WebSocketServerHandshakerFactory
//                    .sendUnsupportedVersionResponse(ctx.channel());
//        } else {
//            handshaker.handshake(ctx.channel(), req);
//        }

        log.info("首次握手连接Uri: {}", req.uri());
        ctx.channel().writeAndFlush(new TextWebSocketFrame("与服务端握手成功 " + DateUtil.formatDateTime(DateUtil.date())));
    }

    public void channelHandlerWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否关闭链路的指令
        if (frame instanceof CloseWebSocketFrame) {
            ctx.close();
            return;
        }

        // 判断是否ping消息
        if (frame instanceof PingWebSocketFrame) {
            ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        if (frame instanceof PongWebSocketFrame) {
            ctx.channel().writeAndFlush(new PingWebSocketFrame(frame.content().retain()));
            return;
        }
        // 本例程仅支持文本消息，不支持二进制消息
        if (!(frame instanceof TextWebSocketFrame)) {
            log.info("本例程仅支持文本消息，不支持二进制消息");
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }

        // 返回应答消息
        String request = ((TextWebSocketFrame) frame).text();
        log.info("服务端收到：" + request);

        ctx.channel().writeAndFlush(new TextWebSocketFrame("服务端已收到消息 " + DateUtil.formatDateTime(DateUtil.date())));

//        // 群发
//        WebSocketChannelPool.sendAll(new TextWebSocketFrame("服务端已收到消息 " + DateUtil.formatDateTime(DateUtil.date())));
    }

    /**
     * 拒绝不合法的请求，并返回错误信息
     * */
    public void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, DefaultFullHttpResponse res) {
        // 返回应答给客户端
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
        }
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        // 如果是非Keep-Alive，关闭连接
        if (!isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

}
