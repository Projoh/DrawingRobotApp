package edu.usf.carrt.mohamed.drawingboard

import org.apache.commons.vfs2.FileSystemException
import org.apache.commons.vfs2.FileSystemOptions
import org.apache.commons.vfs2.Selectors
import org.apache.commons.vfs2.impl.StandardFileSystemManager
import org.apache.commons.vfs2.provider.sftp.SftpFileSystemConfigBuilder
import java.io.File


class SCPSender {
    fun send(localFile: String) {
//        val scp = Scp()
//        val portSSH = 22
//        val srvrSSH = "192.168.4.1"
//        val userSSH = "pi"
//        val pswdSSH = "carrt123"
//        val remoteDir = "/uploads/"
//
//        scp.setPort(portSSH)
//        scp.setLocalFile(localFile)
//        scp.setTodir("$userSSH:$pswdSSH@$srvrSSH:$remoteDir")
//        scp.setProject(Project())
//        scp.setTrust(true)
//        scp.execute()

        val hostName = "192.168.4.1"
        val username = "pi"
        val password = "carrt123"

        val remoteFilePath = "/FakeRemotePath/FakeRemoteFile.txt"

        upload(hostName, username, password, localFile, remoteFilePath)
    }

    fun upload(hostName: String, username: String, password: String, localFilePath: String, remoteFilePath: String) {

        val file = File(localFilePath)
        if (!file.exists())
            throw RuntimeException("Error. Local file not found")

        val manager = StandardFileSystemManager()

        try {
            manager.init()

            // Create local file object
            val localFile = manager.resolveFile(file.absolutePath)

            // Create remote file object
            val remoteFile = manager.resolveFile(createConnectionString(hostName, username, password, remoteFilePath), createDefaultOptions())
            /*
             * use createDefaultOptions() in place of fsOptions for all default
             * options - Ashok.
             */

            // Copy local file to sftp server
            remoteFile.copyFrom(localFile, Selectors.SELECT_SELF)

            println("File upload success")
        } catch (e: Exception) {
            throw RuntimeException(e)
        } finally {
            manager.close()
        }
    }

    fun createConnectionString(hostName: String, username: String, password: String, remoteFilePath: String): String {
        return "sftp://$username:$password@$hostName/$remoteFilePath"
    }

    @Throws(FileSystemException::class)
    fun createDefaultOptions(): FileSystemOptions {
        // Create SFTP options
        val opts = FileSystemOptions()

        // SSH Key checking
        SftpFileSystemConfigBuilder.getInstance().setStrictHostKeyChecking(opts, "no")

        /*
         * Using the following line will cause VFS to choose File System's Root
         * as VFS's root. If I wanted to use User's home as VFS's root then set
         * 2nd method parameter to "true"
         */
        // Root directory set to user home
        SftpFileSystemConfigBuilder.getInstance().setUserDirIsRoot(opts, false)

        // Timeout is count by Milliseconds
        SftpFileSystemConfigBuilder.getInstance().setTimeout(opts, 10000)

        return opts
    }
}