/*
 * Copyright 2018 Google LLC. All rights reserved.
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

package com.example.billing.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billing.BillingApp
import com.example.billing.data.BillingRepository
import com.example.billing.data.ContentResource
import com.example.billing.data.subscriptions.SubscriptionStatus
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface SubscriptionUIState {
    data object Loading : SubscriptionUIState

    data class Error(
        val message: String
    ) : SubscriptionUIState

    data class Success(
        val content: ContentResource?,
        val currentSubscription: SubscriptionStatusViewModel.CurrentSubscription,
    ) : SubscriptionUIState
}

class SubscriptionStatusViewModel(
    repository: BillingRepository,
) : ViewModel() {

    // TODO this should be moved to constructor param and injected by Hilt
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()


    val state: StateFlow<SubscriptionUIState> = customCombine(
        repository.hasPrepaidBasic,
        repository.hasPrepaidPremium,
        repository.hasRenewableBasic,
        repository.hasRenewablePremium,
        repository.basicContent,
        repository.premiumContent,
    ) { hasPrepaidBasic, hasPrepaidPremium, hasRenewableBasic, hasRenewablePremium, basicContent,
        premiumContent ->
        var current: CurrentSubscription = CurrentSubscription.NONE
        var content: ContentResource? = null

        when {
            hasPrepaidBasic -> {
                current = CurrentSubscription.BASIC_PREPAID
                content = basicContent
            }

            hasRenewableBasic -> {
                current = CurrentSubscription.BASIC_RENEWABLE
                content = basicContent
            }

            hasPrepaidPremium -> {
                current = CurrentSubscription.PREMIUM_PREPAID
                content = premiumContent
            }

            hasRenewablePremium -> {
                current = CurrentSubscription.PREMIUM_RENEWABLE
                content = premiumContent
            }

            else -> {
                Log.d(TAG, "No sub in else")
//                current = CurrentSubscription.NONE
//                content = null
            }
        }

        if (content== null && current != CurrentSubscription.NONE) {
            SubscriptionUIState.Loading
        } else {
            SubscriptionUIState.Success(content, current)
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, SubscriptionUIState.Loading)


    fun unregisterInstanceId(repository: BillingRepository) {
        // Unregister current Instance ID before the user signs out.
        // This is an authenticated call, so you cannot do this after the sign-out has completed.
        instanceIdToken?.let {
            repository.unregisterInstanceId(it)
        }
    }

    fun userChanged(repository: BillingRepository) {
        viewModelScope.launch {
            repository.deleteLocalUserData()
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@OnCompleteListener
                }
                val token = task.result
                if (token != null) {
                    registerInstanceId(token, repository = repository)
                }
            })
        }
    }

    fun manualRefresh(repository: BillingRepository) {
        viewModelScope.launch {
            repository.queryProducts()
        }

        viewModelScope.launch {
            val result = repository.fetchSubscriptions()
            if (result.isFailure) {
                _errorMessage.emit(result.exceptionOrNull()?.localizedMessage)
            }
        }
    }

    /**
     * Keep track of the last Instance ID to be registered, so that it
     * can be unregistered when the user signs out.
     */
    private var instanceIdToken: String? = null

    /**
     * Register Instance ID.
     */
    private fun registerInstanceId(token: String, repository: BillingRepository) {
        repository.registerInstanceId(token)
        // Keep track of the Instance ID so that it can be unregistered.
        instanceIdToken = token
    }

    /**
     * Register a new subscription.
     */
    fun registerSubscription(
        product: String,
        purchaseToken: String,
        repository: BillingRepository
    ) {
        viewModelScope.launch {
            val result = repository.registerSubscription(product, purchaseToken)
            if (result.isFailure) {
                _errorMessage.emit(result.exceptionOrNull()?.localizedMessage)
            }
        }
    }

    /**
     * Transfer the subscription to this account.
     */
    fun transferSubscriptions() {
//        Log.d(TAG, "transferSubscriptions")
//        viewModelScope.launch {
//            subscriptions.value.forEach { subscription ->
//                val product = subscription.product
//                val purchaseToken = subscription.purchaseToken
//                if (product != null && purchaseToken != null) {
//                    repository.transferSubscription(
//                        product = product, purchaseToken = purchaseToken
//                    )
//                }
//            }
//        }
    }

    enum class CurrentSubscription {
        BASIC_PREPAID,
        BASIC_RENEWABLE,
        PREMIUM_PREPAID,
        PREMIUM_RENEWABLE,
        NONE;
    }

    private fun <T1, T2, T3, T4, T5, T6, R> customCombine(
        flow: Flow<T1>,
        flow2: Flow<T2>,
        flow3: Flow<T3>,
        flow4: Flow<T4>,
        flow5: Flow<T5>,
        flow6: Flow<T6>,
        transform: suspend (T1, T2, T3, T4, T5, T6) -> R,
    ): Flow<R> = combine(
        combine(flow, flow2, flow3, ::Triple),
        combine(flow4, flow5, flow6, ::Triple),
    ) { t1, t2 ->
        transform(
            t1.first,
            t1.second,
            t1.third,
            t2.first,
            t2.second,
            t2.third,
        )
    }
    companion object {
        private const val TAG = "SubViewModel"
    }
}
