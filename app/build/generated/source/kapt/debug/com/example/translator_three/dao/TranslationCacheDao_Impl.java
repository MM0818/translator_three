package com.example.translator_three.dao;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.example.translator_three.model.TranslationCache;
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

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class TranslationCacheDao_Impl implements TranslationCacheDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<TranslationCache> __insertionAdapterOfTranslationCache;

  private final SharedSQLiteStatement __preparedStmtOfDeleteExpiredCache;

  private final SharedSQLiteStatement __preparedStmtOfClearAllCache;

  public TranslationCacheDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfTranslationCache = new EntityInsertionAdapter<TranslationCache>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `translation_cache` (`id`,`sourceLang`,`targetLang`,`sourceText`,`translatedText`,`timestamp`) VALUES (nullif(?, 0),?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final TranslationCache entity) {
        statement.bindLong(1, entity.getId());
        if (entity.getSourceLang() == null) {
          statement.bindNull(2);
        } else {
          statement.bindString(2, entity.getSourceLang());
        }
        if (entity.getTargetLang() == null) {
          statement.bindNull(3);
        } else {
          statement.bindString(3, entity.getTargetLang());
        }
        if (entity.getSourceText() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getSourceText());
        }
        if (entity.getTranslatedText() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getTranslatedText());
        }
        statement.bindLong(6, entity.getTimestamp());
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
    this.__preparedStmtOfClearAllCache = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM translation_cache";
        return _query;
      }
    };
  }

  @Override
  public Object insertCache(final TranslationCache cache,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfTranslationCache.insert(cache);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, continuation);
  }

  @Override
  public Object deleteExpiredCache(final long expireTime,
      final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteExpiredCache.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, expireTime);
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
          __preparedStmtOfDeleteExpiredCache.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Object clearAllCache(final Continuation<? super Unit> continuation) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAllCache.acquire();
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
          __preparedStmtOfClearAllCache.release(_stmt);
        }
      }
    }, continuation);
  }

  @Override
  public Object getCache(final String sourceLang, final String targetLang, final String sourceText,
      final Continuation<? super TranslationCache> continuation) {
    final String _sql = "\n"
            + "        SELECT * FROM translation_cache \n"
            + "        WHERE sourceLang = ? \n"
            + "        AND targetLang = ? \n"
            + "        AND sourceText = ? \n"
            + "        LIMIT 1\n"
            + "    ";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 3);
    int _argIndex = 1;
    if (sourceLang == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, sourceLang);
    }
    _argIndex = 2;
    if (targetLang == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, targetLang);
    }
    _argIndex = 3;
    if (sourceText == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, sourceText);
    }
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<TranslationCache>() {
      @Override
      @Nullable
      public TranslationCache call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfSourceLang = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceLang");
          final int _cursorIndexOfTargetLang = CursorUtil.getColumnIndexOrThrow(_cursor, "targetLang");
          final int _cursorIndexOfSourceText = CursorUtil.getColumnIndexOrThrow(_cursor, "sourceText");
          final int _cursorIndexOfTranslatedText = CursorUtil.getColumnIndexOrThrow(_cursor, "translatedText");
          final int _cursorIndexOfTimestamp = CursorUtil.getColumnIndexOrThrow(_cursor, "timestamp");
          final TranslationCache _result;
          if (_cursor.moveToFirst()) {
            final long _tmpId;
            _tmpId = _cursor.getLong(_cursorIndexOfId);
            final String _tmpSourceLang;
            if (_cursor.isNull(_cursorIndexOfSourceLang)) {
              _tmpSourceLang = null;
            } else {
              _tmpSourceLang = _cursor.getString(_cursorIndexOfSourceLang);
            }
            final String _tmpTargetLang;
            if (_cursor.isNull(_cursorIndexOfTargetLang)) {
              _tmpTargetLang = null;
            } else {
              _tmpTargetLang = _cursor.getString(_cursorIndexOfTargetLang);
            }
            final String _tmpSourceText;
            if (_cursor.isNull(_cursorIndexOfSourceText)) {
              _tmpSourceText = null;
            } else {
              _tmpSourceText = _cursor.getString(_cursorIndexOfSourceText);
            }
            final String _tmpTranslatedText;
            if (_cursor.isNull(_cursorIndexOfTranslatedText)) {
              _tmpTranslatedText = null;
            } else {
              _tmpTranslatedText = _cursor.getString(_cursorIndexOfTranslatedText);
            }
            final long _tmpTimestamp;
            _tmpTimestamp = _cursor.getLong(_cursorIndexOfTimestamp);
            _result = new TranslationCache(_tmpId,_tmpSourceLang,_tmpTargetLang,_tmpSourceText,_tmpTranslatedText,_tmpTimestamp);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, continuation);
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
