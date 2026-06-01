package com.example.myapp.data.room;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityDeletionOrUpdateAdapter;
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
public final class GrowthDao_Impl implements GrowthDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<GrowthEntity> __insertionAdapterOfGrowthEntity;

  private final EntityDeletionOrUpdateAdapter<GrowthEntity> __deletionAdapterOfGrowthEntity;

  public GrowthDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfGrowthEntity = new EntityInsertionAdapter<GrowthEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `growth_records` (`id`,`babyId`,`height`,`weight`,`headCircumference`,`recordDate`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GrowthEntity entity) {
        statement.bindLong(1, entity.getId());
        statement.bindLong(2, entity.getBabyId());
        statement.bindDouble(3, entity.getHeight());
        statement.bindDouble(4, entity.getWeight());
        statement.bindDouble(5, entity.getHeadCircumference());
        statement.bindLong(6, entity.getRecordDate());
      }
    };
    this.__deletionAdapterOfGrowthEntity = new EntityDeletionOrUpdateAdapter<GrowthEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "DELETE FROM `growth_records` WHERE `id` = ?";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final GrowthEntity entity) {
        statement.bindLong(1, entity.getId());
      }
    };
  }

  @Override
  public Object insert(final GrowthEntity entity, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfGrowthEntity.insert(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object delete(final GrowthEntity entity, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __deletionAdapterOfGrowthEntity.handle(entity);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<GrowthEntity>> getAllByBabyFlow(final long babyId) {
    final String _sql = "SELECT * FROM growth_records WHERE babyId = ? ORDER BY recordDate DESC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, babyId);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"growth_records"}, new Callable<List<GrowthEntity>>() {
      @Override
      @NonNull
      public List<GrowthEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfBabyId = CursorUtil.getColumnIndexOrThrow(_cursor, "babyId");
          final int _cursorIndexOfHeight = CursorUtil.getColumnIndexOrThrow(_cursor, "height");
          final int _cursorIndexOfWeight = CursorUtil.getColumnIndexOrThrow(_cursor, "weight");
          final int _cursorIndexOfHeadCircumference = CursorUtil.getColumnIndexOrThrow(_cursor, "headCircumference");
          final int _cursorIndexOfRecordDate = CursorUtil.getColumnIndexOrThrow(_cursor, "recordDate");
          final List<GrowthEntity> _result = new ArrayList<GrowthEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final GrowthEntity _item;
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final long _tmpBabyId;
            _tmpBabyId = _cursor.getLong(_cursorIndexOfBabyId);
            final float _tmpHeight;
            _tmpHeight = _cursor.getFloat(_cursorIndexOfHeight);
            final float _tmpWeight;
            _tmpWeight = _cursor.getFloat(_cursorIndexOfWeight);
            final float _tmpHeadCircumference;
            _tmpHeadCircumference = _cursor.getFloat(_cursorIndexOfHeadCircumference);
            final long _tmpRecordDate;
            _tmpRecordDate = _cursor.getLong(_cursorIndexOfRecordDate);
            _item = new GrowthEntity(_tmpId,_tmpBabyId,_tmpHeight,_tmpWeight,_tmpHeadCircumference,_tmpRecordDate);
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
