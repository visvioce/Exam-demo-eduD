-- 为 exam_sessions 表添加 version 列（乐观锁）
-- 用于防止考试提交时的并发问题

ALTER TABLE exam_sessions ADD COLUMN version INT NOT NULL DEFAULT 0 COMMENT '乐观锁版本号' AFTER answers;
