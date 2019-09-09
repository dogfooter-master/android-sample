package kr.lazybird.myapplication

import android.app.Activity
import android.content.Context
import android.util.DisplayMetrics
import android.util.Log
import android.view.WindowManager
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*
import java.util.*
import org.webrtc.MediaConstraints
import org.webrtc.PeerConnection
import java.nio.charset.Charset
import org.webrtc.DataChannel
import java.nio.ByteBuffer
import kotlin.collections.ArrayList


class WebRTC internal constructor(private val callbacks: WebRTCCallbacks) : PeerConnection.Observer, WebRTCInterface {

    private abstract class SkeletalSdpObserver : SdpObserver {

        override fun onCreateSuccess(sessionDescription: SessionDescription) {}
        override fun onSetSuccess() {}
        override fun onCreateFailure(s: String) {}
        override fun onSetFailure(s: String) {}

        companion object {

            private val TAG = "SkeletalSdpObserver"
        }
    }
    private abstract class SkeletalDataChannelObserver : DataChannel.Observer {
        override fun onMessage(p0: DataChannel.Buffer?) {}

        override fun onStateChange() {}

        override fun onBufferedAmountChange(p0: Long) {}
    }

    private var peerConnection: PeerConnection? = null
    private var mDataChannel: DataChannel? = null

    init {

        // create PeerConnection

//        val iceServers = Arrays.asList(PeerConnection.IceServer("stun:stun.l.google.com:19302"))
        //val iceServers = Arrays.asList(PeerConnection.IceServer("stun:172.10.24.74:19302"))
        val iceServers = ArrayList<PeerConnection.IceServer>()

        var iceServerBuilder = PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
        iceServerBuilder.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
        var iceServer = iceServerBuilder.createIceServer()
        iceServers.add(iceServer)

        iceServerBuilder = PeerConnection.IceServer.builder("stun:flowork.ai:3478")
        iceServerBuilder.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
        iceServer = iceServerBuilder.createIceServer()
        iceServers.add(iceServer)

        iceServerBuilder = PeerConnection.IceServer.builder("turn:flowork.ai:3478?transport=udp")
        iceServerBuilder.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
        iceServerBuilder.setUsername("flowork")
        iceServerBuilder.setPassword("Hotice1234!")
        iceServer = iceServerBuilder.createIceServer()
        iceServers.add(iceServer)

        iceServerBuilder = PeerConnection.IceServer.builder("turn:flowork.ai:3478?transport=tcp")
        iceServerBuilder.setTlsCertPolicy(PeerConnection.TlsCertPolicy.TLS_CERT_POLICY_INSECURE_NO_CHECK)
        iceServerBuilder.setUsername("flowork")
        iceServerBuilder.setPassword("Hotice1234!")
        iceServer = iceServerBuilder.createIceServer()
        iceServers.add(iceServer)

        peerConnection = factory!!.createPeerConnection(iceServers, WebRTCUtil.peerConnectionConstraints(), this)
//        peerConnection!!.addStream(localStream)

    }

    override fun sendData(data: JSONObject) {
        val message = data.toString()
        val data = ByteBuffer.wrap("$message".toByteArray(Charset.defaultCharset()))
        mDataChannel!!.send(DataChannel.Buffer(data, false))
    }

    override fun createOffer() {
        peerConnection!!.createOffer(object : SkeletalSdpObserver() {
            override fun onCreateSuccess(sessionDescription: SessionDescription) {
                peerConnection!!.setLocalDescription(object : SkeletalSdpObserver() {

                    override fun onSetSuccess() {
                        callbacks.onCreateOffer(sessionDescription.description)
                    }

                }, sessionDescription)
            }

        }, WebRTCUtil.offerConnectionConstraints())
    }

    override fun receiveOffer(sdp: String) {

        // setRemoteDescription
        val remoteDescription = SessionDescription(SessionDescription.Type.OFFER, sdp)
        peerConnection!!.setRemoteDescription(object : SkeletalSdpObserver() {
            override fun onSetSuccess() {

                // createAnswer
                peerConnection!!.createAnswer(object : SkeletalSdpObserver() {
                    override fun onCreateSuccess(sessionDescription: SessionDescription) {
                        peerConnection!!.setLocalDescription(object : SkeletalSdpObserver() {

                            override fun onSetSuccess() {
                                callbacks.onCreateAnswer(sessionDescription.description)
                            }

                        }, sessionDescription)
                    }
                }, WebRTCUtil.answerConnectionConstraints())

            }

            override fun onSetFailure(s: String) {
                Log.d("WebRTC", " ------------ onSetFailure ----------------")
                Log.d("WebRTC", s)
            }
        }, remoteDescription)
    }

    override fun receiveAnswer(sdp: String) {
        val remoteDescription = SessionDescription(SessionDescription.Type.ANSWER, sdp)
        peerConnection!!.setRemoteDescription(object : SkeletalSdpObserver() {
            override fun onSetSuccess() {

            }
        }, remoteDescription)
    }

    override fun receiveCandidate(sdp: String, sdpMid: String, sdpMLineIndex: Int) {
        val iceCandidate = IceCandidate(sdpMid, sdpMLineIndex, sdp)
        peerConnection!!.addIceCandidate(iceCandidate)
    }

