package com.example.myapp.data.room;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Long;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class VaccineDao_Impl implements VaccineDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<VaccineEntity> __insertionAdapterOfVaccineEntity;

  private final SharedSQLiteStatement __preparedStmtOfMarkCompleted;

  public VaccineDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfVaccineEntity = new EntityInsertionAdapter<VaccineEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `vaccines` (`id`,`babyId`,`name`,`dose`,`plannedDate`,`completedDate`,`status`) VALUES (nullif(?, 0),?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final VaccineEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getBabyId());
        statement.bindString(3, entity.getName());
        statement.bindLong(4, entity.getDose());
        statement.bindLong(5, entity.getPlannedDate());
        if (entity.getCompletedDate() == null) {
          statement.bindNull(6);
        } else {
          statement.bindLong(6, entity.getCompletedDate());
        }
        statement.bindString(7, entity.getStatus());
      }
    };
    this.__preparedStmtOfMarkCompleted = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE vaccines SET status = ?, completedDate = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insert(final VaccineEntity entity, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfVaccineEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object markCompleted(final long id, final String status, final long completedDate,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfMarkCompleted.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, status);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, completedDate);
        _argIndex = 3;
        _stmt.bindLong(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfMarkCompleted.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<VaccineEntity>> getAllByBabyFlow(final long babyId) {
    final String _sql = "SELECT * FROM vaccines WHERE babyId = ? ORDER BY plannedDate ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, babyId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"vaccines"}, new Callable<List<VaccineEntity>>() {
      @Override
      @NonNull
      public List<VaccineEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBabyId = CursorUtil.getColumnIndexOrThrow(_cursor, "babyId");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfDose = CursorUtil.getColumnIndexOrThrow(_cursor, "dose");
          final int _cursorIndexOfPlannedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "plannedDate");
          final int _cursorIndexOfCompletedDate = CursorUtil.getColumnIndexOrThrow(_cursor, "completedDate");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final List<VaccineEntity> _result = new ArrayList<VaccineEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final VaccineEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpBabyId;
            _tmpBabyId = _cursor.getLong(_cursorIndexOfBabyId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final int _tmpDose;
            _tmpDose = _cursor.getInt(_cursorIndexOfDose);
            final long _tmpPlannedDate;
            _tmpPlannedDate = _cursor.getLong(_cursorIndexOfPlannedDate);
            final Long _tmpCompletedDate;
            if (_cursor.isNull(_cursorIndexOfCompletedDate)) {
              _tmpCompletedDate = null;
            } else {
              _tmpCompletedDate = _cursor.getLong(_cursorIndexOfCompletedDate);
            }
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            _item = new VaccineEntity(_tmpId,_tmpBabyId,_tmpName,_tmpDose,_tmpPlannedDate,_tmpCompletedDate,_tmpStatus);
            _result.add(_item);
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
