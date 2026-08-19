package com.teleport.messenger.webrtc

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import org.webrtc.*

/** WebRTC-менеджер с STUN и сигналингом через WebSocket. */
class WebRtcCallManager(private val context: Context) {
    private var factory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    private var remoteVideoTrack: VideoTrack? = null
    private var videoCapturer: CameraVideoCapturer? = null
    private var eglBase: EglBase? = null
    private var signalSender: ((JsonObject) -> Unit)? = null
    private var activeChatId: String? = null
    private var isVideoCall = false

    private val _connected = MutableStateFlow(false)
    val connected: StateFlow<Boolean> = _connected

    private val _localVideo = MutableStateFlow<VideoTrack?>(null)
    val localVideo: StateFlow<VideoTrack?> = _localVideo

    private val _remoteVideo = MutableStateFlow<VideoTrack?>(null)
    val remoteVideo: StateFlow<VideoTrack?> = _remoteVideo

    private var muted = false
    private var videoEnabled = true

    private val iceServers = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").createIceServer(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").createIceServer(),
    )

    fun setSignalSender(sender: (JsonObject) -> Unit) {
        signalSender = sender
    }

    fun eglContext(): EglBase.Context? = eglBase?.eglBaseContext

    fun initialize() {
        if (factory != null) return
        PeerConnectionFactory.initialize(
            PeerConnectionFactory.InitializationOptions.builder(context.applicationContext)
                .createInitializationOptions(),
        )
        eglBase = EglBase.create()
        factory = PeerConnectionFactory.builder()
            .setVideoEncoderFactory(DefaultVideoEncoderFactory(eglBase!!.eglBaseContext, true, true))
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(eglBase!!.eglBaseContext))
            .createPeerConnectionFactory()
    }

    fun startCall(isVideo: Boolean, chatId: String) {
        initialize()
        activeChatId = chatId
        isVideoCall = isVideo
        val f = factory ?: return
        val audioSource = f.createAudioSource(MediaConstraints())
        localAudioTrack = f.createAudioTrack("audio0", audioSource)
        localAudioTrack?.setEnabled(true)

        if (isVideo) setupVideo(f)

        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        peerConnection = f.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onIceCandidate(candidate: IceCandidate) {
                sendSignal(buildJsonObject {
                    put("type", "ice")
                    put("chatId", chatId)
                    put("sdpMid", candidate.sdpMid)
                    put("sdpMLineIndex", candidate.sdpMLineIndex)
                    put("candidate", candidate.sdp)
                })
            }

            override fun onConnectionChange(state: PeerConnection.PeerConnectionState) {
                _connected.value = state == PeerConnection.PeerConnectionState.CONNECTED
            }

            override fun onSignalingChange(p0: PeerConnection.SignalingState?) = Unit
            override fun onIceConnectionChange(p0: PeerConnection.IceConnectionState?) = Unit
            override fun onIceConnectionReceivingChange(p0: Boolean) = Unit
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) = Unit
            override fun onIceCandidatesRemoved(p0: Array<out IceCandidate>?) = Unit
            override fun onAddStream(p0: MediaStream?) = Unit
            override fun onRemoveStream(p0: MediaStream?) = Unit
            override fun onDataChannel(p0: DataChannel?) = Unit
            override fun onRenegotiationNeeded() = Unit

            override fun onAddTrack(receiver: RtpReceiver?, streams: Array<out MediaStream>?) {
                val track = receiver?.track() as? VideoTrack ?: return
                remoteVideoTrack = track
                _remoteVideo.value = track
            }
        })
        peerConnection?.addTrack(localAudioTrack)
        if (localVideoTrack != null) peerConnection?.addTrack(localVideoTrack)
        createOffer(isVideo)
        Log.d(TAG, "Call started (video=$isVideo, chat=$chatId)")
    }

    private fun setupVideo(f: PeerConnectionFactory) {
        val egl = eglBase ?: return
        val enumerator = Camera2Enumerator(context)
        val deviceName = enumerator.deviceNames.firstOrNull { enumerator.isFrontFacing(it) }
            ?: enumerator.deviceNames.firstOrNull() ?: return
        videoCapturer = enumerator.createCapturer(deviceName, null)
        val videoSource = f.createVideoSource(videoCapturer!!.isScreencast)
        val helper = SurfaceTextureHelper.create("CaptureThread", egl.eglBaseContext)
        videoCapturer?.initialize(helper, context, videoSource.capturerObserver)
        videoCapturer?.startCapture(640, 480, 24)
        localVideoTrack = f.createVideoTrack("video0", videoSource)
        localVideoTrack?.setEnabled(true)
        videoEnabled = true
        _localVideo.value = localVideoTrack
    }

    private fun createOffer(receiveVideo: Boolean) {
        val pc = peerConnection ?: return
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", receiveVideo.toString()))
        }
        pc.createOffer(object : SdpObserverAdapter() {
            override fun onCreateSuccess(sdp: SessionDescription?) {
                if (sdp == null) return
                pc.setLocalDescription(SdpObserverAdapter(), sdp)
                sendSignal(buildJsonObject {
                    put("type", "offer")
                    put("chatId", activeChatId ?: "")
                    put("sdp", sdp.description)
                })
            }
        }, constraints)
    }

    fun handleSignal(payload: JsonObject) {
        val type = payload["type"]?.jsonPrimitive?.content ?: return
        val chatId = payload["chatId"]?.jsonPrimitive?.content ?: activeChatId ?: return
        when (type) {
            "offer" -> {
                val sdp = payload["sdp"]?.jsonPrimitive?.content ?: return
                if (peerConnection == null) startCall(false, chatId)
                peerConnection?.setRemoteDescription(
                    SdpObserverAdapter(),
                    SessionDescription(SessionDescription.Type.OFFER, sdp),
                )
                peerConnection?.createAnswer(object : SdpObserverAdapter() {
                    override fun onCreateSuccess(answer: SessionDescription?) {
                        if (answer == null) return
                        peerConnection?.setLocalDescription(SdpObserverAdapter(), answer)
                        sendSignal(buildJsonObject {
                            put("type", "answer")
                            put("chatId", chatId)
                            put("sdp", answer.description)
                        })
                    }
                }, MediaConstraints())
            }

            "answer" -> {
                val sdp = payload["sdp"]?.jsonPrimitive?.content ?: return
                peerConnection?.setRemoteDescription(
                    SdpObserverAdapter(),
                    SessionDescription(SessionDescription.Type.ANSWER, sdp),
                )
            }

            "ice" -> {
                val candidate = payload["candidate"]?.jsonPrimitive?.content ?: return
                val sdpMid = payload["sdpMid"]?.jsonPrimitive?.content ?: ""
                val index = payload["sdpMLineIndex"]?.jsonPrimitive?.content?.toIntOrNull() ?: 0
                peerConnection?.addIceCandidate(IceCandidate(sdpMid, index, candidate))
            }
        }
    }

    private fun sendSignal(payload: JsonObject) {
        signalSender?.invoke(payload)
    }

    fun toggleMute(): Boolean {
        muted = !muted
        localAudioTrack?.setEnabled(!muted)
        return muted
    }

    fun toggleVideo(): Boolean {
        videoEnabled = !videoEnabled
        localVideoTrack?.setEnabled(videoEnabled)
        return videoEnabled
    }

    fun isVideoCall(): Boolean = isVideoCall

    fun endCall() {
        runCatching { videoCapturer?.stopCapture() }
        videoCapturer?.dispose()
        videoCapturer = null
        localVideoTrack?.dispose()
        localVideoTrack = null
        remoteVideoTrack = null
        _localVideo.value = null
        _remoteVideo.value = null
        localAudioTrack?.dispose()
        peerConnection?.close()
        peerConnection = null
        localAudioTrack = null
        activeChatId = null
        isVideoCall = false
        _connected.value = false
    }

    fun release() {
        endCall()
        factory?.dispose()
        factory = null
        eglBase?.release()
        eglBase = null
    }

    private open class SdpObserverAdapter : SdpObserver {
        override fun onCreateSuccess(sdp: SessionDescription?) = Unit
        override fun onSetSuccess() = Unit
        override fun onCreateFailure(p0: String?) {
            Log.w(TAG, "SDP create fail: $p0")
        }

        override fun onSetFailure(p0: String?) {
            Log.w(TAG, "SDP set fail: $p0")
        }
    }

    companion object {
        private const val TAG = "WebRtcCallManager"
    }
}
