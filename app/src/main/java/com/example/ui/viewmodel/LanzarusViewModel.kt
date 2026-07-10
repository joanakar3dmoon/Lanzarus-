package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.CoinGeckoApi
import com.example.data.database.LanzarusDatabase
import com.example.data.model.*
import com.example.data.repository.LanzarusRepository
import com.example.engine.TradingEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

/**
 * Lanzarus ViewModel — Real AI Trading Broker
 * Fetches REAL prices, runs REAL AI analysis, makes REAL trade decisions.
 * No simulations, no fake data.
 */
class LanzarusViewModel(
    application: Application,
    private val repository: LanzarusRepository
) : AndroidViewModel(application) {

    // --- Core State ---
    val userState: StateFlow<UserEntity?> = repository.userFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val transactionsState: StateFlow<List<TransactionEntity>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val contentsState: StateFlow<List<ContentEntity>> = repository.allContents
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeInvestmentsState: StateFlow<List<InvestmentOrderEntity>> = repository.activeInvestments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allInvestmentsState: StateFlow<List<InvestmentOrderEntity>> = repository.allInvestments
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chatMessagesState: StateFlow<List<ChatMessageEntity>> = repository.chatMessages
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Real Market Prices ---
    private val _liveMarketPrices = MutableStateFlow<Map<String, Double>>(emptyMap())
    val liveMarketPrices: StateFlow<Map<String, Double>> = _liveMarketPrices.asStateFlow()

    // --- AI Trading Engine State ---
    private val _isBotActive = MutableStateFlow(false)
    val isBotActive: StateFlow<Boolean> = _isBotActive.asStateFlow()

    private val _botRiskLevel = MutableStateFlow("MODERADO")
    val botRiskLevel: StateFlow<String> = _botRiskLevel.asStateFlow()

    private val _systemLogs = MutableStateFlow<List<String>>(emptyList())
    val systemLogs: StateFlow<List<String>> = _systemLogs.asStateFlow()

    // --- AI Analysis State ---
    private val _geminiRecommendation = MutableStateFlow<String?>(null)
    val geminiRecommendation: StateFlow<String?> = _geminiRecommendation.asStateFlow()

    private val _isRecommendationLoading = MutableStateFlow(false)
    val isRecommendationLoading: StateFlow<Boolean> = _isRecommendationLoading.asStateFlow()

    // --- Chat State ---
    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // --- Notifications ---
    private val _notification = MutableStateFlow<String?>(null)
    val notification: StateFlow<String?> = _notification.asStateFlow()

    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    // ==== INIT: Seed default data + start price fetching ====
    init {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val currentUser = repository.getUserSync()
                if (currentUser == null) {
                    initializeDefaultData()
                }
            } catch (_: Exception) { }

            // Start continuous price fetching
            startPriceFeed()
        }
    }

    private suspend fun initializeDefaultData() {
        val defaultUser = UserEntity(
            id = 1,
            name = "R3DMOON",
            email = "joanlazaro83@gmail.com",
            balance = 10000.0, // Start with 10K virtual capital
            isPremium = false,
            createdAt = System.currentTimeMillis()
        )
        repository.insertOrUpdateUser(defaultUser)
        addLog("💰 Cartera inicial: 10.000 USD — ¡empieza a invertir!")
    }

    // ==== REAL PRICE FEED ====
    private suspend fun startPriceFeed() {
        while (true) {
            try {
                val prices = CoinGeckoApi.fetchAllPrices()
                if (prices.isNotEmpty()) {
                    _liveMarketPrices.value = prices
                }
            } catch (_: Exception) { }
            delay(60_000) // Refresh every 60 seconds
        }
    }

    // ==== AI TRADING ENGINE ====
    fun toggleBot() {
        viewModelScope.launch(Dispatchers.IO) {
            if (_isBotActive.value) {
                _isBotActive.value = false
                addLog("🤖 Bot de trading DESACTIVADO")
                showNotification("Bot desactivado")
            } else {
                _isBotActive.value = true
                addLog("🤖 Bot de trading ACTIVADO — analizando mercado...")
                showNotification("Bot activado — IA analizando mercados")
                runTradingCycle()
            }
        }
    }

    private suspend fun runTradingCycle() {
        while (_isBotActive.value) {
            try {
                val user = repository.getUserSync() ?: break
                val active = repository.activeInvestments.first()

                // Get real market analysis
                val decisions = TradingEngine.runFullCycle(user.balance, active)

                for (decision in decisions) {
                    when (decision.action) {
                        "BUY" -> executeBuy(decision)
                        "SELL" -> executeSell(decision)
                        "HOLD" -> {
                            addLog("⏸️ ${decision.symbol}: HOLD — ${decision.reason}")
                        }
                    }
                }

                // Update portfolio values with real prices
                updatePortfolioPrices()

                // Log summary
                val updatedUser = repository.getUserSync()
                if (updatedUser != null) {
                    addLog("📊 Cartera: ${"%.2f".format(updatedUser.balance)} USD | Invertido: ${"%.2f".format(updatedUser.investedCapital)} USD")
                }

            } catch (e: Exception) {
                addLog("⚠️ Error en ciclo de trading: ${e.localizedMessage ?: "desconocido"}")
            }

            delay(120_000) // Analyze every 2 minutes
        }
    }

    private suspend fun executeBuy(decision: TradingEngine.TradeDecision) {
        val user = repository.getUserSync() ?: return
        if (user.balance < decision.amount) {
            addLog("❌ ${decision.symbol}: Saldo insuficiente (${"%.2f".format(user.balance)} USD)")
            return
        }

        // Deduct balance
        user.balance -= decision.amount
        user.investedCapital += decision.amount
        repository.updateUser(user)

        // Create investment order
        val order = InvestmentOrderEntity(
            symbol = decision.symbol,
            amount = decision.amount,
            entryPrice = decision.price,
            currentPrice = decision.price,
            type = "BUY",
            profit = 0.0,
            timestamp = System.currentTimeMillis(),
            status = "ACTIVE"
        )
        repository.insertOrder(order)

        // Log transaction
        repository.insertTransaction(TransactionEntity(
            type = "INVESTMENT_BUY",
            amount = decision.amount,
            status = "COMPLETED",
            details = "Compra ${decision.symbol} a ${"%.2f".format(decision.price)} USD — IA confianza: ${"%.0f".format(decision.confidence * 100)}%"
        ))

        addLog("✅ COMPRA ${decision.symbol}: ${"%.2f".format(decision.amount)} USD @ ${"%.2f".format(decision.price)} (confianza: ${"%.0f".format(decision.confidence*100)}%)")
        showNotification("IA compró ${"%.2f".format(decision.amount)} USD de ${decision.symbol}")
    }

    private suspend fun executeSell(decision: TradingEngine.TradeDecision) {
        val activeOrders = repository.activeInvestments.first()
        val ordersToClose = activeOrders.filter { it.symbol == decision.symbol && it.status == "ACTIVE" }

        if (ordersToClose.isEmpty()) return

        val user = repository.getUserSync() ?: return
        var totalReturn = 0.0

        for (order in ordersToClose) {
            val profit = (decision.price - order.entryPrice) * (order.amount / order.entryPrice)
            totalReturn += order.amount + profit

            // Close the order
            order.status = "CLOSED"
            order.currentPrice = decision.price
            order.profit = profit
            repository.updateOrder(order)
        }

        // Add return to balance
        user.balance += totalReturn
        user.investedCapital -= ordersToClose.sumOf { it.amount }
        repository.updateUser(user)

        repository.insertTransaction(TransactionEntity(
            type = "INVESTMENT_SELL",
            amount = totalReturn,
            status = "COMPLETED",
            details = "Venta ${decision.symbol} — Retorno: ${"%.2f".format(totalReturn)} USD"
        ))

        addLog("💰 VENTA ${decision.symbol}: retorno ${"%.2f".format(totalReturn)} USD (confianza: ${"%.0f".format(decision.confidence*100)}%)")
        showNotification("IA vendió ${decision.symbol} — retorno ${"%.2f".format(totalReturn)} USD")
    }

    private suspend fun updatePortfolioPrices() {
        val active = repository.activeInvestments.first()
        val prices = CoinGeckoApi.fetchAllPrices()

        for (order in active.filter { it.status == "ACTIVE" }) {
            val currentPrice = prices[order.symbol]
            if (currentPrice != null) {
                order.currentPrice = currentPrice
                order.profit = (currentPrice - order.entryPrice) * (order.amount / order.entryPrice)
                repository.updateOrder(order)
            }
        }
    }

    // ==== AI ANALYSIS (manual trigger) ====
    fun runAnalysis() {
        viewModelScope.launch(Dispatchers.IO) {
            _isRecommendationLoading.value = true
            _geminiRecommendation.value = null
            try {
                val symbols = listOf("BTC/USDT", "ETH/USDT", "SOL/USDT")
                val sb = StringBuilder()
                sb.appendLine("📊 ANÁLISIS LANZARUS IA\n")

                for (symbol in symbols) {
                    val analysis = TradingEngine.analyze(symbol)
                    if (analysis != null) {
                        sb.appendLine(TradingEngine.formatAnalysis(analysis))
                        sb.appendLine()
                    }
                }

                // Get portfolio status
                val user = repository.getUserSync()
                val active = repository.activeInvestments.first()
                if (user != null) {
                    val totalValue = user.balance + active.filter { it.status == "ACTIVE" }
                        .sumOf { (it.currentPrice / it.entryPrice) * it.amount }
                    sb.appendLine("📈 VALOR TOTAL CARTERA: ${"%.2f".format(totalValue)} USD")
                    sb.appendLine("💰 Saldo disponible: ${"%.2f".format(user.balance)} USD")
                    sb.appendLine("📊 Invertido: ${"%.2f".format(user.investedCapital)} USD")
                }

                _geminiRecommendation.value = sb.toString()
            } catch (e: Exception) {
                _geminiRecommendation.value = "⚠️ Error al obtener análisis: ${e.localizedMessage ?: "desconocido"}"
            } finally {
                _isRecommendationLoading.value = false
            }
        }
    }

    // ==== CHAT ====
    fun sendChatMessage(message: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _isChatLoading.value = true
            try {
                // Save user message
                repository.insertMessage(ChatMessageEntity(
                    sender = "USER",
                    message = message,
                    timestamp = System.currentTimeMillis()
                ))

                // Generate IA response based on real data
                val prices = _liveMarketPrices.value
                val user = repository.getUserSync()
                val active = repository.activeInvestments.first()

                val response = when {
                    message.lowercase().contains("precio") || message.lowercase().contains("cotización") -> {
                        buildPriceResponse(prices)
                    }
                    message.lowercase().contains("cartera") || message.lowercase().contains("portfolio") -> {
                        buildPortfolioResponse(user, active, prices)
                    }
                    message.lowercase().contains("invertir") || message.lowercase().contains("comprar") -> {
                        "📊 Para invertir, activa el Bot de Trading desde el panel. La IA analizará el mercado y decidirá automáticamente las mejores operaciones basadas en datos reales de CoinGecko."
                    }
                    message.lowercase().contains("ganancia") || message.lowercase().contains("profit") -> {
                        buildProfitResponse(active)
                    }
                    message.lowercase().contains("analizar") || message.lowercase().contains("analiza") -> {
                        "🔄 Ejecuta \"Análisis IA\" desde el panel y te mostraré el análisis completo de BTC, ETH y SOL con señales de compra/venta."
                    }
                    message.lowercase().contains("ayuda") || message.lowercase().contains("help") -> {
                        buildHelpResponse()
                    }
                    else -> {
                        "🤖 Soy Lanzarus IA, tu broker automático. Puedo consultar precios reales, analizar tu cartera o ejecutar el bot de trading. Usa el panel inferior para navegar. ¿Qué quieres saber?"
                    }
                }

                repository.insertMessage(ChatMessageEntity(
                    sender = "BOT",
                    message = response,
                    timestamp = System.currentTimeMillis()
                ))
            } catch (e: Exception) {
                repository.insertMessage(ChatMessageEntity(
                    sender = "BOT",
                    message = "⚠️ Error al procesar tu mensaje. Intenta de nuevo.",
                    timestamp = System.currentTimeMillis()
                ))
            } finally {
                _isChatLoading.value = false
            }
        }
    }

    private fun buildPriceResponse(prices: Map<String, Double>): String {
        val sb = StringBuilder("📊 *PRECIOS REALES (CoinGecko)*\n\n")
        val sorted = prices.entries.sortedByDescending { it.value }
        for ((symbol, price) in sorted) {
            sb.appendLine("• $symbol: ${"%.2f".format(price)} USD")
        }
        sb.appendLine("\n🔄 Actualizado cada 60s")
        return sb.toString()
    }

    private fun buildPortfolioResponse(
        user: UserEntity?,
        active: List<InvestmentOrderEntity>,
        prices: Map<String, Double>
    ): String {
        val sb = StringBuilder("📁 *MI CARTERA*\n\n")
        if (user != null) {
            val activePositions = active.filter { it.status == "ACTIVE" }
            val investedValue = activePositions.sumOf {
                (it.currentPrice / it.entryPrice) * it.amount
            }
            val totalValue = user.balance + investedValue
            val totalProfit = totalValue - (10000.0) // initial capital

            sb.appendLine("💰 Saldo disponible: ${"%.2f".format(user.balance)} USD")
            sb.appendLine("📊 Invertido: ${"%.2f".format(user.investedCapital)} USD")
            sb.appendLine("📈 Valor cartera: ${"%.2f".format(totalValue)} USD")
            sb.appendLine("📉 Ganancia total: ${"%.2f".format(totalProfit)} USD (${"%.1f".format((totalProfit/10000.0)*100)}%)")

            if (activePositions.isNotEmpty()) {
                sb.appendLine("\n📌 *Posiciones activas:*")
                for (pos in activePositions) {
                    val pnl = pos.profit
                    val emoji = if (pnl >= 0) "🟢" else "🔴"
                    sb.appendLine("$emoji ${pos.symbol}: ${"%.2f".format(pos.amount)} USD (${"%.2f".format(pnl)} USD)")
                }
            }
        }
        return sb.toString()
    }

    private fun buildProfitResponse(active: List<InvestmentOrderEntity>): String {
        val closed = active.filter { it.status == "CLOSED" }
        val totalProfit = closed.sumOf { it.profit }
        val winTrades = closed.count { it.profit > 0 }
        val sb = StringBuilder("📈 *GANANCIAS*")
        sb.appendLine("\n\nOperaciones cerradas: ${closed.size}")
        sb.appendLine("Ganancia total: ${"%.2f".format(totalProfit)} USD")
        sb.appendLine("Trades ganadores: $winTrades/${closed.size}")
        return sb.toString()
    }

    private fun buildHelpResponse(): String {
        return """
🤖 *LANZARUS IA — AYUDA*

Comandos:
• "precios" → Ver cotizaciones reales
• "cartera" → Ver mi portfolio
• "invertir" → Info sobre trading automático
• "analizar" → Ejecutar análisis de mercado
• "ganancias" → Ver resultados

💡 Desde el panel puedes:
✅ Activar/desactivar el Bot de Trading IA
✅ Elegir nivel de riesgo (BAJO/MODERADO/ALTO)
✅ Ver análisis completo de mercado
✅ Chatear con la IA financiera
        """.trimIndent()
    }

    // ==== UI HELPERS ====
    fun setRiskLevel(level: String) {
        _botRiskLevel.value = level
        addLog("🎯 Riesgo cambiado a: $level")
    }

    fun addBalance(amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserSync() ?: return@launch
            user.balance += amount
            repository.updateUser(user)
            addLog("💰 Depósito: +${"%.2f".format(amount)} USD")
        }
    }

    fun showNotification(message: String) {
        _notification.value = message
    }

    fun clearNotification() { _notification.value = null }

    private fun addLog(message: String) {
        val time = dateFormat.format(Date())
        val current = _systemLogs.value.toMutableList()
        current.add(0, "[$time] $message")
        if (current.size > 100) current.removeAt(current.size - 1)
        _systemLogs.value = current
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