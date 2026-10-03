-- MySQL dump 10.13  Distrib 8.0.46, for Linux (x86_64)
--
-- Host: localhost    Database: auth_db
-- ------------------------------------------------------
-- Server version	8.0.46

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Current Database: `auth_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `auth_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `auth_db`;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `enabled` bit(1) NOT NULL,
  `full_name` varchar(255) NOT NULL,
  `hazard` enum('DROUGHT','FIRE','FLOOD','MINING_ACCIDENT','ZOONOTIC_DISEASE') DEFAULT NULL,
  `password_hash` varchar(255) NOT NULL,
  `province` varchar(255) DEFAULT NULL,
  `role` enum('NATIONAL_VIEWER','PROVINCIAL_ADMIN','PROVINCIAL_SUPERVISOR','WARD_RECORDER') NOT NULL,
  `username` varchar(255) NOT NULL,
  `ward` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr43af9ap4edm43mmtq01oddj6` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,_binary '','Flood Ward Recorder','FLOOD','$2a$10$WkTedjECtVPL9YTv5XnbMuxMmArAzyfHvoK6n5k.cg59ekyOGOPa.','Mashonaland Central','WARD_RECORDER','flood.recorder','Rushinga Ward 1'),(2,_binary '','Drought Ward Recorder','DROUGHT','$2a$10$RfS6vxOjwqFMUlro24I37enM8rfIsVvPe5vT9o39SiR5UlWUCtkVm','Mashonaland Central','WARD_RECORDER','drought.recorder','Rushinga Ward 2'),(3,_binary '','Fire Ward Recorder','FIRE','$2a$10$0hgzu9w.4iJ38bkK8bO3QOE8sKzp85ApTcBhRVapKByiUnwB1qWke','Mashonaland Central','WARD_RECORDER','fire.recorder','Rushinga Ward 3'),(4,_binary '','Zoonotic Disease Ward Recorder','ZOONOTIC_DISEASE','$2a$10$I/D9dAbLHnqMSb4pj4S1x.azAK4lmCtXbqB6X/UjtTqbdveNNwvXa','Mashonaland Central','WARD_RECORDER','zoonotic.recorder','Rushinga Ward 4'),(5,_binary '','Mining Accident Ward Recorder','MINING_ACCIDENT','$2a$10$Qc8OLSkqNgrrJ7vIq9Jk2.ywTIqUmhPeY3werNLSxWJUIQA.FxlKa','Mashonaland Central','WARD_RECORDER','mining.recorder','Rushinga Ward 5'),(6,_binary '','Flood Provincial Supervisor','FLOOD','$2a$10$50HnBgawEKMNgWOiN3p.hOL80jCLA0K4YeJ/Rg43uGTUX55Na8.mW','Mashonaland Central','PROVINCIAL_SUPERVISOR','flood.supervisor',NULL),(7,_binary '','Drought Provincial Supervisor','DROUGHT','$2a$10$X.NuRcAvdZ8fX6vMdBsJfuhedwB/giunSYU/c/MSOnDfB7hEjAi66','Mashonaland Central','PROVINCIAL_SUPERVISOR','drought.supervisor',NULL),(8,_binary '','Fire Provincial Supervisor','FIRE','$2a$10$PwmTp6K4J6Js.aiX06jQE.4Y0/O36adSvaCv.7CaDiiE/OGrVAZoS','Mashonaland Central','PROVINCIAL_SUPERVISOR','fire.supervisor',NULL),(9,_binary '','Zoonotic Disease Provincial Supervisor','ZOONOTIC_DISEASE','$2a$10$GNQMWr0QP6ibNpInB9coaubSha5ai9iiSl4kmXVmGX.L8YVeJMH2u','Mashonaland Central','PROVINCIAL_SUPERVISOR','zoonotic.supervisor',NULL),(10,_binary '','Mining Accident Provincial Supervisor','MINING_ACCIDENT','$2a$10$ZOQG0CH.5idsQA4KYsYMW.JFHGXxTKMafAICJvU/.Jf.U1c.MM7ju','Mashonaland Central','PROVINCIAL_SUPERVISOR','mining.supervisor',NULL),(11,_binary '','National Viewer',NULL,'$2a$10$7DSITTCX0tm6giu6YE.p/.u8GGnVr5alElcHX01LKO.MoLdXzoZ7K','Mashonaland Central','NATIONAL_VIEWER','national.viewer',NULL),(12,_binary '','Provincial Administrator',NULL,'$2a$10$63S.nGRWGNihNDyuD30yQONmoMNadO6YBOlGRD5Hedk2IBg01Q0Mi','Mashonaland Central','PROVINCIAL_ADMIN','provincial.admin',NULL);
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'auth_db'
--

--
-- Current Database: `flood_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `flood_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `flood_db`;

--
-- Table structure for table `flood_audit_log`
--

DROP TABLE IF EXISTS `flood_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flood_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` enum('APPROVED','CORRECTIONS_REQUESTED','DELETED','REJECTED','RESUBMITTED','SUBMITTED') NOT NULL,
  `incident_id` bigint NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `performed_by_user_id` bigint NOT NULL,
  `performed_by_username` varchar(255) DEFAULT NULL,
  `timestamp` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flood_audit_log`
