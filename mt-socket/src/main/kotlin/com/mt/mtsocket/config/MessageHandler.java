package com.mt.mtsocket.config;

import com.alibaba.fastjson.JSONObject;
import kotlin.reflect.jvm.internal.impl.protobuf.InvalidProtocolBufferException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by gyh on 2020/4/6.
 */
//@Component
public class MessageHandler {}/*implements WebSocketHandler {
    private final Logger log = LoggerFactory.getLogger(this.getClass().getSimpleName());

    @Autowired
    private SocketSessionRegistry sessionRegistry;
    @Autowired
    private ScheduledThreadPoolExecutor executor;
    @Autowired
    private Systemconfig systemconfig;

    @Autowired
    DispatchFactory dispatchFactory;

    @Autowired
    TaskExecutor taskExecutor;

    @Override
    public Mono<Void> handle(WebSocketSession session) {

        return session.receive().doOnSubscribe(s -> {
            log.info("发起连接:{}", s);
            *//**
             * 你有10秒时间登陆，不登陆就关掉连接;并且不给任何错误信息
             *//*
            if (systemconfig.getLoginInterval() != 0) {
                executor.schedule(() -> sessionRegistry.checkAndRemove(session), systemconfig.getLoginInterval(), TimeUnit.SECONDS);
            }
            if (systemconfig.getPingInterval() != 0) {
                executor.schedule(() -> session.send(Flux.just(session.pingMessage(DataBufferFactory::allocateBuffer))).toProcessor(), systemconfig.getPingInterval(), TimeUnit.SECONDS);
            }
        }).doOnTerminate(() -> {
            sessionRegistry.unregisterSession(session);
            StreamReferenceReq req = (StreamReferenceReq) dispatchFactory.getCommand(ReferenceMsgType.SEND_VALUE);
            taskExecutor.execute(() -> Optional.ofNullable(req.removeSession(session)).ifPresent(list -> list.forEach(req::sendStopStreamConfig)));
            log.info("doOnTerminate");
        }).doOnComplete(() -> {
            log.info("doOnComplete");
        }).doOnCancel(() -> {
            log.info("doOnCancel");
        }).doOnNext(message -> {
            if (message.getType().equals(WebSocketMessage.Type.BINARY)) {
                log.info("收到二进制消息");
                Linkproto.LinkCmd linkCmd = null;
                try {
                    linkCmd = Optional.ofNullable(Linkproto.LinkCmd.parseFrom(message.getPayload().asByteBuffer())).orElseThrow(() -> new BusinessException(500, "解析出错了"));
                    BaseDispatch<Linkproto.LinkCmd> dispatch = dispatchFactory.getCommand(linkCmd.getTypeValue());
                    log.info("处理session,{},消息实体,{},类型，{},dispatch:{}", session, linkCmd, linkCmd.getTypeValue(), dispatch);
                    dispatch.excuted(session, linkCmd);
                } catch (InvalidProtocolBufferException e) {
                    e.printStackTrace();
                }
            } else if (message.getType().equals(WebSocketMessage.Type.TEXT)) {
                String content = message.getPayloadAsText();
                log.info("收到文本消息:{}", content);
                Message msg = null;
                try {
                    msg = JSONObject.parseObject(content, Message.class);
                } catch (Exception e) {
                    JSONObject obj = new JSONObject();
                    obj.put("content", "无法理解你发过来的消息内容，不予处理:" + content);
                    obj.put("msgType", Linkproto.LinkCmdType.LINK_CMD_ZERO_VALUE);
                    session.send(Flux.just(session.textMessage(obj.toJSONString()))).then().toProcessor();
                    log.error("解析消息内容出错");
                    return;
                }
                BaseDispatch<Linkproto.LinkCmd> dispatch = dispatchFactory.getCommand(msg.getMsgType());
                if (dispatch != null) {
                    dispatch.executeMsg(session, msg);
                }
            } else if (message.getType().equals(WebSocketMessage.Type.PING)) {
                session.send(Flux.just(session.pongMessage(s -> s.wrap(new byte[256]))));
                log.info("收到ping消息");
            } else if (message.getType().equals(WebSocketMessage.Type.PONG)) {
                log.info("收到pong消息");
                if (systemconfig.getPingInterval() != 0) {
                    executor.schedule(() -> session.send(Flux.just(session.pingMessage(DataBufferFactory::allocateBuffer))).toProcessor(), systemconfig.getPingInterval(), TimeUnit.SECONDS);
                }
            }
        }).doOnError(e -> {
            e.printStackTrace();
            log.error("doOnError");
        }).doOnRequest(r -> {
            log.info("doOnRequest");
        }).then();
    }
}*/