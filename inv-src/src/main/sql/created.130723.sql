SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- Modify output of MySQL Workbench changing "INDEX" to "KEY" and
-- commenting out "CONSTRAINT"

-- -----------------------------------------------------
-- Table `inv_owners`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv_owners` ;

CREATE  TABLE IF NOT EXISTS `inv_owners` (
  `id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `inv_object_id` INT UNSIGNED NULL ,
  `ark` VARCHAR(255) NOT NULL ,
  `name` VARCHAR(255) NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `ark_UNIQUE` (`ark` ASC) )
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv_objects`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv_objects` ;

CREATE  TABLE IF NOT EXISTS `inv_objects` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `inv_owner_id` SMALLINT UNSIGNED NOT NULL ,
  `ark` VARCHAR(255) NOT NULL ,
  `object_type` ENUM('MRT-curatorial', 'MRT-system') NOT NULL ,
  `role` ENUM('MRT-class', 'MRT-content') NOT NULL ,
  `aggregate_role` ENUM('MRT-collection', 'MRT-owner', 'MRT-service-level-agreement', 'MRT-submission-agreement','MRT-none') NULL ,
  `version_number` SMALLINT UNSIGNED NOT NULL ,
  `erc_who` VARCHAR(5394) NULL ,
  `erc_what` VARCHAR(5394) NULL ,
  `erc_when` VARCHAR(5394) NULL ,
  `erc_where` VARCHAR(5394) NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `modified` TIMESTAMP NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `ark_UNIQUE` (`ark` ASC) ,
  KEY `created` (`created` ASC) ,
  KEY `modified` (`modified` ASC) ,
  KEY `id_idx` (`inv_owner_id` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_owner_id` )
    REFERENCES `inv_owners` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv_versions`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv_versions` ;

CREATE  TABLE IF NOT EXISTS `inv_versions` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `inv_object_id` INT UNSIGNED NOT NULL ,
  `ark` VARCHAR(255) NOT NULL ,
  `number` SMALLINT UNSIGNED NOT NULL ,
  `note` VARCHAR(16383) NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`) ,
  KEY `created` (`created` ASC) ,
  KEY `id_idx` (`inv_object_id` ASC) ,
  KEY `ark` (`ark` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_object_id` )
    REFERENCES `inv_objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv_files`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv_files` ;

CREATE  TABLE IF NOT EXISTS `inv_files` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `inv_object_id` INT UNSIGNED NOT NULL ,
  `inv_version_id` INT UNSIGNED NOT NULL ,
  `pathname` VARCHAR(16383) NOT NULL ,
  `source` ENUM('consumer', 'producer', 'system') NOT NULL ,
  `role` ENUM('data', 'metadata') NOT NULL ,
  `full_size` BIGINT UNSIGNED NOT NULL DEFAULT 0 ,
  `billable_size` BIGINT UNSIGNED NOT NULL DEFAULT 0 ,
  `mime_type` VARCHAR(255) NULL ,
  `digest_type` ENUM('adler-32','crc-32','md2','md5','sha-1','sha-256','sha-384','sha-512') NULL ,
  `digest_value` VARCHAR(255) NULL ,
  `created` TIMESTAMP NOT NULL ,
  PRIMARY KEY (`id`) ,
  KEY `mime_type` (`mime_type` ASC) ,
  KEY `created` (`created` ASC) ,
  KEY `id_idx` (`inv_version_id` ASC) ,
  KEY `id_idx1` (`inv_object_id` ASC) ,
  KEY `source` (`source` ASC) ,
  KEY `role` (`role` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_version_id` )
    REFERENCES `inv_versions` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_object_id` )
    REFERENCES `inv_objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv_collections`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv_collections` ;

CREATE  TABLE IF NOT EXISTS `inv_collections` (
  `id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `inv_object_id` INT UNSIGNED NULL ,
  `ark` VARCHAR(255) NOT NULL ,
  `name` VARCHAR(255) NULL ,
  `mnemonic` VARCHAR(255) NULL ,
  `read_privilege` ENUM('public', 'restricted') NULL ,
  `write_privilege` ENUM('public', 'restricted') NULL ,
  `download_privilege` ENUM('public', 'restricted') NULL ,
  `storage_tier` ENUM('standard', 'premium') NULL ,
  PRIMARY KEY (`id`) ,
  UNIQUE INDEX `ark_UNIQUE` (`ark` ASC) ,
  KEY `id_idx` (`inv_object_id` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_object_id` )
    REFERENCES `inv_objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv_collections_inv_objects`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv_collections_inv_objects` ;

CREATE  TABLE IF NOT EXISTS `inv_collections_inv_objects` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `inv_collection_id` SMALLINT UNSIGNED NOT NULL ,
  `inv_object_id` INT UNSIGNED NOT NULL ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx` (`inv_collection_id` ASC) ,
  KEY `id_idx1` (`inv_object_id` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_collection_id` )
    REFERENCES `inv_collections` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_object_id` )
    REFERENCES `inv_objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv_duas`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv_duas` ;

CREATE  TABLE IF NOT EXISTS `inv_duas` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `inv_collection_id` SMALLINT UNSIGNED NULL ,
  `inv_object_id` INT UNSIGNED NOT NULL ,
  `identifier` VARCHAR(255) NULL ,
  `title` VARCHAR(255) NOT NULL ,
  `terms` VARCHAR(16383) NOT NULL ,
  `template` TEXT NULL ,
  `accept_obligation` ENUM('required', 'optional', 'none') NOT NULL ,
  `name_obligation` ENUM('required', 'optional', 'none') NOT NULL ,
  `affiliation_obligation` ENUM('required', 'optional', 'none') NOT NULL ,
  `email_obligation` ENUM('required', 'optional', 'none') NOT NULL ,
  `applicability` ENUM('collection', 'object', 'version', 'file') NOT NULL ,
  `persistence` ENUM('request', 'session', 'permanent') NOT NULL ,
  `notification` VARCHAR(255) NOT NULL ,
  KEY `id_idx` (`inv_collection_id` ASC) ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx1` (`inv_object_id` ASC) ,
  KEY `identifier` (`identifier` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_collection_id` )
    REFERENCES `inv_collections` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_object_id` )
    REFERENCES `inv_objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv_ingests`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv_ingests` ;

