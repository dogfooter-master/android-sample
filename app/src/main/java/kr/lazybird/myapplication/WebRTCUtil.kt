package kr.lazybird.myapplication

import org.webrtc.MediaConstraints

object WebRTCUtil {

    internal fun peerConnectionConstraints(): MediaConstraints {
        return audioVideoConstraints()
    }

    internal fun offerConnectionConstraints(): MediaConstraints {
        return audioVideoConstraints()
    }

    internal fun answerConnectionConstraints(): MediaConstraints {
        return audioVideoConstraints()
    }

    internal fun mediaStreamConstraints(): MediaConstraints {
        val constraints = MediaConstraints()

        return constraints
    }

    private fun audioVideoConstraints(): MediaConstraints {
        val constraints = MediaConstraints()
        constraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
        constraints.mandatory.add(
            MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))

        return constraints
    }

}