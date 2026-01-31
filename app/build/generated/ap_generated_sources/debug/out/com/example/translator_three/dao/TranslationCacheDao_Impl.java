package com.example.translator_three.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.translator_three.model.TranslationCache;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.Collections;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TranslationCacheDao_Impl implements TranslationCacheDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TranslationCache> __insertionAdapterOfTranslationCache;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpiredCache;

  public TranslationCacheDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTranslationCache = new EntityInsertionAdapter<TranslationCache>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR ABORT INTO `translation_cache` (`cacheKey`,`result`,`timestamp`) VALUES (?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          final TranslationCache entity) {
        if (entity.getCacheKey() == null) {
          statement.bindNull(1);
        } else {
          statement.bindString(1, entity.getCacheKey());
        }
        if (entity.getResult() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getResult());
        }
        statement.bindLong(3, entity.getTimestamp());
      }
    };
    this.__preparedStmtOfDeleteExpiredCache = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM translation_cache WHERE timestamp < ?";
        return _query;
      }
    };
  }

  @Override
  public void insertCache(final TranslationCache cache) {
    __db.assertNotSuspendingTransaction();
    __db.beginTransaction();
    try {
      __insertionAdapterOfTranslationCache.insert(cache);
      __db.setTransactionSuccessful();
    } finally {
      __db.endTransaction();
    }
  }

  @Override
  public void deleteExpiredCache(final long expireTime) {
    __db.assertNotSuspendingTransaction();
    final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExpiredCache.acquire();
    int _argIndex = 1;
    _stmt.bindLong(_argIndex, expireTime);
    try {
      __db.beginTransaction();
      try {
        _stmt.executeUpdateDelete();
        __db.setTransactionSuccessful();
      } finally {
        __db.endTransaction();
      }
    } finally {
      __preparedStmtOfDeleteExpiredCache.release(_stmt);
    }
  }

  @Override
  public TranslationCache getCache(final String cacheKey) {
    final String _sql = "SELECT * FROM translation_cache WHERE cacheKey = ? LIMIT 1";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    if (cacheKey == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, cacheKey);
    }
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfCacheKey = CursorUtil.getColumnIndexOrThrow(_cursor, "cacheKey");
      final int _cursorIndexOfResult = CursorUtil.getColumnIndexOrThrow(_cursor, "result");
      final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
      final TranslationCache _result;
      if (_cursor.moveToFirst()) {
        final String _tmpCacheKey;
        if (_cursor.isNull(_cursorIndexOfCacheKey)) {
          _tmpCacheKey = null;
        } else {
          _tmpCacheKey = _cursor.getString(_cursorIndexOfCacheKey);
        }
        final String _tmpResult;
        if (_cursor.isNull(_cursorIndexOfResult)) {
          _tmpResult = null;
        } else {
          _tmpResult = _cursor.getString(_cursorIndexOfResult);
        }
        final long _tmpTimestamp;
        _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
        _result = new TranslationCache(_tmpCacheKey,_tmpResult,_tmpTimestamp);
      } else {
        _result = null;
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
