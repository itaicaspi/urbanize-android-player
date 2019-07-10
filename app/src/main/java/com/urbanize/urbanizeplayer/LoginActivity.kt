package com.urbanize.urbanizeplayer

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import android.app.Activity
import android.os.AsyncTask
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import android.util.JsonReader
import androidx.lifecycle.MutableLiveData
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.StringReader
import retrofit2.Callback
import retrofit2.Call
import retrofit2.Response


class LoginActivity : BaseActivity(), View.OnClickListener {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]

    fun isGooglePlayServicesAvailable(activity: Activity): Boolean {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val status = googleApiAvailability.isGooglePlayServicesAvailable(activity)
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404).show()
            }
            return false
        }
        return true
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        Log.d("itai", isGooglePlayServicesAvailable(this).toString())


//        AsyncTask.execute {
//            val apiKey = "AIzaSyC341Xx6m5yGZMZJ93xaWmf7JOcVF1e4tc"
//            val githubEndpoint = URL("https://www.googleapis.com/identitytoolkit/v3/relyingparty/verifyPassword?key=${apiKey}")
//
//            // Create connection
//            val myConnection = githubEndpoint.openConnection() as HttpsURLConnection
//
//            myConnection.setRequestProperty("Content-Type", "application/json")
//            myConnection.requestMethod = "POST"
//
//            // Create the data
//            val myData = "{\"email\":\"itai@urbanize.co\",\"password\":\"!2218Lati\",\"returnSecureToken\":true}"
//
//            // Enable writing
//            myConnection.doOutput = true
//
//            // Write the data
//            myConnection.outputStream.write(myData.toByteArray())
//
//            if (myConnection.responseCode == 200) {
//                // Success
//                val responseBody = myConnection.inputStream
//                val responseBodyReader = InputStreamReader(responseBody, "UTF-8")
//                val jsonReader = JsonReader(responseBodyReader)
//                jsonReader.beginObject() // Start processing the JSON object
//                while (jsonReader.hasNext()) { // Loop through all keys
//                    val key = jsonReader.nextName() // Fetch the next key
////                    val value = jsonReader.nextString()
//                    if (key == "idToken") { // Check if desired key
//                        // Fetch the value as a String
//                        val apiToken = jsonReader.nextString()
//
//                        Log.d("json_response", "$key $apiToken")
//                        // Do something with the value
//                        // ...
//                        // Write a message to the database
//                        val database = FirebaseDatabase.getInstance()
//                        val myRef = database.getReference("version_information")
//                        // Read from the database
//                        myRef.addValueEventListener(object : ValueEventListener {
//                            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                                // This method is called once with the initial value and again
//                                // whenever data at this location is updated.
//                                val value = dataSnapshot.value
//                                Log.d(TAG, "Value is: $value")
//                            }
//
//                            override fun onCancelled(error: DatabaseError) {
//                                // Failed to read value
//                                Log.w(TAG, "Failed to read value.", error.toException())
//                            }
//                        })
//                        break // Break out of the loop
//                    } else {
//                        jsonReader.skipValue() // Skip values of other keys
//                    }
//                }
//                jsonReader.close()
//            } else {
//                // Error handling code goes here
//            }
//            myConnection.disconnect()
//        }

        // Buttons
        emailSignInButton.setOnClickListener(this)
        signOutButton.setOnClickListener(this)

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = FirebaseAuth.getInstance()
        // [END initialize_auth]
    }

    // [START on_start_check_user]
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    // [END on_start_check_user]

    private fun createAccount(email: String, password: String) {
        Log.d(TAG, "createAccount:$email")
        if (!validateForm()) {
            return
        }

        showProgressDialog()

        // [START create_user_with_email]
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

                // [START_EXCLUDE]
                hideProgressDialog()
                // [END_EXCLUDE]
            }
        // [END create_user_with_email]
    }

    private fun signIn(email: String, password: String) {
        Log.d(TAG, "signIn:$email")
        if (!validateForm()) {
            return
        }

        showProgressDialog()

        // [START sign_in_with_email]
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(baseContext, task.exception.toString(),
                        Toast.LENGTH_SHORT).show()
                    updateUI(null)
                }

                // [START_EXCLUDE]
                if (!task.isSuccessful) {
                    status.setText(R.string.auth_failed)
                }
                hideProgressDialog()
                // [END_EXCLUDE]
            }
        // [END sign_in_with_email]
    }

    private fun signOut() {
        auth.signOut()
        updateUI(null)
    }

    private fun validateForm(): Boolean {
        var valid = true

        val email = fieldEmail.text.toString()
        if (TextUtils.isEmpty(email)) {
            fieldEmail.error = "Required."
            valid = false
        } else {
            fieldEmail.error = null
        }

        val password = fieldPassword.text.toString()
        if (TextUtils.isEmpty(password)) {
            fieldPassword.error = "Required."
            valid = false
        } else {
            fieldPassword.error = null
        }

        return valid
    }

    private fun updateUI(user: FirebaseUser?) {
        hideProgressDialog()
        if (user != null) {
            status.text = getString(R.string.emailpassword_status_fmt,
                user.email, user.isEmailVerified)
            detail.text = getString(R.string.firebase_status_fmt, user.uid)

            emailPasswordButtons.visibility = View.GONE
            emailPasswordFields.visibility = View.GONE
            signedInButtons.visibility = View.VISIBLE

            val intent = Intent(this, MainActivity::class.java)
//            val message = editText_main!!.text.toString()
//            intent.putExtra(EXTRA_MESSAGE, message)
            startActivityForResult(intent, 1)
        } else {
            status.setText(R.string.signed_out)
            detail.text = null

            emailPasswordButtons.visibility = View.VISIBLE
            emailPasswordFields.visibility = View.VISIBLE
            signedInButtons.visibility = View.GONE
        }
    }

    override fun onClick(v: View) {
        val i = v.id
        when (i) {
            R.id.emailSignInButton -> signIn(fieldEmail.text.toString(), fieldPassword.text.toString())
            R.id.signOutButton -> signOut()
        }
    }

    companion object {
        private const val TAG = "EmailPassword"
    }
}