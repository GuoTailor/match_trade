// ------------------------------------------------------------------------------------------
// General apiDoc documentation blocks and old history blocks.
// ------------------------------------------------------------------------------------------


// ------------------------------------------------------------------------------------------
// Current Permissions.
// ------------------------------------------------------------------------------------------
/**
 * @apiDefine user 需要传入一个token作为权限验证
 * 需要header中传递Authorization
 * @apiVersion 0.0.1
 */

/**
 * @apiDefine admin 需要传入一个token作为权限验证,且具有管理员角色
 * 需要权限为admin的用户
 * @apiVersion 0.0.1
 */

/**
 * @apiDefine supperAdmin 需要传入一个token作为权限验证,且具有超级管理员角色
 * 需要权限为admin的用户
 * @apiVersion 0.0.1
 */

/**
 * @apiDefine none 无需登录授权
 * 无需登录授权
 */

/**
 * @apiDefine SUCCESS  成功
 * @apiSuccessExample {json} 成功返回:
 * {"code":0,"msg":"成功","data":null}
 */

/**
 * @api {post} /login 统一登陆
 * @apiDescription  统一登陆，输入任意账号密码登陆
 * @apiName login
 * @apiVersion 0.0.1
 * @apiParam {string} phone 用户名
 * @apiParam {string} password 密码
 * @apiSuccessExample {json} 成功返回:
 * {"code": 0,"data": "eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6IumCk-e-jueQqjEiLCJyb2xlIjpbIlJPTEVfQURNSU4iXSwiZXhwIjoxNTY3Mzk0MjUwfQ.6u_YgQcwitbkCDG91j6ghq7jAwBgYbJS_poc_c_qwhA",
    "msg": "成功"}
 * @apiGroup User
 * @apiPermission none
 */

// ------------------------------------------------------------------------------------------
// History.
// ------------------------------------------------------------------------------------------
