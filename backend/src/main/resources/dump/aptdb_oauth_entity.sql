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
-- Table structure for table `oauth_entity`
--

DROP TABLE IF EXISTS `oauth_entity`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `oauth_entity` (
  `id` int NOT NULL AUTO_INCREMENT,
  `access_token` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `kakao_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `refresh_token` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `user_id` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  `profile_image` varchar(255) CHARACTER SET utf8mb3 COLLATE utf8mb3_bin DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK4wwi9mwstiplih5sf8g0v273r` (`user_id`),
  CONSTRAINT `FKimhxmgyqdlsuny35qnikypkbe` FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb3 COLLATE=utf8mb3_bin;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `oauth_entity`
--

LOCK TABLES `oauth_entity` WRITE;
/*!40000 ALTER TABLE `oauth_entity` DISABLE KEYS */;
INSERT INTO `oauth_entity` VALUES (4,'uw__ZNHSd-gmXzqR7qNWR-iinA2ec5ncAAAAAQo8JB8AAAGTZj5jH8whKCpFsUJR','3790159763','QG4gHmDE6hzlSKw4UM5RGsvG-xKBByHPAAAAAgo8JB8AAAGTZj5jG8whKCpFsUJR','10','http://k.kakaocdn.net/dn/owRSy/btsKUIlOXaR/Mkm4kvN3FoJWa1qwzwUBT1/img_640x640.jpg'),(5,'rAE6UG4h48N-u89DNJ9sUuUiiABXHKveAAAAAQo9c-sAAAGTZkbE93LErHmNOyL0','3790759247','6kjoYjuas-t1hDN5vkDefzTepVYSJLFWAAAAAgo9c-sAAAGTZkbE9HLErHmNOyL0','12','http://k.kakaocdn.net/dn/dJwBqw/btsKDj8lIca/oEALuQvt2lQKn6d2IzqOI1/img_640x640.jpg');
/*!40000 ALTER TABLE `oauth_entity` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-24 22:37:40
