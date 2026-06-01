# 数据库设计

## 1. ER 图 (文本描述)

```
babies (1) ──┬── (N) feeding_records
             ├── (N) sleep_records
             ├── (N) growth_records
             ├── (N) vaccines
             ├── (N) health_profiles (1:1)
             └── (N) reminders
```

## 2. 实体定义

### BabyEntity

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK, auto) | 主键 |
| name | String | 宝宝姓名 |
| gender | String | MALE / FEMALE |
| birthday | Long (timestamp) | 出生日期 |
| avatar | String? | 头像 URI |
| birthHeight | Float | 出生身高(cm) |
| birthWeight | Float | 出生体重(kg) |
| note | String? | 备注 |

### FeedingEntity

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK, auto) | 主键 |
| babyId | Long (FK→babies.id) | 宝宝 ID |
| type | String | BREAST_MILK / FORMULA / SOLID_FOOD / WATER |
| amount | Int | 用量 (ml 或 g) |
| unit | String | ml / g |
| note | String? | 备注 |
| createdAt | Long (timestamp) | 记录时间 |

### SleepEntity

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK, auto) | 主键 |
| babyId | Long (FK→babies.id) | 宝宝 ID |
| startTime | Long (timestamp) | 入睡时间 |
| endTime | Long (timestamp) | 醒来时间 |
| type | String | NIGHT (夜间) / NAP (小睡) |

### GrowthEntity

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK, auto) | 主键 |
| babyId | Long (FK→babies.id) | 宝宝 ID |
| height | Float | 身高 (cm) |
| weight | Float | 体重 (kg) |
| headCircumference | Float | 头围 (cm) |
| recordDate | Long (timestamp) | 测量日期 |

### VaccineEntity

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK, auto) | 主键 |
| babyId | Long (FK→babies.id) | 宝宝 ID |
| name | String | 疫苗名称 |
| dose | Int | 第几针 |
| plannedDate | Long (timestamp) | 计划接种日期 |
| completedDate | Long? (timestamp) | 实际接种日期 |
| status | String | PENDING / COMPLETED / EXPIRED |

### HealthProfileEntity

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK, auto) | 主键 |
| babyId | Long (FK→babies.id) | 宝宝 ID |
| allergies | String? | 过敏史 (JSON array) |
| medicalHistory | String? | 既往病史 (JSON array) |
| doctorNotes | String? | 医生备注 |

### ReminderEntity

| 字段 | 类型 | 说明 |
|------|------|------|
| id | Long (PK, auto) | 主键 |
| babyId | Long (FK→babies.id) | 宝宝 ID |
| type | String | VACCINE / CHECKUP / MEDICATION |
| title | String | 提醒标题 |
| scheduledAt | Long (timestamp) | 提醒时间 |
| enabled | Boolean | 是否启用 |

## 3. 索引

```sql
-- Feeding
CREATE INDEX idx_feeding_baby_time ON feeding_records(babyId, createdAt DESC);

-- Sleep
CREATE INDEX idx_sleep_baby_time ON sleep_records(babyId, startTime DESC);

-- Growth
CREATE INDEX idx_growth_baby_date ON growth_records(babyId, recordDate DESC);

-- Vaccines
CREATE INDEX idx_vaccine_baby_status ON vaccines(babyId, status);
```

注：Room 通过 `@Query` 中的 `ORDER BY` 子句自动生成索引建议。在 V1 版本中小数据量下不创建独立索引，V2 大数据量时通过 Migration 添加。

## 4. DAO 接口设计

所有 DAO 遵循统一模式：

- 查询: `fun getAllByBabyFlow(babyId: Long = 1): Flow<List<Entity>>`
- 写入: `suspend fun insert(entity: Entity)`
- 删除: `suspend fun delete(entity: Entity)` / `suspend fun deleteById(id: Long)`
- 更新: `suspend fun update(entity: Entity)` 或 `@Query UPDATE`

## 5. 数据库版本管理

- V1: 初始版本 (当前)
- V2+: 通过 Migration 而非 fallbackToDestructiveMigration

```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "baby_tracker_v2.db")
    .addMigrations(MIGRATION_1_2)
    .build()
```
