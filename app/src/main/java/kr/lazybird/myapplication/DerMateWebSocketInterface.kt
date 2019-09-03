package kr.lazybird.myapplication

import org.json.JSONObject

interface DerMateWebSocketInterface {

    fun connect(connection: ConnectionInterface)
    fun publishCallMe()
    fun publishOffer(accessToken: String, targetToken: String, sdp: JSONObject)
    fun publishAnswer(accessToken: String, targetToken: String, sdp: JSONObject)
    fun publishCandidate(accessToken: String, targetToken: String, candidate: JSONObject)
    fun close()

}