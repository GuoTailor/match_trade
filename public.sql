/*
 Navicat Premium Data Transfer

 Source Server         : localhost_5432
 Source Server Type    : PostgreSQL
 Source Server Version : 120002
 Source Host           : localhost:5432
 Source Catalog        : match_trade
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 120002
 File Encoding         : 65001

 Date: 09/06/2020 22:19:12
*/


-- ----------------------------
-- Sequence structure for mt_15m_kline_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_15m_kline_id_seq";
CREATE SEQUENCE "public"."mt_15m_kline_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mt_1d_kline_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_1d_kline_id_seq";
CREATE SEQUENCE "public"."mt_1d_kline_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mt_1h_kline_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_1h_kline_id_seq";
CREATE SEQUENCE "public"."mt_1h_kline_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mt_1m_kline_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_1m_kline_id_seq";
CREATE SEQUENCE "public"."mt_1m_kline_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mt_4h_kline_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_4h_kline_id_seq";
CREATE SEQUENCE "public"."mt_4h_kline_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1
CYCLE ;

-- ----------------------------
-- Sequence structure for mt_company_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_company_id_seq";
CREATE SEQUENCE "public"."mt_company_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mt_company_room_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_company_room_id_seq";
CREATE SEQUENCE "public"."mt_company_room_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mt_notify_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_notify_id_seq";
CREATE SEQUENCE "public"."mt_notify_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mt_notify_user_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_notify_user_id_seq";
CREATE SEQUENCE "public"."mt_notify_user_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mt_positions_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_positions_id_seq";
CREATE SEQUENCE "public"."mt_positions_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mt_role_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_role_id_seq";
CREATE SEQUENCE "public"."mt_role_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mt_room_record _id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_room_record _id_seq";
CREATE SEQUENCE "public"."mt_room_record _id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mt_room_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_room_seq";
CREATE SEQUENCE "public"."mt_room_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mt_stockholder_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_stockholder_id_seq";
CREATE SEQUENCE "public"."mt_stockholder_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for mt_trade_info_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."mt_trade_info_id_seq";
CREATE SEQUENCE "public"."mt_trade_info_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for stock_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."stock_id_seq";
CREATE SEQUENCE "public"."stock_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 2147483647
START 1
CACHE 1;

-- ----------------------------
-- Sequence structure for user_id_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."user_id_seq";
CREATE SEQUENCE "public"."user_id_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 1;

