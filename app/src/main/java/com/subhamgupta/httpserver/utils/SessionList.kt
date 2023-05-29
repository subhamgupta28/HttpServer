package com.subhamgupta.httpserver.utils

import com.subhamgupta.httpserver.domain.model.DSession
import com.subhamgupta.httpserver.domain.objects.getSession
import com.subhamgupta.httpserver.domain.objects.getSessions
import com.subhamgupta.httpserver.domain.objects.insertSession
import com.subhamgupta.httpserver.domain.objects.updateSession

object SessionList {
    fun getItemsAsync(): List<DSession> {
        return getSessions()
    }

    fun addOrUpdateAsync(clientId: String, updateItem: (DSession) -> Unit) {
        var item = getSession(clientId)
        var isInsert = false
        if (item == null) {
            item = DSession()
            item.clientId = clientId
            isInsert = true
        }

        updateItem(item)

        if (isInsert) {
            insertSession(item)
        } else {
            updateSession(item, clientId)
        }
    }

    fun deleteAsync(item: DSession) {

    }
}