    override fun close() {
//        peerConnection!!.removeStream(WebRTC.localStream)
        peerConnection!!.close()
        peerConnection = null
    }


    override fun onSignalingChange(signalingState: PeerConnection.SignalingState) {}
    override fun onIceConnectionChange(iceConnectionState: PeerConnection.IceConnectionState) {}
    override fun onIceConnectionReceivingChange(b: Boolean) {}
    override fun onRemoveStream(mediaStream: MediaStream) {}
    override fun onDataChannel(dataChannel: DataChannel) {
        Log.d("SWS", "onDataChannel $dataChannel")
        mDataChannel = dataChannel
        dataChannel.registerObserver(object : SkeletalDataChannelObserver() {
            override fun onBufferedAmountChange(p0: Long) {
                Log.d("SWS", "channel buffered amount change:{$p0}")
            }

            override fun onMessage(p0: DataChannel.Buffer?) {
                Log.d("SWS", "onMessage $dataChannel")
                val buf = p0?.data
                if (buf != null) {
                    val byteArray = ByteArray(buf.remaining())
                    buf.get(byteArray)
                    val received = String(byteArray, Charset.forName("UTF-8"))
                    Log.d("SWS", "received: $received")
//                    try {
//                        val message = JSONObject(received).getString("data")
//                        Log.d("SWS", "&gt;$message")
//                    } catch (e: JSONException) {
//                        Log.d("SWS", "Malformed message received")
//                    }
                }
            }

            override fun onStateChange() {
                Log.d("SWS", "Channel state changed: ${dataChannel.label()} ${dataChannel.state()?.name}")
                if (dataChannel.state() == DataChannel.State.OPEN) {
                    Log.d("SWS", "Data Channel established.")
//                    val message : String = "Hello, World!"
//                    val data = ByteBuffer.wrap("$message".toByteArray(Charset.defaultCharset()))
//                    dataChannel.send(DataChannel.Buffer(data, false))
                } else {
                    Log.d("SWS", "Chat ended.")
                }
            }
        })
    }
    override fun onRenegotiationNeeded() {}
    override fun onAddTrack(rtpReceiver: RtpReceiver, mediaStreams: Array<MediaStream>) {}
    override fun onIceCandidatesRemoved(iceCandidates: Array<IceCandidate>) {}
    override fun onIceGatheringChange(iceGatheringState: PeerConnection.IceGatheringState) {}

    override fun onIceCandidate(iceCandidate: IceCandidate) {
        callbacks.onIceCandidate(iceCandidate.sdp, iceCandidate.sdpMid, iceCandidate.sdpMLineIndex)
    }

    override fun onAddStream(mediaStream: MediaStream) {
        callbacks.onAddedStream(mediaStream)
    }

    companion object {

        private val TAG = "WebRTC"
        private var factory: PeerConnectionFactory? = null
//        internal var localStream: MediaStream? = null
//        private var videoCapturer: VideoCapturer? = null
        private var eglBase: EglBase? = null

//        private var localVideoTrack: VideoTrack? = null
//        private val localRenderer: VideoRenderer? = null

        // interface -----------------

        internal fun setup(activity: Activity, eglBase: EglBase) {
            WebRTC.eglBase = eglBase

            // initialize Factory
            PeerConnectionFactory.initializeAndroidGlobals(activity.applicationContext, true)
            val options = PeerConnectionFactory.Options()
            factory = PeerConnectionFactory(options)
            factory!!.setVideoHwAccelerationOptions(eglBase.eglBaseContext, eglBase.eglBaseContext)

//            var localStream = factory!!.createLocalMediaStream("android_local_stream")
//            this.localStream = localStream
//
//            // videoTrack
//            videoCapturer = createCameraCapturer(Camera2Enumerator(activity))
//            val localVideoSource = factory!!.createVideoSource(videoCapturer!!)
//            localVideoTrack = factory!!.createVideoTrack("android_local_videotrack", localVideoSource)
//            localStream.addTrack(localVideoTrack!!)
//
//            // audioTrack
//            val audioSource = factory!!.createAudioSource(WebRTCUtil.mediaStreamConstraints())
//            val audioTrack = factory!!.createAudioTrack("android_local_audiotrack", audioSource)
//            localStream.addTrack(audioTrack)
//
//            val displayMetrics = DisplayMetrics()
//            val windowManager = activity.application.getSystemService(Context.WINDOW_SERVICE) as WindowManager
//            windowManager.defaultDisplay.getRealMetrics(displayMetrics)
//            val videoWidth = displayMetrics.widthPixels
//            val videoHeight = displayMetrics.heightPixels
//
//            videoCapturer!!.startCapture(videoWidth, videoHeight, 30)
        }

        // implements -------------

        private fun createCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
            return createBackCameraCapturer(enumerator)
        }

        private fun createBackCameraCapturer(enumerator: CameraEnumerator): VideoCapturer? {
            val deviceNames = enumerator.deviceNames

            for (deviceName in deviceNames) {
                if (!enumerator.isFrontFacing(deviceName)) {
                    val videoCapturer = enumerator.createCapturer(deviceName, null)

                    if (videoCapturer != null) {
                        return videoCapturer
                    }
                }
            }

            return null
        }
    }
}