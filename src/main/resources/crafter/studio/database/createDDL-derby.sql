CREATE SCHEMA CRAFTER;

SET SCHEMA CRAFTER;

CREATE TABLE CSTUDIO_ACTIVITY
(
    id BIGINT NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    MODIFIED_DATE TIMESTAMP NOT NULL,
    CREATION_DATE TIMESTAMP NOT NULL,
    SUMMARY VARCHAR(3000) NOT NULL,
    SUMMARY_FORMAT VARCHAR(255) NOT NULL,
    CONTENT_ID VARCHAR(3000) NOT NULL,
    SITE_NETWORK VARCHAR(255) NOT NULL,
    ACTIVITY_TYPE VARCHAR(255) NOT NULL,
    CONTENT_TYPE VARCHAR(255) NOT NULL,
    POST_USER_ID VARCHAR(255) NOT NULL,
  CONSTRAINT primary_key PRIMARY KEY (id)
);

CREATE TABLE CSTUDIO_DEPENDENCY (
  ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  SITE VARCHAR(35) NOT NULL,
  SOURCE_PATH VARCHAR(3000) NOT NULL,
  TARGET_PATH VARCHAR(3000) NOT NULL,
  TYPE VARCHAR(15) NOT NULL
);

CREATE TABLE CSTUDIO_OBJECTSTATE (
  OBJECT_ID VARCHAR(255) PRIMARY KEY NOT NULL,
  SITE VARCHAR(50)   NOT NULL,
  PATH VARCHAR(2000) NOT NULL,
  STATE VARCHAR(255)  NOT NULL,
  SYSTEM_PROCESSING SMALLINT NOT NULL,
  UNIQUE (SITE, PATH)
);

CREATE TABLE CSTUDIO_PAGENAVIGATIONORDERSEQUENCE (
  FOLDER_ID VARCHAR(100) PRIMARY KEY NOT NULL,
  SITE VARCHAR(50)  NOT NULL,
  PATH VARCHAR(255) NOT NULL,
  MAX_COUNT FLOAT NOT NULL
);

CREATE TABLE CSTUDIO_COPYTOENVIRONMENT (
  ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  SITE VARCHAR(50)  NOT NULL,
  ENVIRONMENT VARCHAR(20) NOT NULL,
  PATH VARCHAR(2000) NOT NULL,
  OLDPATH VARCHAR(3000),
  USERNAME VARCHAR(255),
  SCHEDULEDDATE DATE NOT NULL,
  STATE VARCHAR(50) NOT NULL,
  ACTION VARCHAR(20) NOT NULL,
  CONTENTTYPECLASS VARCHAR(20),
  SUBMISSIONCOMMENT VARCHAR(3000)
);

CREATE TABLE CSTUDIO_PUBLISHTOTARGET (
  ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  SITE VARCHAR(50) NOT NULL,
  ENVIRONMENT VARCHAR(20) NOT NULL,
  PATH VARCHAR(2000) NOT NULL,
  OLDPATH VARCHAR(2000),
  USERNAME VARCHAR(255) NOT NULL,
  VERSION BIGINT NOT NULL,
  ACTION VARCHAR(20) NOT NULL,
  CONTENTTYPECLASS VARCHAR(20)
);

CREATE TABLE CSTUDIO_DEPLOYMENTSYNCHISTORY (
  ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  SYNCDATE TIMESTAMP NOT NULL,
  SITE VARCHAR(50) NOT NULL,
  ENVIRONMENT VARCHAR(20) NOT NULL,
  PATH VARCHAR(2000) NOT NULL,
  TARGET VARCHAR(50) NOT NULL,
  USERNAME VARCHAR(255) NOT NULL,
  CONTENTTYPECLASS VARCHAR(25) NOT NULL
);

CREATE TABLE CSTUDIO_SITE (
  ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  SITE_ID VARCHAR(255) NOT NULL,
  NAME VARCHAR(255) NOT NULL,
  DESCRIPTION VARCHAR(45),
  STATUS VARCHAR(255),
  UNIQUE (SITE_ID)
);

CREATE TABLE CSTUDIO_OBJECTMETADATA (
  ID BIGINT PRIMARY KEY NOT NULL GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
  SITE VARCHAR(50) NOT NULL,
  PATH VARCHAR(2000) NOT NULL,
  NAME VARCHAR(255),
  MODIFIED TIMESTAMP,
  MODIFIER VARCHAR(255),
  OWNER VARCHAR(255),
  CREATOR VARCHAR(255),
  FIRSTNAME VARCHAR(255),
  LASTNAME VARCHAR(255),
  LOCKOWNER VARCHAR(255),
  EMAIL VARCHAR(255),
  RENAMED INT,
  OLDURL VARCHAR(2000),
  DELETEURL VARCHAR(2000),
  IMAGEWIDTH INT ,
  IMAGEHEIGHT INT,
  APPROVEDBY VARCHAR(255),
  SUBMITTEDBY VARCHAR(255),
  SUBMITTEDFORDELETION INT,
  SENDEMAIL INT,
  SUBMISSIONCOMMENT VARCHAR(2000),
  LAUNCHDATE TIMESTAMP,
  UNIQUE (SITE, PATH)
);
