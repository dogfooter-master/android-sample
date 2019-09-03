package kr.lazybird.myapplication

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import kr.lazybird.myapplication.R
import org.webrtc.EglBase

const val EXTRA_ACCOUNT = "kr.lazybird.myapplication.ACCOUNT"
const val EXTRA_PASSWORD = "kr.lazybird.myapplication.PASSWORD"

class MainActivity : AppCompatActivity() {

    private var mDerMateWebSocket: DerMateWebSocket? = null
    private val eglBase = EglBase.create()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        buttonConnect.setOnClickListener {
            val intent = Intent(this, StreamActivity::class.java).apply {
                putExtra(EXTRA_ACCOUNT, editTextAccount.text.toString())
                putExtra(EXTRA_PASSWORD, editTextPassword.text.toString())
            }
            startActivity(intent)
        }
    }
}
