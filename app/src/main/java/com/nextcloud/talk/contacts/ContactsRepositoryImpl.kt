/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nextcloud.talk.contacts

import com.nextcloud.talk.api.NcApiCoroutines
import com.nextcloud.talk.models.json.autocomplete.AutocompleteOverall
import com.nextcloud.talk.models.json.conversations.RoomOverall

class ContactsRepositoryImpl(
    private val ncApiCoroutines: NcApiCoroutines
) : ContactsRepository {

    override suspend fun getContacts(
        baseUrl: String,
        ocsApiVersion: String,
        shareList: List<String>,
        options: Map<String, Any>
    ): AutocompleteOverall {
        val response = ncApiCoroutines.getContactsWithSearchParam(
            baseUrl,
            ocsApiVersion,
            shareList,
            options
        )
        return response
    }

    override suspend fun createRoom(
        credentials: String,
        url: String,
        queryMap: MutableMap<String, String>
    ): RoomOverall {
        val response = ncApiCoroutines.createRoom(
            credentials,
            url,
            queryMap
        )
        return response
    }
}
