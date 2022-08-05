#See https://github.com/CDLUC3/merritt-docker/blob/main/mrt-inttest-services/mock-merritt-it/README.md
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `annita` (
  `xnum` smallint(6) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `annita_tbl` (
  `id` smallint(5) unsigned NOT NULL DEFAULT '0',
  `number` int(11) NOT NULL,
  `media_type` enum('magnetic-disk','magnetic-tape','optical-disk','solid-state','unknown') NOT NULL,
  `media_connectivity` enum('cloud','das','nas','san','unknown') NOT NULL,
  `access_mode` enum('on-line','near-line','off-line','unknown') NOT NULL,
  `access_protocol` enum('cifs','nfs','open-stack','s3','zfs','unknown') NOT NULL,
  `node_form` enum('physical','virtual') NOT NULL DEFAULT 'physical',
  `logical_volume` varchar(255) DEFAULT NULL,
  `external_provider` varchar(255) DEFAULT NULL,
  `verify_on_read` tinyint(1) NOT NULL,
  `verify_on_write` tinyint(1) NOT NULL,
  `base_url` varchar(2045) NOT NULL,
  `created` timestamp NOT NULL DEFAULT '1970-01-01 08:00:00',
  `source_node` smallint(5) unsigned DEFAULT NULL,
  `target_node` smallint(5) unsigned DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_audits` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `inv_node_id` smallint(5) unsigned NOT NULL,
  `inv_object_id` int(10) unsigned NOT NULL,
  `inv_version_id` int(10) unsigned NOT NULL,
  `inv_file_id` int(10) unsigned NOT NULL,
  `url` varchar(16383) DEFAULT NULL,
  `status` enum('verified','unverified','size-mismatch','digest-mismatch','system-unavailable','processing','unknown') NOT NULL DEFAULT 'unknown',
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `verified` timestamp NULL DEFAULT NULL,
  `modified` timestamp NULL DEFAULT NULL,
  `failed_size` bigint(20) unsigned NOT NULL DEFAULT '0',
  `failed_digest_value` varchar(255) DEFAULT NULL,
  `note` text,
  PRIMARY KEY (`id`),
  UNIQUE KEY `inv_node_id` (`inv_node_id`,`inv_file_id`),
  KEY `id_idx` (`inv_node_id`),
  KEY `id_idx1` (`inv_object_id`),
  KEY `id_idx2` (`inv_version_id`),
  KEY `id_idx3` (`inv_file_id`),
  KEY `verified` (`verified`),
  KEY `status` (`status`),
  CONSTRAINT `inv_audits_ibfk_1` FOREIGN KEY (`inv_node_id`) REFERENCES `inv_nodes` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `inv_audits_ibfk_2` FOREIGN KEY (`inv_object_id`) REFERENCES `inv_objects` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `inv_audits_ibfk_3` FOREIGN KEY (`inv_version_id`) REFERENCES `inv_versions` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `inv_audits_ibfk_4` FOREIGN KEY (`inv_file_id`) REFERENCES `inv_files` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `inv_audits` VALUES (1,1,1,1,1,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F2222/1/producer%2Fmrt-dc.xml?fixity=no','unknown','2022-08-05 00:26:42',NULL,NULL,0,NULL,NULL),(2,1,1,1,2,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F2222/1/system%2Fmrt-owner.txt?fixity=no','unknown','2022-08-05 00:26:42',NULL,NULL,0,NULL,NULL),(3,1,1,1,3,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F2222/1/system%2Fmrt-erc.txt?fixity=no','unknown','2022-08-05 00:26:42',NULL,NULL,0,NULL,NULL),(4,1,1,1,4,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F2222/1/system%2Fmrt-mom.txt?fixity=no','unknown','2022-08-05 00:26:42',NULL,NULL,0,NULL,NULL),(5,1,1,1,5,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F2222/1/system%2Fmrt-ingest.txt?fixity=no','unknown','2022-08-05 00:26:42',NULL,NULL,0,NULL,NULL),(6,1,1,1,6,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F2222/1/system%2Fmrt-membership.txt?fixity=no','unknown','2022-08-05 00:26:42',NULL,NULL,0,NULL,NULL),(7,1,1,1,7,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F2222/1/producer%2Fhello.txt?fixity=no','unknown','2022-08-05 00:26:42',NULL,NULL,0,NULL,NULL),(8,1,2,2,8,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F3333/1/producer%2Fmrt-dc.xml?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL),(9,1,2,2,9,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F3333/1/system%2Fmrt-owner.txt?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL),(10,1,2,2,10,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F3333/1/system%2Fmrt-erc.txt?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL),(11,1,2,2,11,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F3333/1/system%2Fmrt-mom.txt?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL),(12,1,2,2,12,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F3333/1/system%2Fmrt-ingest.txt?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL),(13,1,2,2,13,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F3333/1/system%2Fmrt-membership.txt?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL),(14,1,2,2,14,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F3333/1/producer%2Fhello.txt?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL),(15,1,3,3,15,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F4444/1/producer%2Fmrt-dc.xml?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL),(16,1,3,3,16,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F4444/1/system%2Fmrt-owner.txt?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL),(17,1,3,3,17,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F4444/1/system%2Fmrt-erc.txt?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL),(18,1,3,3,18,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F4444/1/system%2Fmrt-mom.txt?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL),(19,1,3,3,19,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F4444/1/system%2Fmrt-ingest.txt?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL),(20,1,3,3,20,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F4444/1/system%2Fmrt-membership.txt?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL),(21,1,3,3,21,'http://mock-merritt-it:4567/storage/content/7777/ark%3A%2F1111%2F4444/1/producer%2Fhello.txt?fixity=no','unknown','2022-08-05 00:26:43',NULL,NULL,0,NULL,NULL);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_collections` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `inv_object_id` int(10) unsigned DEFAULT NULL,
  `ark` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `mnemonic` varchar(255) DEFAULT NULL,
  `read_privilege` enum('public','restricted') DEFAULT NULL,
  `write_privilege` enum('public','restricted') DEFAULT NULL,
  `download_privilege` enum('public','restricted') DEFAULT NULL,
  `storage_tier` enum('standard','premium') DEFAULT NULL,
  `harvest_privilege` enum('public','none') NOT NULL DEFAULT 'none',
  PRIMARY KEY (`id`),
  UNIQUE KEY `ark_UNIQUE` (`ark`),
  KEY `id_idx` (`inv_object_id`),
  KEY `id_hp` (`harvest_privilege`),
  CONSTRAINT `inv_collections_ibfk_1` FOREIGN KEY (`inv_object_id`) REFERENCES `inv_objects` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `inv_collections` VALUES (1,NULL,'ark:/99999/collection',NULL,NULL,NULL,NULL,NULL,NULL,'none');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_collections_inv_nodes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `inv_collection_id` smallint(5) unsigned NOT NULL,
  `inv_node_id` smallint(5) unsigned NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `idx__cn_dedup` (`inv_collection_id`,`inv_node_id`),
  KEY `id_idx` (`inv_collection_id`),
  KEY `id_idx1` (`inv_node_id`),
  CONSTRAINT `inv_collections_inv_nodes_ibfk_1` FOREIGN KEY (`inv_collection_id`) REFERENCES `inv_collections` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `inv_collections_inv_nodes_ibfk_2` FOREIGN KEY (`inv_node_id`) REFERENCES `inv_nodes` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_collections_inv_objects` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `inv_collection_id` smallint(5) unsigned NOT NULL,
  `inv_object_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_idx` (`inv_collection_id`),
  KEY `id_idx1` (`inv_object_id`),
  KEY `inv_object_id` (`inv_object_id`,`inv_collection_id`),
  CONSTRAINT `inv_collections_inv_objects_ibfk_1` FOREIGN KEY (`inv_collection_id`) REFERENCES `inv_collections` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `inv_collections_inv_objects_ibfk_2` FOREIGN KEY (`inv_object_id`) REFERENCES `inv_objects` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `inv_collections_inv_objects` VALUES (1,1,1),(2,1,2),(3,1,3);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_duas` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `inv_collection_id` smallint(5) unsigned DEFAULT NULL,
  `inv_object_id` int(10) unsigned NOT NULL,
  `identifier` varchar(255) DEFAULT NULL,
  `title` varchar(255) NOT NULL,
  `terms` varchar(16383) NOT NULL,
  `template` text,
  `accept_obligation` enum('required','optional','none') NOT NULL,
  `name_obligation` enum('required','optional','none') NOT NULL,
  `affiliation_obligation` enum('required','optional','none') NOT NULL,
  `email_obligation` enum('required','optional','none') NOT NULL,
  `applicability` enum('collection','object','version','file') NOT NULL,
  `persistence` enum('request','session','permanent') NOT NULL,
  `notification` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_idx` (`inv_collection_id`),
  KEY `id_idx1` (`inv_object_id`),
  KEY `identifier` (`identifier`),
  CONSTRAINT `inv_duas_ibfk_1` FOREIGN KEY (`inv_collection_id`) REFERENCES `inv_collections` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `inv_duas_ibfk_2` FOREIGN KEY (`inv_object_id`) REFERENCES `inv_objects` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_dublinkernels` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `inv_object_id` int(10) unsigned NOT NULL,
  `inv_version_id` int(10) unsigned NOT NULL,
  `seq_num` smallint(5) unsigned NOT NULL,
  `element` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `qualifier` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `value` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  KEY `id_idx` (`inv_object_id`),
  KEY `id_idx1` (`inv_version_id`),
  CONSTRAINT `inv_dublinkernels_ibfk_1` FOREIGN KEY (`inv_object_id`) REFERENCES `inv_objects` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `inv_dublinkernels_ibfk_2` FOREIGN KEY (`inv_version_id`) REFERENCES `inv_versions` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `inv_dublinkernels` VALUES (1,1,1,1,'who',NULL,'Merritt Team'),(2,1,1,2,'what',NULL,'Hello File'),(3,1,1,3,'when',NULL,'2022'),(4,1,1,4,'where','primary','ark:/7777/7777'),(5,1,1,5,'where','local','my-local-id'),(6,2,2,1,'who',NULL,'Merritt Team'),(7,2,2,2,'what',NULL,'Hello File'),(8,2,2,3,'when',NULL,'2022'),(9,2,2,4,'where','primary','ark:/7777/7777'),(10,2,2,5,'where','local','my-local-id'),(11,3,3,1,'who',NULL,'Merritt Team'),(12,3,3,2,'what',NULL,'Hello File'),(13,3,3,3,'when',NULL,'2022'),(14,3,3,4,'where','primary','ark:/7777/7777'),(15,3,3,5,'where','local','my-local-id');
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER insert_fulltext
AFTER INSERT ON inv_dublinkernels
FOR EACH ROW
BEGIN
  IF NOT NEW.value='(:unas)' THEN
    INSERT INTO sha_dublinkernels
    VALUES (NEW.id, NEW.value);
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER update_fulltext
AFTER UPDATE ON inv_dublinkernels
FOR EACH ROW
BEGIN
  IF NEW.value!='(:unas)' THEN
    UPDATE sha_dublinkernels
    SET value = NEW.value
    WHERE id = NEW.id;
  END IF;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = 'ONLY_FULL_GROUP_BY,STRICT_TRANS_TABLES,NO_ZERO_IN_DATE,NO_ZERO_DATE,ERROR_FOR_DIVISION_BY_ZERO,NO_ENGINE_SUBSTITUTION' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER delete_fulltext
AFTER DELETE ON inv_dublinkernels
FOR EACH ROW
BEGIN
  DELETE FROM sha_dublinkernels
  WHERE id = OLD.id;
END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_embargoes` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `inv_object_id` int(10) unsigned NOT NULL,
  `embargo_end_date` datetime NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `inv_object_id_UNIQUE` (`inv_object_id`),
  KEY `embargo_end_date` (`embargo_end_date`),
  CONSTRAINT `inv_embargoes_ibfk_1` FOREIGN KEY (`inv_object_id`) REFERENCES `inv_objects` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_files` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `inv_object_id` int(10) unsigned NOT NULL,
  `inv_version_id` int(10) unsigned NOT NULL,
  `pathname` longtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `source` enum('consumer','producer','system') NOT NULL,
  `role` enum('data','metadata') NOT NULL,
  `full_size` bigint(20) unsigned NOT NULL DEFAULT '0',
  `billable_size` bigint(20) unsigned NOT NULL DEFAULT '0',
  `mime_type` varchar(255) DEFAULT NULL,
  `digest_type` enum('adler-32','crc-32','md2','md5','sha-1','sha-256','sha-384','sha-512') DEFAULT NULL,
  `digest_value` varchar(255) DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `mime_type` (`mime_type`),
  KEY `created` (`created`),
  KEY `id_idx` (`inv_version_id`),
  KEY `id_idx1` (`inv_object_id`),
  KEY `source` (`source`),
  KEY `role` (`role`),
  CONSTRAINT `inv_files_ibfk_1` FOREIGN KEY (`inv_version_id`) REFERENCES `inv_versions` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `inv_files_ibfk_2` FOREIGN KEY (`inv_object_id`) REFERENCES `inv_objects` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=22 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `inv_files` VALUES (1,1,1,'producer/mrt-dc.xml','producer','data',525,525,'application/xml','sha-256','35dd45b157fc7352ebf2954c4658baa89073cba762fd40b7f9f16bc54a9a2af8','2022-08-05 00:26:42'),(2,1,1,'system/mrt-owner.txt','system','data',16,16,'text/plain','sha-256','1a00b96724193d940c3823c981bd95dce7b5785d468d79971fc188555c35e4f0','2022-08-05 00:26:42'),(3,1,1,'system/mrt-erc.txt','system','metadata',93,93,'text/plain','sha-256','a5adc23d32740161d2b100069975d3262c79b5c5330edea81c1c09408aaaec49','2022-08-05 00:26:42'),(4,1,1,'system/mrt-mom.txt','system','data',104,104,'text/plain','sha-256','b15277ee61ed9a25e753de3c0c71a8ac01066f91322cb16ef07c3690a9f570d3','2022-08-05 00:26:42'),(5,1,1,'system/mrt-ingest.txt','system','data',1568,1568,'text/plain','sha-256','8beef10c35d4cfacee042c98fb1587c55e0052f6150ec1e7589d17685836cdb9','2022-08-05 00:26:42'),(6,1,1,'system/mrt-membership.txt','system','data',22,22,'text/plain','sha-256','be4528f2066fb75a898710cc7bc321dfea67c02a7c5887cc95b0a18800fd4e53','2022-08-05 00:26:42'),(7,1,1,'producer/hello.txt','producer','data',5,5,'text/plain','sha-256','2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824','2022-08-05 00:26:42'),(8,2,2,'producer/mrt-dc.xml','producer','data',525,525,'application/xml','sha-256','35dd45b157fc7352ebf2954c4658baa89073cba762fd40b7f9f16bc54a9a2af8','2022-08-05 00:26:43'),(9,2,2,'system/mrt-owner.txt','system','data',16,16,'text/plain','sha-256','1a00b96724193d940c3823c981bd95dce7b5785d468d79971fc188555c35e4f0','2022-08-05 00:26:43'),(10,2,2,'system/mrt-erc.txt','system','metadata',93,93,'text/plain','sha-256','a5adc23d32740161d2b100069975d3262c79b5c5330edea81c1c09408aaaec49','2022-08-05 00:26:43'),(11,2,2,'system/mrt-mom.txt','system','data',104,104,'text/plain','sha-256','b15277ee61ed9a25e753de3c0c71a8ac01066f91322cb16ef07c3690a9f570d3','2022-08-05 00:26:43'),(12,2,2,'system/mrt-ingest.txt','system','data',1568,1568,'text/plain','sha-256','8beef10c35d4cfacee042c98fb1587c55e0052f6150ec1e7589d17685836cdb9','2022-08-05 00:26:43'),(13,2,2,'system/mrt-membership.txt','system','data',22,22,'text/plain','sha-256','be4528f2066fb75a898710cc7bc321dfea67c02a7c5887cc95b0a18800fd4e53','2022-08-05 00:26:43'),(14,2,2,'producer/hello.txt','producer','data',5,5,'text/plain','sha-256','2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824','2022-08-05 00:26:43'),(15,3,3,'producer/mrt-dc.xml','producer','data',525,525,'application/xml','sha-256','35dd45b157fc7352ebf2954c4658baa89073cba762fd40b7f9f16bc54a9a2af8','2022-08-05 00:26:43'),(16,3,3,'system/mrt-owner.txt','system','data',16,16,'text/plain','sha-256','1a00b96724193d940c3823c981bd95dce7b5785d468d79971fc188555c35e4f0','2022-08-05 00:26:43'),(17,3,3,'system/mrt-erc.txt','system','metadata',93,93,'text/plain','sha-256','a5adc23d32740161d2b100069975d3262c79b5c5330edea81c1c09408aaaec49','2022-08-05 00:26:43'),(18,3,3,'system/mrt-mom.txt','system','data',104,104,'text/plain','sha-256','b15277ee61ed9a25e753de3c0c71a8ac01066f91322cb16ef07c3690a9f570d3','2022-08-05 00:26:43'),(19,3,3,'system/mrt-ingest.txt','system','data',1568,1568,'text/plain','sha-256','8beef10c35d4cfacee042c98fb1587c55e0052f6150ec1e7589d17685836cdb9','2022-08-05 00:26:43'),(20,3,3,'system/mrt-membership.txt','system','data',22,22,'text/plain','sha-256','be4528f2066fb75a898710cc7bc321dfea67c02a7c5887cc95b0a18800fd4e53','2022-08-05 00:26:43'),(21,3,3,'producer/hello.txt','producer','data',5,5,'text/plain','sha-256','2cf24dba5fb0a30e26e83b2ac5b9e29e1b161e5c1fa7425e73043362938b9824','2022-08-05 00:26:43');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_ingests` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `inv_object_id` int(10) unsigned NOT NULL,
  `inv_version_id` int(10) unsigned NOT NULL,
  `filename` varchar(255) NOT NULL,
  `ingest_type` enum('file','container','object-manifest','single-file-batch-manifest','container-batch-manifest','batch-manifest') NOT NULL,
  `profile` varchar(255) NOT NULL,
  `batch_id` varchar(255) NOT NULL,
  `job_id` varchar(255) NOT NULL,
  `user_agent` varchar(255) DEFAULT NULL,
  `submitted` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `storage_url` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `id_idx` (`inv_object_id`),
  KEY `id_idx1` (`inv_version_id`),
  KEY `profile` (`profile`),
  KEY `batch_id` (`batch_id`),
  KEY `user_agent` (`user_agent`),
  KEY `submitted` (`submitted`),
  CONSTRAINT `inv_ingests_ibfk_1` FOREIGN KEY (`inv_object_id`) REFERENCES `inv_objects` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `inv_ingests_ibfk_2` FOREIGN KEY (`inv_version_id`) REFERENCES `inv_versions` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `inv_ingests` VALUES (1,1,1,'Hello File','file','merritt_content','bid-00000000-0000-0000-0000-000000000000','jid-00000000-0000-0000-0000-000000000000','integration test user','2022-05-07 05:23:42','http://mock-merritt-it:4567/storage/manifest/7777/ark%3A%2F1111%2F2222'),(2,2,2,'Hello File','file','merritt_content','bid-00000000-0000-0000-0000-000000000000','jid-00000000-0000-0000-0000-000000000000','integration test user','2022-05-07 05:23:42','http://mock-merritt-it:4567/storage/manifest/7777/ark%3A%2F1111%2F3333'),(3,3,3,'Hello File','file','merritt_content','bid-00000000-0000-0000-0000-000000000000','jid-00000000-0000-0000-0000-000000000000','integration test user','2022-05-07 05:23:42','http://mock-merritt-it:4567/storage/manifest/7777/ark%3A%2F1111%2F4444');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_localids` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `inv_object_ark` varchar(255) NOT NULL,
  `inv_owner_ark` varchar(255) NOT NULL,
  `local_id` varchar(255) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `loc_unique` (`inv_owner_ark`,`local_id`),
  KEY `id_idoba` (`inv_object_ark`),
  KEY `id_idowa` (`inv_owner_ark`),
  KEY `id_idloc` (`local_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_metadatas` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `inv_object_id` int(10) unsigned NOT NULL,
  `inv_version_id` int(10) unsigned NOT NULL,
  `filename` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `md_schema` enum('DataCite','DublinCore','CSDGM','EML','OAI_DublinCore','StashWrapper') COLLATE utf8mb4_unicode_ci NOT NULL,
  `version` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `serialization` enum('anvl','json','xml') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `value` mediumtext COLLATE utf8mb4_unicode_ci,
  PRIMARY KEY (`id`),
  KEY `id_idx` (`inv_object_id`),
  KEY `id_idx1` (`inv_version_id`),
  KEY `id_metax` (`version`(191)),
  CONSTRAINT `inv_metadatas_ibfk_1` FOREIGN KEY (`inv_object_id`) REFERENCES `inv_objects` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `inv_metadatas_ibfk_2` FOREIGN KEY (`inv_version_id`) REFERENCES `inv_versions` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `inv_metadatas` VALUES (1,1,1,'producer/mrt-dc.xml','DublinCore',NULL,'xml','<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<DublinCore xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n        <dc:title>Hello File</dc:title>\n        <dc:creator>Merritt Team</dc:creator>\n        <dc:type>text</dc:type>\n        <dc:publisher>CDL</dc:publisher>\n        <dc:date>2022</dc:date>\n        <dc:language>eng</dc:language>\n        <dc:description>File used for Merritt Integration Tests.</dc:description>\n        <dc:subject>integration test</dc:subject>\n</DublinCore>'),(2,2,2,'producer/mrt-dc.xml','DublinCore',NULL,'xml','<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<DublinCore xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n        <dc:title>Hello File</dc:title>\n        <dc:creator>Merritt Team</dc:creator>\n        <dc:type>text</dc:type>\n        <dc:publisher>CDL</dc:publisher>\n        <dc:date>2022</dc:date>\n        <dc:language>eng</dc:language>\n        <dc:description>File used for Merritt Integration Tests.</dc:description>\n        <dc:subject>integration test</dc:subject>\n</DublinCore>'),(3,3,3,'producer/mrt-dc.xml','DublinCore',NULL,'xml','<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<DublinCore xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n        <dc:title>Hello File</dc:title>\n        <dc:creator>Merritt Team</dc:creator>\n        <dc:type>text</dc:type>\n        <dc:publisher>CDL</dc:publisher>\n        <dc:date>2022</dc:date>\n        <dc:language>eng</dc:language>\n        <dc:description>File used for Merritt Integration Tests.</dc:description>\n        <dc:subject>integration test</dc:subject>\n</DublinCore>');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_nodes` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `number` int(11) NOT NULL,
  `media_type` enum('magnetic-disk','magnetic-tape','optical-disk','solid-state','unknown') NOT NULL,
  `media_connectivity` enum('cloud','das','nas','san','unknown') NOT NULL,
  `access_mode` enum('on-line','near-line','off-line','unknown') NOT NULL,
  `access_protocol` enum('cifs','nfs','open-stack','s3','zfs','unknown') NOT NULL,
  `node_form` enum('physical','virtual') NOT NULL DEFAULT 'physical',
  `node_protocol` enum('file','http') NOT NULL DEFAULT 'file',
  `logical_volume` varchar(255) DEFAULT NULL,
  `external_provider` varchar(255) DEFAULT NULL,
  `verify_on_read` tinyint(1) NOT NULL,
  `verify_on_write` tinyint(1) NOT NULL,
  `base_url` varchar(2045) NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `description` varchar(255) DEFAULT NULL,
  `source_node` smallint(5) unsigned DEFAULT NULL,
  `target_node` smallint(5) unsigned DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `inv_nodes` VALUES (1,7777,'magnetic-disk','cloud','on-line','s3','physical','http','yaml:|7777','nodeio',1,1,'http://mock-merritt-it:4567/store','2022-08-05 00:26:32',NULL,NULL,NULL),(2,8888,'magnetic-disk','cloud','on-line','s3','physical','http','yaml:|8888','nodeio',1,1,'http://mock-merritt-it:4567/store','2022-08-05 00:26:32',NULL,NULL,NULL);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_nodes_inv_objects` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `inv_node_id` smallint(5) unsigned NOT NULL,
  `inv_object_id` int(10) unsigned NOT NULL,
  `role` enum('primary','secondary') NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `replicated` timestamp NULL DEFAULT NULL,
  `version_number` smallint(5) unsigned DEFAULT NULL,
  `replic_start` timestamp NULL DEFAULT NULL,
  `replic_size` bigint(20) unsigned DEFAULT NULL,
  `completion_status` enum('ok','fail','partial','unknown') DEFAULT NULL,
  `note` mediumtext,
  PRIMARY KEY (`id`),
  UNIQUE KEY `inv_object_id` (`inv_object_id`,`inv_node_id`),
  KEY `id_idx` (`inv_node_id`),
  KEY `id_idx1` (`inv_object_id`),
  KEY `id_idx2` (`replicated`),
  KEY `irep_start` (`replic_start`) USING BTREE,
  KEY `irep_size` (`replic_size`) USING BTREE,
  KEY `irep_status` (`completion_status`) USING BTREE,
  CONSTRAINT `inv_nodes_inv_objects_ibfk_1` FOREIGN KEY (`inv_node_id`) REFERENCES `inv_nodes` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION,
  CONSTRAINT `inv_nodes_inv_objects_ibfk_2` FOREIGN KEY (`inv_object_id`) REFERENCES `inv_objects` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8;
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `inv_nodes_inv_objects` VALUES (1,1,1,'primary','2022-08-05 00:26:42',NULL,NULL,NULL,NULL,NULL,NULL),(2,1,2,'primary','2022-08-05 00:26:43',NULL,NULL,NULL,NULL,NULL,NULL),(3,1,3,'primary','2022-08-05 00:26:43',NULL,NULL,NULL,NULL,NULL,NULL);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_objects` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `inv_owner_id` smallint(5) unsigned NOT NULL,
  `ark` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `md5_3` char(3) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `object_type` enum('MRT-curatorial','MRT-system') COLLATE utf8mb4_unicode_ci NOT NULL,
  `role` enum('MRT-class','MRT-content') COLLATE utf8mb4_unicode_ci NOT NULL,
  `aggregate_role` enum('MRT-collection','MRT-owner','MRT-service-level-agreement','MRT-submission-agreement','MRT-none') COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `version_number` smallint(5) unsigned NOT NULL,
  `erc_who` mediumtext COLLATE utf8mb4_unicode_ci,
  `erc_what` mediumtext COLLATE utf8mb4_unicode_ci,
  `erc_when` mediumtext COLLATE utf8mb4_unicode_ci,
  `erc_where` mediumtext COLLATE utf8mb4_unicode_ci,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `modified` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ark_UNIQUE` (`ark`(190)),
  KEY `created` (`created`),
  KEY `modified` (`modified`),
  KEY `id_idx` (`inv_owner_id`),
  CONSTRAINT `inv_objects_ibfk_1` FOREIGN KEY (`inv_owner_id`) REFERENCES `inv_owners` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `inv_objects` VALUES (1,1,'ark:/1111/2222','d28','MRT-curatorial','MRT-content','MRT-none',1,'Merritt Team','Hello File','2022','ark:/7777/7777 ; my-local-id','2022-08-05 00:26:42','2022-08-05 07:26:42'),(2,1,'ark:/1111/3333','10c','MRT-curatorial','MRT-content','MRT-none',1,'Merritt Team','Hello File','2022','ark:/7777/7777 ; my-local-id','2022-08-05 00:26:43','2022-08-05 07:26:43'),(3,1,'ark:/1111/4444','3f5','MRT-curatorial','MRT-content','MRT-none',1,'Merritt Team','Hello File','2022','ark:/7777/7777 ; my-local-id','2022-08-05 00:26:43','2022-08-05 07:26:43');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_owners` (
  `id` smallint(5) unsigned NOT NULL AUTO_INCREMENT,
  `inv_object_id` int(10) unsigned DEFAULT NULL,
  `ark` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `ark_UNIQUE` (`ark`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `inv_owners` VALUES (1,NULL,'ark:/99999/owner',NULL);
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_storage_maints` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `inv_storage_scan_id` int(10) NOT NULL,
  `inv_node_id` smallint(5) unsigned NOT NULL,
  `keymd5` char(32) CHARACTER SET utf8 NOT NULL,
  `size` bigint(20) unsigned NOT NULL DEFAULT '0',
  `file_created` timestamp NULL DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `file_removed` timestamp NULL DEFAULT NULL,
  `maint_status` enum('review','hold','delete','removed','note','error','unknown') CHARACTER SET utf8 NOT NULL DEFAULT 'unknown',
  `maint_type` enum('non-ark','missing-ark','orphan-copy','missing-file','unknown') CHARACTER SET utf8 NOT NULL DEFAULT 'unknown',
  `s3key` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `note` mediumtext CHARACTER SET utf8,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE KEY `keymd5_idx` (`inv_node_id`,`keymd5`) USING BTREE,
  KEY `type_idx` (`maint_type`) USING BTREE,
  KEY `status_idx` (`maint_status`) USING BTREE,
  CONSTRAINT `inv_scans_ibfk_2` FOREIGN KEY (`inv_node_id`) REFERENCES `inv_nodes` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_storage_scans` (
  `id` int(10) NOT NULL AUTO_INCREMENT,
  `inv_node_id` smallint(5) unsigned NOT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `scan_status` enum('pending','started','completed','cancelled','failed','unknown') NOT NULL DEFAULT 'unknown',
  `scan_type` enum('list','next','delete','build','unknown') NOT NULL DEFAULT 'unknown',
  `keys_processed` bigint(20) NOT NULL DEFAULT '0',
  `key_list_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `last_s3_key` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  KEY `scan_type_idx` (`scan_type`) USING BTREE,
  KEY `scan_status_idx` (`scan_status`) USING BTREE,
  KEY `inv_scans_node_id_ibfk_3` (`inv_node_id`) USING BTREE,
  CONSTRAINT `inv_scans_node_id_ibfk_3` FOREIGN KEY (`inv_node_id`) REFERENCES `inv_nodes` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `inv_versions` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `inv_object_id` int(10) unsigned NOT NULL,
  `ark` varchar(255) NOT NULL,
  `number` smallint(5) unsigned NOT NULL,
  `note` varchar(16383) DEFAULT NULL,
  `created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `created` (`created`),
  KEY `id_idx` (`inv_object_id`),
  KEY `ark` (`ark`),
  CONSTRAINT `inv_versions_ibfk_1` FOREIGN KEY (`inv_object_id`) REFERENCES `inv_objects` (`id`) ON DELETE NO ACTION ON UPDATE NO ACTION
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `inv_versions` VALUES (1,1,'ark:/1111/2222',1,NULL,'2022-08-05 00:26:42'),(2,2,'ark:/1111/3333',1,NULL,'2022-08-05 00:26:43'),(3,3,'ark:/1111/4444',1,NULL,'2022-08-05 00:26:43');
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `schema_migrations` (
  `version` varchar(255) NOT NULL,
  UNIQUE KEY `unique_schema_migrations` (`version`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `sha_dublinkernels` (
  `id` int(10) unsigned NOT NULL,
  `value` mediumtext COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`),
  FULLTEXT KEY `value` (`value`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci ROW_FORMAT=DYNAMIC;
/*!40101 SET character_set_client = @saved_cs_client */;
INSERT INTO `sha_dublinkernels` VALUES (1,'Merritt Team'),(2,'Hello File'),(3,'2022'),(4,'ark:/7777/7777'),(5,'my-local-id'),(6,'Merritt Team'),(7,'Hello File'),(8,'2022'),(9,'ark:/7777/7777'),(10,'my-local-id'),(11,'Merritt Team'),(12,'Hello File'),(13,'2022'),(14,'ark:/7777/7777'),(15,'my-local-id');
