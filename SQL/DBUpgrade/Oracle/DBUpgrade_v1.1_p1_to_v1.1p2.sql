/* To remove unnecessary protection elements of StorageContainer */
delete
FROM csm_protection_element
WHERE regexp_like(object_id,  '_[[:digit:]]')
   AND NOT regexp_like(object_id,   'Site_[[:digit:]]')
   AND NOT regexp_like(object_id,   'CollectionProtocol_[[:digit:]]')
   AND NOT regexp_like(object_id,   'User_[[:digit:]]')
   AND object_id NOT LIKE 'SITE_%';


/* Upgrade caTissue schema. */

ALTER TABLE catissue_disposal_event_param MODIFY REASON varchar(350);

/** For bug #11229. Added CONCENTRATION field in metadata tables. **/
UPDATE CATISSUE_QUERY_TABLE_DATA SET ALIAS_NAME='MolecularSpecimen' WHERE TABLE_ID=99;
INSERT INTO CATISSUE_INTERFACE_COLUMN_DATA( IDENTIFIER, TABLE_ID, COLUMN_NAME , ATTRIBUTE_TYPE ) VALUES (345, 99, 'CONCENTRATION', 'double');
INSERT INTO CATISSUE_TABLE_RELATION( RELATIONSHIP_ID, PARENT_TABLE_ID, CHILD_TABLE_ID) VALUES (147, 33, 99);
INSERT INTO CATISSUE_SEARCH_DISPLAY_DATA( RELATIONSHIP_ID, COL_ID, DISPLAY_NAME, ATTRIBUTE_ORDER) VALUES (147, 345, 'Concentration',2);
INSERT INTO CATISSUE_RELATED_TABLES_MAP( FIRST_TABLE_ID, SECOND_TABLE_ID, FIRST_TABLE_JOIN_COLUMN, SECOND_TABLE_JOIN_COLUMN) VALUES (33, 99, 'IDENTIFIER', 'IDENTIFIER');


/* Upgarde DE schema. */

ALTER TABLE DYEXTN_CONTAINER MODIFY CAPTION varchar2(800);
ALTER TABLE DYEXTN_CONTROL MODIFY CAPTION varchar2(800);