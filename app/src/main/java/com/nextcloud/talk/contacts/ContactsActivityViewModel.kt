/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nextcloud.talk.contacts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nextcloud.talk.data.user.model.User
import com.nextcloud.talk.models.RetrofitBucket
import com.nextcloud.talk.models.json.autocomplete.AutocompleteUser
import com.nextcloud.talk.models.json.conversations.Conversation
import com.nextcloud.talk.users.UserManager
import com.nextcloud.talk.utils.ApiUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class ContactsActivityViewModel @Inject constructor(
    private val repository: ContactsRepository,
    private val userManager: UserManager
) : ViewModel() {

    private val _contactsViewState = MutableStateFlow<ContactsUiState>(ContactsUiState.None)
    val contactsViewState: StateFlow<ContactsUiState> = _contactsViewState
    private val _roomViewState = MutableStateFlow<RoomUiState>(RoomUiState.None)
    val roomViewState: StateFlow<RoomUiState> = _roomViewState
    private val _currentUser = userManager.currentUser.blockingGet()
    val currentUser: User = _currentUser
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery
    private val shareTypes: MutableList<String> = mutableListOf("0")
    val shareTypeList: List<String> = shareTypes

    val credentials = ApiUtils.getCredentials(_currentUser.username, _currentUser.token)
    val apiVersion = ApiUtils.getConversationApiVersion(_currentUser, intArrayOf(ApiUtils.API_V4, 1))

    init {
        getContactsFromSearchParams()
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateShareTypes(value: String) {
        shareTypes.add(value)
    }

    fun getContactsFromSearchParams() {
        _contactsViewState.value = ContactsUiState.Loading
        viewModelScope.launch {
            try {
                val contacts = repository.getContacts(
                    searchQuery.value,
                    shareTypeList
                )

                val contactsList: List<AutocompleteUser>? = contacts.ocs!!.data
                _contactsViewState.value = ContactsUiState.Success(contactsList)
            } catch (exception: Exception) {
                _contactsViewState.value = ContactsUiState.Error(exception.message ?: "")
            }
        }
    }

    fun createRoom(roomType: String, sourceType: String, userId: String, conversationName: String?) {
        val retrofitBucket: RetrofitBucket = ApiUtils.getRetrofitBucketForCreateRoom(
            apiVersion,
            _currentUser.baseUrl,
            roomType,
            sourceType,
            userId,
            conversationName
        )
        viewModelScope.launch {
            try {
                val room = repository.createRoom(
                    credentials!!,
                    retrofitBucket.url!!,
                    retrofitBucket.queryMap!!
                )

                val conversation: Conversation? = room.ocs?.data
                _roomViewState.value = RoomUiState.Success(conversation)
            } catch (exception: Exception) {
                _contactsViewState.value = ContactsUiState.Error(exception.message ?: "")
            }
        }
    }

    fun getImageUri(avatarId: String, requestBigSize: Boolean): String {
        return ApiUtils.getUrlForAvatar(
            _currentUser.baseUrl,
            avatarId,
            requestBigSize
        )
    }
}

sealed class ContactsUiState {
    data object None : ContactsUiState()
    data object Loading : ContactsUiState()
    data class Success(val contacts: List<AutocompleteUser>?) : ContactsUiState()
    data class Error(val message: String) : ContactsUiState()
}

sealed class RoomUiState {
    data object None : RoomUiState()
    data class Success(val conversation: Conversation?) : RoomUiState()
    data class Error(val message: String) : RoomUiState()
}
