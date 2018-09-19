package edu.usf.carrt.mohamed.drawingboard

import android.content.Context
import android.content.res.AssetManager
import android.os.AsyncTask
import android.util.Log

import com.jcraft.jsch.Channel
import com.jcraft.jsch.ChannelSftp
import com.jcraft.jsch.JSch
import com.jcraft.jsch.JSchException
import com.jcraft.jsch.Session
import com.jcraft.jsch.SftpException

import net.schmizz.sshj.common.Base64

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import kotlin.concurrent.thread

object SFTPClient {

    @Throws(IOException::class)
    fun upload(file: File) {
        thread(start = true) {
            try{
                val ftpHost = "192.168.4.1"
                val ftpPort = 22
                val ftpUserName = "pi"
                val ftpPassword = "carrt123"
                var ftpRemoteDirectory = "/home/pi/yuh"
                val fileToTransmit = file.absolutePath
                println("Creating session.")
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
                    println("Session created.")
                    session!!.setPassword(ftpPassword)

                    val config = java.util.Properties()
                    config["StrictHostKeyChecking"] = "no"
                    session.setConfig(config)
                    println("Session connected before.")
                    session.connect()
                    println("Session connected.")

                    println("OPEN SFTP CHANNEL")
                    //
                    // Open the SFTP channel
                    //
                    println("Opening Channel.")
                    channel = session.openChannel("sftp")
                    channel!!.connect()
                    c = channel as ChannelSftp?
                    println("Now checing status")
                } catch (e: Exception) {
                    System.err.println("Unable to connect to FTP server." + e.toString())
                    throw e
                }

                //
                // Change to the remote directory
                //
                println("Now performing operations")
                ftpRemoteDirectory = "/home/pi/RobotCommands/"
                println("Changing to FTP remote dir: $ftpRemoteDirectory")
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
                    throw e
                }

                //
                // Disconnect from the FTP server
                //
                try {
                    c.quit()
                } catch (exc: Exception) {
                    System.err.println("Unable to disconnect from FTPserver. " + exc.toString())
                }

            } catch (e: Exception) {
                System.err.println("Error: " + e.toString())
            }
        }

        println("Process Complete.")

    }
}