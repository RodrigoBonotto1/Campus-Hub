package com.example.campus_hub

import android.widget.Button
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [35])
class RegisterActivityTest {

    @Before
    fun setUp() {
        if (FirebaseApp.getApps(ApplicationProvider.getApplicationContext()).isEmpty()) {
            FirebaseApp.initializeApp(ApplicationProvider.getApplicationContext())
        }
    }

    @Test
    fun testRegisterUiComponents() {
        ActivityScenario.launch(RegisterActivity::class.java).use { scenario ->
            scenario.onActivity { activity ->
                val edtNome = activity.findViewById<EditText>(R.id.edtNome)
                val edtEmail = activity.findViewById<EditText>(R.id.edtEmail)
                val edtSenha = activity.findViewById<EditText>(R.id.edtSenha)
                val edtConfirmarSenha = activity.findViewById<EditText>(R.id.edtConfirmarSenha)
                val btnCadastrar = activity.findViewById<Button>(R.id.btnCadastrar)

                assertNotNull(edtNome)
                assertNotNull(edtEmail)
                assertNotNull(edtSenha)
                assertNotNull(edtConfirmarSenha)
                assertNotNull(btnCadastrar)

                // Click register with empty fields (should not crash, handled via Toast)
                btnCadastrar.performClick()
            }
        }
    }
}
