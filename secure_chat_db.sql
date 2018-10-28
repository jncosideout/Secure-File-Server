-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema mydb
-- -----------------------------------------------------
SHOW WARNINGS;
-- -----------------------------------------------------
-- Schema secure_chat_db
-- -----------------------------------------------------
DROP SCHEMA IF EXISTS `secure_chat_db` ;

-- -----------------------------------------------------
-- Schema secure_chat_db
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `secure_chat_db` DEFAULT CHARACTER SET utf8 ;
SHOW WARNINGS;
USE `secure_chat_db` ;

-- -----------------------------------------------------
-- Table `secure_chat_db`.`user_account`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `secure_chat_db`.`user_account` ;

SHOW WARNINGS;
CREATE TABLE IF NOT EXISTS `secure_chat_db`.`user_account` (
  `id` INT(11) NOT NULL AUTO_INCREMENT,
  `user_name` VARCHAR(45) NULL DEFAULT NULL,
  `email` VARCHAR(45) NULL DEFAULT NULL,
  `iterations` INT(11) NULL DEFAULT NULL,
  `salt` VARCHAR(45) NULL DEFAULT NULL,
  `hash` VARCHAR(256) NULL DEFAULT NULL,
  `hash_algorithm` VARCHAR(45) NULL DEFAULT NULL,
  `SHA256_fingerprints` VARCHAR(95) NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE INDEX `email_UNIQUE` (`email` ASC),
  UNIQUE INDEX `user_name_UNIQUE` (`user_name` ASC),
  UNIQUE INDEX `cert_fingerprints_UNIQUE` (`SHA256_fingerprints` ASC))
ENGINE = InnoDB
AUTO_INCREMENT = 102
DEFAULT CHARACTER SET = utf8;

SHOW WARNINGS;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
