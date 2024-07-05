/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nextcloud.talk.contacts

import com.nextcloud.talk.models.json.autocomplete.AutocompleteOverall
import com.nextcloud.talk.models.json.conversations.RoomOverall

interface ContactsRepository {
    suspend fun getContacts(searchQuery: String?, shareTypes: List<String>): AutocompleteOverall

    suspend fun createRoom(credentials: String, url: String, queryMap: MutableMap<String, String>): RoomOverall
}
