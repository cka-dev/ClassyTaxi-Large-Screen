package com.example.billing.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.billing.data.BillingRepository
import com.example.billing.gpbl.BillingClientLifecycle

class BillingViewModelFactory(
    private val repository: BillingRepository,
    private val billingClientLifecycle: BillingClientLifecycle
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return BillingViewModel(repository, billingClientLifecycle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}