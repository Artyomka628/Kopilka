package com.example.ui

import android.content.Context
import android.util.Log
import com.example.model.Transaction
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object FirebaseManager {
    private const val TAG = "FirebaseManager"
    private var isInitialized = false

    fun initialize(context: Context) {
        isInitialized = try {
            val app = try {
                FirebaseApp.getInstance()
            } catch (_: IllegalStateException) {
                FirebaseApp.initializeApp(context)
            }
            app != null
        } catch (e: Exception) {
            Log.w(TAG, "Firebase initialization failed.", e)
            false
        }

        if (!isInitialized) {
            Log.w(TAG, "Firebase is not configured; authentication and cloud sync are disabled.")
        }
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
