package kr.lazybird.myapplication

import org.json.JSONObject
import org.webrtc.MediaStream

internal interface ConnectionCallbacks {

    fun onAddedStream(mediaStream: MediaStream)

}

interface ConnectionInterface {

    fun targetToken(): String
    fun setTargetToken(targetToken: String)
    fun accessToken(): String
    fun setAccessToken(accessToken: String)
    fun clientToken(): String
    fun setClientToken(clientToken: String)
    fun publishOffer(channelType: String, label: String)
    fun receiveOffer(channelType: String, label: String, sdp: String)
    fun receiveAnswer(channelType: String, label: String, sdp: String)
    fun receiveCandidate(channelType: String, label: String, candidate: String, sdpMid: String, sdpMLineIndex: Int)
    fun sendData(data: JSONObject)
    fun sendDataControl(data: JSONObject)
    fun close(channelType: String, label: String)

}