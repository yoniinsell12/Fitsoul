package com.fitsoul.app;

import android.app.Application;
import android.util.Log;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import dagger.hilt.android.HiltAndroidApp;

@HiltAndroidApp
public class FitsoulApplication extends Application {
    private static final String TAG = "FitsoulApplication";
    
    @Override
    public void onCreate() {
        try {
            super.onCreate();
            Log.d(TAG, "FitsoulApplication onCreate started");
            
            // Initialize Firebase
            initializeFirebase();
            
            // Initialize any other application-wide components here
            Log.d(TAG, "All application components initialized successfully");
            
            Log.d(TAG, "FitsoulApplication onCreate completed successfully");
        } catch (Exception e) {
            Log.e(TAG, "Critical error in FitsoulApplication onCreate", e);
            // Log the error but don't crash the app completely
        }
    }
    
    private void initializeFirebase() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase initialized successfully");
            } else {
                Log.d(TAG, "Firebase already initialized");
            }
        } catch (Exception e) {
            Log.e(TAG, "Firebase initialization failed", e);
            // Don't crash the app, but log the error
            // The app can still function without Firebase in some cases
        }
    }
}
