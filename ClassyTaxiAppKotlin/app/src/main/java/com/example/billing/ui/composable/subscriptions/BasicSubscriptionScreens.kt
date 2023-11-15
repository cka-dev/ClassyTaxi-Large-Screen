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

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.billing.Constants
import com.example.billing.R
import com.example.billing.data.ContentResource
import com.example.billing.ui.SubscriptionStatusViewModel
import com.example.billing.ui.composable.ClassyTaxiImage
import com.example.billing.ui.composable.ClassyTaxiScreenHeader
import com.example.billing.ui.composable.LoadingScreen
import com.example.billing.ui.composable.SelectedSubscriptionBasePlan
import com.example.billing.ui.composable.resetSelectedButton

typealias BuyBasePlansListener = (product: String, tag: String, upDowngrade: Boolean) -> Unit

@Composable
fun BasicTabScreens(
    currentSubscription: SubscriptionStatusViewModel.CurrentSubscription,
    contentResource: ContentResource?,
    onBuyBasePlans: BuyBasePlansListener,
    modifier: Modifier = Modifier,
) {
    val selectedBasicConversionButton =
        remember { mutableIntStateOf(SelectedSubscriptionBasePlan.NONE.index) }

    val selectedEntitlementButtonMonthly =
        remember { mutableIntStateOf(SelectedSubscriptionBasePlan.NONE.index) }

    val selectedEntitlementButtonPrepaid =
        remember { mutableIntStateOf(SelectedSubscriptionBasePlan.NONE.index) }

    when (currentSubscription) {
        SubscriptionStatusViewModel.CurrentSubscription.BASIC_PREPAID -> {
            if (contentResource != null) {
                BasicEntitlementScreen(
                    contentResource = contentResource,
                    buttons = {
                        BasicPrepaidEntitlementButtons(
                            selectedEntitlementButtonPrepaid = selectedEntitlementButtonPrepaid
                        )
                    },
                    message = R.string.current_prepaid_basic_subscription_message,
                    currentBasicPlan = Constants.BASIC_PREPAID_PLAN_TAG,
                    selectedButton = selectedEntitlementButtonPrepaid,
                    onBuyBasePlans = onBuyBasePlans
                )
            } else {
                LoadingScreen()
            }
        }

        SubscriptionStatusViewModel.CurrentSubscription.BASIC_RENEWABLE -> {
            if (contentResource != null) {
                BasicEntitlementScreen(
                    contentResource = contentResource,
                    buttons = {
                        BasicMonthlyEntitlementButtons(
                            selectedEntitlementButtonMonthly = selectedEntitlementButtonMonthly,
                        )
                    },
                    message = R.string.current_recurring_basic_subscription_message,
                    currentBasicPlan = Constants.BASIC_MONTHLY_PLAN_TAG,
                    selectedButton = selectedEntitlementButtonMonthly,
                    onBuyBasePlans = onBuyBasePlans
                )
            } else {
                LoadingScreen()
            }
        }

        SubscriptionStatusViewModel.CurrentSubscription.PREMIUM_RENEWABLE -> {
            BasicConversionScreen(
                onBuyBasePlans = onBuyBasePlans,
                buttons = {
                    BasicDowngradeButtons(
                        selectedBasicConversionButton = selectedBasicConversionButton
                    )
                },
                selectedButton = selectedBasicConversionButton,
                message = R.string.current_recurring_premium_subscription_message,
            )
        }

        SubscriptionStatusViewModel.CurrentSubscription.PREMIUM_PREPAID -> {
            BasicConversionScreen(
                onBuyBasePlans = onBuyBasePlans,
                buttons = {
                    BasicDowngradeButtons(
                        selectedBasicConversionButton = selectedBasicConversionButton
                    )
                },
                selectedButton = selectedBasicConversionButton,
                message = R.string.current_prepaid_premium_subscription_message,
            )
        }

        SubscriptionStatusViewModel.CurrentSubscription.NONE -> {
            BasicBasePlansScreen(onBuyBasePlans = onBuyBasePlans)
        }
    }
}

