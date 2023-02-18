package com.example.wotcher

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.wotcher.databinding.ActivityForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordActivity : AppCompatActivity() {

    lateinit var forgotPasswordBinding: ActivityForgotPasswordBinding
    lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {

        forgotPasswordBinding = ActivityForgotPasswordBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        forgotPasswordBinding.forgotPassBtn.setOnClickListener {

            var email = forgotPasswordBinding.forgotPassEmail.text.toString()
            forgotPassword(email)

        }

        setContentView(forgotPasswordBinding.root)
    }

    private fun forgotPassword(email: String) {

        auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->

            if(task.isSuccessful) {
                Toast.makeText(this, "We sent a password reset mail to your mail address", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, task.exception?.toString(), Toast.LENGTH_SHORT).show()
            }
        }
    }



}



