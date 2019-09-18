package kr.lazybird.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.WindowManager
import kr.lazybird.myapplication.ui.login.LoginActivity
import org.webrtc.EglBase
import java.io.*
import android.content.Context.MODE_PRIVATE
import androidx.core.app.ComponentActivity
import androidx.core.app.ComponentActivity.ExtraData
import androidx.core.content.ContextCompat.getSystemService
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import org.json.JSONObject
import java.util.*


const val EXTRA_ACCOUNT = "kr.lazybird.myapplication.ACCOUNT"
const val EXTRA_PASSWORD = "kr.lazybird.myapplication.PASSWORD"
const val EXTRA_ACCESS_TOKEN = "kr.lazybird.myapplication.ACCESS_TOKEN"
const val EXTRA_TARGET_TOKEN = "kr.lazybird.myapplication.CLIENT_TOKEN"
const val EXTRA_DEVICE_ACCESS_TOKEN = "kr.lazybird.myapplication.DEVICE_ACCESS_TOKEN"
const val EXTRA_DEVICE_MAC_ADDRESS = "kr.lazybird.myapplication.DEVICE_MAC_ADDRESS"
const val EXTRA_OPPONENT_DAT = "kr.lazybird.myapplication.OPPONENT_DAT"
const val EXTRA_FINISH = "kr.lazybird.myapplication.FINISH"
const val REQUEST_CODE_LOGIN = 1
const val REQUEST_CODE_AGENT_LIST = 2


class MainActivity : AppCompatActivity() {

//    private var mDerMateWebSocket: DerMateWebSocket? = null
//    private val eglBase = EglBase.create()
    private var mDeviceMacAddress: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try {
            this.supportActionBar!!.hide()
        } catch (e: NullPointerException) {

        }

        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        setContentView(R.layout.activity_main)

        Log.d("SWS", "MainActivity")


        val buffer = StringBuffer()
        var data: String? = null
        var fis: FileInputStream? = null
        try {
            fis = openFileInput("device.dat")
            val iReader = BufferedReader(InputStreamReader(fis))

            data = iReader.readLine()
            while (data != null) {
                buffer.append(data)
                data = iReader.readLine()
            }
            buffer.append("\n")
            iReader.close()

            val obj = JSONObject(buffer.toString())
            mDeviceMacAddress = obj.getString("device_mac_address")
        } catch (e: FileNotFoundException) {
            mDeviceMacAddress = UUID.randomUUID().toString()
            val obj = JSONObject()
            obj.put("device_mac_address", mDeviceMacAddress)
            var fos: FileOutputStream? = null
            try {
                fos = openFileOutput("device.dat", Context.MODE_PRIVATE)
                fos!!.write(obj.toString().toByteArray())
                fos.close()
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            } catch (e: IOException) {
                e.printStackTrace()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

        Log.d("SWS", "read file: $mDeviceMacAddress")


        val intent = Intent(this, LoginActivity::class.java).apply {
            putExtra(EXTRA_DEVICE_MAC_ADDRESS, mDeviceMacAddress)
        }
        startActivityForResult(intent, REQUEST_CODE_LOGIN)

//        buttonConnect.setOnClickListener {
//            ApiClient(applicationContext)
//                .signinUser(editTextAccount.text.toString(), editTextPassword.text.toString()) { payload, message ->
//                    if (payload != null) {
//                        Log.d("SWS", "DEBUG-1 $payload")
//                        val intent = Intent(this, StreamActivity::class.java).apply {
//                            putExtra(EXTRA_ACCOUNT, editTextAccount.text.toString())
//                            putExtra(EXTRA_PASSWORD, editTextPassword.text.toString())
//                        }
//                        startActivity(intent)
//                    } else {
//                        Log.d("SWS", "DEBUG-2 $message")
//                    }
//                }

//            val intent = Intent(this, StreamActivity::class.java).apply {
//                putExtra(EXTRA_ACCOUNT, editTextAccount.text.toString())
//                putExtra(EXTRA_PASSWORD, editTextPassword.text.toString())
//            }
//            startActivity(intent)
//        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d("SWS", "onActivityResult $requestCode, $resultCode, $data")
        // Check which request we're responding to
        if (requestCode == REQUEST_CODE_LOGIN) {
            // Make sure the request was successful
            if (resultCode == Activity.RESULT_OK) {
                val finish = data!!.getBooleanExtra(EXTRA_FINISH, false)
                if ( finish ) {
                    finish()
                    return
                }
                val account = data!!.getStringExtra(EXTRA_ACCOUNT)
                val accessToken = data!!.getStringExtra(EXTRA_ACCESS_TOKEN)
                val deviceAccessToken = data!!.getStringExtra(EXTRA_DEVICE_ACCESS_TOKEN)
                Log.d("SWS", "--> account: $account, accessToken: $accessToken")
                // The user picked a contact.
                // The Intent's data Uri identifies which contact was selected.

                // Do something with the contact here (bigger example below)
                val intent = Intent(this, ScrollingActivity::class.java).apply {
                    putExtra(EXTRA_ACCOUNT, account)
                    putExtra(EXTRA_ACCESS_TOKEN, accessToken)
                    putExtra(EXTRA_DEVICE_ACCESS_TOKEN, deviceAccessToken)
                }
                startActivityForResult(intent, REQUEST_CODE_AGENT_LIST)
            }
        } else if ( requestCode == REQUEST_CODE_AGENT_LIST ) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE_LOGIN)
        }
    }

}
