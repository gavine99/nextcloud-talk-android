/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nextcloud.talk.contacts

import com.nextcloud.talk.api.NcApiCoroutines
import com.nextcloud.talk.models.json.autocomplete.AutocompleteOverall

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
}
