ALTER TABLE inv_nodes_inv_objects DROP backup;

alter table inv_nodes_inv_objects add replicated timestamp null after created;

alter table inv_nodes_inv_objects add key `id_idx2` (`replicated` ASC);

ALTER TABLE inv_nodes_inv_objects ADD `version_number` SMALLINT UNSIGNED NULL AFTER `replicated`;

CREATE  TABLE IF NOT EXISTS `inv_nodes_inv_objects` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `inv_node_id` SMALLINT UNSIGNED NOT NULL ,
  `inv_object_id` INT UNSIGNED NOT NULL ,
  `role` ENUM('primary','secondary') NOT NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  `replicated` TIMESTAMP NULL ,
  `version_number` SMALLINT UNSIGNED NULL ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx` (`inv_node_id` ASC) ,
  KEY `id_idx1` (`inv_object_id` ASC) ,
  KEY `id_idx2` (`replicated` ASC),
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

DROP TABLE IF EXISTS `inv_collection_mappings` ;

DROP TABLE IF EXISTS `inv_collections_inv_nodes` ;

CREATE  TABLE IF NOT EXISTS `inv_collections_inv_nodes` (
  `id` INT NOT NULL AUTO_INCREMENT ,
  `inv_collection_id` SMALLINT UNSIGNED NOT NULL ,
  `inv_node_id` SMALLINT UNSIGNED NOT NULL ,
  `created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ,
  PRIMARY KEY (`id`) ,
  KEY `id_idx` (`inv_collection_id` ASC) ,
  KEY `id_idx1` (`inv_node_id` ASC) ,
-- CONSTRAINT `inv_primary_id`
    FOREIGN KEY (`inv_collection_id` )
    REFERENCES `inv_collections` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION,
-- CONSTRAINT `inv_secondary_id`
    FOREIGN KEY (`inv_node_id` )
    REFERENCES `inv_nodes` (`id` )
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

insert into inv_collections_inv_nodes set inv_collection_id=1, inv_node_id=21

