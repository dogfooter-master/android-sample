package kr.lazybird.myapplication

import org.json.JSONObject
import org.webrtc.MediaStream

class Connection internal constructor(private var _accessToken: String, private var _targetToken: String, private val ws: DerMateWebSocketInterface, private val callbacks: ConnectionCallbacks) : ConnectionInterface {
    private val webRTC: WebRTCInterface

    init {

        this.webRTC = WebRTC(object : WebRTCCallbacks {

            override fun onCreateOffer(sdp: String) {
                try {
                    val json = JSONObject()
                    json.put("sdp", sdp)
                    json.put("type", "offer")
                    ws.publishOffer(accessToken(), targetToken(), json)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onCreateAnswer(sdp: String) {

                try {
                    val json = JSONObject()
                    json.put("sdp", sdp)
                    json.put("type", "answer")
                    ws.publishAnswer(accessToken(), targetToken(), json)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }

            override fun onAddedStream(mediaStream: MediaStream) {
                callbacks.onAddedStream(mediaStream)
            }

            override fun onIceCandidate(sdp: String, sdpMid: String, sdpMLineIndex: Int) {

                try {
                    val json = JSONObject()
                    json.put("type", "candidate")
                    json.put("candidate", sdp)
                    json.put("sdpMid", sdpMid)
                    json.put("sdpMLineIndex", sdpMLineIndex)
                    ws.publishCandidate(accessToken(), targetToken(), json)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })
    }

    // ▼ interface

    override fun accessToken(): String {
        return _accessToken
    }

    override fun setAccessToken(accessToken: String) {
       _accessToken = accessToken
    }

    override fun targetToken(): String {
        return _targetToken
    }

    override fun setTargetToken(targetToken: String) {
        _targetToken = targetToken
    }

    override fun publishOffer() {
        webRTC.createOffer()
    }

    override fun receiveOffer(sdp: String) {
        webRTC.receiveOffer(sdp)
    }

    override fun receiveAnswer(sdp: String) {
        webRTC.receiveAnswer(sdp)
    }

    override fun receiveCandidate(candidate: String, sdpMid: String, sdpMLineIndex: Int) {
        webRTC.receiveCandidate(candidate, sdpMid, sdpMLineIndex)
    }

    override fun close() {
        webRTC.close()
    }

}