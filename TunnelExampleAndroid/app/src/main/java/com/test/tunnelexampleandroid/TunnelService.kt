package com.test.tunnelexampleandroid

import android.content.Intent
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.util.Log
import java.net.DatagramSocket
import java.net.Inet4Address

class TunnelService: VpnService() {
    companion object {
        val ACTION_START = "START"
        val ACTION_STOP = "STOP"

        var isWorking: Boolean = false
            private set(value) {
                field = value
                onIsWorkingChanged?.let { it() }
            }

        var onIsWorkingChanged: (() -> Unit)? = null
    }

    private lateinit var datagramSocket: DatagramSocket
    private var networkThread: Thread? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if (intent.action == ACTION_START) {

            val serverAddress = intent.extras?.getString("serverAddress")
                ?: throw Exception("serverAddress was not provided")

            datagramSocket = DatagramSocket()

            this.protect(datagramSocket)

            val d = Runnable {
                try {
                    datagramSocket.connect(Inet4Address.getByName(serverAddress), 1111)
                } catch (e: Exception) {
                    System.err.println(e.toString())
                    this.stopSelf()
                    return@Runnable
                }
                val builder = Builder()
                val tunnel = builder.addAddress("10.0.100.7", 24)
                    .addRoute("0.0.0.0", 0)
                    .establish()


                val output = ParcelFileDescriptor.AutoCloseOutputStream(tunnel)
                val input = ParcelFileDescriptor.AutoCloseInputStream(tunnel)

                val fd = ParcelFileDescriptor.fromDatagramSocket(datagramSocket)

                val socketInput = ParcelFileDescriptor.AutoCloseInputStream(fd)
                val socketOutput = ParcelFileDescriptor.AutoCloseOutputStream(fd)
                val b = ByteArray(3000)

                isWorking = true

                while (!Thread.currentThread().isInterrupted) {
                    if (socketInput.available() > 0) {
                        val bytesRead = socketInput.read(b, 0, 3000)
                        Log.w("", "Sock input $bytesRead")
                        output.write(b, 0, bytesRead)

                    }
                    val bytesRead = input.read(b, 0, b.size)
                    if (bytesRead > 0) {
                        Log.w("", "Tun input $bytesRead")
                        try {
                            socketOutput.write(b, 0, bytesRead)
                        } catch (e: Exception) {
                            System.err.println(e.toString())
                            this.stopSelf()
                            break
                        }
                    }
                }
                input.close()
                output.close()
                socketInput.close()
                socketOutput.close()
                tunnel?.close()
                isWorking = false
            }
            networkThread = Thread(d)
            networkThread?.start()
            return START_STICKY
        } else {
            networkThread?.interrupt()
            return START_NOT_STICKY
        }
    }

    override fun onRevoke() {
        super.onRevoke()
        networkThread?.interrupt()
    }
}