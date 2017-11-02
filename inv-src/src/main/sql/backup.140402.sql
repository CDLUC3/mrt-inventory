alter table inv_nodes_inv_objects add backup timestamp null after created

alter table inv_nodes_inv_objects add key `id_idx2` (`backup` ASC)

CREATE  TABLE IF NOT EXISTS `inv_nodes_inv_objects` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `inv_node_id` SMALLINT UNSIGNED NOT NULL ,
  `inv_object_id` INT UNSIGNED NOT NULL ,
  `role` ENUM('primary','secondary') NOT NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `backup` TIMESTAMP NULL ,
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

DROP TABLE IF EXISTS `inv_node_mappings` ;

CREATE  TABLE IF NOT EXISTS `inv_node_mappings` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `inv_primary_id` SMALLINT UNSIGNED NOT NULL ,
  `inv_secondary_id` SMALLINT UNSIGNED NOT NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx` (`inv_primary_id` ASC) ,
  KEY `id_idx1` (`inv_secondary_id` ASC) ,
-- CONSTRAINT `inv_primary_id`
    FOREIGN KEY (`inv_primary_id` )
    REFERENCES `inv_nodes` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `inv_secondary_id`
    FOREIGN KEY (`inv_secondary_id` )
    REFERENCES `inv_nodes` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

