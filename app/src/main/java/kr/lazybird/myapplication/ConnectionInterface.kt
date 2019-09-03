package kr.lazybird.myapplication

import org.webrtc.MediaStream

internal interface ConnectionCallbacks {

    fun onAddedStream(mediaStream: MediaStream)

}

interface ConnectionInterface {

    fun targetToken(): String
    fun setTargetToken(targetToken: String)
    fun accessToken(): String
    fun setAccessToken(accessToken: String)
    fun publishOffer()
    fun receiveOffer(sdp: String)
    fun receiveAnswer(sdp: String)
    fun receiveCandidate(candidate: String, sdpMid: String, sdpMLineIndex: Int)
    fun close()

}