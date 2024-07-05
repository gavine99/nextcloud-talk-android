/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nextcloud.talk.contacts

import com.nextcloud.talk.api.NcApiCoroutines
import com.nextcloud.talk.data.user.model.User
import com.nextcloud.talk.models.RetrofitBucket
import com.nextcloud.talk.models.json.autocomplete.AutocompleteOverall
import com.nextcloud.talk.models.json.conversations.RoomOverall
import com.nextcloud.talk.users.UserManager
import com.nextcloud.talk.utils.ApiUtils

class ContactsRepositoryImpl(
    private val ncApiCoroutines: NcApiCoroutines,
    private val userManager: UserManager
) : ContactsRepository {

    private val _currentUser = userManager.currentUser.blockingGet()
    val currentUser: User = _currentUser
    val credentials = ApiUtils.getCredentials(_currentUser.username, _currentUser.token)
    val apiVersion = ApiUtils.getConversationApiVersion(_currentUser, intArrayOf(ApiUtils.API_V4, 1))

    override suspend fun getContacts(searchQuery: String?, shareTypes: List<String>): AutocompleteOverall {
        val retrofitBucket: RetrofitBucket =
            ApiUtils.getRetrofitBucketForContactsSearchFor14(currentUser!!.baseUrl!!, searchQuery)
        val modifiedQueryMap: HashMap<String, Any> = HashMap(retrofitBucket.queryMap)
        modifiedQueryMap["limit"] = 50
        modifiedQueryMap["shareTypes[]"] = shareTypes

        val response = ncApiCoroutines.getContactsWithSearchParam(
            credentials!!,
            retrofitBucket.url!!,
            shareTypes,
            modifiedQueryMap
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
