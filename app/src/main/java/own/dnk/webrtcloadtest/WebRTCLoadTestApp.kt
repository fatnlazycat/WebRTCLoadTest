package own.dnk.webrtcloadtest

import android.support.multidex.MultiDexApplication

class WebRTCLoadTestApp: MultiDexApplication() {
    companion object {
        lateinit var instance: WebRTCLoadTestApp
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}