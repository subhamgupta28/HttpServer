package com.subhamgupta.httpserver.domain.objects

import android.icu.text.CaseMap.Fold
import com.subhamgupta.httpserver.domain.model.DSession
import com.subhamgupta.httpserver.domain.model.FileObj
import com.subhamgupta.httpserver.domain.model.FolderObj
import com.subhamgupta.httpserver.domain.objects.LocalDatabase.folders
import com.subhamgupta.httpserver.domain.objects.LocalDatabase.imageFiles
import com.subhamgupta.httpserver.domain.objects.LocalDatabase.sessionList
import com.subhamgupta.httpserver.domain.objects.LocalDatabase.videoFiles

object LocalDatabase {
    internal val videoFiles = ArrayList<FileObj>()
    internal val imageFiles = ArrayList<FileObj>()
    internal val folders = mutableListOf<FolderObj>()

    internal val sessionList = ArrayList<DSession>()

}

fun setFolders(fo: MutableList<FolderObj>) {
    folders.clear()
    folders.addAll(fo)
}
fun getFolders(): MutableList<FolderObj> = folders


fun setVideoFiles(files: List<FileObj>) {
    videoFiles.clear()
    videoFiles.addAll(files)
}

fun getVideos(): List<FileObj> = videoFiles

fun setImages(images: List<FileObj>){
    imageFiles.clear()
    imageFiles.addAll(images)
}
fun getImages(): List<FileObj> = imageFiles

fun getSessions(): List<DSession> {
    val ses = DSession("1234")
    sessionList.add(ses)
    return sessionList
}

fun getSession(clientId: String): DSession? {
    return sessionList.find { it.clientId == clientId }

}

fun insertSession(session: DSession) {
    sessionList.add(session)
}

fun updateSession(session: DSession, clientId: String) {
    sessionList.find { it.clientId == clientId }?.let {

    }
}