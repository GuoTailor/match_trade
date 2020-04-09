package com.mt.mtsocket.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.socket.CloseStatus;
import org.springframework.web.reactive.socket.WebSocketSession;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;

/**
 * Created by gyh on 2020/4/6.
 */

@Service
public class SocketSessionRegistry {
    private final Logger log = LoggerFactory.getLogger(getClass().getSimpleName());

    /**
     * 这个集合存储session
     */
    private final ConcurrentMap<String, Set<String>> userSessionIds = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, WebSocketSession> clientInfoSessionIds = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, String> sessionIdUser = new ConcurrentHashMap<>();
    private ConcurrentMap<String, CountDownLatch> cacheTimestamp = new ConcurrentHashMap<>();
    private final Object lock = new Object();


    /**
     * 获取sessionId
     *
     * @param user
     * @return
     */
    private Set<String> getSessionIds(String user) {
        Set<String> set = this.userSessionIds.get(user);
        return set != null ? set : Collections.emptySet();
    }

    /**
     * 获取用户session
     *
     * @param user
     * @return
     */
    public Collection<WebSocketSession> getSessionByUser(String user) {
        Set<String> sessionIds = Optional.ofNullable(getSessionIds(user)).orElse(new CopyOnWriteArraySet<>());
        List<WebSocketSession> sessions = new ArrayList<>();
        for (String sessionId : sessionIds) {
            sessions.add(clientInfoSessionIds.get(sessionId));
        }
        return sessions;
    }

    /**
     * 获取用户session
     *
     * @param users
     * @return
     */
    public Collection<WebSocketSession> getSessionByUsers(Collection<String> users) {
        List<WebSocketSession> sessions = new ArrayList<>();
        if (!CollectionUtils.isEmpty(users)) {
            for (String user : users) {
                sessions.addAll(getSessionByUser(user));
            }
        }
        return sessions;
    }

    /**
     * 获取所有session
     *
     * @return Collection<WebSocketSession>
     */
    public Collection<WebSocketSession> getAllSessions() {
        return clientInfoSessionIds.values();
    }

    /**
     * 获取所有session
     *
     * @return
     */
    public ConcurrentMap<String, Set<String>> getAllSessionIds() {
        return this.userSessionIds;
    }

    /**
     * 获取所有session
     *
     * @return
     */
    public ConcurrentMap<String, WebSocketSession> getAllSessionWebSocketInfos() {
        return this.clientInfoSessionIds;
    }

    /**
     * register session
     *
     * @param user
     * @param sessionId
     */
    private void registerSessionId(String user, String sessionId) {

        synchronized (this.lock) {
            Set<String> set = this.userSessionIds.get(user);
            if (set == null) {
                set = new CopyOnWriteArraySet<>();
                this.userSessionIds.put(user, set);
            }
            set.add(sessionId);
        }
    }

    /**
     * 保存session
     *
     * @param session WebSocketSession
     */
    public void registerSession(WebSocketSession session, String user) {
        if (StringUtils.isEmpty(user)) {
            user = parseUserByURI(session).get("user");
        }
        if (!StringUtils.isEmpty(user)) {
            String sessionId = session.getId();
            registerSessionId(user, sessionId);
            registerSessionId(session);
            sessionIdUser.putIfAbsent(sessionId, user);
        }
    }

    /**
     * 从session里面解析参数
     *
     * @param session
     * @return
     */
    private Map<String, String> parseUserByURI(WebSocketSession session) {
        Map<String, String> map = new HashMap<>();
        String[] params = Optional.ofNullable(session.getHandshakeInfo().getUri().getQuery()).orElse("").split("&");
        for (String param : params) {
            String[] temp = param.split("=");
            if (temp.length == 2) {
                map.put(temp[0], temp[1]);
            }
        }
        return map;
    }

    public WebSocketSession getSessionBySessionId(String sessionId) {
        return this.clientInfoSessionIds.get(sessionId);
    }

    private void registerSessionId(WebSocketSession websocketInfo) {
        String sessionId = websocketInfo.getId();
        CountDownLatch signal = cacheTimestamp.putIfAbsent(sessionId, new CountDownLatch(1));
        if (signal == null) {
            signal = cacheTimestamp.get(sessionId);
            try {
                if (!clientInfoSessionIds.containsKey(sessionId)) {
                    WebSocketSession set = this.clientInfoSessionIds.get(sessionId);
                    if (set == null) {
                        clientInfoSessionIds.putIfAbsent(sessionId, websocketInfo);
                    }
                }
            } finally {
                signal.countDown();
                cacheTimestamp.remove(sessionId);
            }
        } else {
            try {
                signal.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void unregisterSessionId(String userName, String sessionId) {

        synchronized (this.lock) {
            Set set = this.userSessionIds.get(userName);
            if (set != null && set.remove(sessionId) && set.isEmpty()) {
                this.userSessionIds.remove(userName);
            }
        }
    }

    private void unregisterSessionId(String sessionId) {

        synchronized (this.lock) {
            WebSocketSession set = this.clientInfoSessionIds.get(sessionId);
            if (set != null) {
                this.clientInfoSessionIds.remove(sessionId);
            }
        }
    }

    public void unregisterSession(WebSocketSession session) {
        String sessionId = session.getId();
        String user = sessionIdUser.get(sessionId);
        if (!StringUtils.isEmpty(user)) {
            unregisterSessionId(sessionId);
            unregisterSessionId(user, sessionId);
            sessionIdUser.remove(sessionId);
        }
    }

    public void checkAndRemove(WebSocketSession session) {
        String sessionId = session.getId();
        if (!this.clientInfoSessionIds.containsKey(sessionId)) {
            log.info("sessionId:{} 10秒内没有登陆,关掉它", sessionId);
            session.close(CloseStatus.NORMAL).toProcessor();
        } else {
            log.info("sessinId:{}已经登陆，是合法的", sessionId);
        }
    }
}
