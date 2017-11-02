DROP TABLE IF EXISTS `inv_localids` ;

CREATE TABLE `inv_localids` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`inv_object_ark` VARCHAR(255) NOT NULL,
	`inv_owner_ark` VARCHAR(255) NOT NULL,
	`local_id` VARCHAR(255) NOT NULL,
	`created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `loc_unique` (`inv_owner_ark`, `local_id`),
	INDEX `id_idoba` (`inv_object_ark`),
	INDEX `id_idowa` (`inv_owner_ark`),
	INDEX `id_idloc` (`local_id`)
)
COLLATE='utf8_general_ci'
ENGINE=InnoDB
ROW_FORMAT=DYNAMIC
;

