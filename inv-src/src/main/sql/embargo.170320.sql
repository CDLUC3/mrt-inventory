CREATE TABLE `inv_embargoes` (
	`id` INT(11) NOT NULL AUTO_INCREMENT,
	`inv_object_id` INT(10) UNSIGNED NOT NULL,
	`embargo_end_date` DATETIME NOT NULL,
	PRIMARY KEY (`id`),
	UNIQUE INDEX `inv_object_id_UNIQUE` (`inv_object_id`),
	INDEX `embargo_end_date` (`embargo_end_date`),
	CONSTRAINT `inv_embargoes_ibfk_1` FOREIGN KEY (`inv_object_id`) REFERENCES `inv_objects` (`id`) ON UPDATE NO ACTION ON DELETE NO ACTION
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
AUTO_INCREMENT=12
;
