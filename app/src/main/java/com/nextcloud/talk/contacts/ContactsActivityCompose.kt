/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nextcloud.talk.contacts

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import autodagger.AutoInjector
import com.nextcloud.talk.R
import com.nextcloud.talk.api.NcApi
import com.nextcloud.talk.application.NextcloudTalkApplication
import com.nextcloud.talk.users.UserManager
import javax.inject.Inject

@AutoInjector(NextcloudTalkApplication::class)
class ContactsActivityCompose : ComponentActivity() {

    @Inject
    lateinit var userManager: UserManager

    @Inject
    lateinit var ncApi: NcApi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NextcloudTalkApplication.sharedApplication!!.componentApplication.inject(this)
        setContent {
            MaterialTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(title = stringResource(R.string.nc_app_product_name), onBackClick = {
                            onBackPressedDispatcher.onBackPressed()
                            // not working currently with compose
                        })
                    },
                    content = {
                        Column(Modifier.padding(it)) {
                            ConversationCreationOptions()
                        }
                    }
                )
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
    Column(modifier = Modifier.padding(10.dp)) {
        Row(modifier = Modifier.padding(10.dp)) {
            Image(
                painter = painterResource(R.drawable.baseline_chat_bubble_outline_24),
                contentDescription = "New Conversation Creation Icon"
            )
            Text(text = stringResource(R.string.nc_create_new_conversation))
        }
        Row(modifier = Modifier.padding(10.dp)) {
            Image(
                painter = painterResource(R.drawable.baseline_format_list_bulleted_24),
                contentDescription = "Join open conversations Icon"
            )
            Text(text = stringResource(R.string.nc_join_open_conversations))
        }
    }
}