@Composable
fun BasicBasePlansScreen(
    onBuyBasePlans: BuyBasePlansListener,
    modifier: Modifier = Modifier,
) {
    val selectedBasicSubscriptionButton =
        remember { mutableStateOf(SelectedSubscriptionBasePlan.NONE.index) }

    Surface(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ClassyTaxiScreenHeader(
                content = {
                    BasePlanScreenButtons(
                        selectedBasicSubscriptionButton = selectedBasicSubscriptionButton,
                    )
                },
                textResource = R.string.basic_paywall_message
            )
        }
    }

    LaunchedEffect(selectedBasicSubscriptionButton.value) {
        when (selectedBasicSubscriptionButton.value) {
            SelectedSubscriptionBasePlan.MONTHLY.index -> {
                onBuyBasePlans(
                    Constants.BASIC_MONTHLY_PLAN_TAG,
                    Constants.BASIC_PRODUCT,
                    false
                )
                resetSelectedButton(
                    selectedIntButton = selectedBasicSubscriptionButton,
                    selectedBooleanButton = null
                )
            }

            SelectedSubscriptionBasePlan.YEARLY.index -> {
                onBuyBasePlans(
                    Constants.BASIC_YEARLY_PLAN_TAG,
                    Constants.BASIC_PRODUCT,
                    false
                )
                resetSelectedButton(
                    selectedIntButton = selectedBasicSubscriptionButton,
                    selectedBooleanButton = null
                )
            }

            SelectedSubscriptionBasePlan.PREPAID.index -> {
                onBuyBasePlans(
                    Constants.BASIC_PREPAID_PLAN_TAG,
                    Constants.BASIC_PRODUCT,
                    false
                )
                resetSelectedButton(
                    selectedIntButton = selectedBasicSubscriptionButton,
                    selectedBooleanButton = null
                )
            }
        }
    }
}

@Composable
fun BasicEntitlementScreen(
    contentResource: ContentResource,
    @StringRes message: Int,
    currentBasicPlan: String,
    selectedButton: MutableState<Int>,
    onBuyBasePlans: BuyBasePlansListener,
    buttons: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        ClassyTaxiImage(
            contentDescription = "Basic Entitlement",
            contentResource = contentResource
        )

        Surface {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ClassyTaxiScreenHeader(content = buttons, textResource = message)
            }
        }
    }

    LaunchedEffect(key1 = currentBasicPlan, key2 = selectedButton.value) {
        when (currentBasicPlan) {
            in listOf(Constants.BASIC_MONTHLY_PLAN_TAG, Constants.BASIC_YEARLY_PLAN_TAG) -> {
                if (selectedButton.value == SelectedSubscriptionBasePlan.PREPAID.index) {
                    onBuyBasePlans(
                        Constants.BASIC_PREPAID_PLAN_TAG,
                        Constants.BASIC_PRODUCT,
                        true
                    )
                    resetSelectedButton(
                        selectedIntButton = selectedButton,
                        selectedBooleanButton = null
                    )
                }
            }

            Constants.BASIC_PREPAID_PLAN_TAG -> {
                when (selectedButton.value) {
                    SelectedSubscriptionBasePlan.MONTHLY.index -> {
                        onBuyBasePlans(
                            Constants.BASIC_MONTHLY_PLAN_TAG,
                            Constants.BASIC_PRODUCT,
                            true
                        )
                        resetSelectedButton(
                            selectedIntButton = selectedButton,
                            selectedBooleanButton = null
                        )
                    }

                    SelectedSubscriptionBasePlan.YEARLY.index -> {
                        onBuyBasePlans(
                            Constants.BASIC_YEARLY_PLAN_TAG,
                            Constants.BASIC_PRODUCT,
                            false
                        )
                        resetSelectedButton(
                            selectedIntButton = selectedButton,
                            selectedBooleanButton = null
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BasicConversionScreen(
    onBuyBasePlans: BuyBasePlansListener,
    selectedButton: MutableState<Int>,
    @StringRes message: Int,
    buttons: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ClassyTaxiScreenHeader(content = buttons, textResource = message)
        }
    }

    LaunchedEffect(selectedButton.value) {
        when (selectedButton.value) {
            SelectedSubscriptionBasePlan.MONTHLY.index -> {
                onBuyBasePlans(
                    Constants.BASIC_MONTHLY_PLAN_TAG,
                    Constants.BASIC_PRODUCT,
                    true
                )
                resetSelectedButton(
                    selectedIntButton = selectedButton,
                    selectedBooleanButton = null
                )
            }

            SelectedSubscriptionBasePlan.YEARLY.index -> {
                onBuyBasePlans(
                    Constants.BASIC_YEARLY_PLAN_TAG,
                    Constants.BASIC_PRODUCT,
                    true
                )
                resetSelectedButton(
                    selectedIntButton = selectedButton,
                    selectedBooleanButton = null
                )
            }

            SelectedSubscriptionBasePlan.PREPAID.index -> {
                onBuyBasePlans(
                    Constants.BASIC_PREPAID_PLAN_TAG,
                    Constants.BASIC_PRODUCT,
                    true
                )
                resetSelectedButton(
                    selectedIntButton = selectedButton,
                    selectedBooleanButton = null
                )
            }
        }
    }
}

@Composable
private fun BasePlanScreenButtons(
    selectedBasicSubscriptionButton: MutableState<Int>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.menu_drawer_body_button_padding)),
            onClick = {
                selectedBasicSubscriptionButton.value = SelectedSubscriptionBasePlan.MONTHLY.index
            }) {
            Text(text = stringResource(id = R.string.monthly_basic_plan_text))
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.menu_drawer_body_button_padding)),
            onClick = {
                selectedBasicSubscriptionButton.value = SelectedSubscriptionBasePlan.YEARLY.index
            }) {
            Text(text = stringResource(id = R.string.yearly_basic_plan_text))
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.menu_drawer_body_button_padding)),
            onClick = {
                selectedBasicSubscriptionButton.value = SelectedSubscriptionBasePlan.PREPAID.index
            }) {
            Text(text = stringResource(id = R.string.prepaid_basic_plan_text))
        }
    }
}

