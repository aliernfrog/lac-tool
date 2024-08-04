package com.aliernfrog.lactool.impl

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.aliernfrog.lactool.TAG
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class CreditData(
    val name: Any,
    val githubUsername: String? = null,
    val description: Any,
    val link: String? = githubUsername?.let { "https://github.com/$githubUsername" }
) {
    private var fetchedAvatar = false
    var avatarURL by mutableStateOf<String?>(null)

    suspend fun fetchAvatar() {
        if (fetchedAvatar) return
        fetchedAvatar = true
        if (githubUsername == null) return
        withContext(Dispatchers.IO) {
            try {
                val res = URL("https://api.github.com/users/$githubUsername").readText()
                val json = JSONObject(res)
                avatarURL = json.getString("avatar_url")
            } catch (e: Exception) {
                Log.e(TAG, "CreditData/fetchAvatar: ", e)
            }
        }
    }
}