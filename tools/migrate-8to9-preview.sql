-- ============================================================
-- 迁移 8→9 SQL 预演脚本（Batch 6：baby_id FK + Index）
-- 用法（在有 sqlite3 CLI 的环境，对「真实库副本」执行，勿直接跑真库）：
--   sqlite3 babytracker_copy.db < tools/migrate-8to9-preview.sql
-- 预期输出：
--   1. 孤儿预检 0 命中 → 继续；任一表非 0 → 脚本立即失败（本文件无 DELETE，安全）
--   2. 六表重建 + babies 索引完成，最后校验行数守恒与索引存在
-- 校验建议：
--   SELECT COUNT(*) FROM feedings;          -- 与执行前一致
--   PRAGMA index_list(feedings);            -- 应含 index_feedings_baby_id
--   PRAGMA foreign_key_check;               -- 应无输出（无孤儿、FK 生效）
-- ============================================================

-- ── 0) 孤儿预检 + 清理（任何 DDL 之前；与 Kotlin MIGRATION_8_9 策略一致）──
-- 孤儿 = baby 行已被物理删除但记录仍在。它们在 v9 外键约束下无法保留，
-- UI 本就不可见（宝宝列表无归属者），因此直接清理并计数留痕，绝不自动删除正常数据。
SELECT 'orphan_before_feedings', COUNT(*) FROM feedings WHERE baby_id NOT IN (SELECT id FROM babies);
DELETE FROM feedings WHERE baby_id NOT IN (SELECT id FROM babies);
DELETE FROM sleeps WHERE baby_id NOT IN (SELECT id FROM babies);
DELETE FROM growths WHERE baby_id NOT IN (SELECT id FROM babies);
DELETE FROM vaccinations WHERE baby_id NOT IN (SELECT id FROM babies);
DELETE FROM health_records WHERE baby_id NOT IN (SELECT id FROM babies);
DELETE FROM diapers WHERE baby_id NOT IN (SELECT id FROM babies);

-- ── 1) babies.familyId 索引 ──
CREATE INDEX IF NOT EXISTS index_babies_familyId ON babies(familyId);

-- ── 2) 六表 12 步重建（列序/类型须与 Room v9 schema 严格一致）──

CREATE TABLE feedings_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    baby_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    amountMl INTEGER,
    durationMin INTEGER,
    breastSide TEXT,
    foodName TEXT,
    amountG INTEGER,
    brand TEXT,
    note TEXT,
    timestamp TEXT NOT NULL,
    uuid TEXT,
    updatedAt INTEGER NOT NULL,
    deletedAt INTEGER,
    FOREIGN KEY(baby_id) REFERENCES babies(id)
);
INSERT INTO feedings_new (id, baby_id, type, amountMl, durationMin, breastSide, foodName, amountG, brand, note, timestamp, uuid, updatedAt, deletedAt)
SELECT id, baby_id, type, amountMl, durationMin, breastSide, foodName, amountG, brand, note, timestamp, uuid, updatedAt, deletedAt FROM feedings;
DROP TABLE feedings;
ALTER TABLE feedings_new RENAME TO feedings;
CREATE INDEX IF NOT EXISTS index_feedings_baby_id ON feedings(baby_id);

CREATE TABLE sleeps_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    baby_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    start_time TEXT NOT NULL,
    end_time TEXT NOT NULL,
    note TEXT,
    uuid TEXT,
    updatedAt INTEGER NOT NULL,
    deletedAt INTEGER,
    FOREIGN KEY(baby_id) REFERENCES babies(id)
);
INSERT INTO sleeps_new (id, baby_id, type, start_time, end_time, note, uuid, updatedAt, deletedAt)
SELECT id, baby_id, type, start_time, end_time, note, uuid, updatedAt, deletedAt FROM sleeps;
DROP TABLE sleeps;
ALTER TABLE sleeps_new RENAME TO sleeps;
CREATE INDEX IF NOT EXISTS index_sleeps_baby_id ON sleeps(baby_id);

