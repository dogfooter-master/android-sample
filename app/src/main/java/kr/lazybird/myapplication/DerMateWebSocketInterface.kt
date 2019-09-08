package kr.lazybird.myapplication

import org.json.JSONObject

interface DerMateWebSocketInterface {

    fun connect(connection: ConnectionInterface)
    fun publishCallMe()
    fun publishOffer(accessToken: String, targetToken: String, sdp: JSONObject, channelType: String, label: String)
    fun publishAnswer(accessToken: String, targetToken: String, sdp: JSONObject, channelType: String, label: String)
    fun publishCandidate(accessToken: String, targetToken: String, candidate: JSONObject, channelType: String, label: String)
    fun sendWebRTCDataChannel(data: JSONObject, label: String)
    fun close()

}