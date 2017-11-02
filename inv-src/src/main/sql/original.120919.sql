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
  INDEX `name` () ,
  INDEX `id_idx` (`object_id` ASC) ,
  CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `inv`.`objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv`.`objects`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `inv`.`objects` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `owner_id` SMALLINT UNSIGNED NOT NULL ,
  `ark` VARCHAR(255) NOT NULL ,
  `type` ENUM('MRT-curatorial', 'MRT-system') NOT NULL ,
  `role` ENUM('MRT-class', 'MRT-content') NOT NULL ,
  `aggregate_role` ENUM('MRT-collection', 'MRT-owner', 'MRT-service-level-agreement', 'MRT-submission-agreement') NULL ,
  `node_number` SMALLINT UNSIGNED NOT NULL ,
  `version_number` SMALLINT UNSIGNED NOT NULL ,
  `who` VARCHAR(16383) NULL ,
  `what` VARCHAR(16383) NULL ,
  `when` VARCHAR(16383) NULL ,
  `where` VARCHAR(16383) NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `modified` TIMESTAMP NOT NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `ark_UNIQUE` (`ark` ASC) ,
  INDEX `created` (`created` ASC) ,
  INDEX `modified` (`modified` ASC) ,
  INDEX `id_idx` (`owner_id` ASC) ,
  CONSTRAINT `id`
    FOREIGN KEY (`owner_id` )
    REFERENCES `inv`.`owners` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv`.`versions`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `inv`.`versions` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `object_id` INT UNSIGNED NOT NULL ,
  `ark` VARCHAR(255) NOT NULL ,
  `number` SMALLINT UNSIGNED NOT NULL ,
  `note` VARCHAR(16383) NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`) ,
  INDEX `created` (`created` ASC) ,
  INDEX `id_idx` (`object_id` ASC) ,
  INDEX `ark` (`ark` ASC) ,
  CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `inv`.`objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv`.`files`
-- -----------------------------------------------------
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
  INDEX `pathname` (`pathname` ASC) ,
  INDEX `mime_type` (`mime_type` ASC) ,
  INDEX `created` (`created` ASC) ,
  INDEX `id_idx` (`version_id` ASC) ,
  INDEX `id_idx1` (`object_id` ASC) ,
  CONSTRAINT `id`
    FOREIGN KEY (`version_id` )
    REFERENCES `inv`.`versions` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `inv`.`objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv`.`collections`
-- -----------------------------------------------------
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
  INDEX `mnemonic` (`mnemonic` ASC) ,
  INDEX `id_idx` (`object_id` ASC) ,
  CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `inv`.`objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv`.`collection_object`
-- -----------------------------------------------------
CREATE  TABLE IF NOT EXISTS `inv`.`collection_object` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `collection_id` SMALLINT UNSIGNED NOT NULL ,
  `object_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  INDEX `id_idx` (`collection_id` ASC) ,
  INDEX `id_idx1` (`object_id` ASC) ,
  CONSTRAINT `id`
    FOREIGN KEY (`collection_id` )
    REFERENCES `inv`.`collections` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `inv`.`objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv`.`duas`
-- -----------------------------------------------------
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
  INDEX `id_idx` (`collection_id` ASC) ,
  PRIMARY KEY (`id`) ,
  INDEX `id_idx1` (`object_id` ASC) ,
  CONSTRAINT `id`
    FOREIGN KEY (`collection_id` )
    REFERENCES `inv`.`collections` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `inv`.`objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv`.`ingests`
-- -----------------------------------------------------
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
  INDEX `id_idx` (`object_id` ASC) ,
  INDEX `id_idx1` (`version_id` ASC) ,
  INDEX `profile` (`profile` ASC) ,
  INDEX `batch_id` (`batch_id` ASC) ,
  INDEX `user_agent` (`user_agent` ASC) ,
  INDEX `submitted` (`submitted` ASC) ,
  CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `inv`.`objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `id`
    FOREIGN KEY (`version_id` )
    REFERENCES `inv`.`versions` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv`.`dublins`
-- -----------------------------------------------------
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
  INDEX `id_idx` (`object_id` ASC) ,
  INDEX `id_idx1` (`version_id` ASC) ,
  INDEX `value` (`value` ASC) ,
  CONSTRAINT `id`
    FOREIGN KEY (`object_id` )
    REFERENCES `inv`.`objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
  CONSTRAINT `id`
    FOREIGN KEY (`version_id` )
    REFERENCES `inv`.`versions` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
