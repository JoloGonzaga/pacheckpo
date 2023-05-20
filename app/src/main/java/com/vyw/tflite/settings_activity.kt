package com.vyw.tflite

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class settings_activity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_READ_CONTACTS = 100
    private val PICK_CONTACT_REQUEST = 101

    private lateinit var mobileNumEditText: EditText
    private lateinit var textView2: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        mobileNumEditText = findViewById(R.id.mobileNum)
        textView2 = findViewById(R.id.textView2)

        val returnIcon = findViewById<ImageView>(R.id.back)
        returnIcon.setOnClickListener {
            startActivity(Intent(this@settings_activity, MainActivity::class.java))
        }

        val addContactButton = findViewById<ImageButton>(R.id.btn_addContact)
        addContactButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_CONTACTS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_CONTACTS),
                    PERMISSIONS_REQUEST_READ_CONTACTS
                )
            } else {
                pickContact()
            }
        }
    }

    private fun pickContact() {
        val pickContactIntent = Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI)
        startActivityForResult(pickContactIntent, PICK_CONTACT_REQUEST)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSIONS_REQUEST_READ_CONTACTS -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickContact()
                } else {
                    Toast.makeText(
                        this,
                        "Permission denied. Unable to pick contact.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_CONTACT_REQUEST && resultCode == RESULT_OK) {
            val contactUri = data?.data
            val projection = arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val cursor = contentResolver.query(contactUri!!, projection, null, null, null)

            if (cursor != null && cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex("data1")

                if (columnIndex != -1) {
                    val phoneNumber = cursor.getString(columnIndex)
                    textView2.text = phoneNumber
                } else {
                    Toast.makeText(
                        this,
                        "Unable to retrieve contact phone number",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                cursor.close()
            } else {
                Toast.makeText(
                    this,
                    "Unable to retrieve contact information",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}

