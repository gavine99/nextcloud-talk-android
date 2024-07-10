/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nextcloud.talk.contacts

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import autodagger.AutoInjector
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.nextcloud.talk.R
import com.nextcloud.talk.application.NextcloudTalkApplication
import com.nextcloud.talk.chat.ChatActivity
import com.nextcloud.talk.models.json.autocomplete.AutocompleteUser
import com.nextcloud.talk.openconversations.ListOpenConversationsActivity
import com.nextcloud.talk.utils.bundle.BundleKeys
import javax.inject.Inject

@AutoInjector(NextcloudTalkApplication::class)
class ContactsActivityCompose : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var contactsViewModel: ContactsViewModel

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NextcloudTalkApplication.sharedApplication!!.componentApplication.inject(this)
        contactsViewModel = ViewModelProvider(this, viewModelFactory)[ContactsViewModel::class.java]

        setContent {
            MaterialTheme {
                val context = LocalContext.current
                var searchState = mutableStateOf(false)
                Scaffold(
                    topBar = {
                        AppBar(
                            title = stringResource(R.string.nc_app_product_name),
                            context = context,
                            contactsViewModel = contactsViewModel,
                            searchState = searchState
                        )
                    },
                    content = {
                        val uiState = contactsViewModel.contactsViewState.collectAsState()
                        Column(Modifier.padding(it)) {
                            ConversationCreationOptions(context = context)
                            ContactsList(
                                contactsUiState = uiState.value,
                                contactsViewModel = contactsViewModel,
                                context = context
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ContactsList(contactsUiState: ContactsUiState, contactsViewModel: ContactsViewModel, context: Context) {
    when (contactsUiState) {
        is ContactsUiState.None -> {
        }
        is ContactsUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is ContactsUiState.Success -> {
            val contacts = contactsUiState.contacts
            Log.d(CompanionClass.TAG, "Contacts:$contacts")
            if (contacts != null) {
                ContactsItem(contacts, contactsViewModel, context)
            }
        }
        is ContactsUiState.Error -> {
            val errorMessage = contactsUiState.message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: $errorMessage", color = Color.Red)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ContactsItem(contacts: List<AutocompleteUser>, contactsViewModel: ContactsViewModel, context: Context) {
    val groupedContacts: Map<String, List<AutocompleteUser>> = contacts.groupBy { contact ->
        (
            if (contact.source == "users") {
                contact.label?.first()?.uppercase()
            } else {
                contact.source?.replaceFirstChar { actorType ->
                    actorType.uppercase()
                }
            }
            ).toString()
    }
    LazyColumn(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        contentPadding = PaddingValues(all = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        groupedContacts.forEach { (initial, contactsForInitial) ->
            stickyHeader {
                Column {
                    Surface(Modifier.fillParentMaxWidth()) {
                        Header(initial)
                    }
                    HorizontalDivider(thickness = 0.1.dp, color = Color.Black)
                }
            }
            items(contactsForInitial) { contact ->
                ContactItemRow(contact = contact, contactsViewModel = contactsViewModel, context = context)
            }
        }
    }
}

@Composable
fun Header(header: String) {
    Text(
        text = header,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
            .padding(start = 60.dp),
        color = Color.Blue,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun ContactItemRow(contact: AutocompleteUser, contactsViewModel: ContactsViewModel, context: Context) {
    val roomUiState by contactsViewModel.roomViewState.collectAsState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                contactsViewModel.createRoom(
                    CompanionClass.ROOM_TYPE_ONE_ONE,
                    contact.source!!,
                    contact.id!!,
                    null
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        val imageUri = contact.id?.let { contactsViewModel.getImageUri(it, true) }
        val imageRequest = ImageRequest.Builder(context)
            .data(imageUri)
            .transformations(CircleCropTransformation())
            .error(R.drawable.account_circle_96dp)
            .placeholder(R.drawable.account_circle_96dp)
            .build()

        AsyncImage(
            model = imageRequest,
            contentDescription = stringResource(R.string.user_avatar),
            modifier = Modifier.size(width = 45.dp, height = 45.dp)
        )
        Text(modifier = Modifier.padding(16.dp), text = contact.label!!)
    }
    when (roomUiState) {
        is RoomUiState.Success -> {
            val conversation = (roomUiState as RoomUiState.Success).conversation
            val bundle = Bundle()
            bundle.putString(BundleKeys.KEY_ROOM_TOKEN, conversation?.token)
            bundle.putString(BundleKeys.KEY_ROOM_ID, conversation?.roomId)
            val chatIntent = Intent(context, ChatActivity::class.java)
            chatIntent.putExtras(bundle)
            chatIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            context.startActivity(chatIntent)
        }
        is RoomUiState.Error -> {
            val errorMessage = (roomUiState as RoomUiState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: $errorMessage", color = Color.Red)
            }
        }
        is RoomUiState.None -> {}
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    context: Context,
    contactsViewModel: ContactsViewModel,
    searchState: MutableState<Boolean>
) {
    val searchQuery by contactsViewModel.searchQuery.collectAsState()
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = {
                (context as? Activity)?.finish()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back_button))
            }
        },
        actions = {
            IconButton(onClick = {
                searchState.value = true
            }) {
                Icon(Icons.Filled.Search, contentDescription = stringResource(R.string.search_icon))
            }
        }
    )
    if (searchState.value) {
        DisplaySearch(
            text = searchQuery,
            onTextChange = { searchQuery ->
                contactsViewModel.updateSearchQuery(query = searchQuery)
                contactsViewModel.getContactsFromSearchParams()
            },
            searchState = searchState
        )
    }
}

@Composable
fun ConversationCreationOptions(context: Context) {
    Column {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .padding(8.dp),
                painter = painterResource(R.drawable.baseline_chat_bubble_outline_24),
                contentDescription = stringResource(R.string.new_conversation_creation_icon)
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                text = stringResource(R.string.nc_create_new_conversation),
                maxLines = 1,
                fontSize = 16.sp
            )
        }
        Row(
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
                .clickable {
                    val intent = Intent(context, ListOpenConversationsActivity::class.java)
                    context.startActivity(intent)
                },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Image(
                modifier = Modifier
                    .width(40.dp)
                    .height(40.dp)
                    .padding(8.dp),
                painter = painterResource(R.drawable.baseline_format_list_bulleted_24),
                contentDescription = stringResource(R.string.new_conversation_creation_icon)
            )
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(),
                text = stringResource(R.string.join_open_conversations_icon),
                fontSize = 16.sp
            )
        }
    }
}

class CompanionClass {
    companion object {
        internal val TAG = ContactsActivityCompose::class.simpleName
        internal const val ROOM_TYPE_ONE_ONE = "1"
    }
}
