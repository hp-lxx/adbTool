package com.devtool.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@Controller
@RequestMapping("testMethod")
public class TestController {
    @Autowired
    private WebSocket webSocket;

    /**
     * 给指定的在线用户发送消息
     *
     * @param userId
     * @param msg
     * @return
     * @throws IOException
     */
    @ResponseBody
    @GetMapping("/sendTo")
    public String sendTo(@RequestParam("userId") String userId, @RequestParam("msg") String msg) throws IOException {
        webSocket.sendMessageTo(msg, userId);
        return "推送成功";
    }

    /**
     * 给所有在线用户发送消息
     *
     * @param msg
     * @return
     * @throws IOException
     * @throws IOException
     */
    @ResponseBody
    @PostMapping("/sendAll")
    public String sendAll(@RequestBody String msg) throws IOException, IOException {
        webSocket.sendMessageAll(msg);
        return "推送成功";
    }
}