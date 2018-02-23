-- MySQL Script to set up the needed table and stored procedures for the WS-PGRADE Extensions.
USE guse;

-- information about queues and resources is stored by dci_bridge_service in dci-bridge.xml, so it would be redundant to have tables to store those
-- we do not assume that all is needed to identify an application is its path, UNICORE, for instance, does not show the path, only app name and version, and
-- even though we are not using this table to store information about UNICORE applications, maybe other cluster systems care only about name and version
CREATE TABLE IF NOT EXISTS tbl_application (
	resource_name	TEXT			NOT NULL,
	resource_type	TEXT			NOT NULL, 
	name			TEXT			NOT NULL,
	version			TEXT			NOT NULL,
	path			TEXT			NOT NULL,
	description		TEXT,	
	UNIQUE INDEX (resource_name(128), resource_type(16), name(256), version(32), path(512))	
);

-- create the "get" stored procedure
-- if all the applications are needed, then set both input parameters to NULL
DROP PROCEDURE IF EXISTS sp_get_all_applications;
DELIMITER #
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
END#
DELIMITER ;

-- create the "add" stored procedure
DROP PROCEDURE IF EXISTS sp_add_application;
DELIMITER #
CREATE PROCEDURE sp_add_application(
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
END#
DELIMITER ;

-- create the "remove" stored procedure
DROP PROCEDURE IF EXISTS sp_delete_applications;
DELIMITER #
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
END#
DELIMITER ;

-- create the "edit" stored procedure
DROP PROCEDURE IF EXISTS sp_edit_application;
DELIMITER #
CREATE PROCEDURE sp_edit_application (
	IN 	param_resource_name		TEXT,
	IN 	param_resource_type 	TEXT,
	IN 	param_name				TEXT,
	IN 	param_version			TEXT,
	IN 	param_path				TEXT,
	IN 	param_description		TEXT
)
BEGIN
	UPDATE tbl_application SET 			
		description = param_description
	WHERE
		resource_name = param_resource_name AND
		resource_type = param_resource_type AND		
		name = param_name AND
		version = param_version AND
		path = param_path;
END#
DELIMITER ;