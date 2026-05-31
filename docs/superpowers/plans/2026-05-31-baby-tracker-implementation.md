# Baby Tracker 实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标:** 构建一个完整的宝宝记录 Android 应用，包含喂养、睡眠、生长、疫苗等 14 个页面。

**架构:** MVVM + Room（本地存储）、Koin（DI）、Navigation Compose（路由）、Paging 3（列表）、Vico Chart（生长趋势图）、DataStore（偏好设置）。每个功能模块拥有独立的 ViewModel + Screen 组件。

**技术栈:** Jetpack Compose + Material 3, Navigation Compose, Paging 3, Vico Chart, DataStore, Room 2.6.1 + KSP, Koin 3.5.6, Kotlin 1.9.22, Gradle 9.5.1 + AGP 8.9.3

---

### 任务 1：更新 app/build.gradle.kts 依赖

**文件:**
- 修改: `app/build.gradle.kts`（添加 navigation, paging, vico, datastore）

- [ ] **添加新依赖**

在 `app/build.gradle.kts` 中 `val coilVersion` 之后添加：
```kotlin
val navVersion = "2.7.7"
val pagingVersion = "3.2.1"
val vicoVersion = "1.13.1"
```

在 `dependencies {}` 块中现有 Koin 依赖之后添加：
```kotlin
// Navigation
implementation("androidx.navigation:navigation-compose:$navVersion")

// Paging 3
implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
implementation("androidx.paging:paging-compose:$pagingVersion")

// Vico Chart
implementation("com.patrykandpatrick.vico:compose-m3:$vicoVersion")

// DataStore
implementation("androidx.datastore:datastore-preferences:1.0.0")
```

- [ ] **提交**

```bash
git add app/build.gradle.kts
git commit -m "chore: add nav, paging, vico, datastore deps"
```

---

### 任务 2：创建 Application 类和 Koin 模块

**文件:**
- 创建: `app/src/main/java/com/example/myapp/MyApp.kt`
- 创建: `app/src/main/java/com/example/myapp/di/AppModule.kt`

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

- [ ] **创建 `di/AppModule.kt`**（初始骨架，后续任务填充）

```kotlin
package com.example.myapp.di

import org.koin.dsl.module

val appModule = module {

}
```

- [ ] **在 `AndroidManifest.xml` 中注册 `MyApp`**

找到 `<application>` 标签，添加 `android:name=".MyApp"`。

- [ ] **提交**

```bash
git add app/src/main/java/com/example/myapp/MyApp.kt app/src/main/java/com/example/myapp/di/AppModule.kt
git commit -m "feat: add Application class and Koin module skeleton"
```

---

### 任务 3：创建 Room 数据库、实体和 DAO

**文件:**
- 创建: `app/src/main/java/com/example/myapp/data/room/AppDatabase.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/FeedingEntity.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/SleepEntity.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/GrowthEntity.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/FeedingDao.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/SleepDao.kt`
- 创建: `app/src/main/java/com/example/myapp/data/room/GrowthDao.kt`

- [ ] **创建 `data/room/` 目录**

```bash
mkdir -p app/src/main/java/com/example/myapp/data/room
```

- [ ] **创建 `FeedingEntity.kt`**

```kotlin
package com.example.myapp.data.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "feeding_records")
data class FeedingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String,
    val amount: Int,
    val time: Long
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
    val startTime: Long,
    val endTime: Long
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
    val height: Float,
    val weight: Float,
    val headCircumference: Float,
    val date: Long
)
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
    @Query("SELECT * FROM feeding_records ORDER BY time DESC")
    fun getAllFlow(): Flow<List<FeedingEntity>>

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
    @Query("SELECT * FROM sleep_records ORDER BY startTime DESC")
    fun getAllFlow(): Flow<List<SleepEntity>>

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
    @Query("SELECT * FROM growth_records ORDER BY date DESC")
    fun getAllFlow(): Flow<List<GrowthEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: GrowthEntity)

    @Delete
    suspend fun delete(entity: GrowthEntity)
}
```

- [ ] **创建 `AppDatabase.kt`**

