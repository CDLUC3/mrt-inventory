alter table inv_nodes add column `node_protocol` ENUM('file', 'http') NOT NULL DEFAULT 'file' AFTER node_form;

alter table inv_nodes add column `description` VARCHAR(255) NULL AFTER created;