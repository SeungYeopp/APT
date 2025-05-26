-- MySQL dump 10.13  Distrib 8.0.38, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: aptdb
-- ------------------------------------------------------
-- Server version	8.0.39

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `review_id` bigint NOT NULL AUTO_INCREMENT,
  `time` datetime(6) DEFAULT NULL,
  `content` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `rating` double DEFAULT NULL,
  `apt_seq` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `user_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `image_url` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`review_id`),
  KEY `FKl0tafuvbqoak2m6clndp8b1yr` (`apt_seq`),
  KEY `FKiyf57dy48lyiftdrf7y87rnxi` (`user_id`),
  KEY `FK776oagvue7f35ufcipsm9qv2e` (`id`),
  CONSTRAINT `FK776oagvue7f35ufcipsm9qv2e` FOREIGN KEY (`id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKiyf57dy48lyiftdrf7y87rnxi` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`),
  CONSTRAINT `FKl0tafuvbqoak2m6clndp8b1yr` FOREIGN KEY (`apt_seq`) REFERENCES `house_infos` (`apt_seq`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review`
--

LOCK TABLES `review` WRITE;
/*!40000 ALTER TABLE `review` DISABLE KEYS */;
INSERT INTO `review` VALUES (23,'2024-11-26 11:29:13.509406','교통이 편리하고 주변에 상점들이 많아 생활하기에 매우 편리해요~',4,'11680-266','13',NULL,'/uploads/1732613972826_KakaoTalk_20241126_103616720_01.png'),(24,'2024-11-26 13:55:15.145487','주변에 편의시설이 많아요~',4.5,'11680-3621','23',NULL,'/uploads/1732613825667_KakaoTalk_20241126_103617303.png'),(25,'2024-11-26 13:55:58.972653','고깃집 바로 앞이에요~',3,'11680-4036','23',NULL,'/uploads/1732613752017_1.png'),(26,'2024-11-26 13:56:50.667785','교통이 편리해요~',4.5,'41135-138','23',NULL,'/uploads/1732613725771_KakaoTalk_20241126_103615312.png'),(27,'2024-11-26 19:05:33.298250','언덕위에 있지만 뷰는 좋아요~ 신축이에요',5,'11500-6919','12',NULL,'/uploads/1732615533289_KakaoTalk_20241126_103616720.png'),(28,'2024-11-26 19:34:23.711974','주변에 마트가 별로 없어요.\r\n너무 슬퍼요ㅠ',1.5,'41463-622','1',NULL,'/uploads/1732623671814_KakaoTalk_20241126_103615905.png'),(29,'2024-11-26 19:37:21.444759','조용하고 편리한 주변환경 덕분에 만족스러웠어요.\r\n교통편이 아주 좋아요.\r\n접근성이 좋고, 주변에 천이 있어서 산책하기 좋아여~~\r\n오래된 단지지만 공원 산책이나 자전거 타기 좋네요',3.5,'41463-622','2',NULL,'/uploads/1732623710355_KakaoTalk_20241126_103617463.png'),(30,'2024-11-26 19:38:50.764442','놀이터가 있어서 친구들과 놀기에 좋아요~!',4,'41463-622','3',NULL,'/uploads/1732621374317_마루딴스땐스댄스.jpg'),(32,'2024-11-26 21:48:42.338727','산책하기 좋아요~',3.5,'41463-622','23',NULL,'/uploads/1732625320802_image.png');
/*!40000 ALTER TABLE `review` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-24 22:37:35
