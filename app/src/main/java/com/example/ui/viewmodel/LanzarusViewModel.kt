package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.database.LanzarusDatabase
import com.example.data.model.*
import com.example.data.repository.LanzarusRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class LanzarusViewModel(
    application: Application,
    private val repository: LanzarusRepository
) : AndroidViewModel(application) {

    val userState: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactionsState: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contentsState: StateFlow<List<ContentEntity>> = repository.allContents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeInvestmentsState: StateFlow<List<InvestmentOrderEntity>> = repository.activeInvestments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessagesState: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Full investment list (needed by MainScreen)
    val allInvestmentsState: StateFlow<List<InvestmentOrderEntity>> = repository.allInvestments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Stubs for UI compatibility - always return safe defaults
    val isBotActive = MutableStateFlow(false).asStateFlow()
    val botRiskLevel = MutableStateFlow("MODERADO").asStateFlow()
    val isChatLoading = MutableStateFlow(false).asStateFlow()
    val systemLogs = MutableStateFlow<List<String>>(emptyList()).asStateFlow()
    val geminiRecommendation = MutableStateFlow<String?>(null).asStateFlow()
    val isRecommendationLoading = MutableStateFlow(false).asStateFlow()
    val liveMarketPrices = MutableStateFlow<Map<String, Double>>(mapOf(
        "BTC/USDT" to 65000.0, "ETH/USDT" to 3400.0
    )).asStateFlow()

    private val _notification = MutableStateFlow<String?>(null)
    val notification: StateFlow<String?> = _notification.asStateFlow()

    fun clearNotification() { _notification.value = null }

    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUser = repository.getUserSync()
                if (currentUser == null) {
                    initializeDefaultData()
                }
            } catch (_: Exception) { }
        }
    }

    private suspend fun initializeDefaultData() {
        val defaultUser = UserEntity(
            id = 1,
            name = "R3DMOON",
            email = "joanlazaro83@gmail.com",
            balance = 0.0,
            isPremium = false,
            createdAt = System.currentTimeMillis()
        )
        repository.insertOrUpdateUser(defaultUser)
    }

    fun addBalance(amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserSync() ?: return@launch
            user.balance += amount
            repository.updateUser(user)
        }
    }

    fun showNotification(message: String) {
        _notification.value = message
    }
}

class LanzarusViewModelFactory(
    private val application: Application,
    private val repository: LanzarusRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LanzarusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LanzarusViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}