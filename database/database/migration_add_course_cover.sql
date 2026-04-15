USE exam_system;

SET @ddl = (
  SELECT IF(
    COUNT(*) = 0,
    'ALTER TABLE courses ADD COLUMN cover_url VARCHAR(500) COMMENT ''课程封面URL'' AFTER description',
    'SELECT ''cover_url already exists'' AS msg'
  )
  FROM information_schema.columns
  WHERE table_schema = 'exam_system'
    AND table_name = 'courses'
    AND column_name = 'cover_url'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
