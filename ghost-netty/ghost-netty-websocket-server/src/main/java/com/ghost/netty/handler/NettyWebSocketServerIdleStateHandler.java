package com.ghost.netty.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * @author ghost
 * @create 2021/4/15 8:44 下午
 */

@Slf4j
public class NettyWebSocketServerIdleStateHandler extends IdleStateHandler {

    /**
     * 设置空闲检测时间为 30s
     */
    private static final int READER_IDLE_TIME = 30;
    private static final int WRITE_IDLE_TIME = 30;
    private static final int ALL_IDLE_TIME = 30;
    public NettyWebSocketServerIdleStateHandler() {
        super(0, 0, ALL_IDLE_TIME, TimeUnit.SECONDS);
    }

    public NettyWebSocketServerIdleStateHandler(int readerIdleTimeSeconds, int writerIdleTimeSeconds, int allIdleTimeSeconds) {
        super(readerIdleTimeSeconds, writerIdleTimeSeconds, allIdleTimeSeconds);
    }

    public NettyWebSocketServerIdleStateHandler(long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(readerIdleTime, writerIdleTime, allIdleTime, unit);
    }

    public NettyWebSocketServerIdleStateHandler(boolean observeOutput, long readerIdleTime, long writerIdleTime, long allIdleTime, TimeUnit unit) {
        super(observeOutput, readerIdleTime, writerIdleTime, allIdleTime, unit);
    }

    @Override
    protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) throws Exception {
        // 这里关闭连接 会继续回调到 handler里面的userEventTriggered
        log.info("{} 秒内没有读取到数据, NettyWebSocketServerIdleStateHandler.channelIdle开始处理", READER_IDLE_TIME);
        super.channelIdle(ctx, evt);
    }
}
