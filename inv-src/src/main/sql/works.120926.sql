SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';


-- -----------------------------------------------------
-- Table `owners`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `owners` ;

CREATE  TABLE IF NOT EXISTS `owners` (
  `id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `object_id` INT UNSIGNED NOT NULL ,
  `ark` VARCHAR(255) NOT NULL ,
  `name` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `ark_UNIQUE` (`ark` ASC) ,
  KEY `name` (`name`) )
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `objects`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `objects` ;

CREATE  TABLE IF NOT EXISTS `objects` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `owner_id` SMALLINT UNSIGNED NOT NULL ,
  `ark` VARCHAR(255) NOT NULL ,
  `type` ENUM('MRT-curatorial', 'MRT-system') NOT NULL ,
  `role` ENUM('MRT-class', 'MRT-content') NOT NULL ,
  `aggregate_role` ENUM('MRT-collection', 'MRT-owner', 'MRT-service-level-agreement', 'MRT-submission-agreement') NULL ,
  `node_number` SMALLINT UNSIGNED NOT NULL ,
  `version_number` SMALLINT UNSIGNED NOT NULL ,
  `who` VARCHAR(5394) NULL ,
  `what` VARCHAR(5394) NULL ,
  `when` VARCHAR(5394) NULL ,
  `where` VARCHAR(5394) NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `modified` TIMESTAMP NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `ark_UNIQUE` (`ark` ASC) ,
  KEY `created` (`created` ASC) ,
  KEY `modified` (`modified` ASC) ,
  KEY `id_idx` (`owner_id` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`owner_id` )
    REFERENCES `owners` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `versions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `versions` ;

CREATE  TABLE IF NOT EXISTS `versions` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `object_id` INT UNSIGNED NOT NULL ,
  `ark` VARCHAR(255) NOT NULL ,
  `number` SMALLINT UNSIGNED NOT NULL ,
  `note` VARCHAR(16383) NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`) ,
  KEY `created` (`created` ASC) ,
  KEY `id_idx` (`object_id` ASC) ,
  KEY `ark` (`ark` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `files`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `files` ;

CREATE  TABLE IF NOT EXISTS `files` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `object_id` INT UNSIGNED NOT NULL ,
  `version_id` INT UNSIGNED NOT NULL ,
  `pathname` VARCHAR(16383) NOT NULL ,
  `source` ENUM('consumer', 'producer', 'system') NOT NULL ,
  `role` ENUM('data', 'metadata') NOT NULL ,
  `full_size` BIGINT UNSIGNED NOT NULL ,
  `billable_size` BIGINT UNSIGNED NOT NULL ,
  `mime_type` VARCHAR(255) NOT NULL ,
  `digest_type` ENUM('adler-32','crc-32','md2','md5','sha-1','sha-256','shar384','sha-512') NULL ,
  `digest_value` VARCHAR(255) NULL ,
  `created` TIMESTAMP NOT NULL ,
  PRIMARY KEY (`id`) ,
  KEY `pathname` (`pathname` ASC) ,
  KEY `mime_type` (`mime_type` ASC) ,
  KEY `created` (`created` ASC) ,
  KEY `id_idx` (`version_id` ASC) ,
  KEY `id_idx1` (`object_id` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`version_id` )
    REFERENCES `versions` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `collections`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `collections` ;

CREATE  TABLE IF NOT EXISTS `collections` (
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
  KEY `id_idx` (`object_id` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `collections_objects`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `collections_objects` ;

CREATE  TABLE IF NOT EXISTS `collections_objects` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `collection_id` SMALLINT UNSIGNED NOT NULL ,
  `object_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx` (`collection_id` ASC) ,
  KEY `id_idx1` (`object_id` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`collection_id` )
    REFERENCES `collections` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `duas`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `duas` ;

CREATE  TABLE IF NOT EXISTS `duas` (
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
  KEY `id_idx1` (`object_id` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`collection_id` )
    REFERENCES `collections` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `ingests`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `ingests` ;

CREATE  TABLE IF NOT EXISTS `ingests` (
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
  KEY `submitted` (`submitted` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`version_id` )
    REFERENCES `versions` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `dublins`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `dublins` ;

CREATE  TABLE IF NOT EXISTS `dublins` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `object_id` INT UNSIGNED NOT NULL ,
  `version_id` INT UNSIGNED NOT NULL ,
  `type` ENUM('core', 'kernel') NOT NULL ,
  `seq_num` SMALLINT UNSIGNED NOT NULL ,
  `element` VARCHAR(255) NOT NULL ,
  `qualifier` VARCHAR(255) NULL ,
  `value` VARCHAR(21327) NOT NULL ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx` (`object_id` ASC) ,
  KEY `id_idx1` (`version_id` ASC) ,
  KEY `value` (`value` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`version_id` )
    REFERENCES `versions` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
