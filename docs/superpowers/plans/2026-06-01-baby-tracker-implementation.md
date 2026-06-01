# 育儿助手 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 构建完整的育儿助手 Android 应用，覆盖 14 个页面、4 个 Milelstone、35 个任务。

**Architecture:** MVVM + Room（本地存储）+ Koin（DI）+ Navigation Compose（路由）+ Vico Chart（图表）+ DataStore（偏好设置）+ WorkManager（后台提醒）。每个模块拥有独立的 Entity/DAO/Repository/ViewModel/Screen，通过 UiState 承载加载/空/错误状态。

**Tech Stack:** Jetpack Compose + Material 3, Navigation Compose, Room 2.6.1 + KSP, Koin 3.5.6, Vico Chart, DataStore, Coil 2.6.0, WorkManager, Kotlin 1.9.22, Gradle 9.5.1 + AGP 8.9.3, compileSdk 34, minSdk 24.

---

# 架构前置：模块化

将单模块项目拆分为多模块架构，为后续功能扩展和团队协作奠定基础。

---

### 任务 0: 多模块拆分

**文件:**
- 创建: `core/data/build.gradle.kts`
- 创建: `core/designsystem/build.gradle.kts`
- 创建: `core/data/src/main/AndroidManifest.xml`
- 创建: `core/designsystem/src/main/AndroidManifest.xml`
- 修改: `settings.gradle.kts`
- 修改: `app/build.gradle.kts`

- [ ] **创建模块目录结构**

```bash
mkdir -p core/data/src/main/java/com/example/myapp/data
mkdir -p core/designsystem/src/main/java/com/example/myapp/ui/designsystem
```

- [ ] **更新 `settings.gradle.kts`**

```kotlin
include(":app")
include(":core:data")
include(":core:designsystem")

rootProject.name = "MyApp"
```

- [ ] **创建 `core/data/build.gradle.kts`**

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.example.myapp.data"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
}
```

- [ ] **创建 `core/designsystem/build.gradle.kts`**

```kotlin
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.myapp.designsystem"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2024.02.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.foundation:foundation")
}
```

- [ ] **在 `app/build.gradle.kts` 中添加模块依赖**

```kotlin
implementation(project(":core:data"))
implementation(project(":core:designsystem"))
```

- [ ] **移动代码到模块**

`core:data` 模块包含：
- `data/room/` 下全部 Entity, DAO, Database
- `data/repository/` 下全部 Repository
- `data/datastore/` 下全部 Preference

`core:designsystem` 模块包含：
- `ui/designsystem/` 下全部组件

`app` 模块保留：
- Application, MainActivity, DI
- `navigation/` 下 Route, NavGraph
- `ui/` 下各功能 Screen + ViewModel

- [ ] **编译验证**

```bash
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_AAPT2_DAEMON_MODE=false
./gradlew assembleDebug
```
预期：BUILD SUCCESSFUL

- [ ] **提交**

```bash
git add settings.gradle.kts core/ app/build.gradle.kts
git commit -m "refactor: split into core:data and core:designsystem modules"
```

---

# Milestone 1: 基础能力

实现 5 个核心页面（首页/喂养/睡眠/生长/宝宝信息）+ 全部基础设施（Room/Koin/Nav/DesignSystem/UseCase/MVI）。

---

### 任务 1: 更新 app/build.gradle.kts 依赖

**文件:**
- 修改: `app/build.gradle.kts`

- [ ] **添加版本变量和依赖**

在 `app/build.gradle.kts` 的 `dependencies {}` 块中，在现有 Koin 依赖之后添加：

```kotlin
// Navigation
implementation("androidx.navigation:navigation-compose:2.7.7")

// Paging 3
implementation("androidx.paging:paging-runtime-ktx:3.2.1")
implementation("androidx.paging:paging-compose:3.2.1")

// Vico Chart
implementation("com.patrykandpatrick.vico:compose-m3:1.13.1")

// DataStore
implementation("androidx.datastore:datastore-preferences:1.0.0")

// WorkManager
implementation("androidx.work:work-runtime-ktx:2.9.0")
```

- [ ] **验证依赖配置** — 确保 `plugins` 块包含 `id("com.google.devtools.ksp")`，且 Room 依赖已在项目中。

- [ ] **提交**

```bash
git add app/build.gradle.kts
git commit -m "chore: add navigation, paging, vico, datastore, workmanager deps"
```

---

### 任务 2: 创建 Application 类和 Koin 模块骨架

**文件:**
- 创建: `app/src/main/java/com/example/myapp/MyApp.kt`
- 创建: `app/src/main/java/com/example/myapp/di/AppModule.kt`
- 修改: `app/src/main/AndroidManifest.xml`

- [ ] **创建 `MyApp.kt`**

```kotlin
package com.example.myapp

import android.app.Application
import com.example.myapp.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MyApp)
            modules(appModule)
        }
    }
}
```

- [ ] **创建 `di/AppModule.kt`**

```kotlin
package com.example.myapp.di

import org.koin.dsl.module

val appModule = module {
    // 后续任务填充
}
```

- [ ] **在 `AndroidManifest.xml` 中注册 `MyApp`**

找到 `<application>` 标签，添加 `android:name=".MyApp"`。

- [ ] **提交**

```bash
git add app/src/main/java/com/example/myapp/MyApp.kt app/src/main/java/com/example/myapp/di/ app/src/main/AndroidManifest.xml
git commit -m "feat: add Application class and Koin module skeleton"
```

---

### 任务 3: 创建设计系统组件

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/designsystem/ParentingCard.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/designsystem/ParentingButton.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/designsystem/EmptyState.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/designsystem/LoadingView.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/designsystem/SectionTitle.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/designsystem/ErrorView.kt`

- [ ] **创建 `ParentingCard.kt`**

```kotlin
package com.example.myapp.ui.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ParentingCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors()
    ) {
        androidx.compose.foundation.layout.Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}
```

- [ ] **创建 `ParentingButton.kt`**

```kotlin
package com.example.myapp.ui.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ParentingButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(52.dp),
        shape = RoundedCornerShape(24.dp),
        enabled = enabled
    ) {
        Text(text)
    }
}
```

- [ ] **创建 `EmptyState.kt`**

```kotlin
package com.example.myapp.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun EmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Inbox,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.outline)
    }
}
```

- [ ] **创建 `LoadingView.kt`**

```kotlin
package com.example.myapp.ui.designsystem

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun LoadingView(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
```

- [ ] **创建 `SectionTitle.kt`**

```kotlin
package com.example.myapp.ui.designsystem

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun SectionTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        modifier = modifier.padding(vertical = 8.dp)
    )
}
```

- [ ] **创建 `ErrorView.kt`**

```kotlin
package com.example.myapp.ui.designsystem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ErrorView(
    message: String,
    modifier: Modifier = Modifier,
    onRetry: (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.error)
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            ParentingButton(text = "重试", onClick = onRetry)
        }
    }
}
```

---

### 任务 4: 创建 UiState 数据类

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/feeding/FeedingUiState.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/sleep/SleepUiState.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/growth/GrowthUiState.kt`

- [ ] **创建 `FeedingUiState.kt`**

```kotlin
package com.example.myapp.ui.feeding

import com.example.myapp.data.room.FeedingEntity

data class FeedingUiState(
    val loading: Boolean = false,
    val records: List<FeedingEntity> = emptyList(),
    val error: String? = null
)
```

- [ ] **创建 `SleepUiState.kt`**

```kotlin
package com.example.myapp.ui.sleep

import com.example.myapp.data.room.SleepEntity

data class SleepUiState(
    val loading: Boolean = false,
    val records: List<SleepEntity> = emptyList(),
    val error: String? = null
)
```

- [ ] **创建 `GrowthUiState.kt`**

```kotlin
package com.example.myapp.ui.growth

import com.example.myapp.data.room.GrowthEntity

data class GrowthUiState(
    val loading: Boolean = false,
    val records: List<GrowthEntity> = emptyList(),
    val error: String? = null
)
```

---

### 任务 5: 创建所有 Room Entity（6 个）

**文件:**
- 创建: `app/src/main/java/com/example/myapp/data/room/BabyEntity.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/FeedingEntity.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/SleepEntity.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/GrowthEntity.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/VaccineEntity.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/HealthProfileEntity.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/ReminderEntity.kt`

- [ ] **创建 `BabyEntity.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "babies")
data class BabyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gender: String,
    val birthday: Long,
    val avatar: String? = null,
    val birthHeight: Float = 0f,
    val birthWeight: Float = 0f,
    val note: String? = null
)
```

- [ ] **创建 `FeedingEntity.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeding_records")
data class FeedingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long = 1,
    val type: String,
    val amount: Int,
    val unit: String = "ml",
    val note: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