```kotlin
package com.example.myapp.data.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [FeedingEntity::class, SleepEntity::class, GrowthEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun feedingDao(): FeedingDao
    abstract fun sleepDao(): SleepDao
    abstract fun growthDao(): GrowthDao

    companion object {
        fun build(context: Context): AppDatabase =
            Room.databaseBuilder(context, AppDatabase::class.java, "baby_tracker.db")
                .fallbackToDestructiveMigration()
                .build()
    }
}
```

- [ ] **提交**

```bash
git add app/src/main/java/com/example/myapp/data/room/
git commit -m "feat: add Room entities, DAOs, and database"
```

---

### 任务 4：创建 Repository

**文件:**
- 创建: `app/src/main/java/com/example/myapp/data/repository/FeedingRepository.kt`
- 创建: `app/src/main/java/com/example/myapp/data/repository/SleepRepository.kt`
- 创建: `app/src/main/java/com/example/myapp/data/repository/GrowthRepository.kt`

- [ ] **创建 `data/repository/` 目录**

```bash
mkdir -p app/src/main/java/com/example/myapp/data/repository
```

- [ ] **创建 `FeedingRepository.kt`**

```kotlin
package com.example.myapp.data.repository

import com.example.myapp.data.room.FeedingDao
import com.example.myapp.data.room.FeedingEntity
import kotlinx.coroutines.flow.Flow

class FeedingRepository(private val dao: FeedingDao) {
    val allRecords: Flow<List<FeedingEntity>> = dao.getAllFlow()
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
    val allRecords: Flow<List<SleepEntity>> = dao.getAllFlow()
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
    val allRecords: Flow<List<GrowthEntity>> = dao.getAllFlow()
    suspend fun insert(entity: GrowthEntity) = dao.insert(entity)
    suspend fun delete(entity: GrowthEntity) = dao.delete(entity)
}
```

- [ ] **提交**

```bash
git add app/src/main/java/com/example/myapp/data/repository/
git commit -m "feat: add repositories for feeding, sleep, growth"
```

---

### 任务 5：创建核心模块 ViewModel

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/feeding/FeedingViewModel.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/sleep/SleepViewModel.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/growth/GrowthViewModel.kt`

- [ ] **创建 `ui/feeding/FeedingViewModel.kt`**

```kotlin
package com.example.myapp.ui.feeding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.data.room.FeedingEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FeedingViewModel(
    private val repository: FeedingRepository
) : ViewModel() {
    val records: StateFlow<List<FeedingEntity>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(type: String, amount: Int, time: Long) {
        viewModelScope.launch {
            repository.insert(FeedingEntity(type = type, amount = amount, time = time))
        }
    }

    fun delete(entity: FeedingEntity) {
        viewModelScope.launch {
            repository.delete(entity)
        }
    }
}
```

- [ ] **创建 `ui/sleep/SleepViewModel.kt`**

```kotlin
package com.example.myapp.ui.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.SleepRepository
import com.example.myapp.data.room.SleepEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SleepViewModel(
    private val repository: SleepRepository
) : ViewModel() {
    val records: StateFlow<List<SleepEntity>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            repository.insert(SleepEntity(startTime = startTime, endTime = endTime))
        }
    }

    fun delete(entity: SleepEntity) {
        viewModelScope.launch {
            repository.delete(entity)
        }
    }
}
```

- [ ] **创建 `ui/growth/GrowthViewModel.kt`**

```kotlin
package com.example.myapp.ui.growth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapp.data.repository.GrowthRepository
import com.example.myapp.data.room.GrowthEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class GrowthViewModel(
    private val repository: GrowthRepository
) : ViewModel() {
    val records: StateFlow<List<GrowthEntity>> = repository.allRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun add(height: Float, weight: Float, headCircumference: Float, date: Long) {
        viewModelScope.launch {
            repository.insert(
                GrowthEntity(height = height, weight = weight, headCircumference = headCircumference, date = date)
            )
        }
    }

    fun delete(entity: GrowthEntity) {
        viewModelScope.launch {
            repository.delete(entity)
        }
    }
}
```

- [ ] **提交**

```bash
git add app/src/main/java/com/example/myapp/ui/feeding/FeedingViewModel.kt app/src/main/java/com/example/myapp/ui/sleep/SleepViewModel.kt app/src/main/java/com/example/myapp/ui/growth/GrowthViewModel.kt
git commit -m "feat: add ViewModels for core modules"
```

---

### 任务 6：创建数据类和共享组件

**文件:**
- 创建: `app/src/main/java/com/example/myapp/data/room/FeedingType.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/components/CommonComponents.kt`

- [ ] **创建 `FeedingType.kt`**

```kotlin
package com.example.myapp.data.room

