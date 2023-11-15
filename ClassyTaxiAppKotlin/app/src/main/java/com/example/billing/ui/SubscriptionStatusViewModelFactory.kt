package com.example.billing.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.billing.data.BillingRepository

class SubscriptionStatusViewModelFactory( private val repository: BillingRepository): ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SubscriptionStatusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SubscriptionStatusViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}