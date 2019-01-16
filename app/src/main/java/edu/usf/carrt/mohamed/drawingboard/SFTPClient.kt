package edu.usf.carrt.mohamed.drawingboard

import android.content.Context
import android.os.Handler
import android.widget.Toast
import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import kotlin.concurrent.thread
import android.os.Looper



object SFTPClient {

    @Throws(IOException::class)
    fun upload(context : Context, file: File) {
        thread(start = true) {
            try {
                val ftpHost = "192.168.4.1"
                val ftpPort = 22
                val ftpUserName = "pi"
                val ftpPassword = "carrt123"
                var ftpRemoteDirectory = "/home/pi/yuh"
                val fileToTransmit = file.absolutePath
                showMessage("Creating session.", context)
                val jsch = JSch()

                var session: Session? = null
                var channel: Channel? = null
                var c: ChannelSftp? = null


                //
                // Now connect and SFTP to the SFTP Server
                //
                try {
                    // Create a session sending through our username and password
                    session = jsch.getSession(ftpUserName, ftpHost, ftpPort)
                    showMessage("Session created.", context)
                    session!!.setPassword(ftpPassword)

                    val config = java.util.Properties()
                    config["StrictHostKeyChecking"] = "no"
                    session.setConfig(config)
                    showMessage("Session connected before.", context)
                    session.connect()
                    showMessage("Session connected.",context)

                    showMessage("OPEN SFTP CHANNEL", context)
                    //
                    // Open the SFTP channel
                    //
                    showMessage("Opening Channel.", context)
                    channel = session.openChannel("sftp")
                    channel!!.connect()
                    c = channel as ChannelSftp?
                    showMessage("Now checing status",context)
                } catch (e: Exception) {
                    System.err.println("Unable to connect to FTP server." + e.toString())
                    showMessage("Unable to connect to FTP server." + e.toString(),context)
                    throw e
                }

                //
                // Change to the remote directory
                //
                showMessage("Now performing operations",context)
                ftpRemoteDirectory = "/home/pi/RobotCommands/"
                showMessage("Changing to FTP remote dir: $ftpRemoteDirectory",context)
                c!!.cd(ftpRemoteDirectory)

                //
                // Send the file we generated
                //
                try {
                    val f = File(fileToTransmit)
                    println("Storing file as remote filename: " + f.name)
                    c.put(FileInputStream(f), f.name)
                } catch (e: Exception) {
                    System.err
                            .println("Storing remote file failed." + e.toString())
                    showMessage("Storing remote file failed." + e.toString(),context)
                    throw e
                }

                //
                // Disconnect from the FTP server
                //
                try {
                    c.quit()
                } catch (exc: Exception) {
                    System.err.println("Unable to disconnect from FTPserver. " + exc.toString())
                    showMessage("Unable to disconnect from FTPserver. " + exc.toString(),context)
                }

            } catch (e: Exception) {
                System.err.println("Error: " + e.toString())
                showMessage("Error: " + e.toString(),context)
            }
        }

        showMessage("Process Complete.",context)

    }

    fun showMessage(message: String, context: Context) {
        val handler = Handler(Looper.getMainLooper())

        handler.post(Runnable {
            Toast.makeText(context, message, Toast.LENGTH_SHORT)
        })
    }
}