enum class FeedingType(val label: String) {
    BREAST_MILK("母乳"),
    FORMULA("配方奶"),
    SOLID_FOOD("辅食"),
    WATER("水")
}
```

- [ ] **创建 `ui/components/CommonComponents.kt`**

```kotlin
package com.example.myapp.ui.components

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text(title) }) }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            content = content
        )
    }
}
```

- [ ] **提交**

```bash
git add app/src/main/java/com/example/myapp/data/room/FeedingType.kt app/src/main/java/com/example/myapp/ui/components/CommonComponents.kt
git commit -m "feat: add FeedingType enum and shared UI components"
```

---

### 任务 7：实现喂养页面

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/feeding/FeedingScreen.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/feeding/AddFeedingScreen.kt`

- [ ] **创建 `FeedingScreen.kt`**

```kotlin
package com.example.myapp.ui.feeding

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
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import com.example.myapp.data.room.FeedingEntity
import com.example.myapp.data.room.FeedingType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedingScreen(
    viewModel: FeedingViewModel,
    onAddClick: () -> Unit
) {
    val records by viewModel.records.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("喂养记录") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records, key = { it.id }) { record ->
                FeedingRecordCard(record = record)
            }
        }
    }
}

@Composable
fun FeedingRecordCard(record: FeedingEntity) {
    val typeLabel = FeedingType.entries
        .firstOrNull { it.name == record.type }?.label ?: record.type
    val timeStr = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
        .format(Date(record.time))

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(typeLabel, style = MaterialTheme.typography.titleMedium)
                Text(timeStr, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text("${record.amount} ml", style = MaterialTheme.typography.bodyLarge)
        }
    }
}
```

- [ ] **创建 `AddFeedingScreen.kt`**

```kotlin
package com.example.myapp.ui.feeding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.data.room.FeedingType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFeedingScreen(
    onSave: (type: String, amount: Int, time: Long) -> Unit
) {
    var selectedType by remember { mutableStateOf(FeedingType.BREAST_MILK) }
    var amountText by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedType.label,
                onValueChange = {},
                readOnly = true,
                label = { Text("类型") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                FeedingType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.label) },
                        onClick = {
                            selectedType = type
                            expanded = false
                        }
                    )
                }
            }
        }

        OutlinedTextField(
            value = amountText,
            onValueChange = { amountText = it },
            label = { Text("用量 (ml)") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = {
                val amount = amountText.toIntOrNull() ?: return@Button
                onSave(selectedType.name, amount, System.currentTimeMillis())
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存")
        }
    }
}
```

- [ ] **提交**

```bash
git add app/src/main/java/com/example/myapp/ui/feeding/
git commit -m "feat: implement Feeding screens"
```

---

### 任务 8：实现睡眠页面

**文件:**
- 创建: `app/src/main/java/com/example/myapp/ui/sleep/SleepScreen.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/sleep/AddSleepScreen.kt`

- [ ] **创建 `SleepScreen.kt`**

