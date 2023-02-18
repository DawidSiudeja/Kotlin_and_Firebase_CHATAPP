package com.example.wotcher

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.wotcher.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    lateinit var loginBinding: ActivityLoginBinding
    val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {

        loginBinding = ActivityLoginBinding.inflate(layoutInflater)
        var view = loginBinding.root

        super.onCreate(savedInstanceState)
        setContentView(view)


        loginBinding.textForgotPassword.setOnClickListener {

            val intent = Intent(this, ForgotPasswordActivity::class.java)
            startActivity(intent)

        }

        loginBinding.textSignUpIntent.setOnClickListener {

            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)

        }

        loginBinding.buttonLogIn.setOnClickListener {

            var emailLogin = loginBinding.editTextLoginEmail.text.toString()
            var passwordLogin = loginBinding.editTextLoginPassword.text.toString()
            loginWithFirebase(emailLogin, passwordLogin)

        }
    }

    private fun loginWithFirebase(emailLogin: String, passwordLogin: String) {
        auth.signInWithEmailAndPassword(emailLogin, passwordLogin).addOnCompleteListener { task ->
            if(task.isSuccessful) {
                Toast.makeText(applicationContext, "Welcome to Wotcher App",Toast.LENGTH_SHORT).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(applicationContext, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()

        val user = auth.currentUser

        if (user != null) {
            Toast.makeText(applicationContext, "Welcome again", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

}