package kr.lazybird.myapplication

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_scrolling.*
import kr.lazybird.myapplication.ui.login.LoggedInUserView
import kr.lazybird.myapplication.ui.login.LoginResult

class ScrollingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scrolling)

        val account = intent.getStringExtra(EXTRA_ACCOUNT)
        val accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN)
        Log.d("SWS", "ScrollingActivity $account, $accessToken")

        val pcList: ArrayList<Agent> = ArrayList()

        ApiClient(applicationContext).getAgentList(accessToken) { payload, message ->
            if (payload != null) {
                Log.d("SWS", "DEBUG-1 $payload")
                if ( payload.agentList != null ) {
                    for (e in payload.agentList) {
                        pcList.add(e)
                        // Creates a vertical Layout Manager
                        pc_list.layoutManager =
                            androidx.recyclerview.widget.LinearLayoutManager(this)

                        // You can use GridLayoutManager if you want multiple columns. Enter the number of columns as a parameter.
//        rv_animal_list.layoutManager = GridLayoutManager(this, 2)

                        // Access the RecyclerView Adapter and load the data into it
                        val adapter = PcAdapter(pcList, this)
                        adapter.itemClick = object : PcAdapter.ItemClick {
                            override fun onClick(view: View, position: Int) {
                                Log.d("SWS", "${pcList[position]}")
                                val intent =
                                    Intent(applicationContext, StreamActivity::class.java).apply {
                                        putExtra(EXTRA_ACCESS_TOKEN, accessToken)
                                        putExtra(EXTRA_TARGET_TOKEN, pcList[position].clientToken)
                                    }
                                startActivity(intent)
                            }
                        }
                        pc_list.adapter = adapter
                    }
                }
            } else {
                Log.d("SWS", "DEBUG-2 $message")
            }
        }
    }
    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }
}