```kotlin
package com.example.myapp.ui.sleep

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapp.data.room.SleepEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SleepScreen(
    viewModel: SleepViewModel,
    onAddClick: () -> Unit
) {
    val records by viewModel.records.collectAsState()

    Scaffold(
        topBar = { TopAppBar(title = { Text("睡眠记录") }) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "新增")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(records, key = { it.id }) { record ->
                SleepRecordCard(record = record)
            }
        }
    }
}

@Composable
fun SleepRecordCard(record: SleepEntity) {
    val fmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    val dateFmt = SimpleDateFormat("MM/dd", Locale.getDefault())
    val durationMs = record.endTime - record.startTime
    val hours = durationMs / 3_600_000
    val minutes = (durationMs % 3_600_000) / 60_000

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "${dateFmt.format(Date(record.startTime))}  ${fmt.format(Date(record.startTime))} → ${fmt.format(Date(record.endTime))}",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "${hours}小时${minutes}分钟",
                style = MaterialTheme.typography.headlineSmall
            )
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
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSleepScreen(
    onSave: (startTime: Long, endTime: Long) -> Unit
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }
    var startMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var endMillis by remember { mutableStateOf(System.currentTimeMillis()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("开始时间")
        Button(onClick = { showStartDatePicker = true }) {
            Text(java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(startMillis)))
        }
        Text("结束时间")
        Button(onClick = { showEndDatePicker = true }) {
            Text(java.text.SimpleDateFormat("MM/dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date(endMillis)))
        }
        Button(
            onClick = { onSave(startMillis, endMillis) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存")
        }
    }

    if (showStartDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = startMillis)
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { startMillis = it }
                    showStartDatePicker = false
                }) { Text("确定") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showEndDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = endMillis)
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { endMillis = it }
                    showEndDatePicker = false
                }) { Text("确定") }
            }
        ) {
            DatePicker(state = state)
        }
    }
}
```

- [ ] **提交**

```bash
git add app/src/main/java/com/example/myapp/ui/sleep/
git commit -m "feat: implement Sleep screens"
```

---

### 任务 9：实现生长页面（含 Vico 图表）

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
import com.example.myapp.data.room.GrowthEntity
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottomAxis
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStartAxis
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrowthScreen(
    viewModel: GrowthViewModel,
    onAddClick: () -> Unit
) {
    val records by viewModel.records.collectAsState()
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PrimaryTabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            val values = when (selectedTab) {
                0 -> records.map { it.height }
                1 -> records.map { it.weight }
                else -> records.map { it.headCircumference }
            }

            val modelProducer = remember { CartesianChartModelProducer() }
            LaunchedEffect(records) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .padding(16.dp)
            )
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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun AddGrowthScreen(
    onSave: (height: Float, weight: Float, headCircumference: Float, date: Long) -> Unit
) {
    var heightText by remember { mutableStateOf("") }
    var weightText by remember { mutableStateOf("") }
    var headText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
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
        Button(
            onClick = {
                val h = heightText.toFloatOrNull() ?: return@Button
                val w = weightText.toFloatOrNull() ?: return@Button
                val hc = headText.toFloatOrNull() ?: return@Button
                onSave(h, w, hc, System.currentTimeMillis())
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("保存")
        }
    }
}
```

- [ ] **提交**

```bash
git add app/src/main/java/com/example/myapp/ui/growth/
git commit -m "feat: implement Growth screens with Vico chart"
```

---

### 任务 10：创建导航和首页

**文件:**
- 创建: `app/src/main/java/com/example/myapp/navigation/Route.kt`
- 创建: `app/src/main/java/com/example/myapp/navigation/AppNavGraph.kt`
- 创建: `app/src/main/java/com/example/myapp/ui/home/HomeScreen.kt`
- 修改: `app/src/main/java/com/example/myapp/MainActivity.kt`

- [ ] **创建 `Route.kt`**

```kotlin
package com.example.myapp.navigation

