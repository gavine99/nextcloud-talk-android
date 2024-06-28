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
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
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
    private lateinit var contactsActivityViewModel: ContactsActivityViewModel

    @SuppressLint("UnrememberedMutableState")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NextcloudTalkApplication.sharedApplication!!.componentApplication.inject(this)
        contactsActivityViewModel = ViewModelProvider(this, viewModelFactory)[ContactsActivityViewModel::class.java]

        setContent {
            MaterialTheme {
                val context = LocalContext.current
                var searchState = mutableStateOf(false)
                Scaffold(
                    topBar = {
                        AppBar(
                            title = stringResource(R.string.nc_app_product_name),
                            context = context,
                            contactsViewModel = contactsActivityViewModel,
                            searchState = searchState
                        )
                    },
                    content = {
                        val uiState = contactsActivityViewModel.contactsViewState.collectAsState()
                        Column(Modifier.padding(it)) {
                            ConversationCreationOptions(context = context)
                            ContactsList(
                                contactsUiState = uiState.value,
                                contactsViewModel = contactsActivityViewModel,
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
fun ContactsList(contactsUiState: ContactsUiState, contactsViewModel: ContactsActivityViewModel, context: Context) {
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
            if (contacts != null) {
                ContactsItem(contacts, contactsViewModel, context)
            }
        }
        is ContactsUiState.Error -> {
        }
    }
}

@Composable
fun ContactsItem(contacts: List<AutocompleteUser>, contactsViewModel: ContactsActivityViewModel, context: Context) {
    LazyColumn(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize(),
        contentPadding = PaddingValues(all = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(items = contacts) { _, contact ->
            ContactItemRow(contact, contactsViewModel, context)
        }
    }
}

@Composable
fun ContactItemRow(contact: AutocompleteUser, contactsViewModel: ContactsActivityViewModel, context: Context) {
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
            }
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
            contentDescription = "Image",
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
        is RoomUiState.Error -> Text(text = "Error: ${(roomUiState as RoomUiState.Error).message}", color = Color.Red)
        RoomUiState.None -> {}
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppBar(
    title: String,
    context: Context,
    contactsViewModel: ContactsActivityViewModel,
    searchState: MutableState<Boolean>
) {
    val searchQuery by contactsViewModel.searchQuery.collectAsState()
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = {
                (context as? Activity)?.finish()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = {
                searchState.value = true
            }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        }
    )
    if (searchState.value) {
        DisplaySearch(
            text = searchQuery,
            // update query on text change
            onTextChange = { searchQuery ->
                contactsViewModel.updateSearchQuery(query = searchQuery)
                contactsViewModel.getContactsFromSearchParams()
            },
            onCloseClick = {
                (context as? Activity)?.finish()
            }
        )
    }
}

// @Composable
// fun ShowSearch(searchState: Boolean,contactsViewModel:ContactsActivityViewModel,context:Context){
//     val searchQuery by contactsViewModel.searchQuery.collectAsState()
//     if(searchState){
//         DisplaySearch(text = searchQuery,
//             // update query on text change
//             onTextChange = {searchQuery ->
//                 contactsViewModel.updateSearchQuery(query = searchQuery)
//             },
//             // on search click, update query and get searched questions title list
//             onSearchClick = { _ ->
//                 contactsViewModel.getContactsFromSearchParams()
//
//             },
//             onCloseClick = {
//                 (context as? Activity)?.finish()
//             })
//
//     }
// }

@Composable
fun ConversationCreationOptions(context: Context) {
    Column(modifier = Modifier.padding(10.dp)) {
        Row(modifier = Modifier.padding(10.dp)) {
            Image(
                painter = painterResource(R.drawable.baseline_chat_bubble_outline_24),
                contentDescription = "New Conversation Creation Icon"
            )
            Text(text = stringResource(R.string.nc_create_new_conversation))
        }
        Row(

            modifier = Modifier
                .padding(10.dp)
                .clickable {
                    val intent = Intent(context, ListOpenConversationsActivity::class.java)
                    context.startActivity(intent)
                }
        ) {
            Image(
                painter = painterResource(R.drawable.baseline_format_list_bulleted_24),
                contentDescription = "Join open conversations Icon"
            )
            Text(text = stringResource(R.string.nc_join_open_conversations))
        }
    }
}

@Composable
fun DisplaySearch(text: String, onTextChange: (String) -> Unit, onCloseClick: () -> Unit)  {
    val focusRequester = remember { FocusRequester() }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .background(Color.White)
    ) {
        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }
        val keyboardController = LocalSoftwareKeyboardController.current
        TextField(
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
                .onFocusChanged {
                },
            value = text,
            onValueChange = { onTextChange(it) },
            placeholder = {
                Text(
                    text = "Search",
                    color = Color.DarkGray
                )
            },

            textStyle = TextStyle(
                color = Color.Black
            ),
            singleLine = true,
            leadingIcon = {
                IconButton(
                    onClick = {
                        onTextChange("")
                        onCloseClick()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Default.ArrowBack,
                        contentDescription = "Search Icon",
                        tint = Color.Black
                    )
                }
            },

            trailingIcon = {
                IconButton(
                    onClick = {
                        if (text.isNotEmpty()) {
                            onTextChange("")
                        } else {
                            onCloseClick()
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close Icon",
                        tint = Color.Black
                    )
                }
            },

            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Search
            ),

            keyboardActions = KeyboardActions(
                onSearch = {
                    if (text.trim().isNotEmpty()) {
                        focusRequester.freeFocus()
                        keyboardController?.hide()
                    } else {
                        return@KeyboardActions
                    }
                }
            ),

            colors = TextFieldDefaults.colors(
                focusedTextColor = Color.Black,
                cursorColor = Color.Black
            )
        )
    }
}

class CompanionClass {
    companion object {
        private val TAG = ContactsActivity::class.simpleName
        const val ROOM_TYPE_ONE_ONE = "1"
    }
}