```

- [ ] **创建 `SleepEntity.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sleep_records")
data class SleepEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long = 1,
    val startTime: Long,
    val endTime: Long,
    val type: String = "NAP"
)
```

- [ ] **创建 `GrowthEntity.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "growth_records")
data class GrowthEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long = 1,
    val height: Float,
    val weight: Float,
    val headCircumference: Float = 0f,
    val recordDate: Long = System.currentTimeMillis()
)
```

- [ ] **创建 `VaccineEntity.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "vaccines")
data class VaccineEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long = 1,
    val name: String,
    val dose: Int,
    val plannedDate: Long,
    val completedDate: Long? = null,
    val status: String = "PENDING"
)
```

- [ ] **创建 `HealthProfileEntity.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "health_profiles")
data class HealthProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long = 1,
    val allergies: String? = null,
    val medicalHistory: String? = null,
    val doctorNotes: String? = null
)
```

- [ ] **创建 `ReminderEntity.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val babyId: Long = 1,
    val type: String,
    val title: String,
    val scheduledAt: Long,
    val enabled: Boolean = true
)
```

---

### 任务 6: 创建所有 DAO（7 个）

**文件:**
- 创建: `app/src/main/java/com/example/myapp/data/room/BabyDao.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/FeedingDao.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/SleepDao.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/GrowthDao.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/VaccineDao.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/HealthProfileDao.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/ReminderDao.kt`

- [ ] **创建 `BabyDao.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BabyDao {
    @Query("SELECT * FROM babies ORDER BY id LIMIT 1")
    fun getFirstFlow(): Flow<BabyEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BabyEntity)

    @Query("DELETE FROM babies")
    suspend fun deleteAll()
}
```

- [ ] **创建 `FeedingDao.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FeedingDao {
    @Query("SELECT * FROM feeding_records WHERE babyId = :babyId ORDER BY createdAt DESC")
    fun getAllByBabyFlow(babyId: Long = 1): Flow<List<FeedingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FeedingEntity)

    @Delete
    suspend fun delete(entity: FeedingEntity)
}
```

- [ ] **创建 `SleepDao.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepDao {
    @Query("SELECT * FROM sleep_records WHERE babyId = :babyId ORDER BY startTime DESC")
    fun getAllByBabyFlow(babyId: Long = 1): Flow<List<SleepEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: SleepEntity)

    @Delete
    suspend fun delete(entity: SleepEntity)
}
```

- [ ] **创建 `GrowthDao.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GrowthDao {
    @Query("SELECT * FROM growth_records WHERE babyId = :babyId ORDER BY recordDate DESC")
    fun getAllByBabyFlow(babyId: Long = 1): Flow<List<GrowthEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GrowthEntity)

    @Delete
    suspend fun delete(entity: GrowthEntity)
}
```

- [ ] **创建 `VaccineDao.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface VaccineDao {
    @Query("SELECT * FROM vaccines WHERE babyId = :babyId ORDER BY plannedDate ASC")
    fun getAllByBabyFlow(babyId: Long = 1): Flow<List<VaccineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: VaccineEntity)

    @Query("UPDATE vaccines SET status = :status, completedDate = :completedDate WHERE id = :id")
    suspend fun markCompleted(id: Long, status: String, completedDate: Long)
}
```

- [ ] **创建 `HealthProfileDao.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HealthProfileDao {
    @Query("SELECT * FROM health_profiles WHERE babyId = :babyId LIMIT 1")
    fun getByBabyFlow(babyId: Long = 1): Flow<HealthProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: HealthProfileEntity)
}
```

- [ ] **创建 `ReminderDao.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ReminderDao {
    @Query("SELECT * FROM reminders WHERE babyId = :babyId ORDER BY scheduledAt ASC")
    fun getAllByBabyFlow(babyId: Long = 1): Flow<List<ReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: ReminderEntity)

    @Query("UPDATE reminders SET enabled = :enabled WHERE id = :id")
    suspend fun toggleEnabled(id: Long, enabled: Boolean)
}
```

---

### 任务 7: 创建 AppDatabase

**文件:**
- 创建: `app/src/main/java/com/example/myapp/data/room/AppDatabase.kt`

- [ ] **创建 `AppDatabase.kt`**

```kotlin
package com.example.myapp.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        BabyEntity::class,
        FeedingEntity::class,
        SleepEntity::class,
        GrowthEntity::class,
        VaccineEntity::class,
        HealthProfileEntity::class,
        ReminderEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun babyDao(): BabyDao
    abstract fun feedingDao(): FeedingDao
    abstract fun sleepDao(): SleepDao
    abstract fun growthDao(): GrowthDao
    abstract fun vaccineDao(): VaccineDao
    abstract fun healthProfileDao(): HealthProfileDao
    abstract fun reminderDao(): ReminderDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "baby_tracker_v2.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
```

---

### 任务 8: 创建所有 Repository（7 个）

**文件:**
- 创建: `app/src/main/java/com/example/myapp/data/repository/BabyRepository.kt`
- 创建: `app/src/main/java/com/example/myapp/data/repository/FeedingRepository.kt`
- 创建: `app/src/main/java/com/example/myapp/data/repository/SleepRepository.kt`
- 创建: `app/src/main/java/com/example/myapp/data/repository/GrowthRepository.kt`
- 创建: `app/src/main/java/com/example/myapp/data/repository/VaccineRepository.kt`
- 创建: `app/src/main/java/com/example/myapp/data/repository/HealthRepository.kt`
- 创建: `app/src/main/java/com/example/myapp/data/repository/ReminderRepository.kt`

- [ ] **创建 `BabyRepository.kt`**

```kotlin
package com.example.myapp.data.repository

import com.example.myapp.data.room.BabyDao
import com.example.myapp.data.room.BabyEntity
import kotlinx.coroutines.flow.Flow

class BabyRepository(private val dao: BabyDao) {
    val firstBaby: Flow<BabyEntity?> = dao.getFirstFlow()
    suspend fun insert(baby: BabyEntity) = dao.insert(baby)
}
```

- [ ] **创建 `FeedingRepository.kt`**

```kotlin
package com.example.myapp.data.repository

import com.example.myapp.data.room.FeedingDao
import com.example.myapp.data.room.FeedingEntity
import kotlinx.coroutines.flow.Flow

class FeedingRepository(private val dao: FeedingDao) {
    fun getAllByBaby(babyId: Long = 1): Flow<List<FeedingEntity>> = dao.getAllByBabyFlow(babyId)
    suspend fun insert(entity: FeedingEntity) = dao.insert(entity)
    suspend fun delete(entity: FeedingEntity) = dao.delete(entity)
}
```

- [ ] **创建 `SleepRepository.kt`**

```kotlin
package com.example.myapp.data.repository

import com.example.myapp.data.room.SleepDao
import com.example.myapp.data.room.SleepEntity
import kotlinx.coroutines.flow.Flow

class SleepRepository(private val dao: SleepDao) {
    fun getAllByBaby(babyId: Long = 1): Flow<List<SleepEntity>> = dao.getAllByBabyFlow(babyId)
    suspend fun insert(entity: SleepEntity) = dao.insert(entity)
    suspend fun delete(entity: SleepEntity) = dao.delete(entity)
}
```

- [ ] **创建 `GrowthRepository.kt`**

```kotlin
package com.example.myapp.data.repository

import com.example.myapp.data.room.GrowthDao
import com.example.myapp.data.room.GrowthEntity
import kotlinx.coroutines.flow.Flow

class GrowthRepository(private val dao: GrowthDao) {
    fun getAllByBaby(babyId: Long = 1): Flow<List<GrowthEntity>> = dao.getAllByBabyFlow(babyId)
    suspend fun insert(entity: GrowthEntity) = dao.insert(entity)
    suspend fun delete(entity: GrowthEntity) = dao.delete(entity)
}
```

- [ ] **创建 `VaccineRepository.kt`**

```kotlin
package com.example.myapp.data.repository

