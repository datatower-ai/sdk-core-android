/*
 * Copyright (C) 2022 ThinkingData
 */

package com.roiquery.analytics.data.persistence;

import android.content.SharedPreferences;

import java.util.concurrent.Future;

/**
 * StorageReportUrl.
 */
public class StorageReportUrl extends SharedPreferencesStorage<String> {
    public StorageReportUrl(Future<SharedPreferences> loadStoredPreferences) {
        super(loadStoredPreferences, "reportUrl");
    }
}
