package kr.lazybird.myapplication

import org.json.JSONObject
import org.webrtc.MediaStream

internal interface WebRTCCallbacks {

    fun onCreateOffer(sdp: String)
    fun onCreateAnswer(sdp: String)
    fun onAddedStream(mediaStream: MediaStream)
    fun onIceCandidate(sdp: String, sdpMid: String, sdpMLineIndex: Int)

}

interface WebRTCInterface {

    fun createOffer()
    fun receiveOffer(sdp: String)
    fun receiveAnswer(sdp: String)
    fun receiveCandidate(sdp: String, sdpMid: String, sdpMLineIndex: Int)
    fun sendData(data: JSONObject)
    fun close()

}