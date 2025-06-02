package ptumall.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ptumall.model.ChatMessageDTO;

import javax.websocket.*;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户端WebSocket服务端点
 */
@Slf4j
@Component
@ServerEndpoint("/ws/chat/{userId}/customer")
public class ChatWebSocketServer {

    // 用于存储在线连接，key为userId_customer
    private static final Map<String, Session> onlineCustomers = new ConcurrentHashMap<>();
    
    // 用于处理JSON
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        String key = userId + "_customer";
        onlineCustomers.put(key, session);
        log.info("用户连接: {}，当前在线用户数: {}", key, onlineCustomers.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        String key = userId + "_customer";
        onlineCustomers.remove(key);
        log.info("用户断开: {}，当前在线用户数: {}", key, onlineCustomers.size());
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        log.info("收到来自用户{}的消息: {}", userId, message);
        
        try {
            // 解析收到的消息
            ChatMessageDTO chatMessageDTO = objectMapper.readValue(message, ChatMessageDTO.class);
            
            // 补充发送者信息
            chatMessageDTO.setSenderId(userId);
            chatMessageDTO.setSenderType("customer");
            chatMessageDTO.setTimestamp(System.currentTimeMillis());
            
            // 接收者
            String receiverKey = chatMessageDTO.getReceiverId() + "_" + chatMessageDTO.getReceiverType();
            
            // 转发消息给接收者（管理员）
            Session adminSession = AdminChatWebSocketServer.getSession(chatMessageDTO.getReceiverId());
            
            if (adminSession != null) {
                AdminChatWebSocketServer.sendMessage(adminSession, objectMapper.writeValueAsString(chatMessageDTO));
                log.info("消息已转发给管理员: {}", receiverKey);
            } else {
                log.info("管理员{}不在线，消息将被保存", receiverKey);
                // 保存消息会由Service层处理
            }
        } catch (Exception e) {
            log.error("消息处理出错", e);
        }
    }

    /**
     * 发生错误时调用
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误: {}", error.getMessage());
    }

    /**
     * 发送消息
     */
    public static void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            log.error("发送消息出错", e);
        }
    }
    
    /**
     * 根据用户ID获取会话
     */
    public static Session getSession(Long userId, String userType) {
        if ("customer".equals(userType)) {
            return onlineCustomers.get(userId + "_customer");
        }
        return null;
    }
} 