--

LOCK TABLES `flood_audit_log` WRITE;
/*!40000 ALTER TABLE `flood_audit_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `flood_audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `flood_incidents`
--

DROP TABLE IF EXISTS `flood_incidents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flood_incidents` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `district` varchar(255) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `occurred_at` datetime(6) NOT NULL,
  `province` varchar(255) NOT NULL,
  `reporter_id` bigint NOT NULL,
  `review_notes` varchar(1000) DEFAULT NULL,
  `severity` enum('CRITICAL','HIGH','LOW','MODERATE') NOT NULL,
  `status` enum('APPROVED','CORRECTIONS_REQUESTED','PENDING','REJECTED') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `ward` varchar(255) NOT NULL,
  `area_flooded_hectares` double NOT NULL,
  `catchment` enum('GWAYI','MANYAME','MAZOWE','MZINGWANE','RUNDE','SANYATI','SAVE') NOT NULL,
  `households_displaced` int NOT NULL,
  `inundation_duration_days` int NOT NULL,
  `peak_water_level_metres` double NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `flood_incidents`
--

LOCK TABLES `flood_incidents` WRITE;
/*!40000 ALTER TABLE `flood_incidents` DISABLE KEYS */;
/*!40000 ALTER TABLE `flood_incidents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'flood_db'
--

--
-- Current Database: `drought_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `drought_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `drought_db`;

--
-- Table structure for table `drought_audit_log`
--

DROP TABLE IF EXISTS `drought_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drought_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` enum('APPROVED','CORRECTIONS_REQUESTED','DELETED','REJECTED','RESUBMITTED','SUBMITTED') NOT NULL,
  `incident_id` bigint NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `performed_by_user_id` bigint NOT NULL,
  `performed_by_username` varchar(255) DEFAULT NULL,
  `timestamp` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drought_audit_log`
--

LOCK TABLES `drought_audit_log` WRITE;
/*!40000 ALTER TABLE `drought_audit_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `drought_audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `drought_incidents`
--

DROP TABLE IF EXISTS `drought_incidents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `drought_incidents` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `district` varchar(255) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `occurred_at` datetime(6) NOT NULL,
  `province` varchar(255) NOT NULL,
  `reporter_id` bigint NOT NULL,
  `review_notes` varchar(1000) DEFAULT NULL,
  `severity` enum('CRITICAL','HIGH','LOW','MODERATE') NOT NULL,
  `status` enum('APPROVED','CORRECTIONS_REQUESTED','PENDING','REJECTED') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `ward` varchar(255) NOT NULL,
  `consecutive_dry_days` int NOT NULL,
  `crop_failure_percent` double NOT NULL,
  `livestock_mortality_count` int NOT NULL,
  `people_facing_water_shortage` int NOT NULL,
  `rainfall_deficit_mm` double NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `drought_incidents`
--

LOCK TABLES `drought_incidents` WRITE;
/*!40000 ALTER TABLE `drought_incidents` DISABLE KEYS */;
/*!40000 ALTER TABLE `drought_incidents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'drought_db'
--

--
-- Current Database: `fire_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `fire_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `fire_db`;

--
-- Table structure for table `fire_audit_log`
--

DROP TABLE IF EXISTS `fire_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fire_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` enum('APPROVED','CORRECTIONS_REQUESTED','DELETED','REJECTED','RESUBMITTED','SUBMITTED') NOT NULL,
  `incident_id` bigint NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `performed_by_user_id` bigint NOT NULL,
  `performed_by_username` varchar(255) DEFAULT NULL,
  `timestamp` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fire_audit_log`
--

LOCK TABLES `fire_audit_log` WRITE;
/*!40000 ALTER TABLE `fire_audit_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `fire_audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `fire_incidents`
--

DROP TABLE IF EXISTS `fire_incidents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `fire_incidents` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `district` varchar(255) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `occurred_at` datetime(6) NOT NULL,
  `province` varchar(255) NOT NULL,
  `reporter_id` bigint NOT NULL,
  `review_notes` varchar(1000) DEFAULT NULL,
  `severity` enum('CRITICAL','HIGH','LOW','MODERATE') NOT NULL,
  `status` enum('APPROVED','CORRECTIONS_REQUESTED','PENDING','REJECTED') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `ward` varchar(255) NOT NULL,
  `area_burned_hectares` double NOT NULL,
  `contained` bit(1) NOT NULL,
  `injuries_fatalities_count` int NOT NULL,
  `structures_destroyed_count` int NOT NULL,
  `suspected_cause` enum('ACCIDENTAL','DELIBERATE','NATURAL') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `fire_incidents`
--

LOCK TABLES `fire_incidents` WRITE;
/*!40000 ALTER TABLE `fire_incidents` DISABLE KEYS */;
/*!40000 ALTER TABLE `fire_incidents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'fire_db'
--

--
-- Current Database: `zoonotic_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `zoonotic_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `zoonotic_db`;

--
-- Table structure for table `zoonotic_disease_audit_log`
--

DROP TABLE IF EXISTS `zoonotic_disease_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `zoonotic_disease_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` enum('APPROVED','CORRECTIONS_REQUESTED','DELETED','REJECTED','RESUBMITTED','SUBMITTED') NOT NULL,
  `incident_id` bigint NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `performed_by_user_id` bigint NOT NULL,
  `performed_by_username` varchar(255) DEFAULT NULL,
  `timestamp` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zoonotic_disease_audit_log`
--

LOCK TABLES `zoonotic_disease_audit_log` WRITE;
/*!40000 ALTER TABLE `zoonotic_disease_audit_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `zoonotic_disease_audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `zoonotic_disease_incidents`
--

DROP TABLE IF EXISTS `zoonotic_disease_incidents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `zoonotic_disease_incidents` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `district` varchar(255) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `occurred_at` datetime(6) NOT NULL,
  `province` varchar(255) NOT NULL,
  `reporter_id` bigint NOT NULL,
  `review_notes` varchar(1000) DEFAULT NULL,
  `severity` enum('CRITICAL','HIGH','LOW','MODERATE') NOT NULL,
  `status` enum('APPROVED','CORRECTIONS_REQUESTED','PENDING','REJECTED') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `ward` varchar(255) NOT NULL,
  `animal_species` enum('CATTLE','DOGS','GOATS','OTHER','PIGS','POULTRY','SHEEP','WILDLIFE') NOT NULL,
  `confirmed_animal_cases` int NOT NULL,
  `disease_name` varchar(255) NOT NULL,
  `human_cases_count` int NOT NULL,
  `outbreak_classification` enum('CLUSTER','OUTBREAK') NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `zoonotic_disease_incidents`
--

LOCK TABLES `zoonotic_disease_incidents` WRITE;
/*!40000 ALTER TABLE `zoonotic_disease_incidents` DISABLE KEYS */;
/*!40000 ALTER TABLE `zoonotic_disease_incidents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'zoonotic_db'
--

--
-- Current Database: `mining_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `mining_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `mining_db`;

--
-- Table structure for table `mining_accident_audit_log`
--

DROP TABLE IF EXISTS `mining_accident_audit_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mining_accident_audit_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `action` enum('APPROVED','CORRECTIONS_REQUESTED','DELETED','REJECTED','RESUBMITTED','SUBMITTED') NOT NULL,
  `incident_id` bigint NOT NULL,
  `notes` varchar(1000) DEFAULT NULL,
  `performed_by_user_id` bigint NOT NULL,
  `performed_by_username` varchar(255) DEFAULT NULL,
  `timestamp` datetime(6) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mining_accident_audit_log`
--

LOCK TABLES `mining_accident_audit_log` WRITE;
/*!40000 ALTER TABLE `mining_accident_audit_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `mining_accident_audit_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `mining_accident_incidents`
--

DROP TABLE IF EXISTS `mining_accident_incidents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mining_accident_incidents` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) NOT NULL,
  `district` varchar(255) NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `occurred_at` datetime(6) NOT NULL,
  `province` varchar(255) NOT NULL,
  `reporter_id` bigint NOT NULL,
  `review_notes` varchar(1000) DEFAULT NULL,
  `severity` enum('CRITICAL','HIGH','LOW','MODERATE') NOT NULL,
  `status` enum('APPROVED','CORRECTIONS_REQUESTED','PENDING','REJECTED') NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  `ward` varchar(255) NOT NULL,
  `accident_type` enum('COLLAPSE','FALL_OF_GROUND','FLOODING','GAS_EXPLOSION') NOT NULL,
  `fatalities_count` int NOT NULL,
  `mine_name` varchar(255) NOT NULL,
  `mine_type` enum('ARTISANAL','FORMAL') NOT NULL,
  `rescue_ongoing` bit(1) NOT NULL,
  `trapped_or_injured_count` int NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `mining_accident_incidents`
--

LOCK TABLES `mining_accident_incidents` WRITE;
/*!40000 ALTER TABLE `mining_accident_incidents` DISABLE KEYS */;
/*!40000 ALTER TABLE `mining_accident_incidents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'mining_db'
--

--
-- Current Database: `report_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `report_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `report_db`;

--
-- Table structure for table `report_log`
--

DROP TABLE IF EXISTS `report_log`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `report_log` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `filters` varchar(1000) DEFAULT NULL,
  `format` varchar(10) NOT NULL,
  `generated_at` datetime(6) NOT NULL,
  `role` varchar(40) NOT NULL,
  `row_count` int NOT NULL,
  `user_id` bigint NOT NULL,
  `warnings` varchar(1000) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `report_log`
--

LOCK TABLES `report_log` WRITE;
/*!40000 ALTER TABLE `report_log` DISABLE KEYS */;
/*!40000 ALTER TABLE `report_log` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'report_db'
--

--
-- Current Database: `alert_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `alert_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `alert_db`;

--
-- Table structure for table `alert_deliveries`
--

DROP TABLE IF EXISTS `alert_deliveries`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alert_deliveries` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `attempted_at` datetime(6) NOT NULL,
  `channel` varchar(20) NOT NULL,
  `detail` varchar(1000) DEFAULT NULL,
  `recipient` varchar(255) NOT NULL,
  `status` enum('FAILED','LOGGED_ONLY','SENT','SUPPRESSED') NOT NULL,
  `alert_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKbvc283gk2auj92wl15y8s3jk2` (`alert_id`),
  CONSTRAINT `FKbvc283gk2auj92wl15y8s3jk2` FOREIGN KEY (`alert_id`) REFERENCES `alerts` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alert_deliveries`
--

LOCK TABLES `alert_deliveries` WRITE;
/*!40000 ALTER TABLE `alert_deliveries` DISABLE KEYS */;
/*!40000 ALTER TABLE `alert_deliveries` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `alerts`
--

DROP TABLE IF EXISTS `alerts`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `alerts` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `alert_reason` varchar(500) DEFAULT NULL,
  `channel` varchar(255) NOT NULL,
  `delivery_detail` varchar(1000) DEFAULT NULL,
  `delivery_status` enum('FAILED','LOGGED_ONLY','SENT','SUPPRESSED') NOT NULL,
  `district` varchar(255) DEFAULT NULL,
  `hazard` enum('DROUGHT','FIRE','FLOOD','MINING_ACCIDENT','ZOONOTIC_DISEASE') NOT NULL,
  `incident_id` bigint NOT NULL,
  `message` varchar(1000) NOT NULL,
  `occurred_at` datetime(6) DEFAULT NULL,
  `province` varchar(255) DEFAULT NULL,
  `received_at` datetime(6) NOT NULL,
  `severity` enum('CRITICAL','HIGH','LOW','MODERATE') NOT NULL,
  `summary` varchar(500) DEFAULT NULL,
  `ward` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKr3n72wwc2iqp7ypmlbimt1l46` (`hazard`,`incident_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `alerts`
--

LOCK TABLES `alerts` WRITE;
/*!40000 ALTER TABLE `alerts` DISABLE KEYS */;
/*!40000 ALTER TABLE `alerts` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Dumping routines for database 'alert_db'
--

--
-- Current Database: `dashboard_db`
--

CREATE DATABASE /*!32312 IF NOT EXISTS*/ `dashboard_db` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;

USE `dashboard_db`;

--
-- Dumping routines for database 'dashboard_db'
--
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-10-03 14:38:07
