alter table inv_collections 
add column `harvest_privilege` ENUM('public','none') NOT NULL DEFAULT 'none' after storage_tier;

alter table inv_collections 
add key id_hp (`harvest_privilege` ASC);

ALTER TABLE inv_metadatas 
MODIFY `md_schema` ENUM('DataCite', 'DublinCore', 'CSDGM', 'EML', 'OAI_DublinCore') NOT NULL;

ALTER TABLE inv_metadatas
MODIFY `filename` VARCHAR(255) NULL;