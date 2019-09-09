package kr.lazybird.myapplication

import android.util.Log
import android.widget.Switch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocketListener
import okhttp3.WebSocket
import okio.ByteString
import org.json.JSONObject
import java.security.SecureRandom
import java.security.cert.CertificateException
import java.security.cert.X509Certificate
import java.util.concurrent.TimeUnit
import javax.net.ssl.*

class DerMateWebSocket internal constructor(
    private var host: String,
    private val accessToken: String,
    private val targetToken: String
) :
    DerMateWebSocketInterface {
    private lateinit var mConnection: WebSocket
    private lateinit var mWebRTCConnection: ConnectionInterface

    // interface -------
    private class DefaultTrustManager : X509TrustManager {
        override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {

        }

        override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {

        }

        override fun getAcceptedIssuers(): Array<X509Certificate> {
            return arrayOf()
        }
    }

    override fun sendWebRTCDataChannel(data: JSONObject, label: String) {
        when ( label ) {
            accessToken -> {
                mWebRTCConnection.sendData(data)
            }
            else -> {
                mWebRTCConnection.sendDataControl(data)
            }
        }
    }

    override fun connect(connection: ConnectionInterface) {

        val HandshakeEndpoint = host

        //val ctx = SSLContext.getInstance("TLS")
        //val trustAllCerts = arrayOf<TrustManager>(DefaultTrustManager())
        //ctx.init(arrayOfNulls<KeyManager>(0), trustAllCerts, SecureRandom())
        //SSLContext.setDefault(ctx)

        val builder = OkHttpClient.Builder()

//        builder.readTimeout(3, TimeUnit.SECONDS)
//        builder.sslSocketFactory(ctx.socketFactory, trustAllCerts[0] as X509TrustManager)
//        builder.hostnameVerifier { _, _ -> true }
        val client = builder.build()
//        val client = builder.sslSocketFactory(ctx.socketFactory).build()
        val request = Request.Builder()
            .url(HandshakeEndpoint)
            .build()

        mWebRTCConnection = connection
        val wsListener = EchoWebSocketListener(accessToken, targetToken, connection)
        mConnection = client.newWebSocket(request, wsListener)
    }

    override fun publishCallMe() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun publishOffer(accessToken: String, targetToken: String, sdp: JSONObject, channelType: String, label: String) {
        val json = JSONObject()
        val payload = JSONObject()
        payload.put("category", "ws")
        payload.put("service", "Offer")
        payload.put("access_token", accessToken)
        payload.put("opponent_client_token", targetToken)
        payload.put("channel_type", channelType)
        payload.put("label", label)
        payload.put("sdp", sdp)
        json.put("data", payload)
        Log.d("SWS", json.toString())
        mConnection.send(json.toString())
    }

    override fun publishAnswer(accessToken: String, targetToken: String, sdp: JSONObject, channelType: String, label: String) {
        val json = JSONObject()
        val payload = JSONObject()
        payload.put("category", "ws")
        payload.put("service", "Answer")
        payload.put("access_token", accessToken)
        payload.put("opponent_client_token", targetToken)
        payload.put("channel_type", channelType)
        payload.put("label", label)
        payload.put("sdp", sdp)
        json.put("data", payload)

        Log.d("SWS", json.toString())
        mConnection.send(json.toString())
    }

    override fun publishCandidate(accessToken: String, targetToken: String, candidate: JSONObject, channelType: String, label: String) {
        val json = JSONObject()
        val payload = JSONObject()
        val c = JSONObject()

        c.put("type", "candidate")
        c.put("candidate", candidate)

        payload.put("category", "ws")
        payload.put("service", "Candidate")
        payload.put("access_token", accessToken)
        payload.put("opponent_client_token", targetToken)
        payload.put("channel_type", channelType)
        payload.put("label", label)
        payload.put("candidate", c)

        json.put("data", payload)

        Log.d("SWS", json.toString())
        mConnection.send(json.toString())
    }

    override fun close() {
        mWebRTCConnection!!.close("stream", accessToken)
        mWebRTCConnection!!.close("data", accessToken)
        mConnection.close(1000, "")
    }
    private class EchoWebSocketListener(
        val accessToken: String,
        val targetToken: String,
        val connection: ConnectionInterface
    ) :
        WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            val jsonObject = JSONObject()
            val payloadObject = JSONObject()
            Log.d("SWS", "DEBUG1")

            payloadObject.put("category", "ws")
            payloadObject.put("service", "Register")
            payloadObject.put("client_type", "android")
            payloadObject.put("access_token", accessToken)

            jsonObject.put("data", payloadObject)
            webSocket.send(jsonObject.toString())
            //webSocket.close(NORMAL_CLOSURE_STATUS, "Goodbye !")
            Log.d("SWS", jsonObject.toString())
        }

        override fun onMessage(webSocket: WebSocket?, text: String?) {
            output("Receiving : " + text!!)
            val json = JSONObject(text)
            val payload = json.getJSONObject("data")

            val service = payload.getString("service")

            when (service) {
                "Offer" -> {
                    val sdp = payload.getJSONObject("sdp")
                    val channelType = payload.getString("channel_type")
                    val label = payload.getString("label")
                    connection.receiveOffer(channelType, label, sdp.getString("sdp"))
                }
                "Candidate" -> {
                    val channelType = payload.getString("channel_type")
                    val label = payload.getString("label")
                    val candidate = payload.getJSONObject("candidate")
                    val candidateInCandidate = candidate.getJSONObject("candidate")
                    connection.receiveCandidate(
                        channelType,
                        label,
                        candidateInCandidate.getString("candidate"),
                        candidateInCandidate.getString("sdpMid"),
                        candidateInCandidate.getInt("sdpMLineIndex")
                    )
                }
                "RequestOffer" -> {
                    val channelType = payload.getString("channel_type")
                    val label = payload.getString("label")
                    connection.publishOffer(channelType, label)
                    val targetClientToken = payload.getString("opponent_client_token")
                    connection.setTargetToken(targetClientToken)
                }
                "Answer" -> {
                    val channelType = payload.getString("channel_type")
                    val label = payload.getString("label")
                    val sdp = payload.getJSONObject("sdp")
                    connection.receiveAnswer(channelType, label, sdp.getString("sdp"))
                }
                "SignIn" -> {
                    val jsonObject = JSONObject()
                    Log.d("SWS", "DEBUG: SIgnIn")
                    connection.setAccessToken(payload.getString("access_token"))

                    val sendJson = json.getJSONObject("data")

                    sendJson.put("category", "ws")
                    sendJson.put("service", "RegisterMate")
                    sendJson.put("client_type", "mate")
                    sendJson.put("access_token", connection.accessToken())

                    jsonObject.put("data", sendJson)
                    webSocket!!.send(json.toString())
                }
                "RegisterComplete" -> {
                    Log.d("SWS", "DEBUG: RegisterComplete")
                    connection.setAccessToken(accessToken)
                    connection.setTargetToken(targetToken)
                    connection.setClientToken(payload.getString("client_token"))
                    connection.publishOffer("stream", "")
                    connection.setTargetToken(targetToken)
                }
                /*
                val msgType = json.getString("type")
                if (msgType == "login") {
                    val msgSuccess = json.getBoolean("success")
                    if (msgSuccess) {
                        connection.publishOffer()
                    }
                } else if (msgType == "candidate") {
                    val msg = JSONObject(text)
                    val candidate = JSONObject(msg.getString("candidate"))
                    connection.receiveCandidate(
                        candidate.getString("candidate"),
                        candidate.getString("sdpMid"),
                        candidate.getInt("sdpMLineIndex")
                    )
                } else if (msgType == "answer") {
                    val msg = JSONObject(text)
                    val sdp = JSONObject(msg.getString("answer"))
                    connection.receiveAnswer(sdp.getString("sdp"))
                } else if (msgType == "offer") {
                    val msg = JSONObject(text)
                    val offer = JSONObject(msg.getString("offer"))
                    connection.receiveOffer(offer.getString("sdp"))
                }
                */
            }
            /*
            val msgType = json.getString("type")
            if (msgType == "login") {
                val msgSuccess = json.getBoolean("success")
                if (msgSuccess) {
                    connection.publishOffer()
                }
            } else if (msgType == "candidate") {
                val msg = JSONObject(text)
                val candidate = JSONObject(msg.getString("candidate"))
                connection.receiveCandidate(
                    candidate.getString("candidate"),
                    candidate.getString("sdpMid"),
                    candidate.getInt("sdpMLineIndex")
                )
            } else if (msgType == "answer") {
                val msg = JSONObject(text)
                val sdp = JSONObject(msg.getString("answer"))
                connection.receiveAnswer(sdp.getString("sdp"))
            } else if (msgType == "offer") {
                val msg = JSONObject(text)
                val offer = JSONObject(msg.getString("offer"))
                connection.receiveOffer(offer.getString("sdp"))
            }
            */
        }

        override fun onMessage(webSocket: WebSocket?, bytes: ByteString?) {
            output("Receiving bytes : " + bytes!!.hex())
        }

        override fun onClosing(webSocket: WebSocket?, code: Int, reason: String?) {
            webSocket!!.close(NORMAL_CLOSURE_STATUS, null)
            output("Closing : $code / $reason")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            output("Error : " + t.message)
        }

        companion object {
            private const val NORMAL_CLOSURE_STATUS = 1000
        }

        private fun output(txt: String) {
            Log.v("WSS", txt)
        }
    }
}


