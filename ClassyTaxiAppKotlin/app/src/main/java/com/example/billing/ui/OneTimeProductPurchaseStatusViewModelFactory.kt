package com.example.billing.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.billing.data.BillingRepository

class OneTimeProductPurchaseStatusViewModelFactory(
    private val repository: BillingRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OneTimeProductPurchaseStatusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OneTimeProductPurchaseStatusViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}