@Composable
private fun BasicDowngradeButtons(
    selectedBasicConversionButton: MutableState<Int>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.menu_drawer_body_button_padding)),
            onClick = {
                selectedBasicConversionButton.value = SelectedSubscriptionBasePlan.MONTHLY.index
            }) {
            Text(text = stringResource(id = R.string.downgrade_to_basic_monthly))
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.menu_drawer_body_button_padding)),
            onClick = {
                selectedBasicConversionButton.value = SelectedSubscriptionBasePlan.YEARLY.index
            }) {
            Text(text = stringResource(id = R.string.downgrade_to_basic_yearly))
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.menu_drawer_body_button_padding)),
            onClick = {
                selectedBasicConversionButton.value = SelectedSubscriptionBasePlan.PREPAID.index
            }) {
            Text(text = stringResource(id = R.string.downgrade_to_basic_prepaid))
        }
    }
}

@Composable
private fun BasicMonthlyEntitlementButtons(
    selectedEntitlementButtonMonthly: MutableState<Int>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.menu_drawer_body_button_padding)),
            onClick = {
                selectedEntitlementButtonMonthly.value = SelectedSubscriptionBasePlan.PREPAID.index
            }) {
            Text(text = stringResource(id = R.string.convert_to_basic_prepaid))
        }
    }
}

@Composable
private fun BasicPrepaidEntitlementButtons(
    selectedEntitlementButtonPrepaid: MutableState<Int>,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier) {
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.menu_drawer_body_button_padding)),
            onClick = {
                selectedEntitlementButtonPrepaid.value = SelectedSubscriptionBasePlan.MONTHLY.index
            }) {
            Text(text = stringResource(id = R.string.convert_to_basic_monthly))
        }
        Button(
            modifier = Modifier
                .fillMaxWidth()
                .padding(dimensionResource(id = R.dimen.menu_drawer_body_button_padding)),
            onClick = {
                selectedEntitlementButtonPrepaid.value = SelectedSubscriptionBasePlan.YEARLY.index
            }) {
            Text(text = stringResource(id = R.string.convert_to_basic_yearly))
        }
    }
}