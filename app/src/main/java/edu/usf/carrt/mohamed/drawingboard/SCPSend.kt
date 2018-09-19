package edu.usf.carrt.mohamed.drawingboard

import android.os.AsyncTask
import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.util.*
import kotlin.concurrent.thread

class SCPSend  {

    fun sendFile(username: String,
                         password: String,
                         hostname: String,
                         file:File) {

        thread(start = true) {
            val jsch = JSch()
            var session = jsch.getSession(username, hostname, 22)
            session.setPassword(password)

            // Avoid asking for key confirmation.
            val properties = Properties()
            properties.put("StrictHostKeyChecking", "no")
            session.setConfig(properties)

            session.connect()


            // Create SSH Channel.
            var command = "scp -p" +" -t "+file.name
            val sshChannel = session!!.openChannel("exec") as ChannelExec


            // Execute command.
            sshChannel.setCommand(command)

            val outputStream = sshChannel.outputStream


            sshChannel.connect()


            // Send modified ime
            command = "T "+(file.lastModified()/1000)+" 0"
            command += (" "+(file.lastModified()/1000)+" 0\n")
            outputStream.write(command.toByteArray())
            outputStream.flush()

            // Send file size
            val filesize = file.length()
            command = "C0644 $filesize "
            command += file.name + "\n"
            outputStream.write(command.toByteArray())
            outputStream.flush()


            // Send file contents
            val fis = FileInputStream(file)
            val buf = ByteArray(1024)
            while(true){
                val len = fis.read(buf,0,buf.size)
                if(len <=0)
                    break;
                outputStream.write(buf,0,len)
            }
            fis.close()
            buf[0] = 0
            outputStream.write(buf,0,1)
            outputStream.flush()

            outputStream.close()






            // Sleep needed in order to wait long enough to get result back.
            Thread.sleep(1_000)
            sshChannel.disconnect()

            session.disconnect()


        }


    }


}