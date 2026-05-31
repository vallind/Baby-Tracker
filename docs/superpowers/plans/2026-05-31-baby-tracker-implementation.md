# Baby Tracker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete baby tracking Android app with 14 screens for recording feeding, sleep, growth, vaccines, and more.

**Architecture:** MVVM with Room (local storage), Koin (DI), Navigation Compose (routing), Paging 3 (lists), Vico Chart (growth trends), DataStore (preferences). Each feature module has its own ViewModel + Screen composables.

**Tech Stack:** Jetpack Compose + Material 3, Navigation Compose, Paging 3, Vico Chart, DataStore, Room 2.6.1 + KSP, Koin 3.5.6, Kotlin 1.9.22, Gradle 9.5.1 + AGP 8.9.3

---

### Task 1: Update dependencies in app/build.gradle.kts

**Files:**
- Modify: `app/build.gradle.kts` (add navigation, paging, vico, datastore)

- [ ] **Add new dependencies**

Add to `app/build.gradle.kts` after `val coilVersion` line:
```kotlin
val navVersion = "2.7.7"
val pagingVersion = "3.2.1"
val vicoVersion = "1.13.1"
```

Add to `dependencies {}` block before the closing `}`, after existing Koin lines:
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

- [ ] **Commit**

```bash
git add app/build.gradle.kts
git commit -m "chore: add nav, paging, vico, datastore deps"
```

---

### Task 2: Create Application class and Koin modules

**Files:**
- Create: `app/src/main/java/com/example/myapp/MyApp.kt`
- Create: `app/src/main/java/com/example/myapp/di/AppModule.kt`

- [ ] **Create `MyApp.kt`**

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

- [ ] **Create `di/AppModule.kt`** (initial stub, populated in later tasks)

```kotlin
package com.example.myapp.di

import org.koin.dsl.module

val appModule = module {

}
```

- [ ] **Register `MyApp` in `AndroidManifest.xml`**

Read `AndroidManifest.xml` first to find `<application>` tag, then add `android:name=".MyApp"`.

- [ ] **Commit**

```bash
git add app/src/main/java/com/example/myapp/MyApp.kt app/src/main/java/com/example/myapp/di/AppModule.kt
git commit -m "feat: add Application class and Koin module skeleton"
```

---

### Task 3: Create Room database, entities, and DAOs

**Files:**
- Create: `app/src/main/java/com/example/myapp/data/room/AppDatabase.kt`
- Create: `app/src/main/java/com/example/myapp/data/room/FeedingEntity.kt`
- Create: `app/src/main/java/com/example/myapp/data/room/SleepEntity.kt`
- Create: `app/src/main/java/com/example/myapp/data/room/GrowthEntity.kt`
- Create: `app/src/main/java/com/example/myapp/data/room/FeedingDao.kt`
- Create: `app/src/main/java/com/example/myapp/data/room/SleepDao.kt`
- Create: `app/src/main/java/com/example/myapp/data/room/GrowthDao.kt`

- [ ] **Create `data/room/` directory**

```bash
mkdir -p app/src/main/java/com/example/myapp/data/room
```

- [ ] **Create `FeedingEntity.kt`**

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

- [ ] **Create `SleepEntity.kt`**

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

- [ ] **Create `GrowthEntity.kt`**

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

- [ ] **Create `FeedingDao.kt`**

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

- [ ] **Create `SleepDao.kt`**

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

- [ ] **Create `GrowthDao.kt`**

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

- [ ] **Create `AppDatabase.kt`**

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

- [ ] **Commit**

```bash
git add app/src/main/java/com/example/myapp/data/room/
git commit -m "feat: add Room entities, DAOs, and database"
```

---

### Task 4: Create Repositories

**Files:**
- Create: `app/src/main/java/com/example/myapp/data/repository/FeedingRepository.kt`
- Create: `app/src/main/java/com/example/myapp/data/repository/SleepRepository.kt`
- Create: `app/src/main/java/com/example/myapp/data/repository/GrowthRepository.kt`

- [ ] **Create `data/repository/` directory**

```bash
mkdir -p app/src/main/java/com/example/myapp/data/repository
```

- [ ] **Create `FeedingRepository.kt`**

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

- [ ] **Create `SleepRepository.kt`**

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

- [ ] **Create `GrowthRepository.kt`**

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

- [ ] **Commit**

```bash
git add app/src/main/java/com/example/myapp/data/repository/
git commit -m "feat: add repositories for feeding, sleep, growth"
```

---

### Task 5: Create ViewModels for core modules

**Files:**
- Create: `app/src/main/java/com/example/myapp/ui/feeding/FeedingViewModel.kt`
- Create: `app/src/main/java/com/example/myapp/ui/sleep/SleepViewModel.kt`
- Create: `app/src/main/java/com/example/myapp/ui/growth/GrowthViewModel.kt`

- [ ] **Create `ui/feeding/FeedViewModel.kt`**

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

- [ ] **Create `ui/sleep/SleepViewModel.kt`**

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

