/*
 * Copyright (C) 2022 ThinkingData
 */

package ai.datatower.analytics.data.persistence;

import android.content.SharedPreferences;

import java.util.concurrent.Future;

/**
 * StorageDisableFlag.
 * */
public class StorageDisableFlag extends SharedPreferencesStorage<Boolean> {

    public StorageDisableFlag(Future<SharedPreferences> loadStoredPreferences) {
        super(loadStoredPreferences, "disableFlag");
    }

    @Override
    protected void save(SharedPreferences.Editor editor, Boolean data) {
        editor.putBoolean(storageKey, data);
        editor.apply();
    }

    @Override
    protected void load(SharedPreferences sharedPreferences) {
        data = sharedPreferences.getBoolean(this.storageKey, false);
    }
}
