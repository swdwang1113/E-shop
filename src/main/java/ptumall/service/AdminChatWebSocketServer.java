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
 * 管理员WebSocket服务端点
 */
@Slf4j
@Component
@ServerEndpoint("/admin/ws/chat/{userId}/admin")
public class AdminChatWebSocketServer {

    // 用于存储在线连接，key为userId_admin
    private static final Map<String, Session> onlineAdmins = new ConcurrentHashMap<>();
    
    // 用于处理JSON
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        String key = userId + "_admin";
        onlineAdmins.put(key, session);
        log.info("管理员连接: {}，当前在线管理员数: {}", key, onlineAdmins.size());
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        String key = userId + "_admin";
        onlineAdmins.remove(key);
        log.info("管理员断开: {}，当前在线管理员数: {}", key, onlineAdmins.size());
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        log.info("收到来自管理员{}的消息: {}", userId, message);
        
        try {
            // 解析收到的消息
            ChatMessageDTO chatMessageDTO = objectMapper.readValue(message, ChatMessageDTO.class);
            
            // 补充发送者信息
            chatMessageDTO.setSenderId(userId);
            chatMessageDTO.setSenderType("admin");
            chatMessageDTO.setTimestamp(System.currentTimeMillis());
            
            // 接收者
            String receiverKey = chatMessageDTO.getReceiverId() + "_" + chatMessageDTO.getReceiverType();
            
            // 转发消息给接收者（客户）
            Session customerSession = ChatWebSocketServer.getSession(
                    chatMessageDTO.getReceiverId(), chatMessageDTO.getReceiverType());
                    
            if (customerSession != null) {
                sendMessage(customerSession, objectMapper.writeValueAsString(chatMessageDTO));
                log.info("消息已转发给客户: {}", receiverKey);
            } else {
                log.info("客户{}不在线，消息将被保存", receiverKey);
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
     * 根据管理员ID获取会话
     */
    public static Session getSession(Long adminId) {
        return onlineAdmins.get(adminId + "_admin");
    }
} 