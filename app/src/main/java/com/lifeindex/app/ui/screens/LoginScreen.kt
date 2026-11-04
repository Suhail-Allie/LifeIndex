package com.lifeindex.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.lifeindex.app.auth.GoogleSignInHelper
import com.lifeindex.app.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch
import com.lifeindex.app.util.ValidationUtils

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onRegisterClick: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var googleError by remember { mutableStateOf<String?>(null) }
    var googleLoading by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSuccess) {
        if (uiState.isSuccess) {
            onLoginSuccess()
            authViewModel.clearState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "LifeIndex",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Track anything. Find everything.",
            style = MaterialTheme.typography.bodyLarge
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome back",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                validationError = null
            },
            label = {
                Text("Email")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                validationError = null
            },
            label = {
                Text("Password")
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        validationError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        uiState.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))
        }

        googleError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Button(
            onClick = {

                when {
                    email.isBlank() ->
                        validationError = "Please enter your email."

                    !ValidationUtils.isValidEmail(email) ->
                        validationError = "Please enter a valid email address."

                    !ValidationUtils.isValidLoginPassword(password) ->
                        validationError = "Please enter your password."

                    else -> {
                        validationError = null

                        authViewModel.login(
                            email.trim(),
                            password
                        )
                    }
                }
            },
            enabled = !uiState.isLoading && !googleLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("Log In")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "OR",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = {
                coroutineScope.launch {
                    googleLoading = true
                    googleError = null

                    try {
                        val idToken = GoogleSignInHelper.getGoogleIdToken(context)
                        authViewModel.loginWithGoogle(idToken)
                    } catch (error: Exception) {
                        googleError = error.message
                            ?.takeIf { it.isNotBlank() }
                            ?: "Google sign-in was cancelled or could not be completed."
                    } finally {
                        googleLoading = false
                    }
                }
            },
            enabled = !uiState.isLoading && !googleLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (googleLoading) {
                CircularProgressIndicator()
            } else {
                Text("Continue with Google")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onRegisterClick
        ) {
            Text("Create an account")
        }
    }
}