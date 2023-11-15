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


package com.example.billing.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.billing.data.BillingRepository
import com.example.billing.data.ContentResource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface OneTimeProductUiState {
    data class Error(
        val message: String
    ) : OneTimeProductUiState

    data class Success(
        val currentOneTimeProductPurchase:
        OneTimeProductPurchaseStatusViewModel.CurrentOneTimeProductPurchase,
        val content: ContentResource?,
        val message: String
    ) : OneTimeProductUiState
}

class OneTimeProductPurchaseStatusViewModel(repository: BillingRepository) : ViewModel() {

    private val userCurrentOneTimeProduct = repository.hasOneTimeProduct

    private val currentOneTimeProductPurchase: Flow<CurrentOneTimeProductPurchase?> =
        userCurrentOneTimeProduct.map { hasCurrentOneTimeProduct ->
            if (hasCurrentOneTimeProduct) {
                CurrentOneTimeProductPurchase.OTP
            } else {
                CurrentOneTimeProductPurchase.NONE
            }
        }

    val uiState: StateFlow<OneTimeProductUiState?> = combine(
        currentOneTimeProductPurchase,
        repository.otpContent
    ) { currentPurchase, content ->
        if (currentPurchase == null) {
            OneTimeProductUiState.Error("No one-time product purchase found.")
        } else {
            OneTimeProductUiState.Success(
                currentOneTimeProductPurchase = currentPurchase,
                content = content,
                message = "Success"
            )
        }
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(stopTimeoutMillis = 5000),
        null
    )

    // TODO show status in UI
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    private val onRefresh: MutableSharedFlow<Unit> = MutableSharedFlow()

    private val foobar: Flow<Result<Unit>> = onRefresh.map {
        repository.fetchOneTimeProductPurchases()
    }

    /**
     * Refresh the status of one-time product purchases.
     */
    fun manualRefresh() {
        viewModelScope.launch {
            onRefresh.emit(Unit)
        }
    }

    enum class CurrentOneTimeProductPurchase {
        OTP,
        NONE,
        PENDING;
    }

    companion object {
        private const val TAG = "OneTimeProductPurchaseViewModel"
    }
}