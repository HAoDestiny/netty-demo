package com.ghost.netty.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

@Service("nettyServer")
@Slf4j
public class NettyServer {
    /**
     * boss 线程组用于处理连接工作
     */
    private EventLoopGroup boss = new NioEventLoopGroup();
    /**
     * work 线程组用于数据处理
     */
    private EventLoopGroup work = new NioEventLoopGroup();

    private  ChannelFuture channelFuture;

    @Value("${netty.port}")
    private Integer port;

    /**
     * 启动Netty Server * * @throws InterruptedException
     *
     * @PostConstruct 在 Spring 初始化 NettyServer类后调用
     */
    @PostConstruct
    public void start() throws InterruptedException {
        log.info("Starting Netty Server");

        ServerBootstrap bootstrap = new ServerBootstrap();

        bootstrap.group(boss, work)
                // 指定Channel
                .channel(NioServerSocketChannel.class)
                // 使用指定的端口设置套接字地址
                .localAddress(new InetSocketAddress(port))
                // 服务端可连接队列数,对应TCP/IP协议listen函数中backlog参数
                .option(ChannelOption.SO_BACKLOG, 1024)
                // 设置TCP长连接,一般如果两个小时内没有数据的通信时,TCP会自动发送一个活动探测数据报文
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // 将小的数据包包装成更大的帧进行传送，提高网络的负载,即TCP延迟传输
                .childOption(ChannelOption.TCP_NODELAY, true)
                // 设置 NioServerSocketChannel 的处理器
//                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new NettyServerHandlerInitializer());

        channelFuture = bootstrap.bind().sync();
        if (channelFuture.isSuccess()) {
            log.info("Netty Server Start Success!");
        }
    }

    @PreDestroy
    public void destory() throws InterruptedException {
        log.info("Stopping Netty Server");
        try {
            // 监听服务端关闭，并阻塞等待
            channelFuture.channel().closeFuture().sync();
        } finally {
            // 优雅关闭两个 EventLoopGroup 对象
            work.shutdownGracefully();
            boss.shutdownGracefully();
        }
        log.info("Server Netty Stopped Success!");
    }
}