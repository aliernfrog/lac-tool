package com.aliernfrog.lactool.impl

import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resumeWithException

class SAFFileCreator(activity: AppCompatActivity, mimeType: String) {
    private var continuation: ((Uri?) -> Unit)? = null

    private val createDocumentLauncher = activity.registerForActivityResult(
        ActivityResultContracts.CreateDocument(mimeType)
    ) { uri ->
        continuation?.invoke(uri)
        continuation = null
    }

    suspend fun createFile(suggestedName: String): Uri? {
        return suspendCancellableCoroutine { cont ->
            this.continuation = { uri ->
                if (cont.isActive) cont.resume(uri) { cause, _, _ ->
                    cont.cancel(cause)
                }
            }
            try {
                createDocumentLauncher.launch(suggestedName)
            } catch (e: Exception) {
                cont.resumeWithException(e)
                this.continuation = null
            }
            cont.invokeOnCancellation {
                this.continuation = null
            }
        }
    }
}