-- ----------------------------
-- Table structure for mt_15m_kline
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_15m_kline";
CREATE TABLE "public"."mt_15m_kline" (
  "id" int8 NOT NULL DEFAULT nextval('mt_15m_kline_id_seq'::regclass),
  "stock_id" int4 NOT NULL,
  "time" timestamp(6) NOT NULL,
  "trades_capacity" int8 NOT NULL,
  "trades_volume" float8 NOT NULL,
  "trades_number" int4 NOT NULL,
  "avg_price" float8 NOT NULL,
  "max_price" float8 NOT NULL,
  "min_price" float8 NOT NULL,
  "open_price" float8 NOT NULL,
  "close_price" float8 NOT NULL,
  "company_id" int4 NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_15m_kline"."stock_id" IS '股票id';
COMMENT ON COLUMN "public"."mt_15m_kline"."time" IS '值的生成时间';
COMMENT ON COLUMN "public"."mt_15m_kline"."trades_capacity" IS '交易量';
COMMENT ON COLUMN "public"."mt_15m_kline"."trades_volume" IS '交易金额';
COMMENT ON COLUMN "public"."mt_15m_kline"."trades_number" IS '交易次数';
COMMENT ON COLUMN "public"."mt_15m_kline"."avg_price" IS '平均价格';
COMMENT ON COLUMN "public"."mt_15m_kline"."max_price" IS '最高价';
COMMENT ON COLUMN "public"."mt_15m_kline"."min_price" IS '最低价';
COMMENT ON COLUMN "public"."mt_15m_kline"."open_price" IS '开盘价';
COMMENT ON COLUMN "public"."mt_15m_kline"."close_price" IS '收盘价';
COMMENT ON COLUMN "public"."mt_15m_kline"."company_id" IS '公司id';

-- ----------------------------
-- Table structure for mt_1d_kline
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_1d_kline";
CREATE TABLE "public"."mt_1d_kline" (
  "id" int8 NOT NULL DEFAULT nextval('mt_1d_kline_id_seq'::regclass),
  "stock_id" int4 NOT NULL,
  "time" timestamp(6) NOT NULL,
  "trades_capacity" int8 NOT NULL,
  "trades_volume" float8 NOT NULL,
  "trades_number" int4 NOT NULL,
  "avg_price" float8 NOT NULL,
  "max_price" float8 NOT NULL,
  "min_price" float8 NOT NULL,
  "open_price" float8 NOT NULL,
  "close_price" float8 NOT NULL,
  "company_id" int4 NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_1d_kline"."stock_id" IS '股票id';
COMMENT ON COLUMN "public"."mt_1d_kline"."time" IS '值的生成时间';
COMMENT ON COLUMN "public"."mt_1d_kline"."trades_capacity" IS '交易量';
COMMENT ON COLUMN "public"."mt_1d_kline"."trades_volume" IS '交易金额';
COMMENT ON COLUMN "public"."mt_1d_kline"."trades_number" IS '交易次数';
COMMENT ON COLUMN "public"."mt_1d_kline"."avg_price" IS '平均价格';
COMMENT ON COLUMN "public"."mt_1d_kline"."max_price" IS '最高价';
COMMENT ON COLUMN "public"."mt_1d_kline"."min_price" IS '最低价';
COMMENT ON COLUMN "public"."mt_1d_kline"."open_price" IS '开盘价';
COMMENT ON COLUMN "public"."mt_1d_kline"."close_price" IS '收盘价';
COMMENT ON COLUMN "public"."mt_1d_kline"."company_id" IS '公司id';

-- ----------------------------
-- Table structure for mt_1h_kline
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_1h_kline";
CREATE TABLE "public"."mt_1h_kline" (
  "id" int8 NOT NULL DEFAULT nextval('mt_1h_kline_id_seq'::regclass),
  "stock_id" int4 NOT NULL,
  "time" timestamp(6) NOT NULL,
  "trades_capacity" int8 NOT NULL,
  "trades_volume" float8 NOT NULL,
  "trades_number" int4 NOT NULL,
  "avg_price" float8 NOT NULL,
  "max_price" float8 NOT NULL,
  "min_price" float8 NOT NULL,
  "open_price" float8 NOT NULL,
  "close_price" float8 NOT NULL,
  "company_id" int4 NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_1h_kline"."stock_id" IS '股票id';
COMMENT ON COLUMN "public"."mt_1h_kline"."time" IS '值的生成时间';
COMMENT ON COLUMN "public"."mt_1h_kline"."trades_capacity" IS '交易量';
COMMENT ON COLUMN "public"."mt_1h_kline"."trades_volume" IS '交易金额';
COMMENT ON COLUMN "public"."mt_1h_kline"."trades_number" IS '交易次数';
COMMENT ON COLUMN "public"."mt_1h_kline"."avg_price" IS '平均价格';
COMMENT ON COLUMN "public"."mt_1h_kline"."max_price" IS '最高价';
COMMENT ON COLUMN "public"."mt_1h_kline"."min_price" IS '最低价';
COMMENT ON COLUMN "public"."mt_1h_kline"."open_price" IS '开盘价';
COMMENT ON COLUMN "public"."mt_1h_kline"."close_price" IS '收盘价';
COMMENT ON COLUMN "public"."mt_1h_kline"."company_id" IS '公司id';

-- ----------------------------
-- Table structure for mt_1m_kline
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_1m_kline";
CREATE TABLE "public"."mt_1m_kline" (
  "id" int8 NOT NULL DEFAULT nextval('mt_1m_kline_id_seq'::regclass),
  "stock_id" int4 NOT NULL,
  "time" timestamp(6) NOT NULL,
  "trades_capacity" int8 NOT NULL,
  "trades_volume" float8 NOT NULL,
  "trades_number" int4 NOT NULL,
  "avg_price" float8 NOT NULL,
  "max_price" float8 NOT NULL,
  "min_price" float8 NOT NULL,
  "open_price" float8 NOT NULL,
  "close_price" float8 NOT NULL,
  "company_id" int4 NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_1m_kline"."stock_id" IS '股票id';
COMMENT ON COLUMN "public"."mt_1m_kline"."time" IS '值的生成时间';
COMMENT ON COLUMN "public"."mt_1m_kline"."trades_capacity" IS '交易量';
COMMENT ON COLUMN "public"."mt_1m_kline"."trades_volume" IS '交易金额';
COMMENT ON COLUMN "public"."mt_1m_kline"."trades_number" IS '交易次数';
COMMENT ON COLUMN "public"."mt_1m_kline"."avg_price" IS '平均价格';
COMMENT ON COLUMN "public"."mt_1m_kline"."max_price" IS '最高价';
COMMENT ON COLUMN "public"."mt_1m_kline"."min_price" IS '最低价';
COMMENT ON COLUMN "public"."mt_1m_kline"."open_price" IS '开盘价';
COMMENT ON COLUMN "public"."mt_1m_kline"."close_price" IS '收盘价';
COMMENT ON COLUMN "public"."mt_1m_kline"."company_id" IS '公司id';

-- ----------------------------
-- Table structure for mt_4h_kline
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_4h_kline";
CREATE TABLE "public"."mt_4h_kline" (
  "id" int8 NOT NULL DEFAULT nextval('mt_4h_kline_id_seq'::regclass),
  "stock_id" int4 NOT NULL,
  "time" timestamp(6) NOT NULL,
  "trades_capacity" int8 NOT NULL,
  "trades_volume" float8 NOT NULL,
  "trades_number" int4 NOT NULL,
  "avg_price" float8 NOT NULL,
  "max_price" float8 NOT NULL,
  "min_price" float8 NOT NULL,
  "open_price" float8 NOT NULL,
  "close_price" float8 NOT NULL,
  "company_id" int4 NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_4h_kline"."stock_id" IS '股票id';
COMMENT ON COLUMN "public"."mt_4h_kline"."time" IS '值的生成时间';
COMMENT ON COLUMN "public"."mt_4h_kline"."trades_capacity" IS '交易量';
COMMENT ON COLUMN "public"."mt_4h_kline"."trades_volume" IS '交易金额';
COMMENT ON COLUMN "public"."mt_4h_kline"."trades_number" IS '交易次数';
COMMENT ON COLUMN "public"."mt_4h_kline"."avg_price" IS '平均价格';
COMMENT ON COLUMN "public"."mt_4h_kline"."max_price" IS '最高价';
COMMENT ON COLUMN "public"."mt_4h_kline"."min_price" IS '最低价';
COMMENT ON COLUMN "public"."mt_4h_kline"."open_price" IS '开盘价';
COMMENT ON COLUMN "public"."mt_4h_kline"."close_price" IS '收盘价';
COMMENT ON COLUMN "public"."mt_4h_kline"."company_id" IS '公司id';

-- ----------------------------
-- Table structure for mt_company
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_company";
CREATE TABLE "public"."mt_company" (
  "id" int4 NOT NULL DEFAULT nextval('mt_company_id_seq'::regclass),
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "room_count" int4 NOT NULL DEFAULT 1,
  "mode" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(0) DEFAULT now(),
  "license_url" varchar(255) COLLATE "pg_catalog"."default",
  "credit_union_code" varchar(255) COLLATE "pg_catalog"."default",
  "legal_person" varchar(255) COLLATE "pg_catalog"."default",
  "unit_address" varchar(255) COLLATE "pg_catalog"."default",
  "unit_contact_name" varchar(255) COLLATE "pg_catalog"."default",
  "unit_contact_phone" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."mt_company"."name" IS '公司名';
COMMENT ON COLUMN "public"."mt_company"."room_count" IS '房间数量';
COMMENT ON COLUMN "public"."mt_company"."mode" IS '竞价模式{1：点选、2： 点选+定时、3：定时 +点选+两两撮合、4：全部}';
COMMENT ON COLUMN "public"."mt_company"."create_time" IS '注册时间';
COMMENT ON COLUMN "public"."mt_company"."license_url" IS '营业执照图片地址';
COMMENT ON COLUMN "public"."mt_company"."credit_union_code" IS '统一信用社代码';
COMMENT ON COLUMN "public"."mt_company"."legal_person" IS '企业法人';
COMMENT ON COLUMN "public"."mt_company"."unit_address" IS '单位地址';
COMMENT ON COLUMN "public"."mt_company"."unit_contact_name" IS '单位联系人姓名';
COMMENT ON COLUMN "public"."mt_company"."unit_contact_phone" IS '单位联系人电话';
COMMENT ON TABLE "public"."mt_company" IS '公司';

-- ----------------------------
-- Table structure for mt_company_room
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_company_room";
CREATE TABLE "public"."mt_company_room" (
  "id" int4 NOT NULL DEFAULT nextval('mt_company_room_id_seq'::regclass),
  "model" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "company_id" int4 NOT NULL,
  "rom_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(6) NOT NULL,
  "room_id" int4 NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_company_room"."model" IS '模式对应撮合模式';
COMMENT ON COLUMN "public"."mt_company_room"."company_id" IS '公司id';
COMMENT ON COLUMN "public"."mt_company_room"."rom_name" IS '房间名字(默认公司名字+撮合模式)';
COMMENT ON COLUMN "public"."mt_company_room"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."mt_company_room"."room_id" IS '房间id';
COMMENT ON TABLE "public"."mt_company_room" IS '公司房间关系表';

-- ----------------------------
-- Table structure for mt_notify
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_notify";
CREATE TABLE "public"."mt_notify" (
  "id" int4 NOT NULL DEFAULT nextval('mt_notify_id_seq'::regclass),
  "content" text COLLATE "pg_catalog"."default",
  "title" text COLLATE "pg_catalog"."default" NOT NULL,
  "send_id" int4 NOT NULL,
  "send_type" varchar(8) COLLATE "pg_catalog"."default" NOT NULL,
  "status" varchar(8) COLLATE "pg_catalog"."default" NOT NULL,
  "create_time" timestamp(0) NOT NULL DEFAULT now(),
  "msg_type" varchar(8) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_notify"."content" IS '消息内容';
COMMENT ON COLUMN "public"."mt_notify"."title" IS '消息标题';
COMMENT ON COLUMN "public"."mt_notify"."send_id" IS '发送者id';
COMMENT ON COLUMN "public"."mt_notify"."send_type" IS '发送类型 mass：群发或assign：指定';
COMMENT ON COLUMN "public"."mt_notify"."status" IS '消息状态 progress：发送中或cancel：取消发送';
COMMENT ON COLUMN "public"."mt_notify"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."mt_notify"."msg_type" IS '消息类型 notify：通知 announce：公告';

-- ----------------------------
-- Table structure for mt_notify_user
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_notify_user";
CREATE TABLE "public"."mt_notify_user" (
  "id" int4 NOT NULL DEFAULT nextval('mt_notify_user_id_seq'::regclass),
  "user_id" int4 NOT NULL,
  "read_time" timestamp(0),
  "msg_id" int4 NOT NULL,
  "status" varchar(6) COLLATE "pg_catalog"."default" NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_notify_user"."user_id" IS '用户id';
COMMENT ON COLUMN "public"."mt_notify_user"."read_time" IS '读取时间';
COMMENT ON COLUMN "public"."mt_notify_user"."msg_id" IS '消息id';
COMMENT ON COLUMN "public"."mt_notify_user"."status" IS '状态，read已读| unread未读';

-- ----------------------------
-- Table structure for mt_positions
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_positions";
CREATE TABLE "public"."mt_positions" (
  "id" int4 NOT NULL DEFAULT nextval('mt_positions_id_seq'::regclass),
  "company_id" int4 NOT NULL,
  "stock_id" int4 NOT NULL,
  "user_id" int4 NOT NULL,
  "amount" int4 NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_positions"."company_id" IS '股票所属公司id';
COMMENT ON COLUMN "public"."mt_positions"."stock_id" IS '股票id';
COMMENT ON COLUMN "public"."mt_positions"."user_id" IS '用户id';
COMMENT ON COLUMN "public"."mt_positions"."amount" IS '数量';
COMMENT ON TABLE "public"."mt_positions" IS '持仓';

-- ----------------------------
-- Table structure for mt_role
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_role";
CREATE TABLE "public"."mt_role" (
  "id" int4 NOT NULL DEFAULT nextval('mt_role_id_seq'::regclass),
  "name" varchar(64) COLLATE "pg_catalog"."default" NOT NULL,
  "name_zh" varchar(64) COLLATE "pg_catalog"."default" NOT NULL
)
;

-- ----------------------------
-- Table structure for mt_room_bicker
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_room_bicker";
CREATE TABLE "public"."mt_room_bicker" (
  "room_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "company_id" int4 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "people" int4 NOT NULL DEFAULT 0,
  "time" time(6) NOT NULL,
  "number_trades" int4 NOT NULL,
  "low_scope" numeric NOT NULL,
  "high_scope" numeric NOT NULL,
  "enable" varchar(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamp(0) NOT NULL DEFAULT now(),
  "stock_id" int4 NOT NULL,
  "start_time" time(6) NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_room_bicker"."room_id" IS '房间id，四张表唯一';
COMMENT ON COLUMN "public"."mt_room_bicker"."company_id" IS '公司id';
COMMENT ON COLUMN "public"."mt_room_bicker"."name" IS '房间名字';
COMMENT ON COLUMN "public"."mt_room_bicker"."people" IS '人数';
COMMENT ON COLUMN "public"."mt_room_bicker"."time" IS '时长';
COMMENT ON COLUMN "public"."mt_room_bicker"."number_trades" IS '单笔交易数量';
COMMENT ON COLUMN "public"."mt_room_bicker"."low_scope" IS '报价最低值';
COMMENT ON COLUMN "public"."mt_room_bicker"."high_scope" IS '报价最高值';
COMMENT ON COLUMN "public"."mt_room_bicker"."enable" IS '是否开启{0：关闭；1：开启}';
COMMENT ON COLUMN "public"."mt_room_bicker"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."mt_room_bicker"."stock_id" IS '股票id';
COMMENT ON COLUMN "public"."mt_room_bicker"."start_time" IS '房间开启时间';
COMMENT ON TABLE "public"."mt_room_bicker" IS '抬杠交易';

-- ----------------------------
-- Table structure for mt_room_click
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_room_click";
CREATE TABLE "public"."mt_room_click" (
  "room_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "company_id" int4 NOT NULL,
  "stock_id" int4 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "people" int4 NOT NULL DEFAULT 0,
  "quote_time" time(6) NOT NULL,
  "second_stage" time(6) NOT NULL,
  "time" time(6) NOT NULL,
  "number_trades" int4 NOT NULL,
  "low_scope" numeric NOT NULL,
  "high_scope" numeric NOT NULL,
  "enable" varchar(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamp(0) NOT NULL DEFAULT now(),
  "rival" int2 NOT NULL,
  "start_time" time(6) NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_room_click"."room_id" IS '房间id，四张表唯一';
COMMENT ON COLUMN "public"."mt_room_click"."company_id" IS '公司id';
COMMENT ON COLUMN "public"."mt_room_click"."stock_id" IS '股票id';
COMMENT ON COLUMN "public"."mt_room_click"."name" IS '房间名字';
COMMENT ON COLUMN "public"."mt_room_click"."people" IS '人数';
COMMENT ON COLUMN "public"."mt_room_click"."quote_time" IS '报价和选择身份时间';
COMMENT ON COLUMN "public"."mt_room_click"."second_stage" IS '第二阶段时间';
COMMENT ON COLUMN "public"."mt_room_click"."time" IS '时长';
COMMENT ON COLUMN "public"."mt_room_click"."number_trades" IS '单笔交易数量';
COMMENT ON COLUMN "public"."mt_room_click"."low_scope" IS '报价最低值';
COMMENT ON COLUMN "public"."mt_room_click"."high_scope" IS '报价最高值';
COMMENT ON COLUMN "public"."mt_room_click"."enable" IS '是否开启{0：关闭；1：开启}';
COMMENT ON COLUMN "public"."mt_room_click"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."mt_room_click"."rival" IS '选择对手个数';
COMMENT ON COLUMN "public"."mt_room_click"."start_time" IS '房间开启时间';
COMMENT ON TABLE "public"."mt_room_click" IS '点选撮和';

-- ----------------------------
-- Table structure for mt_room_continue
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_room_continue";
CREATE TABLE "public"."mt_room_continue" (
  "room_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "company_id" int4 NOT NULL,
  "stock_id" int4 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "people" int4 NOT NULL DEFAULT 0,
  "time" time(6) NOT NULL,
  "number_trades" int4 NOT NULL,
  "low_scope" numeric NOT NULL,
  "high_scope" numeric NOT NULL,
  "enable" varchar(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamp(0) NOT NULL DEFAULT now(),
  "start_time" time(6) NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_room_continue"."room_id" IS '房间id，四张表唯一';
COMMENT ON COLUMN "public"."mt_room_continue"."company_id" IS '公司id';
COMMENT ON COLUMN "public"."mt_room_continue"."stock_id" IS '股票id';
COMMENT ON COLUMN "public"."mt_room_continue"."name" IS '房间名字';
COMMENT ON COLUMN "public"."mt_room_continue"."people" IS '人数';
COMMENT ON COLUMN "public"."mt_room_continue"."time" IS '时长';
COMMENT ON COLUMN "public"."mt_room_continue"."number_trades" IS '单笔交易数量';
COMMENT ON COLUMN "public"."mt_room_continue"."low_scope" IS '报价最低值';
COMMENT ON COLUMN "public"."mt_room_continue"."high_scope" IS '报价最高值';
COMMENT ON COLUMN "public"."mt_room_continue"."enable" IS '是否开启{0：关闭；1：开启}';
COMMENT ON COLUMN "public"."mt_room_continue"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."mt_room_continue"."start_time" IS '房间开启时间';
COMMENT ON TABLE "public"."mt_room_continue" IS '及时撮和';

-- ----------------------------
-- Table structure for mt_room_double
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_room_double";
CREATE TABLE "public"."mt_room_double" (
  "room_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "company_id" int4 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "people" int4 NOT NULL DEFAULT 0,
  "time" time(6) NOT NULL,
  "number_trades" int4 NOT NULL,
  "low_scope" numeric NOT NULL,
  "high_scope" numeric NOT NULL,
  "enable" varchar(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamp(0) NOT NULL DEFAULT now(),
  "stock_id" int4 NOT NULL,
  "start_time" time(6) NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_room_double"."room_id" IS '房间id，四张表唯一';
COMMENT ON COLUMN "public"."mt_room_double"."company_id" IS '公司id';
COMMENT ON COLUMN "public"."mt_room_double"."name" IS '房间名字';
COMMENT ON COLUMN "public"."mt_room_double"."people" IS '人数';
COMMENT ON COLUMN "public"."mt_room_double"."time" IS '时长';
COMMENT ON COLUMN "public"."mt_room_double"."number_trades" IS '单笔交易数量';
COMMENT ON COLUMN "public"."mt_room_double"."low_scope" IS '报价最低值';
COMMENT ON COLUMN "public"."mt_room_double"."high_scope" IS '报价最高值';
COMMENT ON COLUMN "public"."mt_room_double"."enable" IS '是否开启{0：关闭；1：开启}';
COMMENT ON COLUMN "public"."mt_room_double"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."mt_room_double"."stock_id" IS '股票id';
COMMENT ON COLUMN "public"."mt_room_double"."start_time" IS '房间开启时间';
COMMENT ON TABLE "public"."mt_room_double" IS '两两撮和';

-- ----------------------------
-- Table structure for mt_room_record
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_room_record";
CREATE TABLE "public"."mt_room_record" (
  "id" int4 NOT NULL DEFAULT nextval('"mt_room_record _id_seq"'::regclass),
  "mode" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "company_id" int4 NOT NULL,
  "start_time" timestamp(6) NOT NULL DEFAULT now(),
  "end_time" timestamp(6),
  "room_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "stock_id" int4 NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_room_record"."mode" IS '模式对应撮合模式';
COMMENT ON COLUMN "public"."mt_room_record"."company_id" IS '公司id';
COMMENT ON COLUMN "public"."mt_room_record"."start_time" IS '启用时间';
COMMENT ON COLUMN "public"."mt_room_record"."end_time" IS '结束时间';
COMMENT ON COLUMN "public"."mt_room_record"."room_id" IS '房间id，四张房间表唯一';
COMMENT ON COLUMN "public"."mt_room_record"."stock_id" IS '股票id';
COMMENT ON TABLE "public"."mt_room_record" IS '房间启用记录';

-- ----------------------------
-- Table structure for mt_room_timing
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_room_timing";
CREATE TABLE "public"."mt_room_timing" (
  "room_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "company_id" int4 NOT NULL,
  "stock_id" int4 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "people" int4 NOT NULL DEFAULT 0,
  "time" time(6) NOT NULL,
  "number_trades" int4 NOT NULL,
  "low_scope" numeric NOT NULL,
  "high_scope" numeric NOT NULL,
  "enable" varchar(1) COLLATE "pg_catalog"."default" NOT NULL DEFAULT 0,
  "create_time" timestamp(0) NOT NULL DEFAULT now(),
  "start_time" time(6) NOT NULL
)
;
COMMENT ON COLUMN "public"."mt_room_timing"."room_id" IS '房间id，四张表唯一';
COMMENT ON COLUMN "public"."mt_room_timing"."company_id" IS '公司id';
COMMENT ON COLUMN "public"."mt_room_timing"."stock_id" IS '股票id';
COMMENT ON COLUMN "public"."mt_room_timing"."name" IS '房间名字';
COMMENT ON COLUMN "public"."mt_room_timing"."people" IS '人数';
COMMENT ON COLUMN "public"."mt_room_timing"."time" IS '时长';
COMMENT ON COLUMN "public"."mt_room_timing"."number_trades" IS '单笔交易数量';
COMMENT ON COLUMN "public"."mt_room_timing"."low_scope" IS '报价最低值';
COMMENT ON COLUMN "public"."mt_room_timing"."high_scope" IS '报价最高值';
COMMENT ON COLUMN "public"."mt_room_timing"."enable" IS '是否开启{0：关闭；1：开启}';
COMMENT ON COLUMN "public"."mt_room_timing"."create_time" IS '创建时间';
COMMENT ON COLUMN "public"."mt_room_timing"."start_time" IS '房间开启时间';
COMMENT ON TABLE "public"."mt_room_timing" IS '定时撮合';

-- ----------------------------
-- Table structure for mt_stock
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_stock";
CREATE TABLE "public"."mt_stock" (
  "id" int4 NOT NULL DEFAULT nextval('stock_id_seq'::regclass),
  "company_id" int4 NOT NULL,
  "name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "price" numeric(11,4),
  "create_time" timestamp(6) NOT NULL DEFAULT now()
)
;
COMMENT ON COLUMN "public"."mt_stock"."company_id" IS '公司id';
COMMENT ON COLUMN "public"."mt_stock"."name" IS '股票名字';
COMMENT ON COLUMN "public"."mt_stock"."price" IS '单价';
COMMENT ON COLUMN "public"."mt_stock"."create_time" IS '创建时间';
COMMENT ON TABLE "public"."mt_stock" IS '股票';

-- ----------------------------
-- Table structure for mt_stockholder
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_stockholder";
CREATE TABLE "public"."mt_stockholder" (
  "id" int4 NOT NULL DEFAULT nextval('mt_stockholder_id_seq'::regclass),
  "user_id" int4 NOT NULL,
  "role_id" int4 NOT NULL,
  "company_id" int4,
  "real_name" varchar(32) COLLATE "pg_catalog"."default",
  "department" varchar(64) COLLATE "pg_catalog"."default",
  "position" varchar(64) COLLATE "pg_catalog"."default",
  "money" numeric(11,4) NOT NULL DEFAULT 0
)
;
COMMENT ON COLUMN "public"."mt_stockholder"."user_id" IS '用户id';
COMMENT ON COLUMN "public"."mt_stockholder"."role_id" IS '该用户在公司的角色id';
COMMENT ON COLUMN "public"."mt_stockholder"."company_id" IS '公司id';
COMMENT ON COLUMN "public"."mt_stockholder"."real_name" IS '真实姓名';
COMMENT ON COLUMN "public"."mt_stockholder"."department" IS '所在部门';
COMMENT ON COLUMN "public"."mt_stockholder"."position" IS '职位';
COMMENT ON COLUMN "public"."mt_stockholder"."money" IS '资金';

-- ----------------------------
-- Table structure for mt_trade_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_trade_info";
CREATE TABLE "public"."mt_trade_info" (
  "id" int4 NOT NULL DEFAULT nextval('mt_trade_info_id_seq'::regclass),
  "company_id" int4 NOT NULL,
  "stock_id" int4 NOT NULL,
  "room_id" varchar(20) COLLATE "pg_catalog"."default" NOT NULL,
  "model" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "buyer_id" int4 NOT NULL,
  "buyer_price" float8 NOT NULL,
  "seller_id" int4 NOT NULL,
  "seller_price" float8 NOT NULL,
  "trade_price" float8 NOT NULL,
  "trade_time" timestamp(6) NOT NULL,
  "trade_state" varchar(32) COLLATE "pg_catalog"."default" NOT NULL,
  "state_details" varchar(255) COLLATE "pg_catalog"."default",
  "trade_amount" int4 NOT NULL,
  "trade_money" float8 NOT NULL,
  "buyer_name" varchar(255) COLLATE "pg_catalog"."default",
  "seller_name" varchar(255) COLLATE "pg_catalog"."default"
)
;
COMMENT ON COLUMN "public"."mt_trade_info"."company_id" IS '公司id';
COMMENT ON COLUMN "public"."mt_trade_info"."stock_id" IS '股票id';
COMMENT ON COLUMN "public"."mt_trade_info"."room_id" IS '房间id(在那个房间进行的交易)';
COMMENT ON COLUMN "public"."mt_trade_info"."model" IS '模式对应撮合模式';
COMMENT ON COLUMN "public"."mt_trade_info"."buyer_id" IS '买方id';
COMMENT ON COLUMN "public"."mt_trade_info"."buyer_price" IS '买方价格';
COMMENT ON COLUMN "public"."mt_trade_info"."seller_id" IS '卖方id';
COMMENT ON COLUMN "public"."mt_trade_info"."seller_price" IS '卖方价格';
COMMENT ON COLUMN "public"."mt_trade_info"."trade_price" IS '成交价格';
COMMENT ON COLUMN "public"."mt_trade_info"."trade_time" IS '交易时间';
COMMENT ON COLUMN "public"."mt_trade_info"."trade_state" IS '交易状态';
COMMENT ON COLUMN "public"."mt_trade_info"."state_details" IS '状态原因';
COMMENT ON COLUMN "public"."mt_trade_info"."trade_amount" IS '成交数量';
COMMENT ON COLUMN "public"."mt_trade_info"."trade_money" IS '交易金额';
COMMENT ON COLUMN "public"."mt_trade_info"."buyer_name" IS '买家姓名';
COMMENT ON COLUMN "public"."mt_trade_info"."seller_name" IS '卖家姓名';

-- ----------------------------
-- Table structure for mt_user
-- ----------------------------
DROP TABLE IF EXISTS "public"."mt_user";
CREATE TABLE "public"."mt_user" (
  "id" int4 NOT NULL DEFAULT nextval('user_id_seq'::regclass),
  "phone" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "nick_name" varchar(255) COLLATE "pg_catalog"."default",
  "id_num" varchar(18) COLLATE "pg_catalog"."default",
  "password" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "user_photo" varchar(255) COLLATE "pg_catalog"."default",
  "create_time" timestamp(0) NOT NULL DEFAULT now(),
  "last_time" timestamp(6),
  "read_time" timestamp(6) NOT NULL DEFAULT now()
)
;
COMMENT ON COLUMN "public"."mt_user"."id" IS '自增id';
COMMENT ON COLUMN "public"."mt_user"."phone" IS '手机号';
COMMENT ON COLUMN "public"."mt_user"."nick_name" IS '昵称';
COMMENT ON COLUMN "public"."mt_user"."id_num" IS '身份证号码';
COMMENT ON COLUMN "public"."mt_user"."password" IS '密码';
COMMENT ON COLUMN "public"."mt_user"."user_photo" IS '头像url';
COMMENT ON COLUMN "public"."mt_user"."create_time" IS '注册时间';
COMMENT ON COLUMN "public"."mt_user"."last_time" IS '最后登录时间';
COMMENT ON COLUMN "public"."mt_user"."read_time" IS '最后拉取消息时间';

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_15m_kline_id_seq"
OWNED BY "public"."mt_15m_kline"."id";
SELECT setval('"public"."mt_15m_kline_id_seq"', 2, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_1d_kline_id_seq"
OWNED BY "public"."mt_1d_kline"."id";
SELECT setval('"public"."mt_1d_kline_id_seq"', 8, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_1h_kline_id_seq"
OWNED BY "public"."mt_1h_kline"."id";
SELECT setval('"public"."mt_1h_kline_id_seq"', 2, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_1m_kline_id_seq"
OWNED BY "public"."mt_1m_kline"."id";
SELECT setval('"public"."mt_1m_kline_id_seq"', 2, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_4h_kline_id_seq"
OWNED BY "public"."mt_4h_kline"."id";
SELECT setval('"public"."mt_4h_kline_id_seq"', 2, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_company_id_seq"
OWNED BY "public"."mt_company"."id";
SELECT setval('"public"."mt_company_id_seq"', 4, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_company_room_id_seq"
OWNED BY "public"."mt_company_room"."id";
SELECT setval('"public"."mt_company_room_id_seq"', 2, false);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_notify_id_seq"
OWNED BY "public"."mt_notify"."id";
SELECT setval('"public"."mt_notify_id_seq"', 10, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_notify_user_id_seq"
OWNED BY "public"."mt_notify_user"."id";
SELECT setval('"public"."mt_notify_user_id_seq"', 5, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_positions_id_seq"
OWNED BY "public"."mt_positions"."id";
SELECT setval('"public"."mt_positions_id_seq"', 58, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_role_id_seq"
OWNED BY "public"."mt_role"."id";
SELECT setval('"public"."mt_role_id_seq"', 3, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_room_record _id_seq"
OWNED BY "public"."mt_room_record"."id";
SELECT setval('"public"."mt_room_record _id_seq"', 235, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."mt_room_seq"', 28, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_stockholder_id_seq"
OWNED BY "public"."mt_stockholder"."id";
SELECT setval('"public"."mt_stockholder_id_seq"', 73, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."mt_trade_info_id_seq"
OWNED BY "public"."mt_trade_info"."id";
SELECT setval('"public"."mt_trade_info_id_seq"', 255, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."stock_id_seq"
OWNED BY "public"."mt_stock"."id";
SELECT setval('"public"."stock_id_seq"', 3, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
ALTER SEQUENCE "public"."user_id_seq"
OWNED BY "public"."mt_user"."id";
SELECT setval('"public"."user_id_seq"', 35, true);

-- ----------------------------
-- Indexes structure for table mt_15m_kline
-- ----------------------------
CREATE INDEX "mt_15m_kline_time_idx" ON "public"."mt_15m_kline" USING btree (
  "time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mt_15m_kline
-- ----------------------------
ALTER TABLE "public"."mt_15m_kline" ADD CONSTRAINT "mt_sec_kline_copy1_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table mt_1d_kline
-- ----------------------------
CREATE INDEX "mt_1d_kline_time_idx" ON "public"."mt_1d_kline" USING btree (
  "time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mt_1d_kline
-- ----------------------------
ALTER TABLE "public"."mt_1d_kline" ADD CONSTRAINT "mt_1m_kline_copy1_pkey1" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table mt_1h_kline
-- ----------------------------
CREATE INDEX "mt_1h_kline_time_idx" ON "public"."mt_1h_kline" USING btree (
  "time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mt_1h_kline
-- ----------------------------
ALTER TABLE "public"."mt_1h_kline" ADD CONSTRAINT "mt_1m_kline_copy1_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table mt_1m_kline
-- ----------------------------
CREATE INDEX "mt_1m_kline_time_idx" ON "public"."mt_1m_kline" USING btree (
  "time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mt_1m_kline
-- ----------------------------
ALTER TABLE "public"."mt_1m_kline" ADD CONSTRAINT "mt_sec_kline_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table mt_4h_kline
-- ----------------------------
CREATE INDEX "mt_4h_kline_time_idx" ON "public"."mt_4h_kline" USING btree (
  "time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mt_4h_kline
-- ----------------------------
ALTER TABLE "public"."mt_4h_kline" ADD CONSTRAINT "mt_1h_kline_copy1_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table mt_company
-- ----------------------------
ALTER TABLE "public"."mt_company" ADD CONSTRAINT "mt_company_name_key" UNIQUE ("name");

-- ----------------------------
-- Primary Key structure for table mt_company
-- ----------------------------
ALTER TABLE "public"."mt_company" ADD CONSTRAINT "mt_company_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table mt_company_room
-- ----------------------------
ALTER TABLE "public"."mt_company_room" ADD CONSTRAINT "mt_company_room_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table mt_notify
-- ----------------------------
ALTER TABLE "public"."mt_notify" ADD CONSTRAINT "mt_notify_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table mt_notify_user
-- ----------------------------
ALTER TABLE "public"."mt_notify_user" ADD CONSTRAINT "mt_notify_user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table mt_positions
-- ----------------------------
ALTER TABLE "public"."mt_positions" ADD CONSTRAINT "mt_positions_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table mt_role
-- ----------------------------
ALTER TABLE "public"."mt_role" ADD CONSTRAINT "mt_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table mt_room_bicker
-- ----------------------------
CREATE UNIQUE INDEX "mt_room_bicker_room_id_idx" ON "public"."mt_room_bicker" USING btree (
  "room_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mt_room_bicker
-- ----------------------------
ALTER TABLE "public"."mt_room_bicker" ADD CONSTRAINT "mt_room_bicker_pkey" PRIMARY KEY ("room_id");

-- ----------------------------
-- Indexes structure for table mt_room_click
-- ----------------------------
CREATE UNIQUE INDEX "mt_room_click_room_number_idx" ON "public"."mt_room_click" USING btree (
  "room_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mt_room_click
-- ----------------------------
ALTER TABLE "public"."mt_room_click" ADD CONSTRAINT "mt_room_click_pkey" PRIMARY KEY ("room_id");

-- ----------------------------
-- Indexes structure for table mt_room_continue
-- ----------------------------
CREATE UNIQUE INDEX "mt_room_timely_room_number_idx" ON "public"."mt_room_continue" USING btree (
  "room_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mt_room_continue
-- ----------------------------
ALTER TABLE "public"."mt_room_continue" ADD CONSTRAINT "mt_room_timely_pkey" PRIMARY KEY ("room_id");

-- ----------------------------
-- Indexes structure for table mt_room_double
-- ----------------------------
CREATE UNIQUE INDEX "mt_room_double_room_number_idx" ON "public"."mt_room_double" USING btree (
  "room_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mt_room_double
-- ----------------------------
ALTER TABLE "public"."mt_room_double" ADD CONSTRAINT "mt_room_double_pkey" PRIMARY KEY ("room_id");

-- ----------------------------
-- Primary Key structure for table mt_room_record
-- ----------------------------
ALTER TABLE "public"."mt_room_record" ADD CONSTRAINT "mt_room_record _pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table mt_room_timing
-- ----------------------------
CREATE UNIQUE INDEX "mt_room_timing_room_number_idx" ON "public"."mt_room_timing" USING btree (
  "room_id" COLLATE "pg_catalog"."default" "pg_catalog"."text_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mt_room_timing
-- ----------------------------
ALTER TABLE "public"."mt_room_timing" ADD CONSTRAINT "mt_room_timing_pkey" PRIMARY KEY ("room_id");

-- ----------------------------
-- Uniques structure for table mt_stock
-- ----------------------------
ALTER TABLE "public"."mt_stock" ADD CONSTRAINT "mt_stock_name_key" UNIQUE ("name");

-- ----------------------------
-- Primary Key structure for table mt_stock
-- ----------------------------
ALTER TABLE "public"."mt_stock" ADD CONSTRAINT "stock_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Uniques structure for table mt_stockholder
-- ----------------------------
ALTER TABLE "public"."mt_stockholder" ADD CONSTRAINT "mt_user_role_userid_roleid_companyid_key" UNIQUE ("user_id", "role_id", "company_id");

-- ----------------------------
-- Primary Key structure for table mt_stockholder
-- ----------------------------
ALTER TABLE "public"."mt_stockholder" ADD CONSTRAINT "mt_user_role_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table mt_trade_info
-- ----------------------------
CREATE INDEX "mt_trade_info_trade_time_idx" ON "public"."mt_trade_info" USING btree (
  "trade_time" "pg_catalog"."timestamp_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table mt_trade_info
-- ----------------------------
ALTER TABLE "public"."mt_trade_info" ADD CONSTRAINT "mt_trade_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Primary Key structure for table mt_user
-- ----------------------------
ALTER TABLE "public"."mt_user" ADD CONSTRAINT "user_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Foreign Keys structure for table mt_15m_kline
-- ----------------------------
ALTER TABLE "public"."mt_15m_kline" ADD CONSTRAINT "mt_15m_kline_company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mt_15m_kline" ADD CONSTRAINT "mt_15m_kline_stock_id_fkey" FOREIGN KEY ("stock_id") REFERENCES "public"."mt_stock" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_1d_kline
-- ----------------------------
ALTER TABLE "public"."mt_1d_kline" ADD CONSTRAINT "mt_1d_kline_company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mt_1d_kline" ADD CONSTRAINT "mt_1d_kline_stock_id_fkey1" FOREIGN KEY ("stock_id") REFERENCES "public"."mt_stock" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_1h_kline
-- ----------------------------
ALTER TABLE "public"."mt_1h_kline" ADD CONSTRAINT "mt_1h_kline_company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mt_1h_kline" ADD CONSTRAINT "mt_1h_kline_stock_id_fkey" FOREIGN KEY ("stock_id") REFERENCES "public"."mt_stock" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_1m_kline
-- ----------------------------
ALTER TABLE "public"."mt_1m_kline" ADD CONSTRAINT "mt_1m_kline_company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mt_1m_kline" ADD CONSTRAINT "mt_1m_kline_stock_id_fkey" FOREIGN KEY ("stock_id") REFERENCES "public"."mt_stock" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_4h_kline
-- ----------------------------
ALTER TABLE "public"."mt_4h_kline" ADD CONSTRAINT "mt_4h_kline_company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mt_4h_kline" ADD CONSTRAINT "mt_4h_kline_stock_id_fkey" FOREIGN KEY ("stock_id") REFERENCES "public"."mt_stock" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_company_room
-- ----------------------------
ALTER TABLE "public"."mt_company_room" ADD CONSTRAINT "mt_company_room_company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_notify
-- ----------------------------
ALTER TABLE "public"."mt_notify" ADD CONSTRAINT "mt_notify_send_id_fkey" FOREIGN KEY ("send_id") REFERENCES "public"."mt_user" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_notify_user
-- ----------------------------
ALTER TABLE "public"."mt_notify_user" ADD CONSTRAINT "mt_notify_user_msg_id_fkey" FOREIGN KEY ("msg_id") REFERENCES "public"."mt_notify" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mt_notify_user" ADD CONSTRAINT "mt_notify_user_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."mt_user" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_positions
-- ----------------------------
ALTER TABLE "public"."mt_positions" ADD CONSTRAINT "mt_positions_company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "public"."mt_positions" ADD CONSTRAINT "mt_positions_stock_id_fkey" FOREIGN KEY ("stock_id") REFERENCES "public"."mt_stock" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mt_positions" ADD CONSTRAINT "mt_positions_user_id_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."mt_user" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_room_bicker
-- ----------------------------
ALTER TABLE "public"."mt_room_bicker" ADD CONSTRAINT "mt_room_bicker_company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "public"."mt_room_bicker" ADD CONSTRAINT "mt_room_bicker_stock_id_fkey" FOREIGN KEY ("stock_id") REFERENCES "public"."mt_stock" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_room_click
-- ----------------------------
ALTER TABLE "public"."mt_room_click" ADD CONSTRAINT "mt_room_click_company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "public"."mt_room_click" ADD CONSTRAINT "mt_room_click_stock_id_fkey" FOREIGN KEY ("stock_id") REFERENCES "public"."mt_stock" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_room_continue
-- ----------------------------
ALTER TABLE "public"."mt_room_continue" ADD CONSTRAINT "mt_room_timely_company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "public"."mt_room_continue" ADD CONSTRAINT "mt_room_timely_stock_id_fkey" FOREIGN KEY ("stock_id") REFERENCES "public"."mt_stock" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_room_double
-- ----------------------------
ALTER TABLE "public"."mt_room_double" ADD CONSTRAINT "mt_room_double_company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "public"."mt_room_double" ADD CONSTRAINT "mt_room_double_stock_id_fkey" FOREIGN KEY ("stock_id") REFERENCES "public"."mt_stock" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_room_record
-- ----------------------------
ALTER TABLE "public"."mt_room_record" ADD CONSTRAINT "mt_room_record _company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mt_room_record" ADD CONSTRAINT "mt_room_record _stock_id_fkey" FOREIGN KEY ("stock_id") REFERENCES "public"."mt_stock" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_room_timing
-- ----------------------------
ALTER TABLE "public"."mt_room_timing" ADD CONSTRAINT "mt_room_timing_company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;
ALTER TABLE "public"."mt_room_timing" ADD CONSTRAINT "mt_room_timing_stock_id_fkey" FOREIGN KEY ("stock_id") REFERENCES "public"."mt_stock" ("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_stock
-- ----------------------------
ALTER TABLE "public"."mt_stock" ADD CONSTRAINT "stock_cid_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_stockholder
-- ----------------------------
ALTER TABLE "public"."mt_stockholder" ADD CONSTRAINT "mt_user_role_companyid_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE NO ACTION ON UPDATE CASCADE;
ALTER TABLE "public"."mt_stockholder" ADD CONSTRAINT "mt_user_role_roleid_fkey" FOREIGN KEY ("role_id") REFERENCES "public"."mt_role" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mt_stockholder" ADD CONSTRAINT "mt_user_role_userid_fkey" FOREIGN KEY ("user_id") REFERENCES "public"."mt_user" ("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- ----------------------------
-- Foreign Keys structure for table mt_trade_info
-- ----------------------------
ALTER TABLE "public"."mt_trade_info" ADD CONSTRAINT "mt_trade_info_buyer_id_fkey" FOREIGN KEY ("buyer_id") REFERENCES "public"."mt_user" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mt_trade_info" ADD CONSTRAINT "mt_trade_info_company_id_fkey" FOREIGN KEY ("company_id") REFERENCES "public"."mt_company" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mt_trade_info" ADD CONSTRAINT "mt_trade_info_seller_id_fkey" FOREIGN KEY ("seller_id") REFERENCES "public"."mt_user" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE "public"."mt_trade_info" ADD CONSTRAINT "mt_trade_info_stock_id_fkey" FOREIGN KEY ("stock_id") REFERENCES "public"."mt_stock" ("id") ON DELETE CASCADE ON UPDATE CASCADE;
