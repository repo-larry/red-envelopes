/*
 Navicat Premium Data Transfer

 Source Server         : postgresql
 Source Server Type    : PostgreSQL
 Source Server Version : 140005
 Source Host           : 127.0.0.1:5432
 Source Catalog        : red_envelope
 Source Schema         : public

 Target Server Type    : PostgreSQL
 Target Server Version : 140005
 File Encoding         : 65001

 Date: 12/09/2022 14:17:42
*/


-- ----------------------------
-- Sequence structure for envelope_info_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."envelope_info_seq";
CREATE SEQUENCE "public"."envelope_info_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 2;

-- ----------------------------
-- Sequence structure for envelope_record_seq
-- ----------------------------
DROP SEQUENCE IF EXISTS "public"."envelope_record_seq";
CREATE SEQUENCE "public"."envelope_record_seq" 
INCREMENT 1
MINVALUE  1
MAXVALUE 9223372036854775807
START 1
CACHE 2;

-- ----------------------------
-- Table structure for envelope_info
-- ----------------------------
DROP TABLE IF EXISTS "public"."envelope_info";
CREATE TABLE "public"."envelope_info" (
  "id" int4 NOT NULL DEFAULT nextval('envelope_info_seq'::regclass),
  "envelope_id" int8 NOT NULL,
  "account" int4 NOT NULL,
  "number" int4 NOT NULL,
  "remaining_amount" int4 NOT NULL,
  "remaining_number" int4 NOT NULL,
  "uid" int4 NOT NULL,
  "create_time" timestamp(6) NOT NULL,
  "keep_time" int8 NOT NULL,
  "update_time" timestamp(6) NOT NULL,
  "status" int2 NOT NULL
)
;
COMMENT ON COLUMN "public"."envelope_info"."envelope_id" IS '红包ID';
COMMENT ON COLUMN "public"."envelope_info"."keep_time" IS '单位秒';
COMMENT ON COLUMN "public"."envelope_info"."status" IS '是否可用';

-- ----------------------------
-- Records of envelope_info
-- ----------------------------
INSERT INTO "public"."envelope_info" VALUES (73, 1662963249580, 10000, 30, 3203, 10, 2, '2022-09-12 14:13:40.265', 1200, '2022-09-12 14:14:08.174', 1);

-- ----------------------------
-- Table structure for envelope_record
-- ----------------------------
DROP TABLE IF EXISTS "public"."envelope_record";
CREATE TABLE "public"."envelope_record" (
  "id" int4 NOT NULL DEFAULT nextval('envelope_record_seq'::regclass),
  "account" int4 NOT NULL,
  "nick_name" varchar(255) COLLATE "pg_catalog"."default" NOT NULL,
  "uid" int4 NOT NULL,
  "envelope_id" int8 NOT NULL,
  "create_time" timestamp(6) NOT NULL
)
;

-- ----------------------------
-- Records of envelope_record
-- ----------------------------
INSERT INTO "public"."envelope_record" VALUES (51, 260, 'testUser_167', 167, 1662963249580, '2022-09-12 14:14:08.053');
INSERT INTO "public"."envelope_record" VALUES (52, 352, 'testUser_677', 677, 1662963249580, '2022-09-12 14:14:08.072');
INSERT INTO "public"."envelope_record" VALUES (53, 318, 'testUser_6569', 6569, 1662963249580, '2022-09-12 14:14:08.077');
INSERT INTO "public"."envelope_record" VALUES (54, 415, 'testUser_8700', 8700, 1662963249580, '2022-09-12 14:14:08.082');
INSERT INTO "public"."envelope_record" VALUES (55, 397, 'testUser_6929', 6929, 1662963249580, '2022-09-12 14:14:08.089');
INSERT INTO "public"."envelope_record" VALUES (56, 368, 'testUser_9230', 9230, 1662963249580, '2022-09-12 14:14:08.096');
INSERT INTO "public"."envelope_record" VALUES (57, 326, 'testUser_9085', 9085, 1662963249580, '2022-09-12 14:14:08.103');
INSERT INTO "public"."envelope_record" VALUES (58, 380, 'testUser_1901', 1901, 1662963249580, '2022-09-12 14:14:08.11');
INSERT INTO "public"."envelope_record" VALUES (59, 263, 'testUser_5405', 5405, 1662963249580, '2022-09-12 14:14:08.115');
INSERT INTO "public"."envelope_record" VALUES (60, 196, 'testUser_5010', 5010, 1662963249580, '2022-09-12 14:14:08.121');
INSERT INTO "public"."envelope_record" VALUES (61, 355, 'testUser_5138', 5138, 1662963249580, '2022-09-12 14:14:08.125');
INSERT INTO "public"."envelope_record" VALUES (62, 421, 'testUser_5112', 5112, 1662963249580, '2022-09-12 14:14:08.13');
INSERT INTO "public"."envelope_record" VALUES (63, 367, 'testUser_9615', 9615, 1662963249580, '2022-09-12 14:14:08.134');
INSERT INTO "public"."envelope_record" VALUES (64, 400, 'testUser_5872', 5872, 1662963249580, '2022-09-12 14:14:08.14');
INSERT INTO "public"."envelope_record" VALUES (65, 257, 'testUser_6041', 6041, 1662963249580, '2022-09-12 14:14:08.145');
INSERT INTO "public"."envelope_record" VALUES (66, 428, 'testUser_464', 464, 1662963249580, '2022-09-12 14:14:08.153');
INSERT INTO "public"."envelope_record" VALUES (67, 279, 'testUser_6630', 6630, 1662963249580, '2022-09-12 14:14:08.159');
INSERT INTO "public"."envelope_record" VALUES (68, 402, 'testUser_8819', 8819, 1662963249580, '2022-09-12 14:14:08.162');
INSERT INTO "public"."envelope_record" VALUES (69, 423, 'testUser_4815', 4815, 1662963249580, '2022-09-12 14:14:08.165');
INSERT INTO "public"."envelope_record" VALUES (70, 190, 'testUser_3224', 3224, 1662963249580, '2022-09-12 14:14:08.17');

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."envelope_info_seq"', 75, true);

-- ----------------------------
-- Alter sequences owned by
-- ----------------------------
SELECT setval('"public"."envelope_record_seq"', 71, true);

-- ----------------------------
-- Primary Key structure for table envelope_info
-- ----------------------------
ALTER TABLE "public"."envelope_info" ADD CONSTRAINT "envelope_info_pkey" PRIMARY KEY ("id");

-- ----------------------------
-- Indexes structure for table envelope_record
-- ----------------------------
CREATE UNIQUE INDEX "uid_envelope" ON "public"."envelope_record" USING btree (
  "uid" "pg_catalog"."int4_ops" ASC NULLS LAST,
  "envelope_id" "pg_catalog"."int8_ops" ASC NULLS LAST
);

-- ----------------------------
-- Primary Key structure for table envelope_record
-- ----------------------------
ALTER TABLE "public"."envelope_record" ADD CONSTRAINT "envelope_record_pkey" PRIMARY KEY ("id");