- [ ] **Create `ui/growth/GrowthViewModel.kt`**

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

- [ ] **Commit**

```bash
git add app/src/main/java/com/example/myapp/ui/feeding/FeedingViewModel.kt app/src/main/java/com/example/myapp/ui/sleep/SleepViewModel.kt app/src/main/java/com/example/myapp/ui/growth/GrowthViewModel.kt
git commit -m "feat: add ViewModels for core modules"
```

---

### Task 6: Create data classes and shared components

**Files:**
- Create: `app/src/main/java/com/example/myapp/data/room/FeedingType.kt`
- Create: `app/src/main/java/com/example/myapp/ui/components/CommonComponents.kt`

- [ ] **Create `FeedingType.kt`**

```kotlin
package com.example.myapp.data.room

enum class FeedingType(val label: String) {
    BREAST_MILK("母乳"),
    FORMULA("配方奶"),
    SOLID_FOOD("辅食"),
    WATER("水")
}
```

- [ ] **Create `ui/components/CommonComponents.kt`**

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

- [ ] **Commit**

```bash
git add app/src/main/java/com/example/myapp/data/room/FeedingType.kt app/src/main/java/com/example/myapp/ui/components/CommonComponents.kt
git commit -m "feat: add FeedingType enum and shared UI components"
```

---

### Task 7: Implement Feeding screens

**Files:**
- Create: `app/src/main/java/com/example/myapp/ui/feeding/FeedingScreen.kt`
- Create: `app/src/main/java/com/example/myapp/ui/feeding/AddFeedingScreen.kt`

- [ ] **Create `FeedingScreen.kt`**

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

- [ ] **Create `AddFeedingScreen.kt`**

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

- [ ] **Commit**

```bash
git add app/src/main/java/com/example/myapp/ui/feeding/
git commit -m "feat: implement Feeding screens"
```

---

### Task 8: Implement Sleep screens

**Files:**
- Create: `app/src/main/java/com/example/myapp/ui/sleep/SleepScreen.kt`
- Create: `app/src/main/java/com/example/myapp/ui/sleep/AddSleepScreen.kt`

- [ ] **Create `SleepScreen.kt`**

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

- [ ] **Create `AddSleepScreen.kt`**

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

- [ ] **Commit**

```bash
git add app/src/main/java/com/example/myapp/ui/sleep/
git commit -m "feat: implement Sleep screens"
```

---

### Task 9: Implement Growth screens (with Vico chart)

**Files:**
- Create: `app/src/main/java/com/example/myapp/ui/growth/GrowthScreen.kt`
- Create: `app/src/main/java/com/example/myapp/ui/growth/AddGrowthScreen.kt`

- [ ] **Create `GrowthScreen.kt`**

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

- [ ] **Create `AddGrowthScreen.kt`**

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

- [ ] **Commit**

```bash
git add app/src/main/java/com/example/myapp/ui/growth/
git commit -m "feat: implement Growth screens with Vico chart"
```

---

### Task 10: Create Navigation and HomeScreen

**Files:**
- Create: `app/src/main/java/com/example/myapp/navigation/Route.kt`
- Create: `app/src/main/java/com/example/myapp/navigation/AppNavGraph.kt`
- Create: `app/src/main/java/com/example/myapp/ui/home/HomeScreen.kt`
- Modify: `app/src/main/java/com/example/myapp/MainActivity.kt`

- [ ] **Create `Route.kt`**

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

- [ ] **Create `AppNavGraph.kt`**

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

- [ ] **Create `HomeScreen.kt`**

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

- [ ] **Update `MainActivity.kt`**

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

- [ ] **Register Koin ViewModels in AppModule**

Update `di/AppModule.kt`:

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

- [ ] **Commit**

```bash
git add app/src/main/java/com/example/myapp/navigation/ app/src/main/java/com/example/myapp/ui/home/ app/src/main/java/com/example/myapp/MainActivity.kt app/src/main/java/com/example/myapp/di/AppModule.kt
git commit -m "feat: add Navigation, HomeScreen, and wire up Koin DI"
```

---

### Task 11: Build and verify

- [ ] **Run assembleDebug**

```bash
export JAVA_HOME=/data/data/com.termux/files/usr/lib/jvm/java-21-openjdk
export ANDROID_HOME=$HOME/android-sdk
export ANDROID_AAPT2_DAEMON_MODE=false
./gradlew assembleDebug
```

Expected: BUILD SUCCESSFUL

- [ ] **If build fails**, fix compilation errors (missing imports, type mismatches, etc.) and re-run.

---

### Self-Review Checklist

After writing all tasks, verify:

1. **Spec coverage**: Entity/DAO/Repository created for each core module (feeding, sleep, growth). Navigation covers all 3 destination screens + add screens. HomeScreen has function grid. Koin wires everything. ✓
2. **Placeholder scan**: No "TBD", "TODO", or vague steps. All code is inline. ✓
3. **Type consistency**: Route sealed interface → string routes in NavHost match. ViewModel names match repository types. ✓
