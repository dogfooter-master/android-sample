package kr.lazybird.myapplication

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.util.TimeUtils
import android.view.Gravity.CENTER
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.RelativeLayout
import org.webrtc.*
import kotlin.NullPointerException
import android.view.WindowManager
import kotlinx.android.synthetic.main.activity_stream.*
import org.json.JSONObject
import kotlin.math.max
import kotlin.math.min
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.abs


class StreamActivity : AppCompatActivity() {

    private val connectionList = ArrayList<Connection>()
    private var mDerMateWebSocket: DerMateWebSocket? = null
    private val eglBase = EglBase.create()
    private var lastX: Float = 0.0f
    private var lastY: Float = 0.0f
    private var downRawX: Float = 0.0f
    private var downRawY: Float = 0.0f
    private var dX: Float = 0.0f
    private var dY: Float = 0.0f
    private var pressTime: Long? = null
    private var downTime: Long? = null
    private var fbsMoved: Boolean = false
    private var mDeviceAccessToken: String? = null

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
//        window.decorView.apply {
//            // Hide both the navigation bar and the status bar.
//            // SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
//            // a general rule, you should design your app to hide the status bar whenever you
//            // hide the navigation bar.
//            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION or View.SYSTEM_UI_FLAG_FULLSCREEN
//        }
        setContentView(R.layout.activity_stream)

        mDeviceAccessToken = intent.getStringExtra(EXTRA_DEVICE_ACCESS_TOKEN)