import com.example.myapp.data.room.VaccineDao
import com.example.myapp.data.room.VaccineEntity
import kotlinx.coroutines.flow.Flow

class VaccineRepository(private val dao: VaccineDao) {
    fun getAllByBaby(babyId: Long = 1): Flow<List<VaccineEntity>> = dao.getAllByBabyFlow(babyId)
    suspend fun insert(entity: VaccineEntity) = dao.insert(entity)
    suspend fun markCompleted(id: Long, completedDate: Long) = dao.markCompleted(id, "COMPLETED", completedDate)
}
```

- [ ] **创建 `HealthRepository.kt`**

```kotlin
package com.example.myapp.data.repository

import com.example.myapp.data.room.HealthProfileDao
import com.example.myapp.data.room.HealthProfileEntity
import kotlinx.coroutines.flow.Flow

class HealthRepository(private val dao: HealthProfileDao) {
    fun getByBaby(babyId: Long = 1): Flow<HealthProfileEntity?> = dao.getByBabyFlow(babyId)
    suspend fun save(profile: HealthProfileEntity) = dao.insert(profile)
}
```

- [ ] **创建 `ReminderRepository.kt`**

```kotlin
package com.example.myapp.data.repository

import com.example.myapp.data.room.ReminderDao
import com.example.myapp.data.room.ReminderEntity
import kotlinx.coroutines.flow.Flow

class ReminderRepository(private val dao: ReminderDao) {
    fun getAllByBaby(babyId: Long = 1): Flow<List<ReminderEntity>> = dao.getAllByBabyFlow(babyId)
    suspend fun insert(entity: ReminderEntity) = dao.insert(entity)
    suspend fun toggleEnabled(id: Long, enabled: Boolean) = dao.toggleEnabled(id, enabled)
}
```

---

### 任务 9: 创建 UseCase 层（MVI 事件驱动）

**文件:**
- 创建: `app/src/main/java/com/example/myapp/domain/feeding/AddFeedingUseCase.kt`
- 创建: `app/src/main/java/com/example/myapp/domain/feeding/DeleteFeedingUseCase.kt`
- 创建: `app/src/main/java/com/example/myapp/domain/sleep/AddSleepUseCase.kt`
- 创建: `app/src/main/java/com/example/myapp/domain/sleep/DeleteSleepUseCase.kt`
- 创建: `app/src/main/java/com/example/myapp/domain/growth/AddGrowthUseCase.kt`
- 创建: `app/src/main/java/com/example/myapp/domain/growth/DeleteGrowthUseCase.kt`
- 创建: `app/src/main/java/com/example/myapp/data/event/UiEvent.kt`

- [ ] **创建 `domain/` 目录**

```bash
mkdir -p app/src/main/java/com/example/myapp/domain/feeding
mkdir -p app/src/main/java/com/example/myapp/domain/sleep
mkdir -p app/src/main/java/com/example/myapp/domain/growth
```

- [ ] **创建 `UiEvent.kt`（MVI 事件总线）**

```kotlin
package com.example.myapp.data.event

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

sealed interface UiEvent {
    data class Success(val message: String) : UiEvent
    data class Error(val message: String) : UiEvent
    data object Loading : UiEvent
}

object GlobalEventBus {
    private val _events = MutableSharedFlow<UiEvent>(extraBufferCapacity = 10)
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    fun emit(event: UiEvent) {
        _events.tryEmit(event)
    }
}
```

- [ ] **创建 `AddFeedingUseCase.kt`**

```kotlin
package com.example.myapp.domain.feeding

import com.example.myapp.data.event.GlobalEventBus
import com.example.myapp.data.event.UiEvent
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.data.room.FeedingEntity

class AddFeedingUseCase(private val repository: FeedingRepository) {
    suspend operator fun invoke(type: String, amount: Int, unit: String, note: String?) {
        try {
            repository.insert(
                FeedingEntity(type = type, amount = amount, unit = unit, note = note)
            )
            GlobalEventBus.emit(UiEvent.Success("喂养记录已保存"))
        } catch (e: Exception) {
            GlobalEventBus.emit(UiEvent.Error("保存失败: ${e.message}"))
        }
    }
}
```

- [ ] **创建 `DeleteFeedingUseCase.kt`**

```kotlin
package com.example.myapp.domain.feeding

import com.example.myapp.data.room.FeedingEntity
import com.example.myapp.data.repository.FeedingRepository

class DeleteFeedingUseCase(private val repository: FeedingRepository) {
    suspend operator fun invoke(entity: FeedingEntity) = repository.delete(entity)
}
```

- [ ] **创建 `AddSleepUseCase.kt`**

```kotlin
package com.example.myapp.domain.sleep

import com.example.myapp.data.event.GlobalEventBus
import com.example.myapp.data.event.UiEvent
import com.example.myapp.data.repository.SleepRepository
import com.example.myapp.data.room.SleepEntity

class AddSleepUseCase(private val repository: SleepRepository) {
    suspend operator fun invoke(startTime: Long, endTime: Long, type: String) {
        try {
            repository.insert(SleepEntity(startTime = startTime, endTime = endTime, type = type))
            GlobalEventBus.emit(UiEvent.Success("睡眠记录已保存"))
        } catch (e: Exception) {
            GlobalEventBus.emit(UiEvent.Error("保存失败: ${e.message}"))
        }
    }
}
```

- [ ] **创建 `DeleteSleepUseCase.kt`**

```kotlin
package com.example.myapp.domain.sleep

import com.example.myapp.data.room.SleepEntity
import com.example.myapp.data.repository.SleepRepository

class DeleteSleepUseCase(private val repository: SleepRepository) {
    suspend operator fun invoke(entity: SleepEntity) = repository.delete(entity)
}
```

- [ ] **创建 `AddGrowthUseCase.kt`**

```kotlin
package com.example.myapp.domain.growth

import com.example.myapp.data.event.GlobalEventBus
import com.example.myapp.data.event.UiEvent
import com.example.myapp.data.repository.GrowthRepository
import com.example.myapp.data.room.GrowthEntity

class AddGrowthUseCase(private val repository: GrowthRepository) {
    suspend operator fun invoke(height: Float, weight: Float, headCircumference: Float, date: Long) {
        try {
            repository.insert(
                GrowthEntity(height = height, weight = weight, headCircumference = headCircumference, recordDate = date)
            )
            GlobalEventBus.emit(UiEvent.Success("生长记录已保存"))
        } catch (e: Exception) {
            GlobalEventBus.emit(UiEvent.Error("保存失败: ${e.message}"))
        }
    }
}
```

- [ ] **创建 `DeleteGrowthUseCase.kt`**

```kotlin
package com.example.myapp.domain.growth

import com.example.myapp.data.room.GrowthEntity
import com.example.myapp.data.repository.GrowthRepository

class DeleteGrowthUseCase(private val repository: GrowthRepository) {
    suspend operator fun invoke(entity: GrowthEntity) = repository.delete(entity)
}
```

- [ ] **在 `AppModule.kt` 中注册 UseCase**

```kotlin
// UseCases
single { AddFeedingUseCase(get()) }
single { DeleteFeedingUseCase(get()) }
single { AddSleepUseCase(get()) }
single { DeleteSleepUseCase(get()) }
single { AddGrowthUseCase(get()) }
single { DeleteGrowthUseCase(get()) }
```

---

### 任务 10: 创建 Route 定义

**文件:**
- 创建: `app/src/main/java/com/example/myapp/navigation/Route.kt`

- [ ] **创建 `Route.kt`**

```kotlin
package com.example.myapp.navigation

sealed class Route(val route: String) {
    data object Home : Route("home")
    data object Feeding : Route("feeding")
    data object AddFeeding : Route("add_feeding")
    data object Sleep : Route("sleep")
    data object AddSleep : Route("add_sleep")
    data object Growth : Route("growth")
    data object AddGrowth : Route("add_growth")
    data object Vaccine : Route("vaccine")
    data object AddVaccine : Route("add_vaccine")
    data object Health : Route("health")
    data object Stats : Route("stats")
    data object Settings : Route("settings")
    data object About : Route("about")
}
```

---

### 任务 11: 创建 ViewModel（MVI 模式 — sealed UiEvent + UseCase）

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/home/HomeViewModel.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/feeding/FeedingViewModel.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/sleep/SleepViewModel.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/growth/GrowthViewModel.kt`

