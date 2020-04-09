# 撮合交易api文档

## 协议格式
服务器统一返回格式
```json
{
     "code": 0,
     "msg":"成功或失败",
     "data": ["数据"]
}
```

**关于WebSocket**

- 端口和http使用的端口一直：85
- 目前WebSocket处于实验阶段，可以访问http://localhost:85/socket/index.html查看测试页面
- WebSocket测试页面超过3秒没有向服务器发消息将会断开连接

[**数据库表及字段下载**](./public.sql)
