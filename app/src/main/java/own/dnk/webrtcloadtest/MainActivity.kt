package own.dnk.webrtcloadtest

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.lang.StringBuilder
import java.net.ServerSocket
import java.nio.charset.Charset


class MainActivity : AppCompatActivity() {
    val TAG = "MainActivity"

    private val numberOfConnections = 40

    private var views: Array<TextView> = arrayOf()


    private var server: ServerSocket? = null

    private var serverConnection: Runnable = Runnable {
        try {
            server = ServerSocket(53000)

            while (true) {
                val socket = server!!.accept()

                val inputStreamReader = InputStreamReader(socket.getInputStream())
                val bufferedReader = BufferedReader(inputStreamReader)
                var line = bufferedReader.readLine()

                val method = line.split(" ")[0]

                val headersSb = StringBuilder(line)
                while (line.isNotEmpty()) {
                    line = bufferedReader.readLine()
                    headersSb.append(line)
                }
                val headers = headersSb.toString()

                var response = "HTTP/1.1 200 OK\r\n" +
                        "Access-Control-Allow-Origin: *\r\n\r\n"

                when(method) {
                    "GET" -> {
                        response += WebRTCManager.offers.toString()
                    }
                    "POST" -> {
                        val payloadSb = StringBuilder()
                        val l = "Content-Length: (\\d+)".toRegex().find(headers)?.groupValues?.get(1)?.toInt()

                        l?. let {
                            for (i in 0 until l) {
                                payloadSb.append(bufferedReader.read().toChar())
                            }
                        }
                        val payload = payloadSb.toString()

                        try {
                            val json = JSONObject(payload)

                            views
                                .map { it.tag as WebRTCManager}
                                .find { it.toString() == json.getString("id") }
                                ?.onMessage(json)

                        } catch (e: JSONException) {
                            Log.d(TAG, e.toString())
                        }
                    }
                }


                Log.d(TAG, headers)

                val outputStream = socket.getOutputStream()
                outputStream.write(response.toByteArray(Charset.forName("UTF-8")))
                outputStream.flush()
                outputStream.close()

                inputStreamReader.close()
                bufferedReader.close()
                socket.close()
            }
        } catch (e: Exception) {
            Log.e(TAG, e.toString())
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val llMain = findViewById<LinearLayout>(R.id.llMain)
        val layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT)

        for (i in 0 until  numberOfConnections) {
            val tv = TextView(this)
            tv.layoutParams = layoutParams
            tv.text = "View# $i,    "

            val observer = WebRTCManager(onMessageFunc)

            tv.tag = observer

            views += tv

        }
        views.forEach { llMain.addView(it) }

        Thread(serverConnection).start()
    }

    private fun findView(tag: WebRTCManager): TextView? = views.find { it.tag == tag }


    private val onMessageFunc = {
        tag: WebRTCManager, msg: String -> runOnUiThread {
        val view = findView(tag)
        val newText = view?.text?.dropLast(2) ?: "" + msg
        view?.text = newText
    }}

}
