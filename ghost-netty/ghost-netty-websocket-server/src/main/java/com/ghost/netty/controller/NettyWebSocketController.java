package com.ghost.netty.controller;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.ghost.netty.pool.WebSocketChannelPool;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author ghost
 * @create 2021/4/18 3:18 上午
 */

@RestController
@RequestMapping(value = "/netty")
public class NettyWebSocketController {

    @GetMapping(value = "/sendToId/{channelId}")
    public String sendToId(@PathVariable(value = "channelId") String channelId) {

        Channel channel = WebSocketChannelPool.getChannel(channelId);
        if (null == channel) {
            return StrUtil.format("channel {} is not online", channelId);
        }
        channel.writeAndFlush(new TextWebSocketFrame(StrUtil.format("from netty webSocket server message {}", DateUtil.formatDateTime(DateUtil.date()))));
        return StrUtil.format("send to {} {} success", channel.remoteAddress(), channel.id().asShortText());
    }

    @GetMapping(value = "/sendToAll")
    public String sendToId() {
        WebSocketChannelPool.sendAll(new TextWebSocketFrame(StrUtil.format("from netty webSocket server all message {}", DateUtil.formatDateTime(DateUtil.date()))));
        return "success send to count channel is " + WebSocketChannelPool.groupSize();
    }

    @GetMapping(value = "/getChannelSize")
    public String getChannelSize() {
        return "size is " + WebSocketChannelPool.groupSize();
    }
}
