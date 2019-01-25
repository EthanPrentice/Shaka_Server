/* create default user */
CREATE USER 'shaka'@'localhost' IDENTIFIED BY 'Pa$$phras3_123';
GRANT UPDATE ON `shaka`.* TO 'shaka'@'localhost';
GRANT INSERT ON `shaka`.* TO 'shaka'@'localhost';
GRANT SELECT ON `shaka`.* TO 'shaka'@'localhost';
GRANT DELETE ON `shaka`.* TO 'shaka'@'localhost';

/* create Schema */
CREATE SCHEMA IF NOT EXISTS `shaka`;

/* create Parties table */
CREATE TABLE IF NOT EXISTS `shaka`.`Parties` (
  `ID` CHAR(6) NOT NULL,
  `Status` VARCHAR(10) NOT NULL DEFAULT 'inactive',
  `Owner` VARCHAR(45) NOT NULL,
  `Name` VARCHAR(40) NOT NULL,
  `Latitude` DECIMAL(10,8) NULL DEFAULT NULL,
  `Longitude` DECIMAL(11,8) NULL DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `idParties_UNIQUE` (`ID` ASC));

/* create UserStatuses table */
CREATE TABLE IF NOT EXISTS `shaka`.`UserStatuses` (
  `ID` INT(11) NOT NULL,
  `Name` VARCHAR(15) NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `ID` (`ID` ASC));

/* create Users table */
CREATE TABLE IF NOT EXISTS `shaka`.`Users` (
  `ID` VARCHAR(45) NOT NULL,
  `Party` CHAR(6) NOT NULL,
  `AccessToken` VARCHAR(200) NOT NULL,
  `RefreshToken` VARCHAR(200) NOT NULL,
  `Scopes` VARCHAR(200) NOT NULL,
  `TokenType` VARCHAR(45) NOT NULL DEFAULT 'Bearer',
  `ExpiresAt` DATETIME NOT NULL,
  `DisplayName` VARCHAR(60) NULL DEFAULT NULL,
  `Status` INT(11) NOT NULL,
  PRIMARY KEY (`ID`, `Party`),
  UNIQUE INDEX `unique_idUser_idParties` (`Party` ASC, `ID` ASC),
  INDEX `fk_Users_Parties1_idx` (`Party` ASC),
  INDEX `Status` (`Status` ASC),
  CONSTRAINT `users_ibfk_1`
    FOREIGN KEY (`Party`)
    REFERENCES `shaka`.`Parties` (`ID`),
  CONSTRAINT `users_ibfk_2`
    FOREIGN KEY (`Status`)
    REFERENCES `shaka`.`UserStatuses` (`ID`));

/* create UserImages table */
CREATE TABLE IF NOT EXISTS `shaka`.`UserImages` (
  `Url` VARCHAR(200) NOT NULL,
  `Width` SMALLINT(6) NULL DEFAULT NULL,
  `Height` SMALLINT(6) NULL DEFAULT NULL,
  `User` VARCHAR(45) NOT NULL,
  PRIMARY KEY (`Url`),
  INDEX `userimages_ibfk_2` (`User` ASC),
  CONSTRAINT `userimages_ibfk_2`
    FOREIGN KEY (`User`)
    REFERENCES `shaka`.`Users` (`ID`)
    ON DELETE CASCADE);

/* create Songs table */
CREATE TABLE IF NOT EXISTS `shaka`.`Songs` (
  `Party` CHAR(6) NULL DEFAULT NULL,
  `User` VARCHAR(45) NULL DEFAULT NULL,
  `SongName` VARCHAR(255) NOT NULL,
  `SongURI` VARCHAR(150) NOT NULL,
  `AlbumName` VARCHAR(255) NOT NULL,
  `AlbumURI` VARCHAR(150) NOT NULL,
  `ArtistName` VARCHAR(255) NOT NULL,
  `ArtistURI` VARCHAR(150) NOT NULL,
  `QueuePos` INT(11) NULL DEFAULT NULL,
  `ID` INT(11) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`ID`),
  UNIQUE INDEX `unique_idParties_PlaceInQueue` (`Party` ASC, `QueuePos` ASC),
  INDEX `fk_Queues_Parties_idx` (`Party` ASC),
  INDEX `songqueues_ibfk_2` (`Party` ASC, `User` ASC),
  CONSTRAINT `songs_ibfk_1`
    FOREIGN KEY (`Party`)
    REFERENCES `shaka`.`Parties` (`ID`),
  CONSTRAINT `songs_ibfk_2`
    FOREIGN KEY (`Party` , `User`)
    REFERENCES `shaka`.`Users` (`Party` , `ID`));

/* create AlbumImages table */
CREATE TABLE IF NOT EXISTS `shaka`.`AlbumImages` (
  `Url` varchar(200) NOT NULL,
  `Width` smallint(6) NOT NULL,
  `Height` smallint(6) NOT NULL,
  `Album` varchar(45) NOT NULL,
  PRIMARY KEY (`Width`,`Height`,`Album`),
  INDEX `album_idx` (`Album` ASC));


/* create PlayerStatuses table */
CREATE TABLE IF NOT EXISTS `shaka`.`PlayerStatuses` (
  `ID` INT(11) NOT NULL,
  `Name` VARCHAR(25) NOT NULL,
  PRIMARY KEY (`ID`));

/* create Players table */
CREATE TABLE IF NOT EXISTS `shaka`.`Players` (
  `Party` CHAR(6) NOT NULL,
  `Status` INT(11) NULL DEFAULT NULL,
  `CurrentTrack` INT(11) NULL DEFAULT NULL,
  `TrackTime` INT(11) NOT NULL DEFAULT '0',
  PRIMARY KEY (`Party`),
  INDEX `Status` (`Status` ASC),
  INDEX `Party` (`Party` ASC, `CurrentTrack` ASC),
  INDEX `player_ibfk_3` (`CurrentTrack` ASC),
  CONSTRAINT `player_ibfk_1`
    FOREIGN KEY (`Party`)
    REFERENCES `shaka`.`Parties` (`ID`),
  CONSTRAINT `player_ibfk_2`
    FOREIGN KEY (`Status`)
    REFERENCES `shaka`.`PlayerStatuses` (`ID`),
  CONSTRAINT `player_ibfk_3`
    FOREIGN KEY (`CurrentTrack`)
    REFERENCES `shaka`.`Songs` (`ID`));



/* Initialize UserStatuses */
INSERT INTO `shaka`.`UserStatuses` (ID, NAME) VALUES (1001, "Player");
INSERT INTO `shaka`.`UserStatuses` (ID, NAME) VALUES (1002, "User");
INSERT INTO `shaka`.`UserStatuses` (ID, NAME) VALUES (1003, "Inactive");

/* Initialize PlayerStatuses */
INSERT INTO `shaka`.`PlayerStatuses` (ID, NAME) VALUES (1001, "Playing");
INSERT INTO `shaka`.`PlayerStatuses` (ID, NAME) VALUES (1002, "Paused");
INSERT INTO `shaka`.`PlayerStatuses` (ID, NAME) VALUES (1003, "Stopped");
INSERT INTO `shaka`.`PlayerStatuses` (ID, NAME) VALUES (1004, "Error");
