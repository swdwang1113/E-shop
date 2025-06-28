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
 * 负责处理用户客户端与服务器之间的WebSocket通信
 * 实现了实时消息传递、在线状态管理等功能
 */
@Slf4j  // Lombok注解，自动创建日志对象log
@Component  // Spring组件注解，将此类注册为Spring容器管理的Bean
@ServerEndpoint("/ws/chat/{userId}/customer")  // WebSocket端点路径，{userId}为路径参数
public class ChatWebSocketServer {

    // 用于存储在线连接，key为userId_customer，value为WebSocket会话对象
    // 使用ConcurrentHashMap保证线程安全，因为WebSocket连接可能来自多个并发客户端
    private static final Map<String, Session> onlineCustomers = new ConcurrentHashMap<>();
    
    // 用于处理JSON序列化和反序列化
    // 在WebSocket通信中，消息通常以JSON字符串形式传输
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 连接建立成功调用的方法
     * 当用户客户端成功连接到此WebSocket端点时触发
     * 
     * @param session WebSocket会话对象，用于发送消息
     * @param userId 路径中的用户ID参数，标识连接的用户
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        // 构建用户连接的唯一标识键
        String key = userId + "_customer";
        // 将会话存储到在线用户映射表
        onlineCustomers.put(key, session);
        log.info("用户连接: {}，当前在线用户数: {}", key, onlineCustomers.size());
    }

    /**
     * 连接关闭调用的方法
     * 当用户客户端断开连接或会话超时时触发
     * 
     * @param userId 路径中的用户ID参数
     */
    @OnClose
    public void onClose(@PathParam("userId") Long userId) {
        // 构建用户连接的唯一标识键
        String key = userId + "_customer";
        // 从在线用户映射表中移除会话
        onlineCustomers.remove(key);
        log.info("用户断开: {}，当前在线用户数: {}", key, onlineCustomers.size());
    }

    /**
     * 收到客户端消息后调用的方法
     * 当用户客户端发送消息到服务器时触发
     * 
     * @param message 客户端发送的消息内容，通常是JSON字符串
     * @param session WebSocket会话对象
     * @param userId 路径中的用户ID参数
     */
    @OnMessage
    public void onMessage(String message, Session session, @PathParam("userId") Long userId) {
        log.info("收到来自用户{}的消息: {}", userId, message);
        
        try {
            // 将JSON字符串解析为ChatMessageDTO对象
            ChatMessageDTO chatMessageDTO = objectMapper.readValue(message, ChatMessageDTO.class);
            
            // 补充发送者信息，确保消息中包含正确的发送者ID和类型
            chatMessageDTO.setSenderId(userId);
            chatMessageDTO.setSenderType("customer");  // 标记为客户发送的消息
            chatMessageDTO.setTimestamp(System.currentTimeMillis());  // 添加时间戳
            
            // 构建接收者的标识
            String receiverKey = chatMessageDTO.getReceiverId() + "_" + chatMessageDTO.getReceiverType();
            
            // 获取接收者(管理员)的WebSocket会话
            Session adminSession = AdminChatWebSocketServer.getSession(chatMessageDTO.getReceiverId());
            
            // 如果接收者在线，则直接通过WebSocket发送消息
            if (adminSession != null) {
                // 将消息对象转换为JSON字符串并发送
                AdminChatWebSocketServer.sendMessage(adminSession, objectMapper.writeValueAsString(chatMessageDTO));
                log.info("消息已转发给管理员: {}", receiverKey);
            } else {
                // 接收者不在线，消息将只保存在数据库中
                log.info("管理员{}不在线，消息将被保存", receiverKey);
                // 消息的持久化存储由调用WebSocket的业务服务处理
                // 即使接收者不在线，消息也已经保存在数据库中，用户上线后可以查看
            }
        } catch (Exception e) {
            // 处理消息解析或发送过程中的异常
            log.error("消息处理出错", e);
        }
    }

    /**
     * 发生错误时调用的方法
     * 当WebSocket连接发生错误时触发
     * 
     * @param session WebSocket会话对象
     * @param error 错误信息
     */
    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket发生错误: {}", error.getMessage());
        // 错误处理，可以根据实际情况关闭会话或进行其他操作
    }

    /**
     * 发送消息的工具方法
     * 通过WebSocket会话发送文本消息
     * 
     * @param session 接收消息的WebSocket会话
     * @param message 要发送的消息内容
     */
    public static void sendMessage(Session session, String message) {
        try {
            // 使用WebSocket的基本远程端点发送文本消息
            session.getBasicRemote().sendText(message);
        } catch (IOException e) {
            // 处理消息发送失败的异常
            log.error("发送消息出错", e);
        }
    }
    
    /**
     * 根据用户ID获取WebSocket会话
     * 用于查找特定用户的在线状态和会话对象
     * 
     * @param userId 用户ID
     * @param userType 用户类型，必须为"customer"才能获取到会话
     * @return 用户的WebSocket会话，如果用户不在线则返回null
     */
    public static Session getSession(Long userId, String userType) {
        // 只处理客户类型的用户
        if ("customer".equals(userType)) {
            // 通过用户ID构建键并查找会话
            return onlineCustomers.get(userId + "_customer");
        }
        return null;
    }
} 