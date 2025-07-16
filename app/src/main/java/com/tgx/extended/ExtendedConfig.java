/*
 * This source code is part of the eXtended project for Android.
 *
 * You are free to use, modify, and distribute this code.
 * However, please act responsibly: give proper credit to the original author.
 *
 * Copyright © 2025 @whitesiemens
 *
*/

package com.tgx.extended;

import android.os.SystemClock;
import androidx.annotation.NonNull;
import org.drinkmore.Tracer;
import org.thunderdog.challegram.Log;
import org.thunderdog.challegram.tool.UI;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.atomic.AtomicBoolean;

import me.vkryl.core.reference.ReferenceList;
import me.vkryl.leveldb.LevelDB;

public class ExtendedConfig {

  private static final int VERSION = 1;
  private static final String KEY_VERSION = "version";
  private final LevelDB config;
  private ReferenceList<SettingsChangeListener> listeners;

  private ExtendedConfig() {
    File configDir = new File(UI.getAppContext().getFilesDir(), "extended_config");
    if (!configDir.exists() && !configDir.mkdir()) {
      throw new IllegalStateException("Unable to create working directory");
    }

    long start = SystemClock.uptimeMillis();
    config = new LevelDB(new File(configDir, "db").getPath(), true, new LevelDB.ErrorHandler() {
      @Override
      public boolean onFatalError(LevelDB db, Throwable error) {
        Tracer.onDatabaseError(error);
        return true;
      }

      @Override
      public void onError(LevelDB db, String message, Throwable error) {
        android.util.Log.e(Log.LOG_TAG, message, error);
      }
    });

    int dbVersion = 0;
    try {
      dbVersion = Math.max(0, config.tryGetInt(KEY_VERSION));
    } catch (FileNotFoundException ignored) {}

    if (dbVersion > VERSION) {
      Log.e("Downgrading database version: %d -> %d", dbVersion, VERSION);
      config.putInt(KEY_VERSION, VERSION);
    }

    for (int v = dbVersion + 1; v <= VERSION; v++) {
      config.edit().putInt(KEY_VERSION, v).apply();
    }

    Log.i("Opened database in %dms", SystemClock.uptimeMillis() - start);

    initializeSettings();
  }

  // Lazy-loaded singleton via static holder
  private static class Holder {
    private static final ExtendedConfig INSTANCE;
    static {
      if (hasInstance.getAndSet(true)) throw new AssertionError();
      INSTANCE = new ExtendedConfig();
    }
  }

  private static final AtomicBoolean hasInstance = new AtomicBoolean(false);

  public static ExtendedConfig instance() {
    return Holder.INSTANCE;
  }

  private void initializeSettings() {
    for (Setting setting : Setting.values()) {
      if (!containsKey(setting.key)) {
        putBoolean(setting.key, setting.defaultValue);
        setting.value = setting.defaultValue;
      } else {
        setting.value = getBoolean(setting.key, setting.defaultValue);
      }
    }
  }

  // eXtended settings
  public enum Setting {
    // General
    SHOW_IDS("show_ids", false, true);

    public final String key;
    public final boolean defaultValue;
    public final boolean shouldNotify;
    public boolean value;

    Setting(String key, boolean defaultValue, boolean shouldNotify) {
      this.key = key;
      this.defaultValue = defaultValue;
      this.shouldNotify = shouldNotify;
    }
  }

  // Listener logic
  public interface SettingsChangeListener {
    void onSettingsChanged(Setting setting, boolean newVal, boolean oldVal);
  }

  public void addSettingsListener(SettingsChangeListener l) {
    if (listeners == null) listeners = new ReferenceList<>();
    listeners.add(l);
  }

  public void removeSettingsListener(SettingsChangeListener l) {
    if (listeners != null) listeners.remove(l);
  }

  private void notifyClientListeners(Setting setting, boolean newVal, boolean oldVal) {
    if (listeners != null) {
      for (SettingsChangeListener l : listeners)
        l.onSettingsChanged(setting, newVal, oldVal);
    }
  }

  public void toggleSetting(Setting setting) {
    boolean oldValue = setting.value;
    boolean newValue = !oldValue;
    setting.value = newValue;
    putBoolean(setting.key, newValue);
    if (setting.shouldNotify) {
      notifyClientListeners(setting, newValue, oldValue);
    }
  }

  public boolean get(Setting setting) {
    return setting.value;
  }

  // LevelDB
  public LevelDB edit() { return config.edit(); }
  public void remove(String key) { config.remove(key); }
  public void putLong(String key, long value) { config.putLong(key, value); }
  public long getLong(String key, long def) { return config.getLong(key, def); }
  public void putLongArray(String key, long[] value) { config.putLongArray(key, value); }
  public long[] getLongArray(String key) { return config.getLongArray(key); }
  public void putInt(String key, int value) { config.putInt(key, value); }
  public int getInt(String key, int def) { return config.getInt(key, def); }
  public void putFloat(String key, float value) { config.putFloat(key, value); }
  public float getFloat(String key, float def) { return config.getFloat(key, def); }
  public void putBoolean(String key, boolean value) { config.putBoolean(key, value); }
  public boolean getBoolean(String key, boolean def) { return config.getBoolean(key, def); }
  public void putString(String key, @NonNull String value) { config.putString(key, value); }
  public String getString(String key, String def) { return config.getString(key, def); }
  public boolean containsKey(String key) { return config.contains(key); }
  public LevelDB config() { return config; }

}
