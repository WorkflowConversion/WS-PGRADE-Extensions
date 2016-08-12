-- MySQL Script to setup the needed tables, stored procedures for the WorkflowImporterPortlet
-- WARNING: Running this script will remove any stored data by the WorkflowImporterPortlet

USE guse;

-- create the table we need
DROP TABLE IF EXISTS wfip_tbl_application;
CREATE TABLE wfip_tbl_application (
    name			VARCHAR(256) 	NOT NULL,
    version			VARCHAR(16) 	NOT NULL,
    description		VARCHAR(512),
    path			VARCHAR(512) 	NOT NULL,
    
    PRIMARY KEY (name, version)
);

-- create the view stored procedure
DROP PROCEDURE IF EXISTS wfip_sp_get_applications;
DELIMITER $$
CREATE PROCEDURE wfip_sp_get_applications()
	BEGIN
		SELECT id, name, version, description, path FROM wfip_tbl_application
	END $$
DELIMITER ;

-- create the add stored procedure
DROP PROCEDURE IF EXISTS wfip_sp_add_application;
CREATE PROCEDURE wfip_sp_add_application (
	IN name 		VARCHAR(256),
	IN version 		VARCHAR(16),
	IN description	VARCHAR(512),
	IN path			VARCHAR(512)
)
	BEGIN
		INSERT INTO wfip_tbl_application (name, version, description, path) 
		VALUES (name, version, description, path)
	END $$
DELIMITER ;

-- create the update stored procedure
DROP PROCEDURE IF EXISTS wfip_sp_update_application;
DELIMITER $$
CREATE PROCEDURE wfip_sp_update_application (
	IN old_name 	VARCHAR(256),
	IN old_version 	VARCHAR(16),
	IN new_name 	VARCHAR(256),
	IN new_version 	VARCHAR(16),
	IN description	VARCHAR(512),
	IN path			VARCHAR(512)
)
	BEGIN
		UPDATE wfip_tbl_application SET 
			name=new_name, 
			version=new_version,
			description=description,
			path=path
		WHERE name=old_name AND version=old_version
	END $$
DELIMITER ;

