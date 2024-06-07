/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nextcloud.talk.contacts

import com.nextcloud.talk.adapters.items.ContactItem
import com.nextcloud.talk.api.NcApiCoroutines
import okhttp3.ResponseBody

class ContactsRepositoryImpl(
    private val ncApiCoroutines: NcApiCoroutines
) : ContactsRepository {
    override suspend fun getContacts(
        baseUrl: String,
        ocsApiVersion: String,
        shareList: List<String>,
        options: Map<String, Any>
    ): List<ContactItem> {
        val response = ncApiCoroutines.getContactsWithSearchParam(baseUrl, ocsApiVersion, shareList, options)
        return parseResponseToConatctItems(response)
    }

    private fun parseResponseToConatctItems(response: ResponseBody): List<ContactItem> {
        // logic to parse data
        val contactItem = mutableListOf<ContactItem>()
        return contactItem
    }
}