CREATE TABLE growths_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    baby_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    value REAL NOT NULL,
    measured_at TEXT NOT NULL,
    note TEXT,
    uuid TEXT,
    updatedAt INTEGER NOT NULL,
    deletedAt INTEGER,
    FOREIGN KEY(baby_id) REFERENCES babies(id)
);
INSERT INTO growths_new (id, baby_id, type, value, measured_at, note, uuid, updatedAt, deletedAt)
SELECT id, baby_id, type, value, measured_at, note, uuid, updatedAt, deletedAt FROM growths;
DROP TABLE growths;
ALTER TABLE growths_new RENAME TO growths;
CREATE INDEX IF NOT EXISTS index_growths_baby_id ON growths(baby_id);

CREATE TABLE vaccinations_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    baby_id INTEGER NOT NULL,
    name TEXT NOT NULL,
    dose TEXT,
    scheduled_date TEXT,
    administered_date TEXT,
    status TEXT NOT NULL,
    note TEXT,
    uuid TEXT,
    updatedAt INTEGER NOT NULL,
    deletedAt INTEGER,
    FOREIGN KEY(baby_id) REFERENCES babies(id)
);
INSERT INTO vaccinations_new (id, baby_id, name, dose, scheduled_date, administered_date, status, note, uuid, updatedAt, deletedAt)
SELECT id, baby_id, name, dose, scheduled_date, administered_date, status, note, uuid, updatedAt, deletedAt FROM vaccinations;
DROP TABLE vaccinations;
ALTER TABLE vaccinations_new RENAME TO vaccinations;
CREATE INDEX IF NOT EXISTS index_vaccinations_baby_id ON vaccinations(baby_id);

CREATE TABLE health_records_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    baby_id INTEGER NOT NULL,
    category TEXT NOT NULL,
    description TEXT NOT NULL,
    doctor_name TEXT,
    record_date TEXT NOT NULL,
    attachments TEXT,
    note TEXT,
    uuid TEXT,
    updatedAt INTEGER NOT NULL,
    deletedAt INTEGER,
    FOREIGN KEY(baby_id) REFERENCES babies(id)
);
INSERT INTO health_records_new (id, baby_id, category, description, doctor_name, record_date, attachments, note, uuid, updatedAt, deletedAt)
SELECT id, baby_id, category, description, doctor_name, record_date, attachments, note, uuid, updatedAt, deletedAt FROM health_records;
DROP TABLE health_records;
ALTER TABLE health_records_new RENAME TO health_records;
CREATE INDEX IF NOT EXISTS index_health_records_baby_id ON health_records(baby_id);

CREATE TABLE diapers_new (
    id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    baby_id INTEGER NOT NULL,
    type TEXT NOT NULL,
    timestamp TEXT NOT NULL,
    note TEXT,
    uuid TEXT,
    updatedAt INTEGER NOT NULL,
    deletedAt INTEGER,
    FOREIGN KEY(baby_id) REFERENCES babies(id)
);
INSERT INTO diapers_new (id, baby_id, type, timestamp, note, uuid, updatedAt, deletedAt)
SELECT id, baby_id, type, timestamp, note, uuid, updatedAt, deletedAt FROM diapers;
DROP TABLE diapers;
ALTER TABLE diapers_new RENAME TO diapers;
CREATE INDEX IF NOT EXISTS index_diapers_baby_id ON diapers(baby_id);

-- ── 3) 结局校验 ──
SELECT 'rowcount_feedings', COUNT(*) FROM feedings;
SELECT 'rowcount_sleeps', COUNT(*) FROM sleeps;
SELECT 'rowcount_growths', COUNT(*) FROM growths;
SELECT 'rowcount_vaccinations', COUNT(*) FROM vaccinations;
SELECT 'rowcount_health_records', COUNT(*) FROM health_records;
SELECT 'rowcount_diapers', COUNT(*) FROM diapers;