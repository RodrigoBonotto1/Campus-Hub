package com.example.campus_hub

import android.content.Intent
import android.widget.Button
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class LoginActivityTest {

    @Before
    fun setUp() {
        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        }
    }

    @Test
    fun testLoginUiComponentsAndNavigation() {
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val edtEmail = activity.findViewById<EditText>(R.id.edtEmail)
                val edtSenha = activity.findViewById<EditText>(R.id.edtSenha)
                val btnEntrar = activity.findViewById<Button>(R.id.btnEntrar)
                val btnRecuperar = activity.findViewById<Button>(R.id.btnRecuperar)
                val btnCriarConta = activity.findViewById<Button>(R.id.btnCriarConta)

                assertNotNull(edtEmail)
                assertNotNull(edtSenha)
                assertNotNull(btnEntrar)
                assertNotNull(btnRecuperar)
                assertNotNull(btnCriarConta)

                // Test navigation to ForgotPasswordActivity
                btnRecuperar.performClick()
                val expectedIntentForgot = Intent(activity, ForgotPasswordActivity::class.java)
                val actualIntentForgot = shadowOf(activity).nextStartedActivity
                assertEquals(expectedIntentForgot.component, actualIntentForgot.component)
            }
        }
    }

    @Test
    fun testLoginNavigationToRegister() {
        ActivityScenario.launch(LoginActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val btnCriarConta = activity.findViewById<Button>(R.id.btnCriarConta)
                btnCriarConta.performClick()
                val expectedIntentRegister = Intent(activity, RegisterActivity::class.java)
                val actualIntentRegister = shadowOf(activity).nextStartedActivity
                assertEquals(expectedIntentRegister.component, actualIntentRegister.component)
            }
        }
    }
}
