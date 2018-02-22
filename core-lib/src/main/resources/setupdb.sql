-- MySQL Script to set up the needed table and stored procedures for the WS-PGRADE Extensions.
USE guse;

-- create the tables we need
-- information about queues and resources is stored by dci_bridge_service in dci-bridge.xml, so it would be redundant to have tables to store those
CREATE TABLE IF NOT EXISTS tbl_application (
	resource_name	TEXT			NOT NULL,
	resource_type	TEXT			NOT NULL, 
	name			TEXT			NOT NULL,
	version			TEXT			NOT NULL,
	path			TEXT			NOT NULL,
	description		TEXT,
	
	-- we do not assume that all is needed to identify an application is its path, UNICORE, for instance, does not show the path, only app name and version, and
	-- even though we are not using this table to store information about UNICORE applications, maybe other cluster systems care only about name and version
	-- for BLOB and TEXT columns in indices, we need to specify the index prefix length
	-- (see https://dev.mysql.com/doc/refman/5.7/en/create-index.html)
	-- paths in Linux can be up to 4096 characters, but 512 should be plenty
	UNIQUE INDEX (resource_name(128), resource_type(16), name(256), version(32), path(512))
	
) AUTO_INCREMENT = 0;


-- create the "get" stored procedure
-- if all the applications are needed, then set both input parameters to NULL
DROP PROCEDURE IF EXISTS sp_get_applications;
DELIMITER $$
CREATE PROCEDURE sp_get_all_applications(
	IN param_resource_name		TEXT,
	IN param_resource_type		TEXT
)
	BEGIN
		SELECT 
			id, resource_name, resource_type, name, version, path, description 
		FROM 
			tbl_application
		WHERE
			(param_resource_name IS NULL OR resource_name = param_resource_name) AND
			(param_resource_type IS NULL OR resource_type = param_resource_type)
		ORDER BY
			resource_name, resource_type;
	END $$
DELIMITER ;

-- create the "add" stored procedure
DROP PROCEDURE IF EXISTS sp_add_application;
DELIMITER $$	
CREATE PROCEDURE sp_add_application (
	IN 	param_resource_name		TEXT,
	IN 	param_resource_type 	TEXT,
	IN 	param_name				TEXT,
	IN 	param_version			TEXT,
	IN 	param_path				TEXT,
	IN 	param_description		TEXT
)
	BEGIN
		INSERT INTO 
			tbl_application (resource_name, resource_type, name, version, path, description) 
		VALUES 
			(param_resource_name, param_resource_type, param_name, param_version, param_path, param_description);
	END $$
DELIMITER ;

-- create the "remove" stored procedure
-- it would be neat to pass a list of application ids to be removed, but MySQL does not support arrays as parameters... a hack
-- would be to pass a string containing the ids (e.g., '1,2,3,4'), as shown here: https://stackoverflow.com/questions/8149545/pass-array-to-mysql-stored-routine,
-- but that's just sloppy code and the reason SQL injection is possible... so if we want to delete N applications, we will invoke this
-- stored procedure N times, it's not very performant, but it's safe (and we would expect N to be way less than 500)
-- another option is to use MySQL functions to "strip" a comma-separated string (e.g., again, '1,2,3,4'), but then this code would be harder to test
-- in an automated way
DROP PROCEDURE IF EXISTS sp_delete_applications;
DELIMITER $$
CREATE PROCEDURE sp_delete_applications (
	IN	param_resource_name		TEXT,
	IN 	param_resource_type		TEXT
)
	BEGIN
		DELETE FROM 
			tbl_application 
		WHERE 
			resource_name = param_resource_name AND
			resource_type = param_resource_type;
	END $$
DELIMITER ;

-- create the "edit" stored procedure
DROP PROCEDURE IF EXISTS sp_edit_application;
DELIMITER $$
CREATE PROCEDURE sp_edit_application (
	IN 	param_resource_name		TEXT,
	IN 	param_resource_type 	TEXT,
	IN 	param_name				TEXT,
	IN 	param_version			TEXT,
	IN 	param_path				TEXT,
	IN 	param_description		TEXT
)
	BEGIN
		UPDATE wc_tbl_application SET 			
			description = param_description
		WHERE
			resource_name = param_resource_name,
			resource_type = param_resource_type,			
			name = param_name,
			version = param_version,
			path = param_path;
	END $$
DELIMITER ;