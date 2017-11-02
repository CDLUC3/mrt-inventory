SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

CREATE SCHEMA IF NOT EXISTS `inv` DEFAULT CHARACTER SET utf8 COLLATE utf8_unicode_ci ;
USE `inv` ;

-- -----------------------------------------------------
-- Table `inv`.`owners`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv`.`owners`;
CREATE  TABLE IF NOT EXISTS `inv`.`owners` (
  `id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `object_id` INT UNSIGNED NOT NULL ,
  `ark` VARCHAR(255) NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `ark_UNIQUE` (`ark` ASC) ,
  KEY `name` (`name`) ,
  KEY `id_idx` (`object_id` ASC)

) ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;



-- -----------------------------------------------------
-- Table `inv`.`objects`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv`.`objects`;
CREATE  TABLE IF NOT EXISTS `inv`.`objects` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `owner_id` SMALLINT UNSIGNED NOT NULL ,
  `ark` VARCHAR(255) NOT NULL ,
  `type` ENUM('MRT-curatorial', 'MRT-system') NOT NULL ,
  `role` ENUM('MRT-class', 'MRT-content') NOT NULL ,
  `aggregate_role` ENUM('MRT-collection', 'MRT-owner', 'MRT-service-level-agreement', 'MRT-submission-agreement') NULL ,
  `node_number` SMALLINT UNSIGNED NOT NULL ,
  `version_number` SMALLINT UNSIGNED NOT NULL ,
  `who` VARCHAR(2047) NULL ,
  `what` VARCHAR(2047) NULL ,
  `when` VARCHAR(2047) NULL ,
  `where` VARCHAR(2047) NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `modified` TIMESTAMP NULL,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `ark_UNIQUE` (`ark` ASC) ,
  KEY `created` (`created` ASC) ,
  KEY `modified` (`modified` ASC) ,
  KEY `id_idx` (`owner_id` ASC))
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;





-- -----------------------------------------------------
-- Table `inv`.`files`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv`.`files`;
CREATE  TABLE IF NOT EXISTS `inv`.`files` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `object_id` INT UNSIGNED NOT NULL ,
  `version_id` INT UNSIGNED NOT NULL ,
  `pathname` VARCHAR(16383) NOT NULL ,
  `source` ENUM('consumer', 'producer', 'system') NOT NULL ,
  `role` ENUM('data', 'metadata') NOT NULL ,
  `full_size` BIGINT UNSIGNED NOT NULL ,
  `billable_size` BIGINT UNSIGNED NOT NULL ,
  `mime_type` VARCHAR(255) NOT NULL ,
  `created` TIMESTAMP NOT NULL ,
  PRIMARY KEY (`id`) ,
  KEY `pathname` (`pathname` ASC) ,
  KEY `mime_type` (`mime_type` ASC) ,
  KEY `created` (`created` ASC) ,
  KEY `id_idx` (`version_id` ASC) ,
  KEY `id_idx1` (`object_id` ASC) )
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;









-- -----------------------------------------------------
-- Table `inv`.`collections`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv`.`collections`;
CREATE  TABLE IF NOT EXISTS `inv`.`collections` (
  `id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `object_id` INT UNSIGNED NOT NULL ,
  `ark` VARCHAR(255) NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  `mnemonic` VARCHAR(255) NOT NULL ,
  `read_privilege` ENUM('public', 'restricted') NOT NULL ,
  `write_privilege` ENUM('public', 'restricted') NOT NULL ,
  `download_privilege` ENUM('public', 'restricted') NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `ark_UNIQUE` (`ark` ASC) ,
  KEY `mnemonic` (`mnemonic` ASC) ,
  KEY `id_idx` (`object_id` ASC))
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv`.`collection_object`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv`.`collection_object`;
CREATE  TABLE IF NOT EXISTS `inv`.`collection_object` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `collection_id` SMALLINT UNSIGNED NOT NULL ,
  `object_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx` (`collection_id` ASC) ,
  KEY `id_idx1` (`object_id` ASC))
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv`.`duas`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv`.`duas`;
CREATE  TABLE IF NOT EXISTS `inv`.`duas` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `collection_id` SMALLINT UNSIGNED NULL ,
  `object_id` INT UNSIGNED NULL ,
  `title` VARCHAR(255) NOT NULL ,
  `terms` VARCHAR(16383) NOT NULL ,
  `accept_obligation` ENUM('required', 'optional', 'none') NOT NULL ,
  `name_obligation` ENUM('required', 'optional', 'none') NOT NULL ,
  `affiliation_obligation` ENUM('required', 'optional', 'none') NOT NULL ,
  `email_obligation` ENUM('required', 'optional', 'none') NOT NULL ,
  `applicability` ENUM('collection', 'object', 'version', 'file') NOT NULL ,
  `persistence` ENUM('request', 'session', 'permanent') NOT NULL ,
  `notification` VARCHAR(255) NOT NULL ,
  KEY `id_idx` (`collection_id` ASC) ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx1` (`object_id` ASC))
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv`.`ingests`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv`.`ingests`;
CREATE  TABLE IF NOT EXISTS `inv`.`ingests` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `object_id` INT UNSIGNED NOT NULL ,
  `version_id` INT UNSIGNED NOT NULL ,
  `filename` VARCHAR(255) NOT NULL ,
  `type` ENUM('file', 'container', 'object-manifest', 'single-file-batch-manifest', 'container-batch-manifest', 'batch-manifest') NOT NULL ,
  `profile` VARCHAR(255) NOT NULL ,
  `batch_id` VARCHAR(255) NOT NULL ,
  `job_id` VARCHAR(255) NOT NULL ,
  `user_agent` VARCHAR(255) NOT NULL ,
  `submitted` TIMESTAMP NOT NULL ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx` (`object_id` ASC) ,
  KEY `id_idx1` (`version_id` ASC) ,
  KEY `profile` (`profile` ASC) ,
  KEY `batch_id` (`batch_id` ASC) ,
  KEY `user_agent` (`user_agent` ASC) ,
  KEY `submitted` (`submitted` ASC))
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv`.`dublins`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv`.`dublins`;
CREATE  TABLE IF NOT EXISTS `inv`.`dublins` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `object_id` INT UNSIGNED NOT NULL ,
  `version_id` INT UNSIGNED NOT NULL ,
  `type` ENUM('core', 'kernel') NOT NULL ,
  `seq_num` SMALLINT UNSIGNED NOT NULL ,
  `element` VARCHAR(255) NOT NULL ,
  `qualifier` VARCHAR(255) NULL ,
  `value` VARCHAR(16383) NOT NULL ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx` (`object_id` ASC) ,
  KEY `id_idx1` (`version_id` ASC) ,
  KEY `value` (`value` ASC))
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
