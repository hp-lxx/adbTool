package com.devtool.config;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;

import com.devtool.JavaExeBat;
import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import static com.devtool.controller.AdbToolController.ADB_PATH;

/**
 * @Author：陈彦斌
 * @Description：Socket核心类
 * @Date： 2020-07-26
 */

@Component
@ServerEndpoint(value = "/connectWebSocket/{userId}")
public class WebSocket {
    Gson gson = new Gson();
    private Logger logger = LoggerFactory.getLogger(this.getClass());
    /**
     * 在线人数
     */
    public static int onlineNumber = 0;
    /**
     * 以用户的姓名为key，WebSocket为对象保存起来
     */
    private static Map<String, WebSocket> clients = new ConcurrentHashMap<String, WebSocket>();
    /**
     * 会话
     */
    private Session session;
    /**
     * 用户名称
     */
    private String userId;
    BufferedReader br;

    /**
     * 建立连接
     *
     * @param session
     */
    @OnOpen
    public void onOpen(@PathParam("userId") String userId, Session session) {
        onlineNumber++;
        System.out.println("现在来连接的客户id：" + session.getId() + "用户名：" + userId);
        //logger.info("现在来连接的客户id："+session.getId()+"用户名："+userId);
        this.userId = userId;
        this.session = session;
        System.out.println("有新连接加入！ 当前在线人数" + onlineNumber);
        // logger.info("有新连接加入！ 当前在线人数" + onlineNumber);
        try {
            //messageType 1代表上线 2代表下线 3代表在线名单 4代表普通消息
            //先给所有人发送通知，说我上线了
            //把自己的信息加入到map当中去
            clients.put(userId, this);
//            System.out.println("有连接关闭！ 当前在线人数" + onlineNumber);
//            //logger.info("有连接关闭！ 当前在线人数" + clients.size());
//            //给自己发一条消息：告诉自己现在都有谁在线
//            Map<String, Object> map2 = Maps.newHashMap();
//            map2.put("messageType", 3);
//            //移除掉自己
//            Set<String> set = clients.keySet();
//            map2.put("onlineUsers", set);
//            sendMessageTo(gson.toJson(map2), userId);
            if (br == null) {
                beginCmd();
            }
        } catch (IOException e) {
            System.out.println(userId + "上线的时候通知所有人发生了错误");
            //logger.info(userId+"上线的时候通知所有人发生了错误");
        }
    }

    public void beginCmd() throws IOException {
        new Thread() {
            @Override
            public void run() {
                try {
                    sendMessageAll("连接成功", userId);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                JavaExeBat.exeCmd(ADB_PATH + " logcat -c");
                br = JavaExeBat.exeCmdStream(ADB_PATH + " logcat");
                //直到读完为止
                try {
                    String line;
                    while ((line = br.readLine()) != null) {
//                System.out.println(line);
                        if (line.contains("lua") || line.contains("marykay")) {
                            Thread.sleep(3);
                            sendMessageAll(line);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    @OnError
    public void onError(Session session, Throwable error) {
        //logger.info("服务端发生了错误"+error.getMessage());
        //error.printStackTrace();
        System.out.println("服务端发生了错误:" + error.getMessage());
    }

    /**
     * 连接关闭
     */
    @OnClose
    public void onClose() {
        onlineNumber--;
        //webSockets.remove(this);
        clients.remove(userId);
        if (clients.size() == 0) {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                br = null;
            }
        }
        try {
            //messageType 1代表上线 2代表下线 3代表在线名单 4代表普通消息
            Map<String, Object> map1 = Maps.newHashMap();
            map1.put("messageType", 2);
            map1.put("onlineUsers", clients.keySet());
            map1.put("userId", userId);
            sendMessageAll(gson.toJson(map1), userId);
        } catch (IOException e) {
            System.out.println(userId + "下线的时候通知所有人发生了错误");
            //logger.info(userId+"下线的时候通知所有人发生了错误");
        }
        //logger.info("有连接关闭！ 当前在线人数" + onlineNumber);
        //logger.info("有连接关闭！ 当前在线人数" + clients.size());
        System.out.println("有连接关闭！ 当前在线人数" + onlineNumber);
    }

    /**
     * 收到客户端的消息
     *
     * @param message 消息
     * @param session 会话
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            //logger.info("来自客户端消息：" + message+"客户端的id是："+session.getId());
//            System.out.println("来自客户端消息：" + message + " | 客户端的id是：" + session.getId());
//            JsonObject jsonObject = gson.fromJson(message,JsonObject.class);
//            String textMessage = jsonObject.gets("message");
//            String fromuserId = jsonObject.getString("userId");
//            String touserId = jsonObject.getString("to");
//            //如果不是发给所有，那么就发给某一个人
//            //messageType 1代表上线 2代表下线 3代表在线名单 4代表普通消息
//            Map<String, Object> map1 = Maps.newHashMap();
//            map1.put("messageType", 4);
//            map1.put("textMessage", textMessage);
//            map1.put("fromuserId", fromuserId);
//            if (touserId.equals("All")) {
//                map1.put("touserId", "所有人");
//                sendMessageAll(JSON.toJSONString(map1), fromuserId);
//            } else {
//                map1.put("touserId", touserId);
//                System.out.println("开始推送消息给" + touserId);
//                sendMessageTo(JSON.toJSONString(map1), touserId);
//            }
        } catch (Exception e) {
            e.printStackTrace();
            //logger.info("发生了错误了");
        }

    }

    /**
     * 给指定的用户发送消息
     *
     * @param message
     * @param TouserId
     * @throws IOException
     */
    public void sendMessageTo(String message, String TouserId) throws IOException {
        for (WebSocket item : clients.values()) {
            System.out.println("给指定的在线用户发送消息,在线人员名单：【" + item.userId.toString() + "】发送消息:" + message);
            if (item.userId.equals(TouserId)) {
                item.session.getAsyncRemote().sendText(message);
                break;
            }
        }
    }

    /**
     * 给所有用户发送消息
     *
     * @param message    数据
     * @param FromuserId
     * @throws IOException
     */
    public void sendMessageAll(String message, String FromuserId) throws IOException {
        for (WebSocket item : clients.values()) {
            System.out.println("给所有在线用户发送给消息，在线人员名单：【" + item.userId.toString() + "】发送消息:" + message);
            item.session.getAsyncRemote().sendText(message);
        }
    }

    /**
     * 给所有在线用户发送消息
     *
     * @param message 数据
     * @throws IOException
     */
    public void sendMessageAll(String message) throws IOException {
        synchronized (WebSocket.class) {
            for (WebSocket item : clients.values()) {
                System.out.println("服务器给所有在线用户发送消息，当前在线人员为【" + item.userId.toString() + "】发送消息:" + message);
                item.session.getAsyncRemote().sendText(message);
            }
        }

    }

    /**
     * 获取在线用户数
     *
     * @return
     */
    public static synchronized int getOnlineCount() {
        return onlineNumber;
    }
}