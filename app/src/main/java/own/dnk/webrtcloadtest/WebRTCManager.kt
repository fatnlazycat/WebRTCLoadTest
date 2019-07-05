package own.dnk.webrtcloadtest

import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import org.webrtc.*

class WebRTCManager(val onMessageFunc: (WebRTCManager, String) -> Unit?): SdpObserver, PeerConnection.Observer, DataChannel.Observer {
    companion object {
        var offers = JSONObject()

        var offerCounter = 0
    }

    val TAG = "WebRTCManager"

    val id by lazy { return@lazy this.toString() }

    private val peerConnection: PeerConnection? by lazy {
        val iceServers: List<PeerConnection.IceServer>? = emptyList()
        val factory = PeerConnectionFactory.builder().createPeerConnectionFactory()
        return@lazy factory.createPeerConnection(iceServers, this)
    }
    var dataChannel: DataChannel? = null

    private var candidateCounter = 0


    init {
        PeerConnectionFactory.initialize(PeerConnectionFactory.InitializationOptions
                .builder(WebRTCLoadTestApp.instance)
                .setEnableVideoHwAcceleration(false)
                .createInitializationOptions())

        dataChannel = peerConnection?.createDataChannel("dcLabel", DataChannel.Init())
        dataChannel?.registerObserver(this)

        //create offer only after setting the data channel
        peerConnection?.createOffer(this, MediaConstraints())

    }

    fun onMessage(json: JSONObject) {
        when (json.getString("type")) {
            "answer" -> {
                val answerJson = json.getJSONObject("answer")
                val type = answerJson.optString("type")
                val sdp  = answerJson.optString("sdp")
                val remoteSDP = SessionDescription(SessionDescription.Type.fromCanonicalForm(type), sdp)
                peerConnection?.setRemoteDescription(this, remoteSDP)
            }

            "candidate" -> {
                val candidateJson = json.getJSONObject("candidate")
                val candidate = IceCandidate(candidateJson.getString("sdpMid"), candidateJson.getInt("sdpMLineIndex"), candidateJson.getString("candidate"))
                peerConnection?.addIceCandidate(candidate)
            }
        }
    }


    //SdpObserver methods
    override fun onSetFailure(p0: String?) {
        Log.d(TAG, "onSetFailure")
    }

    override fun onSetSuccess() {
        Log.d(TAG, "onSetSuccess")
    }

    override fun onCreateSuccess(sdp: SessionDescription?) {
        Log.d(TAG, "onCreateSuccess")
        try {
            val messageType = sdp?.type?.canonicalForm()
            val messageSdp = sdp?.description

            //check if we've sent the offer already (maybe after renegotiation needed)
            if (messageType == "offer" && messageSdp == peerConnection?.localDescription?.description) return

            val sdpMessage = JSONObject()
            sdpMessage.put("type", messageType)
            sdpMessage.put("sdp", messageSdp)

            val payload = JSONObject()
            payload.put("offer", sdpMessage)

            offers.put(id, payload)

            peerConnection?.setLocalDescription(this, sdp)

            offerCounter += 1
            Log.d(TAG, "offer added, count=$offerCounter")
        } catch (e: JSONException) {
            Log.e(TAG, e.toString())
        }
    }

    override fun onCreateFailure(p0: String?) {
        Log.d(TAG, "onCreateFailure")
    }


    //PeerConnection.Observer methods
    override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) {
        Log.d(TAG, "onIceCandidatesRemoved")
    }

    override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream>?) {
        Log.d(TAG, "onAddTrack")
    }

    override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {
        Log.d(TAG, "onIceGatheringChanged")
    }

    override fun onAddStream(p0: MediaStream?) {
        Log.d(TAG, "onAddStream")
    }

    override fun onIceCandidate(candidate: IceCandidate?) {
        Log.d(TAG, "onIceCandidate")

        try {
            val sdpCandidate = JSONObject()
            sdpCandidate.put("type", "candidate")
            sdpCandidate.put("label", candidate?.sdpMLineIndex)
            sdpCandidate.put("id", candidate?.sdpMid)
            sdpCandidate.put("candidate", candidate?.sdp)

            val oldValue = offers.optJSONObject(id)
            oldValue.put("candidate", sdpCandidate)
            offers.put(id, oldValue)

            candidateCounter += 1
            Log.d(TAG, "candidate added, count=$candidateCounter")
        } catch (e: JSONException) {
            Log.e(TAG, e.toString())
        }
    }

    override fun onDataChannel(p0: DataChannel?) {
        Log.d(TAG, "onDataChannel")
    }

    override fun onSignalingChange(p0: PeerConnection.SignalingState?) {
        Log.d(TAG, "onSignalingChanged, state=${p0?.name}")
    }

    override fun onIceConnectionReceivingChange(p0: Boolean) {
        Log.d(TAG, "onIceConnectionReceivingChange")
    }

    override fun onRemoveStream(p0: MediaStream?) {
        Log.d(TAG, "onRemoveStream")
    }

    override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) {
        Log.d(TAG, "onIceConnectionChange, state=${p0?.name}")
    }

    override fun onRenegotiationNeeded() {
        Log.d(TAG, "onRenegotiationNeeded")
    }


    //DataChannel.Observer methods
    override fun onMessage(buffer: DataChannel.Buffer?) {
        Log.d(TAG, "onMessage")
        val data = buffer?.data
        val bytes = ByteArray(data?.remaining() ?: 0)
        data?.get(bytes)
        val command = String(bytes)
        Log.d(TAG, command)
        onMessageFunc(this, command)
    }

    override fun onBufferedAmountChange(p0: Long) {
        Log.d(TAG, "onBufferedAmountChanged")
    }

    override fun onStateChange() {
        Log.d(TAG, "onStateChange, state=${dataChannel?.state()}")
    }
}