/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nextcloud.talk.contacts

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider
import autodagger.AutoInjector
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation
import com.nextcloud.talk.R
import com.nextcloud.talk.application.NextcloudTalkApplication
import com.nextcloud.talk.models.json.autocomplete.AutocompleteUser
import com.nextcloud.talk.openconversations.ListOpenConversationsActivity
import javax.inject.Inject

@AutoInjector(NextcloudTalkApplication::class)
class ContactsActivityCompose : ComponentActivity() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var contactsActivityViewModel: ContactsActivityViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NextcloudTalkApplication.sharedApplication!!.componentApplication.inject(this)
        contactsActivityViewModel = ViewModelProvider(this, viewModelFactory)[ContactsActivityViewModel::class.java]

        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = stringResource(R.string.nc_app_product_name), onBackClick = {
                        })
                    },
                    content = {
                        val uiState = contactsActivityViewModel.contactsViewState.collectAsState()
                        Column(Modifier.padding(it)) {
                            ConversationCreationOptions()
                            ContactsList(
                                contactsUiState = uiState.value,
                                contactsViewModel = contactsActivityViewModel
                            )
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ContactsList(contactsUiState: ContactsUiState, contactsViewModel: ContactsActivityViewModel) {
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
                ContactsItem(contacts, contactsViewModel)
            }
        }

        is ContactsUiState.Error -> {
        }
    }
}

@Composable
fun ContactsItem(contacts: List<AutocompleteUser>, contactsViewModel: ContactsActivityViewModel) {
    LazyColumn(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxSize()
            .clickable {
            },
        contentPadding = PaddingValues(all = 10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        itemsIndexed(items = contacts) { _, contact ->
            Row {
                val imageUri = contact.id?.let { contactsViewModel.getImageUri(it, true) }
                val imageRequest = ImageRequest.Builder(LocalContext.current)
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
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = {
                onBackClick
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
        },
        actions = {
            IconButton(onClick = { /* Handle search action */ }) {
                Icon(Icons.Filled.Search, contentDescription = "Search")
            }
        }
    )
}

@Composable
fun ConversationCreationOptions() {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(10.dp)) {
        Row(modifier = Modifier.padding(10.dp)) {
            Image(
                painter = painterResource(R.drawable.baseline_chat_bubble_outline_24),
                contentDescription = "New Conversation Creation Icon"
            )
            Text(text = stringResource(R.string.nc_create_new_conversation))
        }
        Row(

            modifier = Modifier.padding(10.dp).clickable {
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
