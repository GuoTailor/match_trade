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

### 关于WebSocket

**注意**

- websocket url路径：ws://101.37.34.61:85/socket/room?roomId=房间id&bearer=token
- <a style="color:red;"> 目前WebSocket基本已稳定</a>，可以点击[这里](http://101.37.34.61:85/socket/index.html) 查看测试页面
- WebSocket测试页面超过3秒没有向服务器发消息将会断开连接

**通讯格式**

请求格式
```json
{
     "order": "/echo",
     "data": {},
     "req": 1
}
```
协议解释：

| 字段    | 类型    | 说明    |
| :---: | :----:    | :---- |   
| order | String    | 请求的路径，类似于url路径，用于标识请求的接口，注意这个是放在socket里面传输的，不是websocket的url路径 |
| data  | Object    | 请求的数据，可以参考[这个试列](http://47.107.178.147/#api-Socket-echo) |
| req   | int       | 区分请求用，当有多个请求同时到服务器时，服务器将用该位标识<br> 回应的是那一条请求. 该位建议跟随请求次数自增，当增加到2^31-1时复位为0 |

响应格式
```json
{
     "data": {
        "code": 0,
        "msg":"成功或失败",
        "data": ["数据"]
      },
     "req": 1
}
```
响应格式中的data字段和统一响应格式（http响应格式）一致


[**数据库表及字段下载**](./public.sql)
