package kr.lazybird.myapplication

import android.util.Log
import org.json.JSONObject
import org.webrtc.MediaStream

class Connection internal constructor(
    private var _accessToken: String,
    private var _opponentDAT: String,
    private var _deviceAccessToken: String,
    private val ws: DerMateWebSocketInterface,
    private val callbacks: ConnectionCallbacks) : ConnectionInterface {

    private val webRTC: WebRTCInterface
    private val webRTCData: WebRTCInterface
    private val webRTCControlData: WebRTCInterface

    init {
        this.webRTC = WebRTC(object : WebRTCCallbacks {
            override fun onCreateOffer(sdp: String) {
                try {
                    val json = JSONObject()
                    json.put("sdp", sdp)
                    json.put("type", "offer")
                    ws.publishOffer(accessToken(), opponentDAT(), json, "stream", "")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCreateAnswer(sdp: String) {
                try {
                    val json = JSONObject()
                    json.put("sdp", sdp)
                    json.put("type", "answer")
                    ws.publishAnswer(accessToken(), opponentDAT(), json, "stream", "")
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
                    ws.publishCandidate(accessToken(), opponentDAT(), json, "stream", "")
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })
        this.webRTCData = WebRTC(object : WebRTCCallbacks {
            override fun onCreateOffer(sdp: String) {
                try {
                    val json = JSONObject()
                    json.put("sdp", sdp)
                    json.put("type", "offer")
                    ws.publishOffer(accessToken(), opponentDAT(), json, "data", accessToken())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCreateAnswer(sdp: String) {
                try {
                    val json = JSONObject()
                    json.put("sdp", sdp)
                    json.put("type", "answer")
                    ws.publishAnswer(accessToken(), opponentDAT(), json, "data", accessToken())
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
                    ws.publishCandidate(accessToken(), opponentDAT(), json, "data", accessToken())
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })
        this.webRTCControlData = WebRTC(object : WebRTCCallbacks {
            override fun onCreateOffer(sdp: String) {
                try {
                    val json = JSONObject()
                    json.put("sdp", sdp)
                    json.put("type", "offer")
                    ws.publishOffer(accessToken(), opponentDAT(), json, "data", deviceAccessToken())
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onCreateAnswer(sdp: String) {
                try {
                    val json = JSONObject()
                    json.put("sdp", sdp)
                    json.put("type", "answer")
                    ws.publishAnswer(accessToken(), opponentDAT(), json, "data", deviceAccessToken())
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
                    ws.publishCandidate(accessToken(), opponentDAT(), json, "data", deviceAccessToken())
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

    override fun opponentDAT(): String {
        return _opponentDAT
    }

    override fun setOpponentDAT(opponentDAT: String) {
        _opponentDAT = opponentDAT
    }

    override fun deviceAccessToken(): String {
        return _deviceAccessToken
    }

    override fun setDeviceAccessToken(deviceAccessToken: String) {
        _deviceAccessToken = deviceAccessToken
    }
    override fun publishOffer(channelType: String, label: String) {
        when (channelType) {
            "data" -> {
                when (label) {
                    accessToken() -> webRTCData.createOffer()
                    else -> webRTCControlData.createOffer()
                }
            }
            else -> {
                webRTC.createOffer()
            }
        }
    }

    override fun sendData(data: JSONObject) {
        webRTCData.sendData(data)
    }

    override fun sendDataControl(data: JSONObject) {
        webRTCControlData.sendData(data)
    }

    override fun receiveOffer(channelType: String, label: String, sdp: String) {
        when (channelType) {
            "data" -> {
                Log.d("SWS", "receiveOffer $label, ${accessToken()}")
                when (label) {
                    accessToken() -> webRTCData.receiveOffer(sdp)
                    else -> webRTCControlData.receiveOffer(sdp)
                }
            }
            else -> {
                webRTC.receiveOffer(sdp)
            }
        }
    }

    override fun receiveAnswer(channelType: String, label: String, sdp: String) {
        when (channelType) {
            "data" -> {
                Log.d("SWS", "receiveAnswer $label, ${accessToken()}")
                when (label) {
                    accessToken() -> webRTCData.receiveAnswer(sdp)
                    else -> webRTCControlData.receiveAnswer(sdp)
                }
            }
            else -> {
                webRTC.receiveAnswer(sdp)
            }
        }
    }

    override fun receiveCandidate(channelType: String, label: String, candidate: String, sdpMid: String, sdpMLineIndex: Int) {
        when (channelType) {
            "data" -> {
                Log.d("SWS", "receiveCandidate $label, ${accessToken()}")
                when (label) {
                    accessToken() -> webRTCData.receiveCandidate(candidate, sdpMid, sdpMLineIndex)
                    else -> webRTCControlData.receiveCandidate(candidate, sdpMid, sdpMLineIndex)
                }
            }
            else -> {
                webRTC.receiveCandidate(candidate, sdpMid, sdpMLineIndex)
            }
        }
    }

    override fun close(channelType: String, label: String) {
        when (channelType) {
            "data" -> {
                when (label) {
                    accessToken() -> webRTCData.close()
                    else -> webRTCControlData.close()
                }
            }
            else -> {
                webRTC.close()
            }
        }
    }
}