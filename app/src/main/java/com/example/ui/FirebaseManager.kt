package com.example.ui

import android.content.Context
import android.util.Log
import com.example.model.Transaction
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private var isInitialized = false

    fun initialize(context: Context) {
        // Double check if the default Firebase app is already initialized
        var defaultAppInitialized = false
        try {
            FirebaseApp.getInstance()
            defaultAppInitialized = true
        } catch (e: IllegalStateException) {
            // Default app not initialized
        }

        if (defaultAppInitialized) {
            isInitialized = true
            return
        }

        try {
            if (FirebaseApp.getApps(context).isEmpty()) {
                // Try initializing with google-services.json defaults (automatic)
                FirebaseApp.initializeApp(context)
                defaultAppInitialized = true
                Log.d(TAG, "Firebase initialized automatically.")
            } else {
                Log.d(TAG, "Firebase apps already exist, checking if default app is initialized.")
                try {
                    FirebaseApp.getInstance()
                    defaultAppInitialized = true
                } catch (e: IllegalStateException) {
                    // There are some apps, but default is not one of them. We will initialize default manually below.
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Automatic Firebase initialization failed. Trying manual fallback.", e)
        }

        if (!defaultAppInitialized) {
            try {
                // Fallback initialization to prevent crashes when google-services.json is missing
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:1234567890:android:abcdef")
                    .setProjectId("kopilka-uzwnpa")
                    .setApiKey("placeholder_api_key_for_kopilka")
                    .build()
                FirebaseApp.initializeApp(context, options)
                Log.d(TAG, "Firebase initialized with fallback options.")
                defaultAppInitialized = true
            } catch (fallbackEx: Exception) {
                Log.e(TAG, "Fallback Firebase initialization failed as well.", fallbackEx)
            }
        }

        isInitialized = defaultAppInitialized
    }

    fun getAuth(): FirebaseAuth? {
        return if (isInitialized) {
            try {
                FirebaseAuth.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting FirebaseAuth instance", e)
                null
            }
        } else {
            null
        }
    }

    fun getFirestore(): FirebaseFirestore? {
        return if (isInitialized) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e: Exception) {
                Log.e(TAG, "Error getting FirebaseFirestore instance", e)
                null
            }
        } else {
            null
        }
    }

    fun isUserSignedIn(): Boolean {
        return getAuth()?.currentUser != null
    }

    fun getCurrentUserEmail(): String? {
        return getAuth()?.currentUser?.email
    }

    fun getCurrentUserId(): String? {
        return getAuth()?.currentUser?.uid
    }

    /**
     * Merge remote transactions list with local transactions list.
     */
    fun mergeTransactions(local: List<Transaction>, remote: List<Transaction>): List<Transaction> {
        val mergedMap = LinkedHashMap<String, Transaction>()
        // Put remote first
        for (tx in remote) {
            mergedMap[tx.id] = tx
        }
        // Overwrite or append with local
        for (tx in local) {
            mergedMap[tx.id] = tx
        }
        // Return sorted descending by timestamp
        return mergedMap.values.sortedByDescending { it.timestamp }
    }
}
