DROP TABLE IF EXISTS `sha_dublinkernels` ;

CREATE TABLE sha_dublinkernels (
id INT UNSIGNED NOT NULL PRIMARY KEY,
value VARCHAR(20000) NOT NULL,
FULLTEXT (value)
) ENGINE = MyISAM;

DELIMITER //
DROP TRIGGER IF EXISTS insert_fulltext//
CREATE TRIGGER insert_fulltext
AFTER INSERT ON inv_dublinkernels
FOR EACH ROW
BEGIN
  IF NOT NEW.value='(:unas)' THEN
    INSERT INTO sha_dublinkernels
    VALUES (NEW.id, NEW.value);
  END IF;
END;
//
DROP TRIGGER IF EXISTS update_fulltext//
CREATE TRIGGER update_fulltext
AFTER UPDATE ON inv_dublinkernels
FOR EACH ROW
BEGIN
  IF NEW.value!='(:unas)' THEN
    UPDATE sha_dublinkernels
    SET value = NEW.value
    WHERE id = NEW.id;
  END IF;
END;
//
DROP TRIGGER IF EXISTS delete_fulltext//
CREATE TRIGGER delete_fulltext
AFTER DELETE ON inv_dublinkernels
FOR EACH ROW
BEGIN
  DELETE FROM sha_dublinkernels
  WHERE id = OLD.id;
END;
//
DELIMITER ;

insert into sha_dublinkernels select id, value from inv_dublinkernels where not inv_dublinkernels.element='where' and not inv_dublinkernels.value='(:unas)'