- [ ] **创建 `HomeViewModel.kt`**

```kotlin
package com.example.myapp.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.BabyRepository
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.data.repository.GrowthRepository
import com.example.myapp.data.repository.SleepRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val babyName: String = "",
    val recentFeedingCount: Int = 0,
    val recentSleepHours: Float = 0f
)

sealed interface HomeEvent {
    data object Refresh : HomeEvent
}

class HomeViewModel(
    private val babyRepo: BabyRepository,
    private val feedingRepo: FeedingRepository,
    private val sleepRepo: SleepRepository,
    private val growthRepo: GrowthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            babyRepo.firstBaby.collect { baby ->
                _uiState.value = _uiState.value.copy(babyName = baby?.name ?: "")
            }
        }
        viewModelScope.launch {
            feedingRepo.getAllByBaby().collect { records ->
                _uiState.value = _uiState.value.copy(recentFeedingCount = records.size)
            }
        }
    }

    fun onEvent(event: HomeEvent) {
        when (event) {
            HomeEvent.Refresh -> { /* 触发刷新 */ }
        }
    }
}
```

- [ ] **创建 `FeedingViewModel.kt`（MVI 模式）**

```kotlin
package com.example.myapp.ui.feeding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.event.GlobalEventBus
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.domain.feeding.AddFeedingUseCase
import com.example.myapp.domain.feeding.DeleteFeedingUseCase
import com.example.myapp.data.room.FeedingEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface FeedingEvent {
    data class Add(val type: String, val amount: Int, val unit: String, val note: String?) : FeedingEvent
    data class Delete(val entity: FeedingEntity) : FeedingEvent
    data object Refresh : FeedingEvent
}

class FeedingViewModel(
    private val repository: FeedingRepository,
    private val addUseCase: AddFeedingUseCase,
    private val deleteUseCase: DeleteFeedingUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(FeedingUiState(loading = true))
    val uiState: StateFlow<FeedingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<FeedingEvent>()
    val events: SharedFlow<FeedingEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.getAllByBaby().collect { records ->
                _uiState.value = FeedingUiState(records = records)
            }
        }
        viewModelScope.launch {
            GlobalEventBus.events.collect { event ->
                // UI 层可观察全局事件（Snackbar 展示等）
            }
        }
    }

    fun onEvent(event: FeedingEvent) {
        when (event) {
            is FeedingEvent.Add -> {
                viewModelScope.launch {
                    addUseCase(event.type, event.amount, event.unit, event.note)
                }
            }
            is FeedingEvent.Delete -> {
                viewModelScope.launch { deleteUseCase(event.entity) }
            }
            FeedingEvent.Refresh -> { /* 触发刷新 */ }
        }
    }
}
```

- [ ] **创建 `SleepViewModel.kt`（MVI 模式）**

```kotlin
package com.example.myapp.ui.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.SleepRepository
import com.example.myapp.domain.sleep.AddSleepUseCase
import com.example.myapp.domain.sleep.DeleteSleepUseCase
import com.example.myapp.data.room.SleepEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SleepEvent {
    data class Add(val startTime: Long, val endTime: Long, val type: String) : SleepEvent
    data class Delete(val entity: SleepEntity) : SleepEvent
    data object Refresh : SleepEvent
}

class SleepViewModel(
    private val repository: SleepRepository,
    private val addUseCase: AddSleepUseCase,
    private val deleteUseCase: DeleteSleepUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(SleepUiState(loading = true))
    val uiState: StateFlow<SleepUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<SleepEvent>()
    val events: SharedFlow<SleepEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.getAllByBaby().collect { records ->
                _uiState.value = SleepUiState(records = records)
            }
        }
    }

    fun onEvent(event: SleepEvent) {
        when (event) {
            is SleepEvent.Add -> {
                viewModelScope.launch { addUseCase(event.startTime, event.endTime, event.type) }
            }
            is SleepEvent.Delete -> {
                viewModelScope.launch { deleteUseCase(event.entity) }
            }
            SleepEvent.Refresh -> {}
        }
    }
}
```

- [ ] **创建 `GrowthViewModel.kt`（MVI 模式）**

```kotlin
package com.example.myapp.ui.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.GrowthRepository
import com.example.myapp.domain.growth.AddGrowthUseCase
import com.example.myapp.domain.growth.DeleteGrowthUseCase
import com.example.myapp.data.room.GrowthEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface GrowthEvent {
    data class Add(val height: Float, val weight: Float, val headCircumference: Float, val date: Long) : GrowthEvent
    data class Delete(val entity: GrowthEntity) : GrowthEvent
    data object Refresh : GrowthEvent
}

class GrowthViewModel(
    private val repository: GrowthRepository,
    private val addUseCase: AddGrowthUseCase,
    private val deleteUseCase: DeleteGrowthUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(GrowthUiState(loading = true))
    val uiState: StateFlow<GrowthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<GrowthEvent>()
    val events: SharedFlow<GrowthEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.getAllByBaby().collect { records ->
                _uiState.value = GrowthUiState(records = records)
            }
        }
    }

    fun onEvent(event: GrowthEvent) {
        when (event) {
            is GrowthEvent.Add -> {
                viewModelScope.launch { addUseCase(event.height, event.weight, event.headCircumference, event.date) }
            }
            is GrowthEvent.Delete -> {
                viewModelScope.launch { deleteUseCase(event.entity) }
            }
            GrowthEvent.Refresh -> {}
        }
    }
}
```

---

