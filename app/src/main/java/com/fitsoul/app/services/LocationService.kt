package com.fitsoul.app.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

class LocationService : Service() {
    companion object {
        private const val TAG = "LocationService"
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "LocationService started - placeholder implementation")
        return START_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "LocationService destroyed")
    }
}
