# This is a modification of the sha_dublinkernels TEXT table from MyISAM to InnoDB
# This is required because MyISAM is now deprecated for future releases

ALTER TABLE sha_dublinkernels engine=InnoDB  row_format=dynamic;

#Fix for replication to remove all duplicate replication inv entries and prevent any future duplicates - early bug
#This alter could not be performed on versions < 5.6
#Note the IGNORE option will later be deprecated but is extremely useful here
#This fix will probably require 15 minutes

ALTER IGNORE TABLE inv_collections_inv_nodes ADD UNIQUE (inv_collection_id, inv_node_id);
ALTER IGNORE TABLE inv_nodes_inv_objects ADD UNIQUE (inv_object_id, inv_node_id);
ALTER IGNORE TABLE inv_audits ADD UNIQUE (inv_node_id, inv_file_id);

#Note to review status of completion

select *
from information_schema.key_column_usage
where constraint_schema = 'inv' 