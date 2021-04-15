package com.ghost.netty.client;

import com.ghost.netty.protobuf.MessageBase;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class NettyClient {
    private EventLoopGroup group = new NioEventLoopGroup();

    @Value("${netty.port}")
    private int port;

    @Value("${netty.host}")
    private String host;

    private SocketChannel socketChannel;

    public void sendMsg(MessageBase.Message message) {
        socketChannel.writeAndFlush(message);
    }

    public void sendMsg(Object message) {
        socketChannel.writeAndFlush(message);
    }

    @PostConstruct
    public void start() {
        log.info("Connect Netty Server:{}:{} Starting", host, port);
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(host, port)
                // 设置TCP的长连接，默认的 keepalive的心跳时间是两个小时
                .option(ChannelOption.SO_KEEPALIVE, true)
                // 将小的数据包包装成更大的帧进行传送，提高网络的负载,即TCP延迟传输
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new NettyClientHandlerInitializer());
        ChannelFuture future = bootstrap.connect();

        //客户端断线重连逻辑
        future.addListener((ChannelFutureListener) future1 -> {
            log.info("Connect Netty Server {}", future1.isSuccess());
            if (future1.isSuccess()) {
                log.info("Connect Netty Server Success!");
            } else {
                log.info("Connect Netty Server fail，Reconnection after 10 seconds！");
                future1.channel().eventLoop().schedule(this::start, 10L, TimeUnit.SECONDS);
            }
        });
        socketChannel = (SocketChannel) future.channel();
    }
}