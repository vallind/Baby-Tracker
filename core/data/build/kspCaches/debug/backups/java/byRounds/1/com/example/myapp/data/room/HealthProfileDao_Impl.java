package com.example.myapp.data.room;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class HealthProfileDao_Impl implements HealthProfileDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<HealthProfileEntity> __insertionAdapterOfHealthProfileEntity;

  public HealthProfileDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfHealthProfileEntity = new EntityInsertionAdapter<HealthProfileEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `health_profiles` (`id`,`babyId`,`allergies`,`medicalHistory`,`doctorNotes`) VALUES (nullif(?, 0),?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final HealthProfileEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getBabyId());
        if (entity.getAllergies() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getAllergies());
        }
        if (entity.getMedicalHistory() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getMedicalHistory());
        }
        if (entity.getDoctorNotes() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getDoctorNotes());
        }
      }
    };
  }

  @Override
  public Object insert(final HealthProfileEntity entity,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfHealthProfileEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<HealthProfileEntity> getByBabyFlow(final long babyId) {
    final String _sql = "SELECT * FROM health_profiles WHERE babyId = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, babyId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"health_profiles"}, new Callable<HealthProfileEntity>() {
      @Override
      @Nullable
      public HealthProfileEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBabyId = CursorUtil.getColumnIndexOrThrow(_cursor, "babyId");
          final int _cursorIndexOfAllergies = CursorUtil.getColumnIndexOrThrow(_cursor, "allergies");
          final int _cursorIndexOfMedicalHistory = CursorUtil.getColumnIndexOrThrow(_cursor, "medicalHistory");
          final int _cursorIndexOfDoctorNotes = CursorUtil.getColumnIndexOrThrow(_cursor, "doctorNotes");
          final HealthProfileEntity _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpBabyId;
            _tmpBabyId = _cursor.getLong(_cursorIndexOfBabyId);
            final String _tmpAllergies;
            if (_cursor.isNull(_cursorIndexOfAllergies)) {
              _tmpAllergies = null;
            } else {
              _tmpAllergies = _cursor.getString(_cursorIndexOfAllergies);
            }
            final String _tmpMedicalHistory;
            if (_cursor.isNull(_cursorIndexOfMedicalHistory)) {
              _tmpMedicalHistory = null;
            } else {
              _tmpMedicalHistory = _cursor.getString(_cursorIndexOfMedicalHistory);
            }
            final String _tmpDoctorNotes;
            if (_cursor.isNull(_cursorIndexOfDoctorNotes)) {
              _tmpDoctorNotes = null;
            } else {
              _tmpDoctorNotes = _cursor.getString(_cursorIndexOfDoctorNotes);
            }
            _result = new HealthProfileEntity(_tmpId,_tmpBabyId,_tmpAllergies,_tmpMedicalHistory,_tmpDoctorNotes);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
