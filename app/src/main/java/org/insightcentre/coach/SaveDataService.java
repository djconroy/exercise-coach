package org.insightcentre.coach;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;

public class SaveDataService extends IntentService {
    public static final String EXTRA_CONTENT_VALUES = "content values";
    public static final String EXTRA_NEW_EXTRA_SESSION = "new extra session";

    public SaveDataService() {
        super("SaveDataService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Uri uri = intent.getData();
            ContentValues values = intent.getParcelableExtra(EXTRA_CONTENT_VALUES);

            if (intent.getBooleanExtra(EXTRA_NEW_EXTRA_SESSION, false)) {
                getContentResolver().insert(uri, values);
            } else {
                getContentResolver().update(uri, values, null, null);
            }
        }
    }
}