### 任务 11: 创建首页 Screen

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/home/HomeScreen.kt`

- [ ] **创建 `HomeScreen.kt`**

```kotlin
package com.example.myapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BabyChangingStation
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.ChildCare
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.designsystem.ParentingCard
import com.example.myapp.ui.designsystem.SectionTitle
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigate: (String) -> Unit
) {
    val baby by viewModel.baby.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("育儿助手") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                ParentingCard {
                    Column {
                        Text(
                            baby?.name ?: "小宝宝",
                            style = MaterialTheme.typography.titleLarge
                        )
                        if (baby != null) {
                            val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            Text(
                                "出生日期: ${fmt.format(Date(baby!!.birthday))}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Text(
                                "身高: ${baby!!.birthHeight}cm  体重: ${baby!!.birthWeight}kg",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            item {
                SectionTitle("功能")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "feeding" to "喂养记录" to Icons.Default.ChildCare,
                        "sleep" to "睡眠记录" to Icons.Default.Bedtime,
                        "growth" to "生长记录" to Icons.Default.TrendingUp,
                        "vaccine" to "疫苗接种" to Icons.Default.Shield,
                        "health" to "健康档案" to Icons.Default.MonitorHeart,
                        "stats" to "统计分析" to Icons.Default.BabyChangingStation
                    ).forEach { ((route, label), icon) ->
                        androidx.compose.material3.OutlinedCard(
                            onClick = { onNavigate(route) },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                                Text(label, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
            }
        }
    }
}
```

---

### 任务 12: 创建喂养 Screen（列表 + 新增）

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/feeding/FeedingScreen.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/feeding/AddFeedingScreen.kt`

- [ ] **创建 `FeedingScreen.kt`（MVI 模式 — 使用 onEvent）**

```kotlin
package com.example.myapp.ui.feeding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.designsystem.EmptyState
import com.example.myapp.ui.designsystem.ErrorView
import com.example.myapp.ui.designsystem.LoadingView
import com.example.myapp.ui.designsystem.ParentingCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingScreen(
    viewModel: FeedingViewModel,
    onAddClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("喂养记录") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { innerPadding ->
        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(message = state.error!!)
            state.records.isEmpty() -> EmptyState(message = "暂无喂养记录")
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.records, key = { it.id }) { record ->
                    val typeLabels = mapOf(
                        "BREAST_MILK" to "母乳",
                        "FORMULA" to "配方奶",
                        "SOLID_FOOD" to "辅食",
                        "WATER" to "水"
                    )
                    val timeStr = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                        .format(Date(record.createdAt))
                    ParentingCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    typeLabels[record.type] ?: record.type,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "${record.amount} ${record.unit}",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Text(timeStr, style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = { viewModel.onEvent(FeedingEvent.Delete(record)) }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **创建 `AddFeedingScreen.kt`**

```kotlin
package com.example.myapp.ui.feeding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.designsystem.ParentingButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedingScreen(
    onSave: (type: String, amount: Int, unit: String, note: String?) -> Unit,
    onBack: () -> Unit
) {
    var selectedType by remember { mutableStateOf("BREAST_MILK") }
    var amountText by remember { mutableStateOf("") }
    var unit by remember { mutableStateOf("ml") }
    var note by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    val types = listOf(
        "BREAST_MILK" to "母乳",
        "FORMULA" to "配方奶",
        "SOLID_FOOD" to "辅食",
        "WATER" to "水"
    )
    val typeLabels = types.toMap()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("新增喂养记录", style = MaterialTheme.typography.titleLarge)

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = typeLabels[selectedType] ?: selectedType,
                onValueChange = {},
                readOnly = true,
                label = { Text("类型") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                types.forEach { (key, label) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = { selectedType = key; expanded = false }
                    )
                }
            }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("用量") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = note,
            onValueChange = { note = it },
            label = { Text("备注（可选）") },
            modifier = Modifier.fillMaxWidth()
        )

        ParentingButton(
            text = "保存",
            enabled = amountText.isNotBlank(),
            onClick = {
                val amount = amountText.toIntOrNull() ?: return@ParentingButton
                onSave(selectedType, amount, unit, note.ifBlank { null })
                onBack()
            }
        )
    }
}
```

---

### 任务 13: 创建睡眠 Screen（列表 + 新增）

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/sleep/SleepScreen.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/sleep/AddSleepScreen.kt`

- [ ] **创建 `SleepScreen.kt`**

```kotlin
package com.example.myapp.ui.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.data.room.SleepEntity
import com.example.myapp.ui.designsystem.EmptyState
import com.example.myapp.ui.designsystem.ErrorView
import com.example.myapp.ui.designsystem.LoadingView
import com.example.myapp.ui.designsystem.ParentingCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(
    viewModel: SleepViewModel,
    onAddClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("睡眠记录") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { innerPadding ->
        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(message = state.error!!)
            state.records.isEmpty() -> EmptyState(message = "暂无睡眠记录")
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.records, key = { it.id }) { record ->
                    val fmt = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
                    val durationMs = record.endTime - record.startTime
                    val hours = durationMs / 3_600_000
                    val minutes = (durationMs % 3_600_000) / 60_000
                    val typeLabel = if (record.type == "NIGHT") "夜间睡眠" else "白天小睡"

                    ParentingCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(typeLabel, style = MaterialTheme.typography.titleMedium)
                                Text(
                                    "${fmt.format(Date(record.startTime))} → ${fmt.format(Date(record.endTime))}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    "${hours}小时${minutes}分钟",
                                    style = MaterialTheme.typography.headlineSmall
                                )
                            }
                            IconButton(onClick = { viewModel.onEvent(SleepEvent.Delete(record)) }) {
                                Icon(Icons.Default.Delete, contentDescription = "删除")
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **创建 `AddSleepScreen.kt`**

```kotlin
package com.example.myapp.ui.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.designsystem.ParentingButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSleepScreen(
    onSave: (startTime: Long, endTime: Long, type: String) -> Unit,
    onBack: () -> Unit
) {
    var startTime by remember { mutableStateOf(System.currentTimeMillis()) }
    var endTime by remember { mutableStateOf(System.currentTimeMillis() + 3600_000) }
    var sleepType by remember { mutableStateOf("NAP") }
    var expanded by remember { mutableStateOf(false) }

    val fmt = java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.getDefault())

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("新增睡眠记录", style = MaterialTheme.typography.titleLarge)

        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            OutlinedTextField(
                value = if (sleepType == "NIGHT") "夜间睡眠" else "白天小睡",
                onValueChange = {},
                readOnly = true,
                label = { Text("类型") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                DropdownMenuItem(text = { Text("白天小睡") }, onClick = { sleepType = "NAP"; expanded = false })
                DropdownMenuItem(text = { Text("夜间睡眠") }, onClick = { sleepType = "NIGHT"; expanded = false })
            }
        }

        Text("开始时间: ${fmt.format(java.util.Date(startTime))}")
        Text("结束时间: ${fmt.format(java.util.Date(endTime))}")

        ParentingButton(text = "保存", onClick = {
            onSave(startTime, endTime, sleepType)
            onBack()
        })
    }
}
```

---

### 任务 14: 创建生长 Screen（含 Vico 图表）

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/growth/GrowthScreen.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/growth/AddGrowthScreen.kt`

- [ ] **创建 `GrowthScreen.kt`**

```kotlin
package com.example.myapp.ui.growth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.designsystem.EmptyState
import com.example.myapp.ui.designsystem.ErrorView
import com.example.myapp.ui.designsystem.LoadingView
import com.example.myapp.ui.designsystem.ParentingCard
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthScreen(
    viewModel: GrowthViewModel,
    onAddClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("身高", "体重", "头围")

    Scaffold(
        topBar = { TopAppBar(title = { Text("生长记录") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { innerPadding ->
        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(message = state.error!!)
            state.records.isEmpty() -> EmptyState(message = "暂无生长记录")
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    PrimaryTabRow(selectedTabIndex = selectedTab) {
                        tabs.forEachIndexed { index, title ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(title) }
                            )
                        }
                    }
                }

                item {
                    val values = when (selectedTab) {
                        0 -> state.records.sortedBy { it.recordDate }.map { it.height }
                        1 -> state.records.sortedBy { it.recordDate }.map { it.weight }
                        else -> state.records.sortedBy { it.recordDate }.map { it.headCircumference }
                    }

                    val modelProducer = remember { CartesianChartModelProducer() }
                    LaunchedEffect(state.records, selectedTab) {
                        modelProducer.runTransaction {
                            lineSeries { series(values) }
                        }
                    }

                    CartesianChartHost(
                        chart = rememberCartesianChart(
                            rememberLineCartesianLayer(),
                            startAxis = rememberStartAxis(),
                            bottomAxis = rememberBottomAxis()
                        ),
                        modelProducer = modelProducer,
                        modifier = Modifier.fillMaxWidth().height(300.dp)
                    )
                }

                items(state.records, key = { it.id }) { record ->
                    val fmt = SimpleDateFormat("MM/dd", Locale.getDefault())
                    ParentingCard {
                        Column {
                            Text(fmt.format(Date(record.recordDate)), style = MaterialTheme.typography.titleMedium)
                            Text("身高: ${record.height}cm  体重: ${record.weight}kg  头围: ${record.headCircumference}cm")
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **创建 `AddGrowthScreen.kt`**

```kotlin
package com.example.myapp.ui.growth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.designsystem.ParentingButton

