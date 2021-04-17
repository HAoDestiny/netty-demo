package com.ghost.netty.pool;

import io.netty.channel.Channel;
import io.netty.channel.ChannelId;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author ghost
 * @create 2021/4/18 1:40 上午
 */

public class WebSocketChannelPool {

    private static ChannelGroup group = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    private static ConcurrentMap<String, ChannelId> channelIdConcurrentMap = new ConcurrentHashMap<>();

    public static void addChannel(Channel channel) {
        group.add(channel);
        channelIdConcurrentMap.put(channel.id().asShortText(), channel.id());
    }

    public static void removeChannel(Channel channel) {
        group.remove(channel);
        channelIdConcurrentMap.remove(channel.id().asShortText());
    }

    public static Channel getChannel(String channelSortId) {
        ChannelId channelId = channelIdConcurrentMap.get(channelSortId);
        if (null == channelId) {
            return null;
        }
        return group.find(channelId);
    }

    public static int groupSize() {
        return group.size();
    }

    /**
     * 群发
     * @param msg
     */
    public static void sendAll(WebSocketFrame msg) {
        group.writeAndFlush(msg);
    }
}
