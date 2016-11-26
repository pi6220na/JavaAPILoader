-- MySQL Workbench Forward Engineering

SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0;
SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0;
SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='TRADITIONAL,ALLOW_INVALID_DATES';

-- -----------------------------------------------------
-- Schema java_api
-- -----------------------------------------------------

-- -----------------------------------------------------
-- Schema java_api
-- -----------------------------------------------------
CREATE SCHEMA IF NOT EXISTS `java_api` DEFAULT CHARACTER SET utf8 ;
USE `java_api` ;

-- -----------------------------------------------------
-- Table `java_api`.`package`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `java_api`.`package` ;

CREATE TABLE IF NOT EXISTS `java_api`.`package` (
  `package_ID` VARCHAR(36) NOT NULL,
  `name` VARCHAR(200) NULL,
  `description` VARCHAR(400) NULL,
  PRIMARY KEY (`package_ID`),
  UNIQUE INDEX `package_ID_UNIQUE` (`package_ID` ASC))
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `java_api`.`klass`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `java_api`.`klass` ;

CREATE TABLE IF NOT EXISTS `java_api`.`klass` (
  `klass_ID` VARCHAR(36) NOT NULL,
  `type_flag` TINYINT(1) NULL COMMENT 'type_flag: 1=class, 2=interface, 3=nested class, 4=nested interface',
  `name` VARCHAR(200) NULL,
  `summary` VARCHAR(400) NULL,
  `k_package_ID_fk` VARCHAR(36) NULL,
  PRIMARY KEY (`klass_ID`),
  UNIQUE INDEX `klass_ID_UNIQUE` (`klass_ID` ASC),
  INDEX `package_ID_fk_idx` (`k_package_ID_fk` ASC),
  CONSTRAINT `k_package_ID_fk`
    FOREIGN KEY (`k_package_ID_fk`)
    REFERENCES `java_api`.`package` (`package_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `java_api`.`enums`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `java_api`.`enums` ;

CREATE TABLE IF NOT EXISTS `java_api`.`enums` (
  `enums_ID` VARCHAR(36) NOT NULL,
  `name` VARCHAR(200) NULL,
  `summary` VARCHAR(400) NULL,
  `detail` VARCHAR(1000) NULL,
  `e_package_ID_fk` VARCHAR(36) NULL,
  PRIMARY KEY (`enums_ID`),
  UNIQUE INDEX `enums_ID_UNIQUE` (`enums_ID` ASC),
  INDEX `package_ID_fk_idx` (`e_package_ID_fk` ASC),
  CONSTRAINT `e_package_ID_fk`
    FOREIGN KEY (`e_package_ID_fk`)
    REFERENCES `java_api`.`package` (`package_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `java_api`.`annotation`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `java_api`.`annotation` ;

CREATE TABLE IF NOT EXISTS `java_api`.`annotation` (
  `annotation_ID` VARCHAR(36) NOT NULL,
  `name` VARCHAR(200) NULL,
  `summary` VARCHAR(400) NULL,
  `detail` VARCHAR(1000) NULL,
  `a_package_ID_fk` VARCHAR(36) NULL,
  PRIMARY KEY (`annotation_ID`),
  UNIQUE INDEX `annotation_ID_UNIQUE` (`annotation_ID` ASC),
  INDEX `package_ID_fk_idx` (`a_package_ID_fk` ASC),
  CONSTRAINT `a_package_ID_fk`
    FOREIGN KEY (`a_package_ID_fk`)
    REFERENCES `java_api`.`package` (`package_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `java_api`.`method`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `java_api`.`method` ;

CREATE TABLE IF NOT EXISTS `java_api`.`method` (
  `method_ID` VARCHAR(36) NOT NULL,
  `modifier` VARCHAR(100) NULL,
  `name` VARCHAR(200) NULL,
  `summary` VARCHAR(400) NULL,
  `detail` VARCHAR(1000) NULL,
  `m_klass_ID_fk` VARCHAR(36) NULL,
  PRIMARY KEY (`method_ID`),
  UNIQUE INDEX `method_ID_UNIQUE` (`method_ID` ASC),
  INDEX `klass_ID_fk_idx` (`m_klass_ID_fk` ASC),
  CONSTRAINT `m_klass_ID_fk`
    FOREIGN KEY (`m_klass_ID_fk`)
    REFERENCES `java_api`.`klass` (`klass_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `java_api`.`constructor`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `java_api`.`constructor` ;

CREATE TABLE IF NOT EXISTS `java_api`.`constructor` (
  `constructor_ID` VARCHAR(36) NOT NULL,
  `name` VARCHAR(200) NULL,
  `summary` VARCHAR(400) NULL,
  `detail` VARCHAR(1000) NULL,
  `c_klass_ID_fk` VARCHAR(36) NULL,
  PRIMARY KEY (`constructor_ID`),
  UNIQUE INDEX `constructor_ID_UNIQUE` (`constructor_ID` ASC),
  INDEX `klass_ID_fk_idx` (`c_klass_ID_fk` ASC),
  CONSTRAINT `c_klass_ID_fk`
    FOREIGN KEY (`c_klass_ID_fk`)
    REFERENCES `java_api`.`klass` (`klass_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `java_api`.`field`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `java_api`.`field` ;

CREATE TABLE IF NOT EXISTS `java_api`.`field` (
  `field_ID` VARCHAR(36) NOT NULL,
  `name` VARCHAR(200) NULL,
  `summary` VARCHAR(400) NULL,
  `detail` VARCHAR(1000) NULL,
  `f_klass_ID_fk` VARCHAR(36) NULL,
  PRIMARY KEY (`field_ID`),
  UNIQUE INDEX `field_ID_UNIQUE` (`field_ID` ASC),
  INDEX `klass_ID_fk_idx` (`f_klass_ID_fk` ASC),
  CONSTRAINT `f_klass_ID_fk`
    FOREIGN KEY (`f_klass_ID_fk`)
    REFERENCES `java_api`.`klass` (`klass_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `java_api`.`exception`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `java_api`.`exception` ;

CREATE TABLE IF NOT EXISTS `java_api`.`exception` (
  `exception_ID` VARCHAR(36) NOT NULL,
  `name` VARCHAR(200) NULL,
  `summary` VARCHAR(400) NULL,
  `detail` VARCHAR(1000) NULL,
  `x_package_ID_fk` VARCHAR(36) NULL,
  PRIMARY KEY (`exception_ID`),
  UNIQUE INDEX `exception_ID_UNIQUE` (`exception_ID` ASC),
  INDEX `x_package_ID_fk_idx` (`x_package_ID_fk` ASC),
  CONSTRAINT `x_package_ID_fk`
    FOREIGN KEY (`x_package_ID_fk`)
    REFERENCES `java_api`.`package` (`package_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;


-- -----------------------------------------------------
-- Table `java_api`.`errors`
-- -----------------------------------------------------
DROP TABLE IF EXISTS `java_api`.`errors` ;

CREATE TABLE IF NOT EXISTS `java_api`.`errors` (
  `errors_ID` VARCHAR(36) NOT NULL,
  `name` VARCHAR(200) NULL,
  `summary` VARCHAR(400) NULL,
  `detail` VARCHAR(1000) NULL,
  `r_package_ID_fk` VARCHAR(36) NULL,
  PRIMARY KEY (`errors_ID`),
  UNIQUE INDEX `errors_ID_UNIQUE` (`errors_ID` ASC),
  INDEX `r_package_ID_fk_idx` (`r_package_ID_fk` ASC),
  CONSTRAINT `r_package_ID_fk`
    FOREIGN KEY (`r_package_ID_fk`)
    REFERENCES `java_api`.`package` (`package_ID`)
    ON DELETE NO ACTION
    ON UPDATE NO ACTION)
ENGINE = InnoDB;

USE `java_api`;

DELIMITER $$

USE `java_api`$$
DROP TRIGGER IF EXISTS `java_api`.`package_BEFORE_INSERT` $$
USE `java_api`$$
CREATE DEFINER = CURRENT_USER TRIGGER `java_api`.`package_BEFORE_INSERT` BEFORE INSERT ON `package` FOR EACH ROW
BEGIN
	SET NEW.package_id = UUID(); 
END$$


USE `java_api`$$
DROP TRIGGER IF EXISTS `java_api`.`klass_BEFORE_INSERT` $$
USE `java_api`$$
CREATE DEFINER = CURRENT_USER TRIGGER `java_api`.`klass_BEFORE_INSERT` BEFORE INSERT ON `klass` FOR EACH ROW
BEGIN
	SET NEW.klass_id = UUID(); 
END$$


USE `java_api`$$
DROP TRIGGER IF EXISTS `java_api`.`enums_BEFORE_INSERT` $$
USE `java_api`$$
CREATE DEFINER = CURRENT_USER TRIGGER `java_api`.`enums_BEFORE_INSERT` BEFORE INSERT ON `enums` FOR EACH ROW
BEGIN
	SET NEW.enums_id = UUID(); 
END$$


USE `java_api`$$
DROP TRIGGER IF EXISTS `java_api`.`annotation_BEFORE_INSERT` $$
USE `java_api`$$
CREATE DEFINER = CURRENT_USER TRIGGER `java_api`.`annotation_BEFORE_INSERT` BEFORE INSERT ON `annotation` FOR EACH ROW
BEGIN
	SET NEW.annotation_id = UUID(); 
END$$


USE `java_api`$$
DROP TRIGGER IF EXISTS `java_api`.`method_BEFORE_INSERT` $$
USE `java_api`$$
CREATE DEFINER = CURRENT_USER TRIGGER `java_api`.`method_BEFORE_INSERT` BEFORE INSERT ON `method` FOR EACH ROW
BEGIN
	SET NEW.method_id = UUID(); 
END$$


USE `java_api`$$
DROP TRIGGER IF EXISTS `java_api`.`constructor_BEFORE_INSERT` $$
USE `java_api`$$
CREATE DEFINER = CURRENT_USER TRIGGER `java_api`.`constructor_BEFORE_INSERT` BEFORE INSERT ON `constructor` FOR EACH ROW
BEGIN
	SET NEW.constructor_id = UUID(); 
END$$


USE `java_api`$$
DROP TRIGGER IF EXISTS `java_api`.`field_BEFORE_INSERT` $$
USE `java_api`$$
CREATE DEFINER = CURRENT_USER TRIGGER `java_api`.`field_BEFORE_INSERT` BEFORE INSERT ON `field` FOR EACH ROW
BEGIN
	SET NEW.field_id = UUID(); 
END$$


USE `java_api`$$
DROP TRIGGER IF EXISTS `java_api`.`exception_BEFORE_INSERT` $$
USE `java_api`$$
CREATE DEFINER = CURRENT_USER TRIGGER `java_api`.`exception_BEFORE_INSERT` BEFORE INSERT ON `exception` FOR EACH ROW
BEGIN
	SET NEW.exception_id = UUID(); 
END$$


USE `java_api`$$
DROP TRIGGER IF EXISTS `java_api`.`errors_BEFORE_INSERT` $$
USE `java_api`$$
CREATE DEFINER = CURRENT_USER TRIGGER `java_api`.`errors_BEFORE_INSERT` BEFORE INSERT ON `errors` FOR EACH ROW
BEGIN
	SET NEW.errors_id = UUID(); 
END$$


DELIMITER ;

SET SQL_MODE=@OLD_SQL_MODE;
SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS;
SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS;
