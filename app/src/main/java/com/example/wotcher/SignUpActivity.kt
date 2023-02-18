package com.example.wotcher

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.wotcher.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*

class SignUpActivity : AppCompatActivity() {

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    lateinit var signupBinding: ActivitySignUpBinding
    val database = FirebaseDatabase.getInstance()
    val databaseUsers = database.reference.child("Users")


    lateinit var  activityResultLauncher : ActivityResultLauncher<Intent>
    var imageUri : Uri? = null
    val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference = firebaseStorage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        signupBinding = ActivitySignUpBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        registerActivityForResult()

        signupBinding.userProfileImg.setOnClickListener{
            chooseImage()
        }

        signupBinding.buttonSignUp.setOnClickListener {

            var name = signupBinding.editTextSignUpName.text.toString()
            var email = signupBinding.editTextSignUpEmail.text.toString()
            var password = signupBinding.editTextSignUpPassword.text.toString()
            var passwordRepeat = signupBinding.editTextRepeatPassword.text.toString()

            if (password == passwordRepeat) {
                signupWithFirebase(name, email, password)
            } else {
                Toast.makeText(applicationContext, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            }
        }
        setContentView(signupBinding.root)
    }

    private fun chooseImage() {
        if(ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE), 1)

        } else {

            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)

        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            activityResultLauncher.launch(intent)

        }

    }
    fun registerActivityForResult() {

        activityResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
            , ActivityResultCallback { result ->

                val resultCode = result.resultCode
                val imageData = result.data

                if (resultCode == RESULT_OK && imageData != null) {

                    imageUri = imageData.data

                    // Picasso

                    imageUri?.let {

                        Picasso.get().load(it).into(signupBinding.userProfileImg)

                    }

                }

            })

    }
    private fun signupWithFirebase(name:String, email: String, password: String) {

        signupBinding.buttonSignUp.isClickable = false
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->

            if(task.isSuccessful) {
                auth.currentUser?.let {
                    val userUID = it.uid
                    databaseUsers.child(userUID).child("name").setValue(name)
                    databaseUsers.child(userUID).child("email").setValue(email)
                    databaseUsers.child(userUID).child("userId").setValue(userUID)

                    uploadPhoto(it.uid)
                }

                Toast.makeText(applicationContext, "Your account has been created", Toast.LENGTH_SHORT).show()
                finish()
                signupBinding.buttonSignUp.isClickable = true

            } else {
                Toast.makeText(applicationContext, task.exception?.localizedMessage, Toast.LENGTH_SHORT).show()
            }

        }

    }


    fun uploadPhoto(userUID: String) {

        val imageName = UUID.randomUUID().toString()

        imageUri?.let { uri ->

            storageReference.child("images").child(imageName).putFile(uri).addOnSuccessListener {
                val myUploaddedImageReference = storageReference.child("images").child(imageName)
                myUploaddedImageReference.downloadUrl.addOnSuccessListener { url ->
                    val imageURL = url.toString()
                    if (userUID != null) {
                        databaseUsers.child(userUID).child("profilePicture").setValue(imageURL)
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(applicationContext,it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }

}