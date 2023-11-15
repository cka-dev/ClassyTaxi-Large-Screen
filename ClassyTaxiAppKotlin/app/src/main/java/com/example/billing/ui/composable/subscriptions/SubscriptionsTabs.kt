/*
 * Copyright 2023 Google LLC. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package com.example.billing.ui.composable.subscriptions

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.billing.R
import com.example.billing.ui.BillingViewModel
import com.example.billing.ui.SubscriptionStatusViewModel
import com.example.billing.ui.SubscriptionUIState
import com.example.billing.ui.composable.LoadingScreen
import com.example.billing.ui.composable.SelectedSubscriptionTab

@Composable
fun SubscriptionRoute(
    billingViewModel: BillingViewModel,
    subscriptionStatusViewModel: SubscriptionStatusViewModel,
    modifier: Modifier = Modifier,
) {
    val subUIState by subscriptionStatusViewModel.state.collectAsStateWithLifecycle()
    SubscriptionsScreen(
        subUIState,
        onBuyBasePlans = billingViewModel::buyBasePlans,
        onOpenSubscriptions = billingViewModel::openPlayStoreSubscriptions,
    )
}

@Composable
fun SubscriptionsScreen(
    state: SubscriptionUIState,
    onBuyBasePlans: BuyBasePlansListener,
    onOpenSubscriptions: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (state) {
        is SubscriptionUIState.Success -> SuccessScreen(
            state, onBuyBasePlans = onBuyBasePlans,
            onOpenSubscriptions = onOpenSubscriptions
        )

        is SubscriptionUIState.Error -> ErrorScreen()
        is SubscriptionUIState.Loading -> LoadingScreen()
    }
}

@Composable
fun SuccessScreen(
    state: SubscriptionUIState.Success,
    onBuyBasePlans: BuyBasePlansListener,
    onOpenSubscriptions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (selectedTab, setSelectedTab) = rememberSaveable {
        mutableIntStateOf(
            SelectedSubscriptionTab.BASIC.index
        )
    }

    Surface(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Color.White
            ) {
                Tab(
                    selected = selectedTab == SelectedSubscriptionTab.BASIC.index,
                    onClick = { setSelectedTab(SelectedSubscriptionTab.BASIC.index) },
                    text = {
                        Text(
                            text = stringResource(id = R.string.tab_text_home),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                )
                Tab(
                    selected = selectedTab == SelectedSubscriptionTab.PREMIUM.index,
                    onClick = { setSelectedTab(SelectedSubscriptionTab.PREMIUM.index) },
                    text = {
                        Text(
                            text = stringResource(id = R.string.tab_text_premium),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                )
                Tab(
                    selected = selectedTab == SelectedSubscriptionTab.SETTINGS.index,
                    onClick = { setSelectedTab(SelectedSubscriptionTab.SETTINGS.index) },
                    text = {
                        Text(
                            text = stringResource(id = R.string.tab_text_settings),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                )
            }

            when (selectedTab) {
                SelectedSubscriptionTab.BASIC.index -> {
                    BasicTabScreens(
                        onBuyBasePlans = onBuyBasePlans,
                        currentSubscription = state.currentSubscription,
                        contentResource = state.content,
                    )
                }

                SelectedSubscriptionTab.PREMIUM.index -> {
                    PremiumTabScreens(
                        onBuyBasePlans = onBuyBasePlans,
                        currentSubscription = state.currentSubscription,
                        contentResource = state.content,
                    )
                }

                SelectedSubscriptionTab.SETTINGS.index -> SubscriptionSettingsScreen(
                    onOpenSubscriptions = onOpenSubscriptions
                )
            }
        }
    }
}

@Composable
fun ErrorScreen(
    modifier: Modifier = Modifier
) {
    // Show an error message, or handle the error state appropriately
    Log.wtf("SubscriptionScreen", "SubscriptionUIState.Error")
}