        remote_view_container.setOnTouchListener { _: View, event:MotionEvent ->
            val v = findViewById<RelativeLayout>(R.id.remote_view_container)
//            Log.d("SWS", "setOnTouchListener ${event.x}, ${event.y}, ${v.width}, ${v.height}")
            val tempX = lastX
            val tempY = lastY
            lastX = event.x
            lastY = event.y
            val posX = event.x.div(v.width.toFloat())
            val posY = event.y.div(v.height.toFloat())
            Log.d("SWS", "setOnTouchListener $posX, $posY")
            var data = JSONObject()
            data.put("x", posX.toString())
            data.put("y", posY.toString())
            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    data.put("command", "mouse_down")
//                    mDerMateWebSocket!!.sendWebRTCDataChannel(data, "")
                    mDerMateWebSocket!!.sendWebRTCDataChannel(data, mDeviceAccessToken!!)
                }
                MotionEvent.ACTION_MOVE -> {
                    data.put("command", "mouse_move")
                    if ( tempX != event.x || tempY != event.y ) {
//                        mDerMateWebSocket!!.sendWebRTCDataChannel(data, "")
                        mDerMateWebSocket!!.sendWebRTCDataChannel(data, mDeviceAccessToken!!)
                    }
                }
                MotionEvent.ACTION_UP -> {
                    data.put("command", "mouse_up")
//                    mDerMateWebSocket!!.sendWebRTCDataChannel(data, "")
                    mDerMateWebSocket!!.sendWebRTCDataChannel(data, mDeviceAccessToken!!)
                }
            }
            true
        }
        fab_main.setOnTouchListener { v: View, event:MotionEvent ->
//            Log.d("SWS", "setOnTouchListener ${event.action}")

            when(event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downRawX = event.rawX
                    downRawY = event.rawY
                    dX = v.x - downRawX
                    dY = v.y - downRawY

                    val justDownTime = Calendar.getInstance().timeInMillis

                    if ( downTime != null ) {
                        Log.d("SWS", "DOWN: ${justDownTime.minus(downTime!!)}")
                    }
//                    if ( downTime != null && justDownTime.minus(downTime!!) < 300 ) {
//                        var data = JSONObject()
//                        data.put("command", "switch")
//                        mDerMateWebSocket!!.sendWebRTCDataChannel(data, mDeviceAccessToken!!)
//                    }

                    downTime = justDownTime
                    pressTime = downTime
                    fbsMoved = false
                    v.scaleX = 0.9f
                    v.scaleY = 0.9f
                }
                MotionEvent.ACTION_MOVE -> {

                    val viewParent = v.parent as View
                    val parentWidth = viewParent.width
                    val parentHeight = viewParent.height

                    val upTime = Calendar.getInstance().timeInMillis

//                    Log.d("SWS", "fab_main setOnTouchListener before ($dX, $dY), (${v.x}, ${v.y}) ${event.x} ${event.y} ${upTime.minus(pressTime!!)}")

                    val upRawX = event.rawX
                    val upRawY = event.rawY
                    val upDX = upRawX - downRawX
                    val upDY = upRawY - downRawY
//                    Log.d("SWS", "DEBUG1 ${upTime.minus(pressTime!!)} $fbsMoved")
                    if ( upTime.minus(pressTime!!) > 500 && !fbsMoved ) {
//                        Log.d("SWS", "DEBUG2 ${upTime.minus(pressTime!!)} ${abs(upDX)} ${abs(upDY)} ${v.width} ${v.height}")
                        if (abs(upDX) < v.width && abs(upDY) < v.height) { // A click
//                            val streamMenu = findViewById<ConstraintLayout>(R.id.stream_menu)
//                            val streamFabLayout = findViewById<CoordinatorLayout>(R.id.stream_fab_layout)
//                            streamMenu.visibility = View.VISIBLE
//                            streamFabLayout.visibility = View.GONE
                        } else {
                            fbsMoved = true
                        }
                    }

                    var newX = event.rawX + dX
                    newX = max(
                        1.0f,
                        newX
                    ) // Don't allow the FAB past the left hand side of the parent
                    newX = min(
                        parentWidth - v.width.toFloat(),
                        newX
                    ) // Don't allow the FAB past the right hand side of the parent

                    var newY = event.rawY + dY
                    newY = max(
                        1.0f,
                        newY
                    ) // Don't allow the FAB past the top of the parent
                    newY = min(
                        parentHeight - v.height.toFloat(),
                        newY
                    ) // Don't allow the FAB past the bottom of the parent

                    v.x = newX
                    v.y = newY
//                    v.animate()
//                        .x(newX)
//                        .y(newY)
//                        .setDuration(0)
//                        .start()

                    if (v.layoutParams is ViewGroup.MarginLayoutParams) {
                        val p = v.layoutParams as ViewGroup.MarginLayoutParams
                        p.setMargins(0, 0, 0, 0)
                        v.requestLayout()
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if ( !fab_main.isOrWillBeHidden ) {
                        val upRawX = event.rawX
                        val upRawY = event.rawY

                        val upDX = upRawX - downRawX
                        val upDY = upRawY - downRawY

                        val upTime = Calendar.getInstance().timeInMillis

                        if (abs(upDX) < v.width*0.5f && abs(upDY) < v.height*0.5f && !fbsMoved) {
                            val streamMenu = findViewById<ConstraintLayout>(R.id.stream_menu)
                            val streamFabLayout = findViewById<CoordinatorLayout>(R.id.stream_fab_layout)
                            streamMenu.visibility = View.VISIBLE
                            streamFabLayout.visibility = View.GONE
//                            var data = JSONObject()
//                            data.put("command", "switch")
//                            val accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN)
//                            mDerMateWebSocket!!.sendWebRTCDataChannel(data, accessToken)
                        }
                    }
                    pressTime = -1
                    v.scaleX = 1.0f
                    v.scaleY = 1.0f
                }
            }
            false
        }
        fab_back.setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    var data = JSONObject()
                    data.put("command", "back")
                    mDerMateWebSocket!!.sendWebRTCDataChannel(data, mDeviceAccessToken!!)
                    v.scaleX = 0.9f
                    v.scaleY = 0.9f
                }
                MotionEvent.ACTION_UP -> {
                    v.scaleX = 1.0f
                    v.scaleY = 1.0f
                }
            }
            true
        }
        fab_home.setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    var data = JSONObject()
                    data.put("command", "home")
                    mDerMateWebSocket!!.sendWebRTCDataChannel(data, mDeviceAccessToken!!)
                    v.scaleX = 0.9f
                    v.scaleY = 0.9f
                }
                MotionEvent.ACTION_UP -> {
                    v.scaleX = 1.0f
                    v.scaleY = 1.0f
                }
            }
            true
        }
        fab_recent.setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    var data = JSONObject()
                    data.put("command", "recent")
                    mDerMateWebSocket!!.sendWebRTCDataChannel(data, mDeviceAccessToken!!)
                    v.scaleX = 0.9f
                    v.scaleY = 0.9f
                }
                MotionEvent.ACTION_UP -> {
                    v.scaleX = 1.0f
                    v.scaleY = 1.0f
                }
            }
            true
        }
        fab_next.setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    var data = JSONObject()
                    data.put("command", "switch")
                    mDerMateWebSocket!!.sendWebRTCDataChannel(data, mDeviceAccessToken!!)
                    v.scaleX = 0.9f
                    v.scaleY = 0.9f
                }
                MotionEvent.ACTION_UP -> {
                    v.scaleX = 1.0f
                    v.scaleY = 1.0f
                }
            }
            true
        }
        fab_close.setOnTouchListener { v: View, event: MotionEvent ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val streamMenu = findViewById<ConstraintLayout>(R.id.stream_menu)
                    val streamFabLayout = findViewById<CoordinatorLayout>(R.id.stream_fab_layout)
                    streamMenu.visibility = View.GONE
                    streamFabLayout.visibility = View.VISIBLE
                    v.scaleX = 0.9f
                    v.scaleY = 0.9f
                }
                MotionEvent.ACTION_UP -> {
                    v.scaleX = 1.0f
                    v.scaleY = 1.0f
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

    override fun onConfigurationChanged(newConfig: Configuration?) {
        super.onConfigurationChanged(newConfig)
        if ( newConfig!!.orientation == Configuration.ORIENTATION_LANDSCAPE ) {

            // TODO: 세로가 더 길 경우
//            val remoteViewFrameLayout = this@StreamActivity.findViewById(R.id.remote_view_frame_layout) as FrameLayout
//            var lp = CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
//            lp.width = (remoteViewFrameLayout.width.toFloat() * (9.0f/16.0f)).toInt()
//            lp.gravity = CENTER
//            remoteViewFrameLayout.layoutParams = lp
        } else if ( newConfig!!.orientation == Configuration.ORIENTATION_PORTRAIT ) {

            // TODO: 가로가 더 길 경우
//            val remoteViewFrameLayout = this@StreamActivity.findViewById(R.id.remote_view_frame_layout) as FrameLayout
//            var lp = CoordinatorLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
//            lp.height = (remoteViewFrameLayout.height.toFloat() * (9.0f/16.0f)).toInt()
//            lp.gravity = CENTER
//            remoteViewFrameLayout.layoutParams = lp
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        val text  = "(${event?.x}, ${event?.y})"
        val v = findViewById<RelativeLayout>(R.id.remote_view_container)
        Log.d("SWS", "onTouchEvent: $text, ${v.width}, ${v.height}")
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

        val accessToken = intent.getStringExtra(EXTRA_ACCESS_TOKEN)
        val deviceAccessToken = intent.getStringExtra(EXTRA_DEVICE_ACCESS_TOKEN)
        val opponentDAT = intent.getStringExtra(EXTRA_OPPONENT_DAT)
        Log.d("SWS", "DEBUG: $accessToken, $deviceAccessToken, $opponentDAT")
        mDerMateWebSocket = DerMateWebSocket(resources.getString(R.string.host), accessToken, deviceAccessToken, opponentDAT)
        val conn = createConnection()
        mDerMateWebSocket!!.connect(conn)
    }

    private var remoteIndex = 0

    private fun createConnection(): Connection {
        val connection = Connection("", "", "", mDerMateWebSocket!!, object : ConnectionCallbacks {
            override fun onAddedStream(mediaStream: MediaStream) {
                Log.d("SWS", "onAddedStream ${mediaStream.videoTracks.size}")
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
//                    params.width = 900
//                    params.height = 900
//                    params.leftMargin = 10
//                    params.rightMargin = 10
//                    params.topMargin = 10
                    remoteRenderer.layoutParams = params

                    val videoRenderer = setupRenderer(remoteRenderer)
                    remoteVideoTrack.addRenderer(videoRenderer)

                    val remoteViewContainer = this@StreamActivity.findViewById(R.id.remote_view_container) as RelativeLayout
                    remoteViewContainer.addView(remoteRenderer)

                    remoteRenderer.setOnSystemUiVisibilityChangeListener {
                        Log.d("SWS", "setOnSystemUiVisibilityChangeListener")
                    }
                    remoteIndex += 1
                }
            }
        })

        connectionList.add(connection)
        return connection
    }
    override fun onBackPressed() {
        mDerMateWebSocket!!.close()
//        eglBase.detachCurrent()
//        eglBase.releaseSurface()
//        eglBase.release()
        finish()
        super.onBackPressed()
    }
}
