-- MySQL Script to setup the needed tables, stored procedures for the WS-PGRADE Portlets
-- WARNING: Running this script will remove any stored data by the WS-PGRADE Portlets

USE guse;

-- create the table we need
DROP TABLE IF EXISTS wfip_tbl_application;
CREATE TABLE wfip_tbl_application (
	id				INT	UNSIGNED 	NOT NULL AUTO_INCREMENT,
    name			VARCHAR(256) 	NOT NULL,
    version			VARCHAR(16) 	NOT NULL,
    resource		VARCHAR(256)	NOT NULL,
    resource_type	VARCHAR(64)		NOT NULL,
    description		VARCHAR(512),
    path			VARCHAR(512) 	NOT NULL,
        
    PRIMARY KEY (id),
    UNIQUE KEY wfip_idx_application (name, version, resource, resource_type)
) AUTO_INCREMENT = 0;

-- create the view stored procedure
DROP PROCEDURE IF EXISTS wfip_sp_get_all_applications;
DELIMITER $$
CREATE PROCEDURE wfip_sp_get_all_applications()
	BEGIN
		SELECT id, name, version, resource, resource_type, description, path FROM wfip_tbl_application;
	END $$
DELIMITER ;

-- create the add stored procedure
DROP PROCEDURE IF EXISTS wfip_sp_add_application;
DELIMITER $$
CREATE PROCEDURE wfip_sp_add_application (
	IN 	param_name 			VARCHAR(256),
	IN 	param_version 		VARCHAR(16),
	IN 	param_resource		VARCHAR(256),
	IN 	param_resource_type	VARCHAR(64),
	IN 	param_description	VARCHAR(512),
	IN 	param_path			VARCHAR(512),
	OUT	param_id			INT UNSIGNED
)
	BEGIN
		INSERT INTO wfip_tbl_application (name, version, resource, resource_type, description, path) 
		VALUES (param_name, param_version, param_resource, param_resource_type, param_description, param_path);
		
		SET param_id = LAST_INSERT_ID();
	END $$
DELIMITER ;

-- create the update stored procedure
DROP PROCEDURE IF EXISTS wfip_sp_update_application;
DELIMITER $$
CREATE PROCEDURE wfip_sp_update_application (
	IN param_id				INT,
	IN param_name	 		VARCHAR(256),
	IN param_version 		VARCHAR(16),
	IN param_resource		VARCHAR(256),
	IN param_resource_type	VARCHAR(64),
	IN param_description	VARCHAR(512),
	IN param_path			VARCHAR(512)
)
	BEGIN
		UPDATE wfip_tbl_application SET 
			name=param_name, 
			version=param_version,
			resource=param_resource,
			resource_type=param_resource_type,
			description=param_description,
			path=param_path
		WHERE 
			id=param_id;
	END $$
DELIMITER ;

-- create the delete stored procedure
DROP PROCEDURE IF EXISTS wfip_sp_delete_application;
DELIMITER $$
CREATE PROCEDURE wfip_sp_delete_application (
	IN param_id				INT
)
	BEGIN
		DELETE FROM wfip_tbl_application WHERE id=param_id;
	END $$
DELIMITER ;