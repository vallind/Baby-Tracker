package com.example.myapp.data.room;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile BabyDao _babyDao;

  private volatile FeedingDao _feedingDao;

  private volatile SleepDao _sleepDao;

  private volatile GrowthDao _growthDao;

  private volatile VaccineDao _vaccineDao;

  private volatile HealthProfileDao _healthProfileDao;

  private volatile ReminderDao _reminderDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(1) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `babies` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `gender` TEXT NOT NULL, `birthday` INTEGER NOT NULL, `avatar` TEXT, `birthHeight` REAL NOT NULL, `birthWeight` REAL NOT NULL, `note` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `feeding_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `babyId` INTEGER NOT NULL, `type` TEXT NOT NULL, `amount` INTEGER NOT NULL, `unit` TEXT NOT NULL, `note` TEXT, `createdAt` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `sleep_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `babyId` INTEGER NOT NULL, `startTime` INTEGER NOT NULL, `endTime` INTEGER NOT NULL, `type` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `growth_records` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `babyId` INTEGER NOT NULL, `height` REAL NOT NULL, `weight` REAL NOT NULL, `headCircumference` REAL NOT NULL, `recordDate` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `vaccines` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `babyId` INTEGER NOT NULL, `name` TEXT NOT NULL, `dose` INTEGER NOT NULL, `plannedDate` INTEGER NOT NULL, `completedDate` INTEGER, `status` TEXT NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `health_profiles` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `babyId` INTEGER NOT NULL, `allergies` TEXT, `medicalHistory` TEXT, `doctorNotes` TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS `reminders` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `babyId` INTEGER NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `scheduledAt` INTEGER NOT NULL, `enabled` INTEGER NOT NULL)");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '97123a30549743cec99328d33c6c97af')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `babies`");
        db.execSQL("DROP TABLE IF EXISTS `feeding_records`");
        db.execSQL("DROP TABLE IF EXISTS `sleep_records`");
        db.execSQL("DROP TABLE IF EXISTS `growth_records`");
        db.execSQL("DROP TABLE IF EXISTS `vaccines`");
        db.execSQL("DROP TABLE IF EXISTS `health_profiles`");
        db.execSQL("DROP TABLE IF EXISTS `reminders`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsBabies = new HashMap<String, TableInfo.Column>(8);
        _columnsBabies.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBabies.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBabies.put("gender", new TableInfo.Column("gender", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBabies.put("birthday", new TableInfo.Column("birthday", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBabies.put("avatar", new TableInfo.Column("avatar", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBabies.put("birthHeight", new TableInfo.Column("birthHeight", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBabies.put("birthWeight", new TableInfo.Column("birthWeight", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsBabies.put("note", new TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysBabies = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesBabies = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoBabies = new TableInfo("babies", _columnsBabies, _foreignKeysBabies, _indicesBabies);
        final TableInfo _existingBabies = TableInfo.read(db, "babies");
        if (!_infoBabies.equals(_existingBabies)) {
          return new RoomOpenHelper.ValidationResult(false, "babies(com.example.myapp.data.room.BabyEntity).\n"
                  + " Expected:\n" + _infoBabies + "\n"
                  + " Found:\n" + _existingBabies);
        }
        final HashMap<String, TableInfo.Column> _columnsFeedingRecords = new HashMap<String, TableInfo.Column>(7);
        _columnsFeedingRecords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFeedingRecords.put("babyId", new TableInfo.Column("babyId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFeedingRecords.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFeedingRecords.put("amount", new TableInfo.Column("amount", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFeedingRecords.put("unit", new TableInfo.Column("unit", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFeedingRecords.put("note", new TableInfo.Column("note", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsFeedingRecords.put("createdAt", new TableInfo.Column("createdAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysFeedingRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesFeedingRecords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoFeedingRecords = new TableInfo("feeding_records", _columnsFeedingRecords, _foreignKeysFeedingRecords, _indicesFeedingRecords);
        final TableInfo _existingFeedingRecords = TableInfo.read(db, "feeding_records");
        if (!_infoFeedingRecords.equals(_existingFeedingRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "feeding_records(com.example.myapp.data.room.FeedingEntity).\n"
                  + " Expected:\n" + _infoFeedingRecords + "\n"
                  + " Found:\n" + _existingFeedingRecords);
        }
        final HashMap<String, TableInfo.Column> _columnsSleepRecords = new HashMap<String, TableInfo.Column>(5);
        _columnsSleepRecords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepRecords.put("babyId", new TableInfo.Column("babyId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepRecords.put("startTime", new TableInfo.Column("startTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepRecords.put("endTime", new TableInfo.Column("endTime", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsSleepRecords.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysSleepRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesSleepRecords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoSleepRecords = new TableInfo("sleep_records", _columnsSleepRecords, _foreignKeysSleepRecords, _indicesSleepRecords);
        final TableInfo _existingSleepRecords = TableInfo.read(db, "sleep_records");
        if (!_infoSleepRecords.equals(_existingSleepRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "sleep_records(com.example.myapp.data.room.SleepEntity).\n"
                  + " Expected:\n" + _infoSleepRecords + "\n"
                  + " Found:\n" + _existingSleepRecords);
        }
        final HashMap<String, TableInfo.Column> _columnsGrowthRecords = new HashMap<String, TableInfo.Column>(6);
        _columnsGrowthRecords.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrowthRecords.put("babyId", new TableInfo.Column("babyId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrowthRecords.put("height", new TableInfo.Column("height", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrowthRecords.put("weight", new TableInfo.Column("weight", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrowthRecords.put("headCircumference", new TableInfo.Column("headCircumference", "REAL", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsGrowthRecords.put("recordDate", new TableInfo.Column("recordDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysGrowthRecords = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesGrowthRecords = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoGrowthRecords = new TableInfo("growth_records", _columnsGrowthRecords, _foreignKeysGrowthRecords, _indicesGrowthRecords);
        final TableInfo _existingGrowthRecords = TableInfo.read(db, "growth_records");
        if (!_infoGrowthRecords.equals(_existingGrowthRecords)) {
          return new RoomOpenHelper.ValidationResult(false, "growth_records(com.example.myapp.data.room.GrowthEntity).\n"
                  + " Expected:\n" + _infoGrowthRecords + "\n"
                  + " Found:\n" + _existingGrowthRecords);
        }
        final HashMap<String, TableInfo.Column> _columnsVaccines = new HashMap<String, TableInfo.Column>(7);
        _columnsVaccines.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaccines.put("babyId", new TableInfo.Column("babyId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaccines.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaccines.put("dose", new TableInfo.Column("dose", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaccines.put("plannedDate", new TableInfo.Column("plannedDate", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaccines.put("completedDate", new TableInfo.Column("completedDate", "INTEGER", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsVaccines.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysVaccines = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesVaccines = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoVaccines = new TableInfo("vaccines", _columnsVaccines, _foreignKeysVaccines, _indicesVaccines);
        final TableInfo _existingVaccines = TableInfo.read(db, "vaccines");
        if (!_infoVaccines.equals(_existingVaccines)) {
          return new RoomOpenHelper.ValidationResult(false, "vaccines(com.example.myapp.data.room.VaccineEntity).\n"
                  + " Expected:\n" + _infoVaccines + "\n"
                  + " Found:\n" + _existingVaccines);
        }
        final HashMap<String, TableInfo.Column> _columnsHealthProfiles = new HashMap<String, TableInfo.Column>(5);
        _columnsHealthProfiles.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthProfiles.put("babyId", new TableInfo.Column("babyId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthProfiles.put("allergies", new TableInfo.Column("allergies", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthProfiles.put("medicalHistory", new TableInfo.Column("medicalHistory", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsHealthProfiles.put("doctorNotes", new TableInfo.Column("doctorNotes", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysHealthProfiles = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesHealthProfiles = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoHealthProfiles = new TableInfo("health_profiles", _columnsHealthProfiles, _foreignKeysHealthProfiles, _indicesHealthProfiles);
        final TableInfo _existingHealthProfiles = TableInfo.read(db, "health_profiles");
        if (!_infoHealthProfiles.equals(_existingHealthProfiles)) {
          return new RoomOpenHelper.ValidationResult(false, "health_profiles(com.example.myapp.data.room.HealthProfileEntity).\n"
                  + " Expected:\n" + _infoHealthProfiles + "\n"
                  + " Found:\n" + _existingHealthProfiles);
        }
        final HashMap<String, TableInfo.Column> _columnsReminders = new HashMap<String, TableInfo.Column>(6);
        _columnsReminders.put("id", new TableInfo.Column("id", "INTEGER", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("babyId", new TableInfo.Column("babyId", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("type", new TableInfo.Column("type", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("title", new TableInfo.Column("title", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("scheduledAt", new TableInfo.Column("scheduledAt", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsReminders.put("enabled", new TableInfo.Column("enabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysReminders = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesReminders = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoReminders = new TableInfo("reminders", _columnsReminders, _foreignKeysReminders, _indicesReminders);
        final TableInfo _existingReminders = TableInfo.read(db, "reminders");
        if (!_infoReminders.equals(_existingReminders)) {
          return new RoomOpenHelper.ValidationResult(false, "reminders(com.example.myapp.data.room.ReminderEntity).\n"
                  + " Expected:\n" + _infoReminders + "\n"
                  + " Found:\n" + _existingReminders);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "97123a30549743cec99328d33c6c97af", "c0ccc99febe58188fe9f4171562e54b4");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "babies","feeding_records","sleep_records","growth_records","vaccines","health_profiles","reminders");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `babies`");
      _db.execSQL("DELETE FROM `feeding_records`");
      _db.execSQL("DELETE FROM `sleep_records`");
      _db.execSQL("DELETE FROM `growth_records`");
      _db.execSQL("DELETE FROM `vaccines`");
      _db.execSQL("DELETE FROM `health_profiles`");
      _db.execSQL("DELETE FROM `reminders`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(BabyDao.class, BabyDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(FeedingDao.class, FeedingDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(SleepDao.class, SleepDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(GrowthDao.class, GrowthDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(VaccineDao.class, VaccineDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(HealthProfileDao.class, HealthProfileDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(ReminderDao.class, ReminderDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public BabyDao babyDao() {
    if (_babyDao != null) {
      return _babyDao;
    } else {
      synchronized(this) {
        if(_babyDao == null) {
          _babyDao = new BabyDao_Impl(this);
        }
        return _babyDao;
      }
    }
  }

  @Override
  public FeedingDao feedingDao() {
    if (_feedingDao != null) {
      return _feedingDao;
    } else {
      synchronized(this) {
        if(_feedingDao == null) {
          _feedingDao = new FeedingDao_Impl(this);
        }
        return _feedingDao;
      }
    }
  }

  @Override
  public SleepDao sleepDao() {
    if (_sleepDao != null) {
      return _sleepDao;
    } else {
      synchronized(this) {
        if(_sleepDao == null) {
          _sleepDao = new SleepDao_Impl(this);
        }
        return _sleepDao;
      }
    }
  }

  @Override
  public GrowthDao growthDao() {
    if (_growthDao != null) {
      return _growthDao;
    } else {
      synchronized(this) {
        if(_growthDao == null) {
          _growthDao = new GrowthDao_Impl(this);
        }
        return _growthDao;
      }
    }
  }

  @Override
  public VaccineDao vaccineDao() {
    if (_vaccineDao != null) {
      return _vaccineDao;
    } else {
      synchronized(this) {
        if(_vaccineDao == null) {
          _vaccineDao = new VaccineDao_Impl(this);
        }
        return _vaccineDao;
      }
    }
  }

  @Override
  public HealthProfileDao healthProfileDao() {
    if (_healthProfileDao != null) {
      return _healthProfileDao;
    } else {
      synchronized(this) {
        if(_healthProfileDao == null) {
          _healthProfileDao = new HealthProfileDao_Impl(this);
        }
        return _healthProfileDao;
      }
    }
  }

  @Override
  public ReminderDao reminderDao() {
    if (_reminderDao != null) {
      return _reminderDao;
    } else {
      synchronized(this) {
        if(_reminderDao == null) {
          _reminderDao = new ReminderDao_Impl(this);
        }
        return _reminderDao;
      }
    }
  }
}