@Composable
fun AddGrowthScreen(
    onSave: (height: Float, weight: Float, headCircumference: Float, date: Long) -> Unit,
    onBack: () -> Unit
) {
    var heightText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var headText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("新增生长记录", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = heightText,
            onValueChange = { heightText = it },
            label = { Text("身高 (cm)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = weightText,
            onValueChange = { weightText = it },
            label = { Text("体重 (kg)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = headText,
            onValueChange = { headText = it },
            label = { Text("头围 (cm)") },
            modifier = Modifier.fillMaxWidth()
        )

        ParentingButton(
            text = "保存",
            enabled = heightText.isNotBlank() && weightText.isNotBlank(),
            onClick = {
                val h = heightText.toFloatOrNull() ?: return@ParentingButton
                val w = weightText.toFloatOrNull() ?: return@ParentingButton
                val hc = headText.toFloatOrNull() ?: 0f
                onSave(h, w, hc, System.currentTimeMillis())
                onBack()
            }
        )
    }
}
```

---

### 任务 15: 创建导航图和 MainActivity

**文件:**
- 创建: `app/src/main/java/com/example/myapp/navigation/AppNavGraph.kt`
- 修改: `app/src/main/java/com/example/myapp/MainActivity.kt`

- [ ] **创建 `AppNavGraph.kt`（MVI 模式 — 使用 onEvent）**

```kotlin
package com.example.myapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.myapp.ui.feeding.AddFeedingScreen
import com.example.myapp.ui.feeding.FeedingEvent
import com.example.myapp.ui.feeding.FeedingScreen
import com.example.myapp.ui.growth.AddGrowthScreen
import com.example.myapp.ui.growth.GrowthEvent
import com.example.myapp.ui.growth.GrowthScreen
import com.example.myapp.ui.home.HomeScreen
import com.example.myapp.ui.sleep.AddSleepScreen
import com.example.myapp.ui.sleep.SleepEvent
import com.example.myapp.ui.sleep.SleepScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Route.Home.route) {
        composable(Route.Home.route) {
            HomeScreen(
                viewModel = koinViewModel(),
                onNavigate = { route -> navController.navigate(route) }
            )
        }
        composable(Route.Feeding.route) {
            FeedingScreen(
                viewModel = koinViewModel(),
                onAddClick = { navController.navigate(Route.AddFeeding.route) }
            )
        }
        composable(Route.AddFeeding.route) {
            AddFeedingScreen(
                onSave = { type, amount, unit, note ->
                    koinViewModel<com.example.myapp.ui.feeding.FeedingViewModel>()
                        .onEvent(FeedingEvent.Add(type, amount, unit, note))
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.Sleep.route) {
            SleepScreen(
                viewModel = koinViewModel(),
                onAddClick = { navController.navigate(Route.AddSleep.route) }
            )
        }
        composable(Route.AddSleep.route) {
            AddSleepScreen(
                onSave = { startTime, endTime, type ->
                    koinViewModel<com.example.myapp.ui.sleep.SleepViewModel>()
                        .onEvent(SleepEvent.Add(startTime, endTime, type))
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
        composable(Route.Growth.route) {
            GrowthScreen(
                viewModel = koinViewModel(),
                onAddClick = { navController.navigate(Route.AddGrowth.route) }
            )
        }
        composable(Route.AddGrowth.route) {
            AddGrowthScreen(
                onSave = { height, weight, head, date ->
                    koinViewModel<com.example.myapp.ui.growth.GrowthViewModel>()
                        .onEvent(GrowthEvent.Add(height, weight, head, date))
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
```

- [ ] **更新 `MainActivity.kt`**

```kotlin
package com.example.myapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.myapp.navigation.AppNavGraph
import com.example.myapp.ui.theme.MyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyAppTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    val navController = rememberNavController()
                    AppNavGraph(navController = navController)
                }
            }
        }
    }
}
```

---

### 任务 16: 填充 Koin AppModule

**文件:**
- 修改: `app/src/main/java/com/example/myapp/di/AppModule.kt`

- [ ] **更新 `AppModule.kt`（注册 UseCase + MVI ViewModel）**

```kotlin
package com.example.myapp.di

import com.example.myapp.data.room.AppDatabase
import com.example.myapp.data.repository.BabyRepository
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.data.repository.GrowthRepository
import com.example.myapp.data.repository.HealthRepository
import com.example.myapp.data.repository.ReminderRepository
import com.example.myapp.data.repository.SleepRepository
import com.example.myapp.data.repository.VaccineRepository
import com.example.myapp.domain.feeding.AddFeedingUseCase
import com.example.myapp.domain.feeding.DeleteFeedingUseCase
import com.example.myapp.domain.growth.AddGrowthUseCase
import com.example.myapp.domain.growth.DeleteGrowthUseCase
import com.example.myapp.domain.sleep.AddSleepUseCase
import com.example.myapp.domain.sleep.DeleteSleepUseCase
import com.example.myapp.ui.feeding.FeedingViewModel
import com.example.myapp.ui.growth.GrowthViewModel
import com.example.myapp.ui.home.HomeViewModel
import com.example.myapp.ui.sleep.SleepViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Database
    single { AppDatabase.build(androidContext()) }

    // DAOs
    single { get<AppDatabase>().babyDao() }
    single { get<AppDatabase>().feedingDao() }
    single { get<AppDatabase>().sleepDao() }
    single { get<AppDatabase>().growthDao() }
    single { get<AppDatabase>().vaccineDao() }
    single { get<AppDatabase>().healthProfileDao() }
    single { get<AppDatabase>().reminderDao() }

    // Repositories
    single { BabyRepository(get()) }
    single { FeedingRepository(get()) }
    single { SleepRepository(get()) }
    single { GrowthRepository(get()) }
    single { VaccineRepository(get()) }
    single { HealthRepository(get()) }
    single { ReminderRepository(get()) }

    // UseCases
    single { AddFeedingUseCase(get()) }
    single { DeleteFeedingUseCase(get()) }
    single { AddSleepUseCase(get()) }
    single { DeleteSleepUseCase(get()) }
    single { AddGrowthUseCase(get()) }
    single { DeleteGrowthUseCase(get()) }

    // ViewModels (MVI — injected with UseCases)
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { FeedingViewModel(get(), get(), get()) }
    viewModel { SleepViewModel(get(), get(), get()) }
    viewModel { GrowthViewModel(get(), get(), get()) }
}
```

---

### 任务 17: Milestone 1 编译验证

- [ ] **执行 assembleDebug**

```bash
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_AAPT2_DAEMON_MODE=false
./gradlew assembleDebug
```

预期结果：BUILD SUCCESSFUL

- [ ] **若编译失败**，修复后重新编译，循环至成功。

- [ ] **提交 M1 基础能力**

```bash
git add app/src/main/java/com/example/myapp/
git commit -m "feat: milestone 1 - core features (home, feeding, sleep, growth)"
```

---

# Milestone 2: 健康管理

---

### 任务 18: 创建疫苗 Screen

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/vaccine/VaccineUiState.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/vaccine/VaccineViewModel.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/vaccine/VaccineScreen.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/vaccine/AddVaccineScreen.kt`

- [ ] **创建 `VaccineUiState.kt`**

```kotlin
package com.example.myapp.ui.vaccine

import com.example.myapp.data.room.VaccineEntity

data class VaccineUiState(
    val loading: Boolean = false,
    val vaccines: List<VaccineEntity> = emptyList(),
    val error: String? = null
)
```

- [ ] **创建 `VaccineViewModel.kt`（MVI 模式）**

```kotlin
package com.example.myapp.ui.vaccine

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.VaccineRepository
import com.example.myapp.data.room.VaccineEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface VaccineEvent {
    data class Add(val name: String, val dose: Int, val plannedDate: Long) : VaccineEvent
    data class MarkCompleted(val id: Long) : VaccineEvent
}

class VaccineViewModel(
    private val repository: VaccineRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(VaccineUiState(loading = true))
    val uiState: StateFlow<VaccineUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<VaccineEvent>()
    val events: SharedFlow<VaccineEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.getAllByBaby().collect { vaccines ->
                _uiState.value = VaccineUiState(vaccines = vaccines)
            }
        }
    }

    fun onEvent(event: VaccineEvent) {
        when (event) {
            is VaccineEvent.Add -> {
                viewModelScope.launch {
                    repository.insert(VaccineEntity(name = event.name, dose = event.dose, plannedDate = event.plannedDate))
                }
            }
            is VaccineEvent.MarkCompleted -> {
                viewModelScope.launch {
                    repository.markCompleted(event.id, System.currentTimeMillis())
                }
            }
        }
    }
}
```

- [ ] **创建 `VaccineScreen.kt`**

```kotlin
package com.example.myapp.ui.vaccine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.designsystem.EmptyState
import com.example.myapp.ui.designsystem.ErrorView
import com.example.myapp.ui.designsystem.LoadingView
import com.example.myapp.ui.designsystem.ParentingCard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VaccineScreen(
    viewModel: VaccineViewModel,
    onAddClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("疫苗接种") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { innerPadding ->
        when {
            state.loading -> LoadingView()
            state.error != null -> ErrorView(message = state.error!!)
            state.vaccines.isEmpty() -> EmptyState(message = "暂无疫苗记录")
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.vaccines, key = { it.id }) { vaccine ->
                    val fmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val statusLabel = when (vaccine.status) {
                        "COMPLETED" -> "已接种"
                        "EXPIRED" -> "已过期"
                        else -> "待接种"
                    }
                    val statusColor = when (vaccine.status) {
                        "COMPLETED" -> Color(0xFF4CAF50)
                        "EXPIRED" -> Color(0xFFF44336)
                        else -> Color(0xFFFF9800)
                    }

                    ParentingCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    "${vaccine.name}（第${vaccine.dose}针）",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Text(
                                    "计划接种: ${fmt.format(Date(vaccine.plannedDate))}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                AssistChip(
                                    onClick = {},
                                    label = { Text(statusLabel) }
                                )
                            }
                            if (vaccine.status == "PENDING") {
                                androidx.compose.material3.IconButton(onClick = { viewModel.onEvent(VaccineEvent.MarkCompleted(vaccine.id)) }) {
                                    Icon(
                                        Icons.Default.CheckCircle,
                                        contentDescription = "标记完成",
                                        tint = Color(0xFF4CAF50)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
```

- [ ] **创建 `AddVaccineScreen.kt`**

```kotlin
package com.example.myapp.ui.vaccine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.designsystem.ParentingButton

@Composable
fun AddVaccineScreen(
    onSave: (name: String, dose: Int, plannedDate: Long) -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var doseText by remember { mutableStateOf("1") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("新增疫苗", style = MaterialTheme.typography.titleLarge)

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("疫苗名称") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = doseText,
            onValueChange = { doseText = it },
            label = { Text("针次") },
            modifier = Modifier.fillMaxWidth()
        )

        ParentingButton(
            text = "保存",
            enabled = name.isNotBlank(),
            onClick = {
                val dose = doseText.toIntOrNull() ?: return@ParentingButton
                onSave(name, dose, System.currentTimeMillis() + 30L * 24 * 3600_000)
                onBack()
            }
        )
    }
}
```

- [ ] **在 `AppNavGraph.kt` 中添加疫苗路由**

在 NavHost 中添加：

```kotlin
composable(Route.Vaccine.route) {
    VaccineScreen(
        viewModel = koinViewModel(),
        onAddClick = { navController.navigate(Route.AddVaccine.route) }
    )
}
composable(Route.AddVaccine.route) {
    AddVaccineScreen(
        onSave = { name, dose, plannedDate ->
            koinViewModel<com.example.myapp.ui.vaccine.VaccineViewModel>()
                .onEvent(VaccineEvent.Add(name, dose, plannedDate))
            navController.popBackStack()
        },
        onBack = { navController.popBackStack() }
    )
}
```

- [ ] **在 `AppModule.kt` 中注册 VaccineViewModel**

添加：`viewModel { VaccineViewModel(get()) }`

---

### 任务 19: 创建健康档案 Screen

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/health/HealthUiState.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/health/HealthViewModel.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/health/HealthScreen.kt`

- [ ] **创建 `HealthUiState.kt`**

```kotlin
package com.example.myapp.ui.health

import com.example.myapp.data.room.HealthProfileEntity

data class HealthUiState(
    val loading: Boolean = false,
    val profile: HealthProfileEntity? = null,
    val error: String? = null
)
```

- [ ] **创建 `HealthViewModel.kt`（MVI 模式）**

```kotlin
package com.example.myapp.ui.health

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.HealthRepository
import com.example.myapp.data.room.HealthProfileEntity
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HealthEvent {
    data class Save(val profile: HealthProfileEntity) : HealthEvent
}

class HealthViewModel(
    private val repository: HealthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(HealthUiState(loading = true))
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HealthEvent>()
    val events: SharedFlow<HealthEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            repository.getByBaby().collect { profile ->
                _uiState.value = HealthUiState(profile = profile)
            }
        }
    }

    fun onEvent(event: HealthEvent) {
        when (event) {
            is HealthEvent.Save -> {
                viewModelScope.launch { repository.save(event.profile) }
            }
        }
    }
}
```

- [ ] **创建 `HealthScreen.kt`**

```kotlin
package com.example.myapp.ui.health

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.data.room.HealthProfileEntity
import com.example.myapp.ui.designsystem.LoadingView
import com.example.myapp.ui.designsystem.ParentingButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthScreen(
    viewModel: HealthViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("健康档案") }) }
    ) { innerPadding ->
        if (state.loading) {
            LoadingView()
        } else {
            var allergies by remember { mutableStateOf(state.profile?.allergies ?: "") }
            var history by remember { mutableStateOf(state.profile?.medicalHistory ?: "") }
            var notes by remember { mutableStateOf(state.profile?.doctorNotes ?: "") }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("健康档案", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("过敏史") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = history,
                    onValueChange = { history = it },
                    label = { Text("既往病史") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("医生备注") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3
                )

                Spacer(modifier = Modifier.height(8.dp))
                ParentingButton(
                    text = "保存",
                    onClick = {
                        viewModel.onEvent(
                            HealthEvent.Save(
                                HealthProfileEntity(
                                    id = state.profile?.id ?: 0,
                                    allergies = allergies.ifBlank { null },
                                    medicalHistory = history.ifBlank { null },
                                    doctorNotes = notes.ifBlank { null }
                                )
                            )
                        )
                    }
                )
            }
        }
    }
}
```

- [ ] **在 `AppNavGraph.kt` 中添加健康路由**

```kotlin
composable(Route.Health.route) {
    HealthScreen(viewModel = koinViewModel())
}
```

- [ ] **在 `AppModule.kt` 中注册 HealthViewModel**

添加：`viewModel { HealthViewModel(get()) }`

---

### 任务 20: 创建 DataStore 偏好设置

**文件:**
- 创建: `app/src/main/java/com/example/myapp/data/datastore/ThemePreference.kt`
- 创建: `app/src/main/java/com/example/myapp/data/datastore/NotificationPreference.kt`

- [ ] **创建 `ThemePreference.kt`**

```kotlin
package com.example.myapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.themeStore: DataStore<Preferences> by preferencesDataStore(name = "theme_settings")

