-- MySQL dump 10.13  Distrib 8.0.40, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: aptdb
-- ------------------------------------------------------
-- Server version	8.0.40

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
-- Dumping data for table `chat_history`
--

LOCK TABLES `chat_history` WRITE;
/*!40000 ALTER TABLE `chat_history` DISABLE KEYS */;
INSERT INTO `chat_history` VALUES (180,'You are a helpful assistant for real estate recommendations.','system',NULL,'23'),(181,'You are a helpful assistant for real estate recommendations.','system',NULL,'23'),(182,'안녕','user',NULL,'23'),(183,'안녕하세요! 아파트나 동네에 대해 궁금한 점이 있으신가요? 어떤 평수나 가격대, 혹은 원하는 지역이 있다면 말씀해 주세요. 더 나은 추천을 드릴 수 있을 것 같아요!','assistant',NULL,'23'),(184,'강남으로 출근하기 좋은 동네 추천해줘','user',NULL,'23'),(185,'추천 동네: <strong>잠실</strong>. 이 지역은 강남으로의 출퇴근이 용이하며, 지하철 2호선과 8호선이 있어 교통이 편리합니다. 또한, 롯데월드와 다양한 상업시설이 있어 생활 인프라가 잘 갖춰져 있습니다.\n\n혹시 원하는 평수나 가격대가 있으면 말씀해 주세요! 더 구체적인 아파트 추천도 가능해요.','assistant',NULL,'23'),(186,'아이 키우기 좋은 아파트를 추천해줘','user',NULL,'23'),(187,'추천 아파트: <strong>잠실 엘스</strong>. 이 아파트는 30평대 이상의 넓은 공간을 제공하며, 아이들을 위한 교육 환경이 잘 갖춰져 있습니다. 또한, 잠실 지역은 좋은 학군이 있으며, 다양한 놀이시설과 공원이 인근에 있어 아이 키우기에 적합합니다. 가격대는 10억~15억 사이로 예상할 수 있습니다.\n\n더 궁금한 점이 있거나 다른 지역도 고려하고 싶으시면 말씀해 주세요!','assistant',NULL,'23'),(188,'40평대 아파트를 추천해줘','user',NULL,'23'),(189,'추천 아파트: <strong>잠실 파크리오</strong>. 이 아파트는 40평대 이상의 넓은 공간을 제공하며, 아이 키우기에 좋은 환경과 함께 우수한 학군이 가까이 있어 교육적 장점이 큽니다. 가격대는 15억~20억 사이로 예상되며, 다양한 생활 편의시설도 가까워 생활이 편리합니다.\n\n혹시 다른 조건이나 지역을 고려하고 싶으시면 말씀해 주세요!','assistant',NULL,'23');
/*!40000 ALTER TABLE `chat_history` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2025-05-26 14:52:50
