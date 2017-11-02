ALTER TABLE inv_metadatas 
MODIFY `md_schema` 
ENUM('DataCite', 'DublinCore', 'CSDGM', 'EML', 'OAI_DublinCore', 'StashWrapper') NOT NULL;