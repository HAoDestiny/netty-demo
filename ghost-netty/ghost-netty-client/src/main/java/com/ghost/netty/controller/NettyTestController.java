package com.ghost.netty.controller;

import com.ghost.netty.client.NettyClient;
import com.ghost.netty.protobuf.MessageBase;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * @author ghost
 * @create 2021/4/15 6:55 下午
 */

@RestController
@RequestMapping(value = "/netty")
public class NettyTestController {

    private final NettyClient nettyClient;

    public NettyTestController(NettyClient nettyClient) {
        this.nettyClient = nettyClient;
    }

    @GetMapping(value = "/send/{msg}")
    @ResponseBody
    public String send(@PathVariable(value = "msg") String msg) {
        nettyClient.sendMsg(msg);
        return "success";
    }

    @GetMapping(value = "/send/protobuf/{msg}")
    @ResponseBody
    public String sendProtobuf(@PathVariable(value = "msg") String msg) {
        nettyClient.sendMsg(MessageBase.Message.newBuilder()
                .setCmd(MessageBase.Message.CommandType.NORMAL)
                .setContent(msg)
                .setRequestId(UUID.randomUUID().toString())
                .build());
        return "success";
    }
}
