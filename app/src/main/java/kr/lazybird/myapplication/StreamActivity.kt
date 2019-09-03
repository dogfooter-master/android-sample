package kr.lazybird.myapplication

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.RelativeLayout
import org.webrtc.*
import kotlin.NullPointerException
import android.view.WindowManager
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_stream.*


class StreamActivity : AppCompatActivity() {

    private val connectionList = ArrayList<Connection>()
    private var mDerMateWebSocket: DerMateWebSocket? = null
    private val eglBase = EglBase.create()

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
        setContentView(R.layout.activity_stream)
        remote_view_container.setOnTouchListener { _: View, event:MotionEvent ->
            Log.d("SWS", "setOnTouchListener $event")
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                }
                MotionEvent.ACTION_MOVE -> {
                }
                MotionEvent.ACTION_UP -> {
                }
            }
            true
        }
//        checkPermission()
        connectStreamServer()
    }

    private fun checkPermission() {
//        val permissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
//        ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_CAMERA_PERMISSION)
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val text  = "(${event?.x}, ${event?.y})"
        Log.d("SWS", "onTouchEvent: $text")
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
        return super.onTouchEvent(event)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>, grantResults: IntArray
    ) {
        if (requestCode != REQUEST_CODE_CAMERA_PERMISSION) {
            return
        }
//        WebRTC.setup(this, eglBase)

//        val localStream = WebRTC.localStream
//        val renderer = findViewById<SurfaceViewRenderer>(R.id.remote_view_container)
//        val localRenderer = setupRenderer(renderer)
//        localStream!!.videoTracks.first.addRenderer(localRenderer)

    }

    companion object {
        private const val REQUEST_CODE_CAMERA_PERMISSION = 1
    }

    private fun setupRenderer(renderer: SurfaceViewRenderer): VideoRenderer {

        renderer.init(eglBase.eglBaseContext, null)
        renderer.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL)
        renderer.setZOrderMediaOverlay(true)
        renderer.setEnableHardwareScaler(true)

        return VideoRenderer(renderer)
    }
    private fun connectStreamServer() {
        if (mDerMateWebSocket != null) {
            mDerMateWebSocket!!.close()
        }
        WebRTC.setup(this, eglBase)

        val account = intent.getStringExtra(EXTRA_ACCOUNT)
        val password = intent.getStringExtra(EXTRA_PASSWORD)
        mDerMateWebSocket = DerMateWebSocket("wss://dermaster.io/ws", account, password)
        val conn = createConnection()
        mDerMateWebSocket!!.connect(conn)
    }

    private var remoteIndex = 0

    private fun createConnection(): Connection {
        val connection = Connection("", "", mDerMateWebSocket!!, object : ConnectionCallbacks {
            override fun onAddedStream(mediaStream: MediaStream) {
                Log.d("SWS", "onAddedStream")
                if (mediaStream.videoTracks.size == 0) {
                    Log.e("createConnection", "noVideoTracks")
                    return
                }

                val remoteVideoTrack = mediaStream.videoTracks.first

                this@StreamActivity.runOnUiThread {
                    val remoteRenderer = SurfaceViewRenderer(this@StreamActivity)

//                    val row = remoteIndex / 1
//                    val col = remoteIndex % 1

                    val params = RelativeLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
//                    params.columnSpec = GridLayout.spec(col, 1)
//                    params.rowSpec = GridLayout.spec(row, 1)
//                    params.width = 1600
//                    params.height = 900
//                    params.leftMargin = 10
//                    params.rightMargin = 10
//                    params.topMargin = 10

                    remoteRenderer.layoutParams = params

                    val videoRenderer = setupRenderer(remoteRenderer)
                    remoteVideoTrack.addRenderer(videoRenderer)

                    val remoteViewContainer = this@StreamActivity.findViewById(R.id.remote_view_container) as RelativeLayout
                    remoteViewContainer.addView(remoteRenderer)

                    remoteIndex += 1
                }
            }
        })

        connectionList.add(connection)
        return connection
    }
}