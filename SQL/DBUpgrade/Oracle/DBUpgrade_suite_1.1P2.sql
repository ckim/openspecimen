ALTER TABLE CATISSUE_CONTAINER ADD (CONT_FULL number(1,0));
UPDATE CATISSUE_CONTAINER SET CONT_FULL = FULL;
ALTER TABLE CATISSUE_CONTAINER DROP COLUMN FULL;
