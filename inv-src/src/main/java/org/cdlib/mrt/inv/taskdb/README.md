### Problem
Many system repairs and migrations are one-time actions that require specialized handling that is difficult to generalize.  Having a simplified process for recording actions taken that can be integrated with other sql tools is useful
to:
- better guarantee that all items/objects have been modified
- identification of problem 

### Proposed
An inv_tasks table for the inv db that provides a simple API through the replic microservice that provides the following information:
- name of general tasks being performed
- name of item being updated by general takk
- status of task: ok, fail
- first attempt at modifying content
- last attempt at modifying content
- number of retries
- note describing any error or feature of attempted update

### Features
- single entry for basic level of one-off
   - entry id remains unchanged between updates
   - parts of multiple steps of one-off change are not included one-off succeeded or failed. If a failure then details may be noted in "note"
   - counts of retries
   - date for first attempt
   - date for last attempt
   
- replic API 
  - maintains underlying microservice restrictions
  - using simple REST API: POST, GET, DELETE
  - use MULTIPART_FORM to allow curl api without encoding
  - uses consistent update handling to maintain single entry features:
~~~
"INSERT INTO inv_tasks "
            + "(task_name, task_item, retries, current_status, note) "
            + "VALUES (?, ?, 0, ?, ?) "
            +  "ON DUPLICATE KEY UPDATE current_status = ?, note = ?, retries = retries + 1, entry_last=NOW();";
~~~


### inv_tasks CREATE
~~~
CREATE TABLE `inv_tasks` (
	`id` INT NOT NULL AUTO_INCREMENT,
	`created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`updated` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
	`task_name` VARCHAR(255) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`task_item` VARCHAR(511) NOT NULL COLLATE 'utf8mb4_unicode_ci',
	`retries` INT NOT NULL DEFAULT '0',
	`current_status` ENUM('ok','fail','pending','partial','unknown') NOT NULL DEFAULT 'unknown' COLLATE 'utf8mb4_unicode_ci',
	`note` MEDIUMTEXT NULL DEFAULT NULL COLLATE 'utf8mb4_unicode_ci',
	PRIMARY KEY (`id`) USING BTREE,
	UNIQUE INDEX `inx_tasks` (`task_name`, `task_item`) USING BTREE,
	INDEX `updated_inx` (`updated`) USING BTREE,
	INDEX `task_name_inx` (`task_name`) USING BTREE,
	INDEX `task_item_inx` (`task_item`) USING BTREE,
	INDEX `status_inx` (`current_status`) USING BTREE
)
COLLATE='utf8mb4_unicode_ci'
ENGINE=InnoDB
ROW_FORMAT=DYNAMIC
;
~~~

### columns
- id - auto incremented id for entry
- created - date of entry creation
- updated - date of last update
- task_name - general name of task for one-off
- task_item - individual entry for item in task
- retries - number of attempts on this process
- current_status - status of last attempt at processing
- note - note related to last attempt at processing

### comments
- task_name + task_item are unique - so get/post/delete work off of this combination

### Example  Replic call API
~~~
-bash-4.2$ cat addfail.sh
curl -s -X POST  -F "name=changeToken"  -F "item=ark:/28722/bk0003d9n5m"  -F "status=fail" -F "note=Exception copy"   http://localhost:38001/mrtreplic/task | jq
-bash-4.2$ ./addfail.sh
{
  "task_name": "changeToken",
  "retries": "0",
  "note": "Exception copy",
  "created": "2024-08-26 14:14:31",
  "current_status": "fail",
  "id": "213",
  "updated": "2024-08-26 14:14:31",
  "task_item": "ark:/28722/bk0003d9n5m"
}

-bash-4.2$ cat .add.sh
cat: .add.sh: No such file or directory
-bash-4.2$ cat ./add.sh
curl -s -X POST  -F "name=changeToken"  -F "item=ark:/28722/bk0003d9n5m"  -F "status=ok"   http://localhost:38001/mrtreplic/task |jq
-bash-4.2$ ./add.sh
{
  "task_name": "changeToken",
  "retries": "1",
  "created": "2024-08-26 14:14:31",
  "current_status": "ok",
  "id": "213",
  "updated": "2024-08-26 14:14:53",
  "task_item": "ark:/28722/bk0003d9n5m"
}

-bash-4.2$ cat get.sh
curl -s -X GET  -F "name=changeToken"  -F "item=ark:/28722/bk0003d9n5m"  -F "status=ok"   http://localhost:38001/mrtreplic/task | jq
-bash-4.2$ ./get.sh
{
  "task_name": "changeToken",
  "retries": "1",
  "created": "2024-08-26 14:14:31",
  "current_status": "ok",
  "id": "213",
  "updated": "2024-08-26 14:14:53",
  "task_item": "ark:/28722/bk0003d9n5m"
}

-bash-4.2$ cat del.sh
curl -s -X DELETE  -F "name=changeToken"  -F "item=ark:/28722/bk0003d9n5m"  -F "status=ok"   http://localhost:38001/mrtreplic/task | jq
-bash-4.2$ ./del.sh
{
  "task_name": "changeToken",
  "retries": "1",
  "created": "2024-08-26 14:14:31",
  "current_status": "ok",
  "id": "213",
  "updated": "2024-08-26 14:14:53",
  "task_item": "ark:/28722/bk0003d9n5m"
}

-bash-4.2$ ./get.sh
{}
~~~
