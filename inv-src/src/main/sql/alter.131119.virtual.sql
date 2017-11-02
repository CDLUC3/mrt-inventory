alter table inv_nodes add column `node_form` ENUM('physical', 'virtual') NOT NULL DEFAULT 'physical' AFTER access_protocol;

alter table inv_nodes add column `source_node` SMALLINT UNSIGNED NULL AFTER created;

alter table inv_nodes add column `target_node` SMALLINT UNSIGNED NULL AFTER source_node;