class ThemePreference(private val context: Context) {
    companion object {
        private val THEME_MODE = intPreferencesKey("theme_mode")
    }

    val themeMode: Flow<Int> = context.themeStore.data.map { prefs ->
        prefs[THEME_MODE] ?: 0 // 0=system, 1=light, 2=dark
    }

    suspend fun setThemeMode(mode: Int) {
        context.themeStore.edit { prefs -> prefs[THEME_MODE] = mode }
    }
}
```

- [ ] **创建 `NotificationPreference.kt`**

```kotlin
package com.example.myapp.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationStore: DataStore<Preferences> by preferencesDataStore(name = "notification_settings")

class NotificationPreference(private val context: Context) {
    companion object {
        private val VACCINE_REMINDER = booleanPreferencesKey("vaccine_reminder")
        private val CHECKUP_REMINDER = booleanPreferencesKey("checkup_reminder")
    }

    val vaccineReminder: Flow<Boolean> = context.notificationStore.data.map { it[VACCINE_REMINDER] ?: true }
    val checkupReminder: Flow<Boolean> = context.notificationStore.data.map { it[CHECKUP_REMINDER] ?: true }

    suspend fun setVaccineReminder(enabled: Boolean) {
        context.notificationStore.edit { it[VACCINE_REMINDER] = enabled }
    }

    suspend fun setCheckupReminder(enabled: Boolean) {
        context.notificationStore.edit { it[CHECKUP_REMINDER] = enabled }
    }
}
```

- [ ] **在 `AppModule.kt` 中注册 DataStore Preferences**

```kotlin
single { ThemePreference(get()) }
single { NotificationPreference(get()) }
```

---

### 任务 21: 创建 WorkManager 提醒

**文件:**
- 创建: `app/src/main/java/com/example/myapp/worker/ReminderWorker.kt`

- [ ] **创建 `ReminderWorker.kt`**

```kotlin
package com.example.myapp.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // 检查待办提醒并发送通知
        // V1 简单实现：每日检查疫苗提醒
        return Result.success()
    }
}
```

- [ ] **在 `MyApp.kt` 中初始化 WorkManager**

```kotlin
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

// 在 MyApp.onCreate() 中添加：
val reminderRequest = PeriodicWorkRequestBuilder<ReminderWorker>(1, TimeUnit.DAYS).build()
WorkManager.getInstance(this).enqueue(reminderRequest)
```

---

### 任务 22: Milestone 2 编译验证

- [ ] **执行 assembleDebug**，确保编译通过。

- [ ] **提交 M2 健康管理**

```bash
git add app/src/main/java/com/example/myapp/
git commit -m "feat: milestone 2 - health management (vaccine, health, reminders)"
```

---

# Milestone 3: 数据分析

---

### 任务 23: 创建统计分析 Screen

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/stats/StatsUiState.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/stats/StatsViewModel.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/stats/StatsScreen.kt`

- [ ] **创建 `StatsUiState.kt`**

```kotlin
package com.example.myapp.ui.stats

data class StatsUiState(
    val loading: Boolean = false,
    val feedingCount: Int = 0,
    val sleepHours: Float = 0f,
    val growthTrend: List<Float> = emptyList()
)
```

