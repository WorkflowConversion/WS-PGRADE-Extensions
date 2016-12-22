-- MySQL Script to setup the needed tables, stored procedures for the WS-PGRADE Portlets
-- WARNING: Running this script will remove any stored data by the WS-PGRADE Portlets

USE guse;

-- we need to delete the tables in certain order
DROP TABLE IF EXISTS wfip_tbl_application;
DROP TABLE IF EXISTS wfip_tbl_queue;
DROP TABLE IF EXISTS wfip_tbl_resource;

-- create the tables we need
CREATE TABLE wfip_tbl_resource (
	id			INT UNSIGNED	NOT NULL AUTO_INCREMENT PRIMARY KEY,
	type		VARCHAR(64) 	NOT NULL,
	location	VARCHAR(256)	NOT NULL,
	
	UNIQUE KEY wfip_idx_resource (type, location)
) AUTO_INCREMENT = 0;

CREATE TABLE wfip_tbl_queue (
	id				INT UNSIGNED 	NOT NULL AUTO_INCREMENT PRIMARY KEY,
	resource_id		INT UNSIGNED	NOT NULL,
	name			VARCHAR(64)		NOT NULL,
	
	UNIQUE KEY wfip_idx_queue (resource_id, name),
	FOREIGN KEY wfip_fk_queue (resource_id) REFERENCES wfip_tbl_resource(id) 
		ON DELETE CASCADE
		ON UPDATE CASCADE
) AUTO_INCREMENT = 0;

CREATE TABLE wfip_tbl_application (
	id				INT	UNSIGNED 	NOT NULL AUTO_INCREMENT PRIMARY KEY,
	resource_id		INT UNSIGNED	NOT NULL,
    name			VARCHAR(256) 	NOT NULL,
    version			VARCHAR(16) 	NOT NULL,
    description		VARCHAR(512),
    path			VARCHAR(512) 	NOT NULL,
        
    UNIQUE KEY wfip_idx_application (resource_id, name, version),
    FOREIGN KEY wfip_fk_application (resource_id) REFERENCES wfip_tbl_resource(id)
    	ON DELETE CASCADE
    	ON UPDATE CASCADE
) AUTO_INCREMENT = 0;

-- create the view stored procedure
DROP PROCEDURE IF EXISTS wfip_sp_get_all_resources;
DELIMITER $$
CREATE PROCEDURE wfip_sp_get_all_resources()
	BEGIN
		SELECT id, type, location FROM wfip_tbl_resource;
	END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS wfip_sp_get_all_queues;
DELIMITER $$
CREATE PROCEDURE wfip_sp_get_all_queues()
	BEGIN
		SELECT id, resource_id, name
			FROM wfip_tbl_queue INNER JOIN wfip_tbl_resource
			ON (wfip_tbl_queue.resource_id = wfip_tbl_resource.id);
	END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS wfip_sp_get_all_applications;
DELIMITER $$
CREATE PROCEDURE wfip_sp_get_all_applications()
	BEGIN
		SELECT id, resource_id, name, version, description, path   
			FROM wfip_tbl_application INNER JOIN wfip_tbl_resource
			ON (wfip_tbl_application.resource_id = wfip_tbl_resource.id);
	END $$
DELIMITER ;

-- create the add stored procedures
DROP PROCEDURE IF EXISTS wfip_sp_add_resource;
DELIMITER $$
CREATE PROCEDURE wfip_sp_add_resource (
	IN 	param_type		VARCHAR(64),
	IN 	param_location 	VARCHAR(256),	
	OUT	param_id		INT 
)
	BEGIN
		INSERT INTO wfip_tbl_resource (type, location) 
		VALUES (param_type, param_location);
		
		SET param_id = LAST_INSERT_ID();
	END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS wfip_sp_add_queue;
DELIMITER $$
CREATE PROCEDURE wfip_sp_add_queue (
	IN 	param_resource_id	INT ,
	IN 	param_name 			VARCHAR(64),	
	OUT	param_id			INT 
)
	BEGIN
		INSERT INTO wfip_tbl_queue (resource_id, name) 
		VALUES (param_resource_id, param_name);
		
		SET param_id = LAST_INSERT_ID();
	END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS wfip_sp_add_application;
DELIMITER $$
CREATE PROCEDURE wfip_sp_add_application (
	IN 	param_resource_id	VARCHAR(256),
	IN 	param_name 			VARCHAR(256),	
	IN 	param_version 		VARCHAR(16),	
	IN 	param_description	VARCHAR(512),
	IN 	param_path			VARCHAR(512),
	OUT	param_id			INT 
)
	BEGIN
		INSERT INTO wfip_tbl_application (resource_id, name, version, resource, resource_type, description, path) 
		VALUES (param_resource_id, param_name, param_version, param_resource, param_resource_type, param_description, param_path);
		
		SET param_id = LAST_INSERT_ID();
	END $$
DELIMITER ;

-- create the delete stored procedures
DROP PROCEDURE IF EXISTS wfip_sp_delete_resource;
DELIMITER $$
CREATE PROCEDURE wfip_sp_delete_resource (
	IN param_id				INT
)
	BEGIN
		DELETE FROM wfip_tbl_resource WHERE id=param_id;
	END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS wfip_sp_delete_queue;
DELIMITER $$
CREATE PROCEDURE wfip_sp_delete_queue (
	IN param_id				INT
)
	BEGIN
		DELETE FROM wfip_tbl_queue WHERE id=param_id;
	END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS wfip_sp_delete_application;
DELIMITER $$
CREATE PROCEDURE wfip_sp_delete_application (
	IN param_id				INT
)
	BEGIN
		DELETE FROM wfip_tbl_application WHERE id=param_id;
	END $$
DELIMITER ;

-- create the update stored procedures
DROP PROCEDURE IF EXISTS wfip_sp_update_resource;
DELIMITER $$
CREATE PROCEDURE wfip_sp_update_resource (
	IN param_id			INT,
	IN param_type		VARCHAR(64),
	IN param_location	VARCHAR(256)
)
	BEGIN
		UPDATE wfip_tbl_resource SET 
			type=param_type,
			location=param_location
		WHERE
			id=param_id;
	END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS wfip_sp_update_queue;
DELIMITER $$
CREATE PROCEDURE wfip_sp_update_queue (
	IN param_id				INT,
	IN param_resource_id	INT,
	IN param_name			VARCHAR(64)
)
	BEGIN
		UPDATE wfip_tbl_update_queue SET
			name=param_name,
			resource_id=param_resource_id
		WHERE
			id=param_id;
	END $$
DELIMITER ;

DROP PROCEDURE IF EXISTS wfip_sp_update_application;
DELIMITER $$
CREATE PROCEDURE wfip_sp_update_application (
	IN param_id				INT,
	IN param_resource_id	INT,
	IN param_name	 		VARCHAR(256),
	IN param_version 		VARCHAR(16),
	IN param_resource		VARCHAR(256),
	IN param_resource_type	VARCHAR(64),
	IN param_description	VARCHAR(512),
	IN param_path			VARCHAR(512)
)
	BEGIN
		UPDATE wfip_tbl_application SET
			resource_id=param_resource_id,
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