CREATE  TABLE IF NOT EXISTS `inv_ingests` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `inv_object_id` INT UNSIGNED NOT NULL ,
  `inv_version_id` INT UNSIGNED NOT NULL ,
  `filename` VARCHAR(255) NOT NULL ,
  `ingest_type` ENUM('file', 'container', 'object-manifest', 'single-file-batch-manifest', 'container-batch-manifest', 'batch-manifest') NOT NULL ,
  `profile` VARCHAR(255) NOT NULL ,
  `batch_id` VARCHAR(255) NOT NULL ,
  `job_id` VARCHAR(255) NOT NULL ,
  `user_agent` VARCHAR(255) NULL ,
  `submitted` TIMESTAMP NOT NULL ,
  `storage_url` VARCHAR(255) NULL ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx` (`inv_object_id` ASC) ,
  KEY `id_idx1` (`inv_version_id` ASC) ,
  KEY `profile` (`profile` ASC) ,
  KEY `batch_id` (`batch_id` ASC) ,
  KEY `user_agent` (`user_agent` ASC) ,
  KEY `submitted` (`submitted` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_object_id` )
    REFERENCES `inv_objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_version_id` )
    REFERENCES `inv_versions` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv_dublinkernels`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv_dublinkernels` ;

CREATE  TABLE IF NOT EXISTS `inv_dublinkernels` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `inv_object_id` INT UNSIGNED NOT NULL ,
  `inv_version_id` INT UNSIGNED NOT NULL ,
  `seq_num` SMALLINT UNSIGNED NOT NULL ,
  `element` VARCHAR(255) NOT NULL ,
  `qualifier` VARCHAR(255) NULL ,
  `value` VARCHAR(20000) NOT NULL ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx` (`inv_object_id` ASC) ,
  KEY `id_idx1` (`inv_version_id` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_object_id` )
    REFERENCES `inv_objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_version_id` )
    REFERENCES `inv_versions` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;


-- -----------------------------------------------------
-- Table `inv_metadatas`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv_metadatas` ;

CREATE  TABLE IF NOT EXISTS `inv_metadatas` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `inv_object_id` INT UNSIGNED NOT NULL ,
  `inv_version_id` INT UNSIGNED NOT NULL ,
  `filename` VARCHAR(255) NOT NULL ,
  `md_schema` ENUM('DataCite', 'DublinCore', 'CSDGM', 'EML') NOT NULL ,
  `version` VARCHAR(255) NULL ,
  `serialization` ENUM('anvl', 'json', 'xml') NULL ,
  `value` TEXT NULL ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx` (`inv_object_id` ASC) ,
  KEY `id_idx1` (`inv_version_id` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_object_id` )
    REFERENCES `inv_objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_version_id` )
    REFERENCES `inv_versions` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `inv_nodes`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv_nodes` ;

CREATE  TABLE IF NOT EXISTS `inv_nodes` (
  `id` SMALLINT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `number` INT NOT NULL ,
  `media_type` ENUM('magnetic-disk','magnetic-tape','optical-disk','solid-state','unknown') NOT NULL ,
  `media_connectivity` ENUM('cloud','das','nas','san','unknown') NOT NULL ,
  `access_mode` ENUM('on-line', 'near-line', 'off-line','unknown') NOT NULL ,
  `access_protocol` ENUM('cifs','nfs','open-stack','s3','zfs','unknown') NOT NULL ,
  `logical_volume` VARCHAR(255) NULL ,
  `external_provider` VARCHAR(255) NULL ,
  `verify_on_read` TINYINT(1) NOT NULL ,
  `verify_on_write` TINYINT(1) NOT NULL ,
  `base_url` VARCHAR(2045) NOT NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`) )
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `inv_nodes_inv_objects`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `inv_nodes_inv_objects` ;

CREATE  TABLE IF NOT EXISTS `inv_nodes_inv_objects` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `inv_node_id` SMALLINT UNSIGNED NOT NULL ,
  `inv_object_id` INT UNSIGNED NOT NULL ,
  `role` ENUM('primary','secondary') NOT NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx` (`inv_node_id` ASC) ,
  KEY `id_idx1` (`inv_object_id` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_node_id` )
    REFERENCES `inv_nodes` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_object_id` )
    REFERENCES `inv_objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;



SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
