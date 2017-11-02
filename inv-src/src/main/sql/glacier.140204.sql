DROP TABLE IF EXISTS `inv_glacier_transactions` ;

DROP TABLE IF EXISTS `inv_glacier_copies` ;

CREATE  TABLE IF NOT EXISTS `inv_glacier_copies` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT ,
  `inv_object_id` INT UNSIGNED NOT NULL ,
  `inv_version_id` INT UNSIGNED NOT NULL ,
  `inv_file_id` INT UNSIGNED NOT NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `copy_region` VARCHAR(50) NOT NULL ,
  `copy_vault` VARCHAR(255) NOT NULL ,
  `copy_key` VARCHAR(255) NOT NULL ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx1` (`inv_object_id` ASC) ,
  KEY `id_idx2` (`inv_version_id` ASC) ,
  KEY `id_idx3` (`inv_file_id` ASC) ,
  KEY `copy_key` (`copy_key`),
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_object_id` )
    REFERENCES `inv_objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_version_id` )
    REFERENCES `inv_versions` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_file_id` )
    REFERENCES `inv_files` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;

CREATE  TABLE IF NOT EXISTS `inv_glacier_transactions` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `requested` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `glacier_action` ENUM('add','delete','retrieve','inventory') NOT NULL ,
  `copy_region` VARCHAR(50) NOT NULL ,
  `copy_vault` VARCHAR(255) NOT NULL ,
  `jobid` VARCHAR(2000) NULL ,
  `email` VARCHAR(2000) NULL ,
  `inv_file_id` INT UNSIGNED NULL ,
  `inv_copy_id` INT UNSIGNED NULL ,
  `completed` TIMESTAMP NULL ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx1` (`jobid` ASC) ,
  KEY `id_idx2` (`inv_copy_id` ASC) ,
  KEY `id_idx3` (`inv_file_id` ASC) ,
  KEY `id_idx4` (`glacier_action` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_file_id` )
    REFERENCES `inv_files` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB
ROW_FORMAT = DYNAMIC;

