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
	UNIQUE INDEX (resource_name(64), resource_type(16), name(64), version(32), path(128))	
);

-- create the "get" stored procedure
DROP PROCEDURE IF EXISTS sp_get_applications;
DELIMITER #
CREATE PROCEDURE sp_get_applications(
	IN param_resource_name		TEXT,
	IN param_resource_type		TEXT
)
BEGIN
	SELECT 
		resource_name, resource_type, name, version, path, description 
	FROM 
		tbl_application
	WHERE
		resource_name = param_resource_name AND
		resource_type = param_resource_type
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
