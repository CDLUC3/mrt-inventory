CREATE  TABLE IF NOT EXISTS `inv_embargoes` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `inv_object_id` INT UNSIGNED NOT NULL ,
  `embargo_end_date` TIMESTAMP NOT NULL ,
  PRIMARY KEY (`id`) , 
  UNIQUE INDEX `inv_object_id_UNIQUE` (`inv_object_id` ASC) ,
  KEY `embargo_end_date` (`embargo_end_date` ASC) ,
-- CONSTRAINT `id`
    FOREIGN KEY (`inv_object_id` )
    REFERENCES `inv_objects` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;