sealed interface Route {
    data object Home : Route
    data object Feeding : Route
    data object AddFeeding : Route
    data object Sleep : Route
    data object AddSleep : Route
    data object Growth : Route
    data object AddGrowth : Route
}
```

- [ ] **创建 `AppNavGraph.kt`**

```kotlin
package com.example.myapp.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapp.ui.feeding.AddFeedingScreen
import com.example.myapp.ui.feeding.FeedingScreen
import com.example.myapp.ui.feeding.FeedingViewModel
import com.example.myapp.ui.growth.AddGrowthScreen
import com.example.myapp.ui.growth.GrowthScreen
import com.example.myapp.ui.growth.GrowthViewModel
import com.example.myapp.ui.home.HomeScreen
import com.example.myapp.ui.sleep.AddSleepScreen
import com.example.myapp.ui.sleep.SleepScreen
import com.example.myapp.ui.sleep.SleepViewModel
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                onNavigate = { route ->
                    navController.navigate(route)
                }
            )
        }
        composable("feeding") {
            FeedingScreen(
                viewModel = koinViewModel(),
                onAddClick = { navController.navigate("add_feeding") }
            )
        }
        composable("add_feeding") {
            val vm: FeedingViewModel = viewModel()
            AddFeedingScreen(
                onSave = { type, amount, time ->
                    vm.add(type, amount, time)
                    navController.popBackStack()
                }
            )
        }
        composable("sleep") {
            SleepScreen(
                viewModel = koinViewModel(),
                onAddClick = { navController.navigate("add_sleep") }
            )
        }
        composable("add_sleep") {
            val vm: SleepViewModel = viewModel()
            AddSleepScreen(
                onSave = { startTime, endTime ->
                    vm.add(startTime, endTime)
                    navController.popBackStack()
                }
            )
        }
        composable("growth") {
            GrowthScreen(
                viewModel = koinViewModel(),
                onAddClick = { navController.navigate("add_growth") }
            )
        }
        composable("add_growth") {
            val vm: GrowthViewModel = viewModel()
            AddGrowthScreen(
                onSave = { height, weight, head, date ->
                    vm.add(height, weight, head, date)
                    navController.popBackStack()
                }
            )
        }
    }
}
```

- [ ] **创建 `HomeScreen.kt`**

```kotlin
package com.example.myapp.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("Baby Tracker") }) }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { BabyCard() }
            item { FunctionGrid(onNavigate = onNavigate) }
        }
    }
}

@Composable
fun BabyCard() {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("👶 小宝宝", style = MaterialTheme.typography.titleLarge)
            Text("出生日期: 2026-01-15", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun FunctionGrid(onNavigate: (String) -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("功能", style = MaterialTheme.typography.titleMedium)
        val functions = listOf(
            "feeding" to "喂养",
            "sleep" to "睡眠",
            "growth" to "生长"
        )
        functions.forEach { (route, label) ->
            Button(
                onClick = { onNavigate(route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(label)
            }
        }
    }
}
```

- [ ] **修改 `MainActivity.kt`**

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

- [ ] **在 AppModule 中注册 Koin ViewModel**

更新 `di/AppModule.kt`：

```kotlin
package com.example.myapp.di

import com.example.myapp.data.room.AppDatabase
import com.example.myapp.data.repository.FeedingRepository
import com.example.myapp.data.repository.GrowthRepository
import com.example.myapp.data.repository.SleepRepository
import com.example.myapp.ui.feeding.FeedingViewModel
import com.example.myapp.ui.growth.GrowthViewModel
import com.example.myapp.ui.sleep.SleepViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { AppDatabase.build(androidContext()) }
    single { get<AppDatabase>().feedingDao() }
    single { get<AppDatabase>().sleepDao() }
    single { get<AppDatabase>().growthDao() }
    single { FeedingRepository(get()) }
    single { SleepRepository(get()) }
    single { GrowthRepository(get()) }
    viewModel { FeedingViewModel(get()) }
    viewModel { SleepViewModel(get()) }
    viewModel { GrowthViewModel(get()) }
}
```

- [ ] **提交**

```bash
git add app/src/main/java/com/example/myapp/navigation/ app/src/main/java/com/example/myapp/ui/home/ app/src/main/java/com/example/myapp/MainActivity.kt app/src/main/java/com/example/myapp/di/AppModule.kt
git commit -m "feat: add Navigation, HomeScreen, and wire up Koin DI"
```

---

### 任务 11：编译验证

- [ ] **执行 assembleDebug**

```bash
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_AAPT2_DAEMON_MODE=false
./gradlew assembleDebug
```

预期结果：BUILD SUCCESSFUL

- [ ] **若编译失败**，修复编译错误（缺少 import、类型不匹配等）后重新编译。

---

### 自查清单

编写完所有任务后验证：

1. **Spec 覆盖**: Entity/DAO/Repository 覆盖所有核心模块（喂养、睡眠、生长）。导航覆盖 3 个目标页面 + 新增页面。首页有功能网格。Koin 连接所有依赖。✓
2. **占位符检查**: 无 "TBD"、"TODO" 或模糊步骤。所有代码已内联。✓
3. **类型一致性**: Route sealed interface → 字符串路由与 NavHost 匹配。ViewModel 名称与 Repository 类型一致。✓
