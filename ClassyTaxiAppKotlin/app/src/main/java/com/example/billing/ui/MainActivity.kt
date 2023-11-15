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

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.Purchase
import com.example.billing.BillingApp
import com.example.billing.Constants
import com.example.billing.data.BillingRepository
import com.example.billing.gpbl.BillingClientLifecycle
import com.example.billing.ui.composable.home.ClassyTaxiApp
import com.firebase.ui.auth.AuthUI
import kotlinx.coroutines.launch


/**
 * MainActivity contains a UI that leverages Material Components to build an optimized
 * Android experience for Classy Taxi.
 *
 * Uses [FirebaseUserViewModel] to maintain authentication state.
 * The menu is updated when the user changes.
 * When sign-in or sign-out is completed, call the [FirebaseUserViewModel] to update the state.
 *
 */
class MainActivity : AppCompatActivity() {
    private lateinit var billingClientLifecycle: BillingClientLifecycle

    private lateinit var billingViewModel: BillingViewModel
    private lateinit var subscriptionViewModel: SubscriptionStatusViewModel
    private lateinit var oneTimePurchaseViewModel: OneTimeProductPurchaseStatusViewModel



    private val authenticationViewModel: FirebaseUserViewModel by viewModels()
    private lateinit var registerResult: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val billingRepository = (application as BillingApp).repository



        oneTimePurchaseViewModel = ViewModelProvider(
            this,
            OneTimeProductPurchaseStatusViewModelFactory(billingRepository)
        )[OneTimeProductPurchaseStatusViewModel::class.java]

        subscriptionViewModel = ViewModelProvider(
            this,
            SubscriptionStatusViewModelFactory(billingRepository)
        )[SubscriptionStatusViewModel::class.java]

        registerResult = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            if (it.resultCode == RESULT_OK) {
                Log.d(TAG, "Sign-in SUCCESS!")
                authenticationViewModel.updateFirebaseUser()
            } else {
                Log.d(TAG, "Sign-in FAILED!")
            }
        }

        // Billing APIs are all handled in the this lifecycle observer.
        billingClientLifecycle = (application as BillingApp).billingClientLifecycle
        lifecycle.addObserver(billingClientLifecycle)

        billingViewModel = ViewModelProvider(
            this,
            BillingViewModelFactory(billingRepository, billingClientLifecycle)
        )[BillingViewModel::class.java]

        // Launch the billing flow when the user clicks a button to buy something.
        billingViewModel.buyEvent.observe(this) {
            if (it != null) {
                billingClientLifecycle.launchBillingFlow(this, it)
            }
        }

        // Open the Play Store when this event is triggered.
        billingViewModel.openPlayStoreSubscriptionsEvent.observe(this) { product ->
            Log.i(TAG, "Viewing subscriptions on the Google Play Store")
            val url = if (product == null) {
                // If the Product is not specified, just open the Google Play subscriptions URL.
                Constants.PLAY_STORE_SUBSCRIPTION_URL
            } else {
                // If the Product is specified, open the deeplink for this Product on Google Play.
                String.format(Constants.PLAY_STORE_SUBSCRIPTION_DEEPLINK_URL, product, packageName)
            }
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        // Update authentication UI.
        authenticationViewModel.firebaseUser.observe(this) {
            invalidateOptionsMenu()
            if (it == null) {
                triggerSignIn()
            } else {
                Log.d(TAG, "CURRENT user: ${it.email} ${it.displayName}")
            }
        }

        // Update purchases information when user changes.
        authenticationViewModel.userChangeEvent.observe(this) {
            Log.d(TAG, "user changed for some reason")
            subscriptionViewModel.userChanged( repository = billingRepository)
            lifecycleScope.launch {
                registerPurchases(billingClientLifecycle.subscriptionPurchases.value, repository = billingRepository)
                registerPurchases(billingClientLifecycle.oneTimeProductPurchases.value, repository = billingRepository)
            }
        }

        setContent {
            ClassyTaxiApp(
                billingViewModel = billingViewModel,
                subscriptionViewModel = subscriptionViewModel,
                authenticationViewModel = authenticationViewModel,
                oneTimeProductViewModel = oneTimePurchaseViewModel,
                repository = billingRepository,
            )
        }

    }


    /**
     * Register Product purchases with the server.
     */
    private suspend fun registerPurchases(purchaseList: List<Purchase>, repository: BillingRepository) {
        billingViewModel.registerPurchases(purchaseList, repository = repository)
    }

    /**
     * Sign in with FirebaseUI Auth.
     */
    private fun triggerSignIn() {
        Log.d(TAG, "Attempting SIGN-IN!")
        val providers = listOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build()
        )
        registerResult.launch(
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
        )
    }


    companion object {
        private const val TAG = "MainActivity"
    }
}
