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
import com.example.wotcher.databinding.ActivityEditUserProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso
import java.util.*
import kotlin.collections.HashMap

class EditUserProfileActivity : AppCompatActivity() {

    lateinit var editProfileBinding: ActivityEditUserProfileBinding
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()
    val databaseUsers = database.reference.child("Users")

    lateinit var  activityResultLauncher : ActivityResultLauncher<Intent>
    var imageUri : Uri? = null
    val firebaseStorage: FirebaseStorage = FirebaseStorage.getInstance()
    val storageReference = firebaseStorage.reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        editProfileBinding = ActivityEditUserProfileBinding.inflate(layoutInflater)

        getAndSetData()
        registerActivityForResult()

        editProfileBinding.userProfileImg.setOnClickListener{
            chooseImage()
        }
        editProfileBinding.buttonSaveData.setOnClickListener {
            updateData()
        }



        setContentView(editProfileBinding.root)
    }

    private fun updateData() {
        val nickname = editProfileBinding.editTextSignUpName.text.toString()
        auth.currentUser?.let {
            val userUID = it.uid
            uploadPhoto(it.uid)

            val userMap = mutableMapOf<String, Any>()
            userMap["name"] = nickname

            databaseUsers.child(userUID).updateChildren(userMap).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this@EditUserProfileActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                }
            }
        }
    }


    private fun getAndSetData() {

        databaseUsers.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                auth.currentUser?.let {

                    val userUID = it.uid
                    val name = snapshot.child(userUID).child("name").value.toString()
                    val profilePicture = snapshot.child(userUID).child("profilePicture").value

                    editProfileBinding.editTextSignUpName.setText(name)

                    if (profilePicture == null) {
                        editProfileBinding.userProfileImg.setImageResource(R.drawable.user_no_profile_image);
                    } else {
                        Picasso.get().load(profilePicture.toString()).into(editProfileBinding.userProfileImg)
                    }

                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

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

                        Picasso.get().load(it).into(editProfileBinding.userProfileImg)

                    }

                }

            })

    }


    fun uploadPhoto(userUID: String) {
        val imageName = UUID.randomUUID().toString()

        val imageReference = storageReference.child("images").child(imageName)

        imageUri?.let { uri ->
            imageReference.putFile(uri).addOnSuccessListener {
                val myUploaddedImageReference = storageReference.child("images").child(imageName)
                myUploaddedImageReference.downloadUrl.addOnSuccessListener { url ->

                    val imageURL = url.toString()
                    val userMapImg = mutableMapOf<String, Any>()
                    userMapImg["profilePicture"] = imageURL

                    databaseUsers.child(userUID).updateChildren(userMapImg)
                }
            }.addOnFailureListener {
                Toast.makeText(applicationContext,it.localizedMessage, Toast.LENGTH_SHORT).show()
            }
        }
    }


}


