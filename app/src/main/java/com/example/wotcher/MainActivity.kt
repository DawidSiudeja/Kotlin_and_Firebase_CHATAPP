package com.example.wotcher

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wotcher.databinding.ActivityMainBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MainActivity : AppCompatActivity() {

    lateinit var mainBinding: ActivityMainBinding
    lateinit var adapter: UserListAdapter

    val database : FirebaseDatabase = FirebaseDatabase.getInstance()
    val databaseRefrence : DatabaseReference = database.reference.child("Users")

    var userList = ArrayList<Users>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainBinding = ActivityMainBinding.inflate(layoutInflater)


        adapter = UserListAdapter(this@MainActivity, userList)
        retriveDataFromDatabase()

        mainBinding.logoutUser.setOnClickListener {

            logoutUserFirebase()

        }

        setContentView(mainBinding.root)
    }

    fun retriveDataFromDatabase() {

        databaseRefrence.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                userList.clear()

                for (eachUser in snapshot.children) {
                    val user = eachUser.getValue(Users::class.java)

                    if (user != null) {
                        userList.add(user)
                    }

                }

                adapter = UserListAdapter(this@MainActivity, userList)
                mainBinding.listOfUsers.layoutManager = LinearLayoutManager(this@MainActivity)
                mainBinding.listOfUsers.adapter = adapter


            }



            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun logoutUserFirebase() {
        FirebaseAuth.getInstance().signOut()

        Toast.makeText(applicationContext, "Logout is successful", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.editProfileLink) {

            val intent = Intent(this@MainActivity, EditUserProfileActivity::class.java)
            startActivity(intent)

        }
        return super.onOptionsItemSelected(item)
    }
}