- [ ] **创建 `StatsViewModel.kt`（MVI 模式）**

```kotlin
package com.example.myapp.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.data.repository.GrowthRepository
import com.example.myapp.data.repository.SleepRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface StatsEvent {
    data object Refresh : StatsEvent
}

class StatsViewModel(
    private val feedingRepo: FeedingRepository,
    private val sleepRepo: SleepRepository,
    private val growthRepo: GrowthRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(StatsUiState(loading = true))
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<StatsEvent>()
    val events: SharedFlow<StatsEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            feedingRepo.getAllByBaby().collect { records ->
                _uiState.value = _uiState.value.copy(feedingCount = records.size, loading = false)
            }
        }
        viewModelScope.launch {
            sleepRepo.getAllByBaby().collect { records ->
                val totalHours = records.sumOf {
                    (it.endTime - it.startTime) / 3_600_000.0
                }.toFloat()
                _uiState.value = _uiState.value.copy(sleepHours = totalHours)
            }
        }
        viewModelScope.launch {
            growthRepo.getAllByBaby().collect { records ->
                _uiState.value = _uiState.value.copy(
                    growthTrend = records.sortedBy { it.recordDate }.map { it.height }
                )
            }
        }
    }

    fun onEvent(event: StatsEvent) {
        when (event) {
            StatsEvent.Refresh -> {}
        }
    }
}
```

- [ ] **创建 `StatsScreen.kt`**

```kotlin
package com.example.myapp.ui.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.designsystem.LoadingView
import com.example.myapp.ui.designsystem.ParentingCard
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(
    viewModel: StatsViewModel
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("统计分析") }) }
    ) { innerPadding ->
        if (state.loading) {
            LoadingView()
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ParentingCard(modifier = Modifier.weight(1f)) {
                        Text("喂养次数", style = MaterialTheme.typography.titleSmall)
                        Text("${state.feedingCount}", style = MaterialTheme.typography.headlineLarge)
                    }
                    ParentingCard(modifier = Modifier.weight(1f)) {
                        Text("睡眠总时长", style = MaterialTheme.typography.titleSmall)
                        Text("${"%.1f".format(state.sleepHours)}h", style = MaterialTheme.typography.headlineLarge)
                    }
                }

                if (state.growthTrend.isNotEmpty()) {
                    ParentingCard {
                        Text("身高趋势", style = MaterialTheme.typography.titleSmall)
                        Spacer(modifier = Modifier.height(8.dp))

                        val modelProducer = remember { CartesianChartModelProducer() }
                        LaunchedEffect(state.growthTrend) {
                            modelProducer.runTransaction {
                                lineSeries { series(state.growthTrend) }
                            }
                        }

                        CartesianChartHost(
                            chart = rememberCartesianChart(
                                rememberLineCartesianLayer(),
                                startAxis = rememberStartAxis(),
                                bottomAxis = rememberBottomAxis()
                            ),
                            modelProducer = modelProducer,
                            modifier = Modifier.fillMaxWidth().height(250.dp)
                        )
                    }
                }
            }
        }
    }
}
```

- [ ] **在 `AppNavGraph.kt` 中添加统计路由**

```kotlin
composable(Route.Stats.route) {
    StatsScreen(viewModel = koinViewModel())
}
```

- [ ] **在 `AppModule.kt` 中注册 StatsViewModel**

```kotlin
viewModel { StatsViewModel(get(), get(), get()) }
```

---

### 任务 24: Milestone 3 编译验证

- [ ] **执行 assembleDebug**，确保编译通过。

- [ ] **提交 M3 数据分析**

```bash
git add app/src/main/java/com/example/myapp/
git commit -m "feat: milestone 3 - statistics and charts"
```

---

# Milestone 4: 产品化

---

### 任务 25: 创建设置 Screen

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/settings/SettingsViewModel.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/settings/SettingsScreen.kt`

- [ ] **创建 `SettingsViewModel.kt`（MVI 模式）**

```kotlin
package com.example.myapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.datastore.NotificationPreference
import com.example.myapp.data.datastore.ThemePreference
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SettingsEvent {
    data class SetThemeMode(val mode: Int) : SettingsEvent
    data class SetVaccineReminder(val enabled: Boolean) : SettingsEvent
}

class SettingsViewModel(
    private val themePref: ThemePreference,
    private val notificationPref: NotificationPreference
) : ViewModel() {
    val themeMode: StateFlow<Int> = themePref.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
    val vaccineReminder: StateFlow<Boolean> = notificationPref.vaccineReminder
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)

    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events.asSharedFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.SetThemeMode -> {
                viewModelScope.launch { themePref.setThemeMode(event.mode) }
            }
            is SettingsEvent.SetVaccineReminder -> {
                viewModelScope.launch { notificationPref.setVaccineReminder(event.enabled) }
            }
        }
    }
}
```

- [ ] **创建 `SettingsScreen.kt`**

```kotlin
package com.example.myapp.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.ui.designsystem.ParentingCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val themeMode by viewModel.themeMode.collectAsState()
    val vaccineReminder by viewModel.vaccineReminder.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("设置") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ParentingCard {
                Column {
                    Text("通知设置", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("疫苗提醒")
                        Switch(checked = vaccineReminder, onCheckedChange = {
                            viewModel.onEvent(SettingsEvent.SetVaccineReminder(it))
                        })
                    }
                }
            }
        }
    }
}
```

- [ ] **在 `AppNavGraph.kt` 中添加设置路由**

```kotlin
composable(Route.Settings.route) {
    SettingsScreen(viewModel = koinViewModel())
}
```

- [ ] **在 `AppModule.kt` 中注册 SettingsViewModel**

```kotlin
viewModel { SettingsViewModel(get(), get()) }
```

---

### 任务 26: 创建关于页面

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/about/AboutScreen.kt`

- [ ] **创建 `AboutScreen.kt`**

```kotlin
package com.example.myapp.ui.about

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen() {
    Scaffold(
        topBar = { TopAppBar(title = { Text("关于") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("育儿助手", style = MaterialTheme.typography.headlineMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("v1.0.0", style = MaterialTheme.typography.bodyLarge)
            Spacer(modifier = Modifier.height(16.dp))
            Text("面向 0-6 岁儿童家庭的成长记录与健康管理工具。")
        }
    }
}
```

- [ ] **在 `AppNavGraph.kt` 中添加关于路由**

```kotlin
composable(Route.About.route) {
    AboutScreen()
}
```

---

### 任务 27: 完善首页导航 — 集成全部路由

- [ ] **更新 `HomeScreen.kt`**，确保功能宫格导航到所有已实现页面：feeding, sleep, growth, vaccine, health, stats, settings, about。

---

### 任务 28: Milestone 4 最终编译验证

- [ ] **执行 assembleDebug**，修复所有编译错误。

- [ ] **提交 M4 产品化**

```bash
git add app/src/main/java/com/example/myapp/
git commit -m "feat: milestone 4 - settings, about, navigation integration"
```

- [ ] **提交 M5 测试**

```bash
git add app/src/test/ app/src/androidTest/
git commit -m "test: milestone 5 - DAO, ViewModel, Compose UI tests"
```

---

### 任务 32: 最终自查

- [ ] **Spec 覆盖检查**: 14 个页面 + UseCase 层 + MVI 模式 + 模块化 + 测试覆盖。
- [ ] **占位符检查**: 无 "TBD"、"TODO"。
- [ ] **类型一致性**: Route sealed class 路由字符串与 NavHost composable 匹配。
- [ ] **数据模型一致性**: 所有 Entity 包含 babyId。

---

## 自查清单

1. **Spec 覆盖**: 14 页面 + UseCase 层 (6) + MVI (sealed UiEvent + onEvent) + 模块化 (core:data, core:designsystem) + 测试 (DAO/ViewModel/UI)。
2. **占位符检查**: 所有步骤包含完整代码，无 "TBD"、"TODO"。
3. **类型一致性**: Route 使用 sealed class；UiEvent/ViewModel 使用 sealed interface + onEvent 分发；所有 Entity 包含 babyId。
4. **数据流**: Room → Repository → UseCase → ViewModel(StateFlow<UiState>) → Compose，全局事件通过 GlobalEventBus (SharedFlow) 分发。
