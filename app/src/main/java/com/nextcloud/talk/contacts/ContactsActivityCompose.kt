/*
 * Nextcloud Talk - Android Client
 *
 * SPDX-FileCopyrightText: 2024 Your Name <your@email.com>
 * SPDX-License-Identifier: GPL-3.0-or-later
 */

package com.nextcloud.talk.contacts

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun textMessage() {
    // Composable functions start with capital letter, lint shows error, need to suppress this error later
    Text(text = "This is a test Message")
}
