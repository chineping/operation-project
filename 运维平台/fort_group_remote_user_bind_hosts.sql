/*
 Navicat Premium Data Transfer

 Source Server         : zxy_django
 Source Server Type    : MySQL
 Source Server Version : 50640
 Source Host           : localhost:3306
 Source Schema         : ironfort2

 Target Server Type    : MySQL
 Target Server Version : 50640
 File Encoding         : 65001

 Date: 12/07/2018 11:41:01
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for fort_group_remote_user_bind_hosts
-- ----------------------------
DROP TABLE IF EXISTS `fort_group_remote_user_bind_hosts`;
CREATE TABLE `fort_group_remote_user_bind_hosts`  (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `group_id` int(11) NOT NULL,
  `remoteuserbindhost_id` int(11) NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `fort_group_remote_user_b_group_id_remoteuserbindh_84a2dd61_uniq`(`group_id`, `remoteuserbindhost_id`) USING BTREE,
  INDEX `fort_group_remote_us_remoteuserbindhost_i_c4fc3b2c_fk_fort_remo`(`remoteuserbindhost_id`) USING BTREE,
  CONSTRAINT `fort_group_remote_user_bind_hosts_ibfk_1` FOREIGN KEY (`group_id`) REFERENCES `fort_group` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fort_group_remote_user_bind_hosts_ibfk_2` FOREIGN KEY (`remoteuserbindhost_id`) REFERENCES `fort_remoteuserbindhost` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_bin ROW_FORMAT = Compact;

SET FOREIGN_KEY_CHECKS = 1;
