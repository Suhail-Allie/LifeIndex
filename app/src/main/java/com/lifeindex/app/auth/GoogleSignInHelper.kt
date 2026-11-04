package com.lifeindex.app.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

/** Retrieves a Google ID token. The backend verifies it before issuing a LifeIndex session. */
object GoogleSignInHelper {

    suspend fun getGoogleIdToken(context: Context): String {
        val webClientId = context.getString(com.lifeindex.app.R.string.default_web_client_id)

        require(webClientId.isNotBlank()) {
            "Google Web OAuth client ID is missing. Check google-services.json."
        }

        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = CredentialManager.create(context).getCredential(
            context = context,
            request = request
        )

        val credential = result.credential

        if (credential !is CustomCredential ||
            credential.type != GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            throw IllegalStateException("Google did not return a valid ID token.")
        }

        return GoogleIdTokenCredential.createFrom(credential.data).idToken
    }
}