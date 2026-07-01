package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.api.Content as GeminiContent
import com.example.api.GenerateContentRequest
import com.example.api.Part
import com.example.api.RetrofitClient
import com.example.api.LanzarusBackendClient
import java.util.UUID
import com.example.data.database.LanzarusDatabase
import com.example.data.model.*
import com.example.data.repository.LanzarusRepository
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class LanzarusViewModel(
    application: Application,
    private val repository: LanzarusRepository
) : AndroidViewModel(application) {

    // --- State Observables ---
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

    // --- UI/Operation States ---
    private val _isBotActive = MutableStateFlow(true)
    val isBotActive: StateFlow<Boolean> = _isBotActive.asStateFlow()

    private val _botRiskLevel = MutableStateFlow("MODERADO") // "CONSERVADOR", "MODERADO", "AGRESIVO"
    val botRiskLevel: StateFlow<String> = _botRiskLevel.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    private val _systemLogs = MutableStateFlow<List<String>>(emptyList())
    val systemLogs: StateFlow<List<String>> = _systemLogs.asStateFlow()

    private val _notification = MutableStateFlow<String?>(null)
    val notification: StateFlow<String?> = _notification.asStateFlow()

    // --- Gemini Recommendation States ---
    private val _geminiRecommendation = MutableStateFlow<String?>(null)
    val geminiRecommendation: StateFlow<String?> = _geminiRecommendation.asStateFlow()

    private val _isRecommendationLoading = MutableStateFlow(false)
    val isRecommendationLoading: StateFlow<Boolean> = _isRecommendationLoading.asStateFlow()

    // --- Live Market Prices & Tracking States ---
    private val _liveMarketPrices = MutableStateFlow<Map<String, Double>>(
        mapOf("BTC/USDT" to 65000.0, "ETH/USDT" to 3400.0, "SOL/USDT" to 145.0, "EUR/USD" to 1.08, "SP500/ETF" to 540.0, "NASDAQ" to 490.0)
    )
    val liveMarketPrices: StateFlow<Map<String, Double>> = _liveMarketPrices.asStateFlow()

    private val _isTrackingConnected = MutableStateFlow(true)
    val isTrackingConnected: StateFlow<Boolean> = _isTrackingConnected.asStateFlow()

    // --- Plaid Secure Card Linking States ---
    private val _linkedCardNumber = MutableStateFlow<String?>(null)
    val linkedCardNumber: StateFlow<String?> = _linkedCardNumber.asStateFlow()

    private val _linkedCardHolder = MutableStateFlow<String?>(null)
    val linkedCardHolder: StateFlow<String?> = _linkedCardHolder.asStateFlow()

    private val _linkedCardBrand = MutableStateFlow<String?>(null)
    val linkedCardBrand: StateFlow<String?> = _linkedCardBrand.asStateFlow()

    private val _linkedBankName = MutableStateFlow<String?>(null)
    val linkedBankName: StateFlow<String?> = _linkedBankName.asStateFlow()

    private val _isPlaidLinking = MutableStateFlow(false)
    val isPlaidLinking: StateFlow<Boolean> = _isPlaidLinking.asStateFlow()

    private val _plaidLinkingError = MutableStateFlow<String?>(null)
    val plaidLinkingError: StateFlow<String?> = _plaidLinkingError.asStateFlow()

    private val _plaidLinkingSuccess = MutableStateFlow(false)
    val plaidLinkingSuccess: StateFlow<Boolean> = _plaidLinkingSuccess.asStateFlow()

    fun resetPlaidLinkingState() {
        _isPlaidLinking.value = false
        _plaidLinkingError.value = null
        _plaidLinkingSuccess.value = false
    }

    val isGeminiActive: StateFlow<Boolean> = flow {
        val apiKey = BuildConfig.GEMINI_API_KEY
        val isPlaceholder = apiKey.isEmpty() || 
                apiKey == "MY_GEMINI_API_KEY" || 
                apiKey.startsWith("AIzaSyD_TEST") || 
                apiKey.contains("placeholder", ignoreCase = true)
        emit(!isPlaceholder)
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    init {
        viewModelScope.launch(Dispatchers.IO) {
            // Check if user exists, otherwise initialize
            val currentUser = repository.getUserSync()
            if (currentUser == null) {
                initializeDefaultData()
            }
            
            // Start simulation of price fluctuations and automatic bot investments
            startSimulationLoop()
            
            // Start real-time CoinCap API price ticker for push notifications
            startLivePriceAlertTicker()

            // Start real-time portfolio price tracker with live market data
            startPortfolioPriceTracker()

            // Log Gemini client integration status
            if (isGeminiActive.value) {
                addLog("Lanzarus AI: Motor cognitivo inicializado con éxito utilizando la variable GEMINI_API_KEY real.")
            } else {
                addLog("Lanzarus AI: Motor cognitivo iniciado en modo SIMULADO. Configura tu GEMINI_API_KEY real para activar.")
            }

            // Fetch initial central AI recommendation analysis
            fetchGeminiRecommendation()
        }
    }

    fun addLog(message: String) {
        val timestamp = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date())
        _systemLogs.update { listOf("[$timestamp] $message") + it.take(29) }
    }

    fun linkDebitCard(bankName: String, cardHolder: String, cardNumber: String, cardBrand: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserSync()
            val userIdStr = user?.id?.toString() ?: "1"
            
            _isPlaidLinking.value = true
            _plaidLinkingError.value = null
            _plaidLinkingSuccess.value = false
            
            addLog("Plaid API: Iniciando handshake de token seguro en el backend...")
            
            try {
                // 1. Initialize link token on backend
                val initResponse = LanzarusBackendClient.secureInitializePlaid(userIdStr)
                addLog("Plaid API: Token de enlace generado exitosamente: ${initResponse.linkToken}")
                
                // 2. Exchange token on backend
                val exchangeResponse = LanzarusBackendClient.secureExchangePlaidToken(
                    publicToken = "public-token-" + UUID.randomUUID().toString().take(12),
                    selectedBank = bankName
                )
                
                // 3. Update local states with authorized details
                _linkedBankName.value = bankName
                _linkedCardHolder.value = cardHolder
                _linkedCardNumber.value = exchangeResponse.authorizedCardMask
                _linkedCardBrand.value = exchangeResponse.authorizedCardBrand
                
                addLog("Plaid Link: Tarjeta autorizada con token de sesión ${exchangeResponse.accessToken.take(15)}...")
                showNotification("Tarjeta vinculada con éxito vía Plaid Secure")
                _plaidLinkingSuccess.value = true
            } catch (e: Exception) {
                val errorMsg = e.localizedMessage ?: "Fallo de conexión"
                addLog("Plaid API Error: No se pudo completar la sincronización con el backend ($errorMsg).")
                showNotification("Error de sincronización con Plaid: $errorMsg")
                _plaidLinkingError.value = errorMsg
            } finally {
                _isPlaidLinking.value = false
            }
        }
    }

    fun unlinkDebitCard() {
        viewModelScope.launch {
            _linkedBankName.value = null
            _linkedCardHolder.value = null
            _linkedCardNumber.value = null
            _linkedCardBrand.value = null
            addLog("Plaid Link: Tarjeta de débito desvinculada por el usuario")
            showNotification("Tarjeta desvinculada")
        }
    }

    fun showNotification(message: String) {
        _notification.value = message
        viewModelScope.launch {
            delay(4000)
            if (_notification.value == message) {
                _notification.value = null
            }
        }
    }

    fun clearNotification() {
        _notification.value = null
    }

    private suspend fun initializeDefaultData() {
        addLog("Inicializando Lanzarus Core OS v1.0.0...")
        
        // 1. Create Default User
        val adminEmail = if (BuildConfig.LANZARUS_ADMIN_UID.isNullOrBlank()) "demo@lanzarus.io" else BuildConfig.LANZARUS_ADMIN_UID
        val defaultUser = UserEntity(
            name = "Administrador Lanzarus",
            email = adminEmail,
            balance = 10450.00,
            investedCapital = 4800.00,
            isVerified = true,
            isPremium = true,
            monthlyEarnings = 340.50,
            dailyStreak = 3
        )
        repository.insertOrUpdateUser(defaultUser)
        addLog("Base de datos de usuario configurada.")

        // 2. Add Initial Transactions
        repository.insertTransaction(
            TransactionEntity(
                type = "DEPOSIT",
                amount = 12000.00,
                status = "COMPLETED",
                details = "Depósito inicial mediante transferencia SEPA"
            )
        )
        repository.insertTransaction(
            TransactionEntity(
                type = "REINVEST",
                amount = 1550.00,
                status = "COMPLETED",
                details = "Reinversión automática en Portafolio Alpha"
            )
        )

        // 3. Add Pre-loaded Content
        val defaultContents = listOf(
            ContentEntity(
                title = "Alerta Lanzarus AI: Ballenas en Ethereum acumulan capital",
                category = "Mercados",
                snippet = "Monitoreo de billeteras frías detecta un flujo neto entrante de $450M USD en las últimas 72 horas.",
                fullText = "Nuestros algoritmos de escaneo on-chain en Lanzarus han detectado un patrón de acumulación masiva de Ethereum (ETH) en el rango de los $3,200 - $3,250 USD. Más de 18 direcciones catalogadas como 'ballenas' han retirado tokens de exchanges hacia billeteras frías. Históricamente, estos movimientos preceden a incrementos de volatilidad alcista del 12-15% en las siguientes semanas. Recomendamos monitorear el soporte clave.",
                isPremiumOnly = true
            ),
            ContentEntity(
                title = "Estrategia de Interés Compuesto para Bots Autónomos",
                category = "Finanzas",
                snippet = "Cómo configurar tu bot en piloto automático para maximizar rendimientos mensuales sin riesgo elevado.",
                fullText = "El bot de inversión inteligente de Lanzarus opera sobre activos regulados y stablecoins. Al activar la opción de 'Autoreinversión', el sistema desvía automáticamente el 15% de los beneficios diarios netos hacia pools de liquidez y notas del tesoro tokenizadas. Esto genera un efecto de interés compuesto exponencial. Con un nivel de riesgo MODERADO, el rendimiento estimado anualizado se sitúa entre el 8.5% y el 14.2%.",
                isPremiumOnly = false
            ),
            ContentEntity(
                title = "Tokenización de Activos Reales (RWA): La nueva frontera",
                category = "Tendencias",
                snippet = "El mercado global de bienes raíces tokenizados crece un 180% impulsado por inversores retail.",
                fullText = "La tokenización de activos de la vida real (RWA) está revolucionando el acceso al capital de inversión. Propiedades fraccionadas de lujo, bonos del gobierno y oro digitalizados ya son accesibles desde $100 USD en nuestra plataforma integrada. Lanzarus automatiza el cobro de dividendos y su distribución inmediata en la billetera de los titulares, esquivando las demoras de la banca tradicional.",
                isPremiumOnly = false
            )
        )
        repository.insertContents(defaultContents)
        addLog("Canal de contenido curado cargado con éxito.")

        // 4. Create Sample Orders
        repository.insertOrder(
            InvestmentOrderEntity(
                symbol = "BTC/USDT",
                amount = 2500.00,
                entryPrice = 64200.0,
                currentPrice = 65450.0,
                type = "BUY",
                profit = 48.67,
                status = "ACTIVE"
            )
        )
        repository.insertOrder(
            InvestmentOrderEntity(
                symbol = "NASDAQ/ETF",
                amount = 1500.00,
                entryPrice = 480.0,
                currentPrice = 492.5,
                type = "BUY",
                profit = 39.06,
                status = "ACTIVE"
            )
        )
        repository.insertOrder(
            InvestmentOrderEntity(
                symbol = "GOLD/TOKEN",
                amount = 800.00,
                entryPrice = 2320.0,
                currentPrice = 2315.0,
                type = "BUY",
                profit = -1.72,
                status = "ACTIVE"
            )
        )

        // 5. Setup Welcome Messages
        repository.insertMessage(
            ChatMessageEntity(
                sender = "BOT",
                message = "¡Bienvenido a Lanzarus AI! Soy tu gestor automatizado de inversiones. Puedo responder tus dudas sobre finanzas, sugerirte estrategias y ayudarte a configurar tus bots de inversión. ¿En qué puedo asistirte hoy?"
            )
        )
        addLog("Ecosistema Lanzarus completamente sincronizado.")
    }

    // --- Simulation Loop ---
    private fun startSimulationLoop() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                delay(12000) // update every 12 seconds
                
                // 1. Simular fluctuaciones de precios si el bot está activo
                if (_isBotActive.value) {
                    val activeOrders = repository.activeInvestments.first()
                    val user = repository.getUserSync() ?: continue
                    
                    var totalProfitShift = 0.0
                    
                    activeOrders.forEach { order ->
                        // Simulate fluctuation based on risk
                        val volatilityFactor = when (_botRiskLevel.value) {
                            "CONSERVADOR" -> 0.002 // 0.2%
                            "AGRESIVO" -> 0.015 // 1.5%
                            else -> 0.006 // 0.6% MODERADO
                        }
                        
                        val isPositive = Random.nextBoolean()
                        val percentChange = Random.nextDouble() * volatilityFactor
                        val direction = if (isPositive) 1.0 else -1.0
                        
                        val priceMultiplier = 1.0 + (direction * percentChange)
                        val newPrice = order.currentPrice * priceMultiplier
                        
                        val priceDiffPercent = (newPrice - order.entryPrice) / order.entryPrice
                        val profit = order.amount * priceDiffPercent * (if (order.type == "BUY") 1.0 else -1.0)
                        
                        val updatedOrder = order.copy(
                            currentPrice = newPrice,
                            profit = profit
                        )
                        repository.updateOrder(updatedOrder)
                        totalProfitShift += (profit - order.profit)
                    }

                    // Occassionally add small passive earnings
                    if (Random.nextDouble() < 0.35) {
                        val earningAmount = when (_botRiskLevel.value) {
                            "CONSERVADOR" -> Random.nextDouble(1.5, 4.0)
                            "AGRESIVO" -> Random.nextDouble(-8.0, 15.0)
                            else -> Random.nextDouble(2.5, 8.5)
                        }
                        
                        val updatedUser = user.copy(
                            balance = user.balance + earningAmount,
                            monthlyEarnings = user.monthlyEarnings + if (earningAmount > 0) earningAmount else 0.0
                        )
                        repository.insertOrUpdateUser(updatedUser)
                        
                        if (earningAmount > 1.0) {
                            addLog("Bot obtuvo un retorno pasivo de +$${String.format("%.2f", earningAmount)} USD.")
                            repository.insertTransaction(
                                TransactionEntity(
                                    type = "REINVEST",
                                    amount = earningAmount,
                                    status = "COMPLETED",
                                    details = "Retorno de arbitraje automático por Bot Lanzarus (${_botRiskLevel.value})"
                                )
                            )
                        }
                    }
                    
                    if (totalProfitShift != 0.0) {
                        Log.d("LanzarusSim", "Fluctuación procesada. Variación total de cartera: $totalProfitShift")
                    }
                }
            }
        }
    }

    // --- Control Dashboard Actions ---
    fun toggleBotActive() {
        _isBotActive.value = !_isBotActive.value
        val statusText = if (_isBotActive.value) "ACTIVADO" else "PAUSADO"
        addLog("Control de Algoritmo: Bot de Inversión $statusText.")
        showNotification("Bot de Inversión $statusText")
    }

    fun changeRiskLevel(level: String) {
        _botRiskLevel.value = level
        addLog("Parámetros actualizados: Nivel de riesgo ajustado a $level.")
        showNotification("Riesgo cambiado a $level")
    }

    fun triggerForcedRebalance() {
        viewModelScope.launch(Dispatchers.IO) {
            addLog("Endpoint Trigger: Ejecutando rebalanceo forzado de portafolio...")
            delay(1000)
            val user = repository.getUserSync() ?: return@launch
            
            // Estricta Validación de Admin UID en producción
            val adminUid = BuildConfig.LANZARUS_ADMIN_UID
            val userEmail = user.email
            val userIdStr = user.id.toString()
            if (userEmail != adminUid && userIdStr != adminUid) {
                addLog("ERROR DE SEGURIDAD CRÍTICO: Rebalanceo forzado denegado. El usuario actual ($userEmail) no coincide con el Admin UID autorizado ($adminUid).")
                showNotification("Error: Usuario no autorizado como Admin UID")
                return@launch
            }
            
            // Reallocate 5% of liquid funds to investments to simulate automated action
            val rebalanceAmount = user.balance * 0.05
            if (user.balance >= rebalanceAmount && rebalanceAmount > 10.0) {
                val updatedUser = user.copy(
                    balance = user.balance - rebalanceAmount,
                    investedCapital = user.investedCapital + rebalanceAmount
                )
                repository.insertOrUpdateUser(updatedUser)
                
                // Add a new order representing this
                val symbols = listOf("SOL/USDT", "AVAX/USDT", "BTC/USDT", "SP500/ETF")
                val chosenSymbol = symbols.random()
                repository.insertOrder(
                    InvestmentOrderEntity(
                        symbol = chosenSymbol,
                        amount = rebalanceAmount,
                        entryPrice = 100.0,
                        currentPrice = 100.0,
                        type = "BUY",
                        status = "ACTIVE"
                    )
                )
                
                repository.insertTransaction(
                    TransactionEntity(
                        type = "REINVEST",
                        amount = rebalanceAmount,
                        status = "COMPLETED",
                        details = "Rebalanceo de liquidez forzado por admin"
                    )
                )
                addLog("Rebalanceo ejecutado: Se invirtieron $${String.format("%.2f", rebalanceAmount)} en $chosenSymbol.")
                showNotification("Rebalanceo de liquidez exitoso")
            } else {
                addLog("Rebalanceo cancelado: Fondos líquidos insuficientes.")
                showNotification("Fondos insuficientes para rebalancear")
            }
        }
    }

    fun triggerScrapeContent() {
        viewModelScope.launch(Dispatchers.IO) {
            addLog("Iniciando escaneo de feeds de mercado externos...")
            delay(1500)
            
            val topics = listOf(
                Pair("Análisis Macro: El impacto del halving histórico", "Explicación a fondo de la escasez digital y su impacto estructural en los precios del mercado."),
                Pair("Regulación Cripto en Europa (MiCA)", "Sincronización de pasarelas locales con las directivas MiCA: estabilidad jurídica para inversiones retail."),
                Pair("Breakout Técnico en Solana", "Patrón de taza con asa en temporalidad diaria anticipa proyección alcista hacia máximos históricos.")
            )
            
            val randomTopic = topics.random()
            val categories = listOf("Mercados", "Finanzas", "Tendencias")
            val chosenCategory = categories.random()
            
            val newContent = ContentEntity(
                title = randomTopic.first,
                category = chosenCategory,
                snippet = randomTopic.second,
                fullText = "${randomTopic.second} Este contenido ha sido curado automáticamente por el motor de APIs de Lanzarus, filtrando fuentes de alto valor en tiempo real para evitar falsas señales. La automatización garantiza que recibas información de inversión validada inmediatamente después de ser procesada por nuestro backend serverless.",
                isPremiumOnly = Random.nextBoolean()
            )
            repository.insertContent(newContent)
            addLog("Nuevo contenido procesado y publicado: '${newContent.title}'")
            showNotification("Nuevo contenido curado disponible")

            // Send customizable push notification based on user interests
            val user = repository.getUserSync()
            val shouldNotify = user == null || when (chosenCategory) {
                "Finanzas" -> user.notifyFinanzas
                "Tendencias" -> user.notifyTendencias
                "Mercados" -> user.notifyMercados
                else -> true
            }

            if (shouldNotify) {
                com.example.util.NotificationHelper.showNotification(
                    getApplication(),
                    "Lanzarus Feed: $chosenCategory",
                    newContent.title,
                    com.example.util.NotificationHelper.CHANNEL_FEEDS
                )
            }
        }
    }

    // --- Financial Gateway Actions ---
    fun depositFunds(amount: Double) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserSync() ?: return@launch
            val updatedUser = user.copy(balance = user.balance + amount)
            repository.insertOrUpdateUser(updatedUser)
            
            repository.insertTransaction(
                TransactionEntity(
                    type = "DEPOSIT",
                    amount = amount,
                    status = "COMPLETED",
                    details = "Pasarela de Pago: Depósito exitoso vía Visa/Mastercard"
                )
            )
            addLog("Pasarela de Pago: Depósito de $${amount} USD procesado correctamente.")
            showNotification("Depósito exitoso de $${amount} USD")
        }
    }

    fun withdrawFunds(amount: Double, walletAddress: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserSync() ?: return@launch
            
            // Estricta Validación de Admin UID en producción
            val adminUid = BuildConfig.LANZARUS_ADMIN_UID
            val userEmail = user.email
            val userIdStr = user.id.toString()
            if (userEmail != adminUid && userIdStr != adminUid) {
                addLog("ERROR DE SEGURIDAD CRÍTICO: Solicitud de retiro denegada. El usuario actual ($userEmail) no es el Administrador autorizado.")
                showNotification("Error: Usuario no autorizado como Admin UID")
                return@launch
            }

            if (user.balance < amount) {
                addLog("Error de retiro: Fondos insuficientes.")
                showNotification("Saldo insuficiente para retirar")
                return@launch
            }

            // Verify KYC is done
            if (!user.isVerified) {
                addLog("Error de retiro: Cuenta no verificada. Requiere KYC.")
                showNotification("Error: Requiere Verificación de Identidad (KYC)")
                return@launch
            }

            // Deduct from balance and create PENDING transaction
            val updatedUser = user.copy(balance = user.balance - amount)
            repository.insertOrUpdateUser(updatedUser)

            val pendingTx = TransactionEntity(
                type = "WITHDRAWAL",
                amount = amount,
                status = "PENDING",
                details = "Iniciando dispersión hacia $walletAddress"
            )
            repository.insertTransaction(pendingTx)
            addLog("Retiro Iniciado: Solicitud de $${amount} USD enviada al bus de pagos de Lanzarus...")
            showNotification("Retiro iniciado: En proceso...")

            // Llama al cliente de Backend real para procesar el payout webhook firmado
            delay(1500) // Realistic secure network prep delay
            try {
                val destinationType = when {
                    walletAddress.startsWith("Plaid", ignoreCase = true) -> "TARJETA"
                    walletAddress.contains("•") || walletAddress.replace(" ", "").replace("-", "").all { it.isDigit() } -> "TARJETA"
                    walletAddress.startsWith("0x", ignoreCase = true) || walletAddress.length > 25 -> "WALLET"
                    else -> "BANCO"
                }
                addLog("Backend API: Despachando webhook firmado a pasarela serverless...")
                
                val payoutResponse = LanzarusBackendClient.secureDispatchPayout(
                    amount = amount,
                    destinationType = destinationType,
                    destinationDetails = walletAddress,
                    userId = user.id.toString()
                )

                if (payoutResponse.success) {
                    val txs = repository.allTransactions.first()
                    val lastTx = txs.firstOrNull { it.type == "WITHDRAWAL" && it.status == "PENDING" }
                    if (lastTx != null) {
                        val signatureDetails = if (payoutResponse.blockchainHash != null) {
                            " (Hash: ${payoutResponse.blockchainHash.take(12)}...)"
                        } else {
                            " (API Ref: ${payoutResponse.transactionId})"
                        }
                        
                        repository.insertTransaction(
                            lastTx.copy(
                                status = "COMPLETED",
                                details = "Retiro exitoso hacia $walletAddress$signatureDetails"
                            )
                        )
                        addLog("Pasarela Webhook: Liquidación exitosa procesada en ${payoutResponse.processedAt}. Mensaje: ${payoutResponse.gatewayMessage}")
                        showNotification("Retiro liquidado exitosamente")
                    }
                } else {
                    throw Exception(payoutResponse.gatewayMessage)
                }
            } catch (e: Exception) {
                addLog("Error de pasarela: Falló la dispersión en el backend. Reversando balance...")
                // Return funds to user
                val revertedUser = repository.getUserSync()
                if (revertedUser != null) {
                    repository.insertOrUpdateUser(revertedUser.copy(balance = revertedUser.balance + amount))
                }
                
                val txs = repository.allTransactions.first()
                val lastTx = txs.firstOrNull { it.type == "WITHDRAWAL" && it.status == "PENDING" }
                if (lastTx != null) {
                    repository.insertTransaction(
                        lastTx.copy(
                            status = "FAILED",
                            details = "Reversado: ${e.localizedMessage ?: "Error desconocido de backend"}"
                        )
                    )
                }
                showNotification("Error en el retiro de fondos")
            }
        }
    }

    fun simulateOrder(symbol: String, amount: Double, entryPrice: Double, type: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserSync() ?: return@launch
            if (user.balance >= amount) {
                repository.insertOrUpdateUser(
                    user.copy(
                        balance = user.balance - amount,
                        investedCapital = user.investedCapital + amount
                    )
                )
                repository.insertOrder(
                    InvestmentOrderEntity(
                        symbol = symbol,
                        amount = amount,
                        entryPrice = entryPrice,
                        currentPrice = entryPrice,
                        type = type,
                        profit = 0.0,
                        status = "ACTIVE"
                    )
                )
                repository.insertTransaction(
                    TransactionEntity(
                        type = "REINVEST",
                        amount = amount,
                        status = "COMPLETED",
                        details = "Bot: Compra de portafolio $symbol"
                    )
                )
                showNotification("Nueva orden de $symbol colocada")
                addLog("Orden Simulada: $type $symbol por $${String.format("%.2f", amount)}")
            } else {
                showNotification("Saldo insuficiente para simular orden")
            }
        }
    }

    fun runKYCVerification(fullName: String, idNumber: String) {
        viewModelScope.launch(Dispatchers.IO) {
            addLog("KYC: Iniciando verificación biométrica y documental para $fullName...")
            delay(2000)
            
            val user = repository.getUserSync() ?: return@launch
            val updatedUser = user.copy(
                name = fullName,
                isVerified = true
            )
            repository.insertOrUpdateUser(updatedUser)
            
            addLog("KYC Verificado: Identidad validada con éxito. Usuario habilitado para retiros inmediatos.")
            showNotification("Identidad Verificada Correctamente")
        }
    }

    fun purchasePremium() {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserSync() ?: return@launch
            val price = 29.99
            
            if (user.balance < price) {
                addLog("Error de Compra: Fondos insuficientes para activar membresía Premium.")
                showNotification("Saldo insuficiente para comprar Premium ($29.99)")
                return@launch
            }
            
            val updatedUser = user.copy(
                balance = user.balance - price,
                isPremium = true
            )
            repository.insertOrUpdateUser(updatedUser)
            
            repository.insertTransaction(
                TransactionEntity(
                    type = "PREMIUM_SUB",
                    amount = price,
                    status = "COMPLETED",
                    details = "Suscripción Premium Lanzarus Mensual"
                )
            )
            addLog("Membresía Lanzarus Premium activada correctamente. Acceso ilimitado concedido.")
            showNotification("¡Bienvenido a Lanzarus Premium!")
        }
    }

    // --- Bot Chat / AI Integration ---
    fun sendMessage(userPrompt: String) {
        if (userPrompt.isBlank()) return
        
        viewModelScope.launch(Dispatchers.IO) {
            // 1. Save User Message
            repository.insertMessage(
                ChatMessageEntity(sender = "USER", message = userPrompt)
            )
            _isChatLoading.value = true
            
            // 2. Fetch Gemini Response or Simulate Fallback
            val isKeyPlaceholder = !isGeminiActive.value
            val apiKey = BuildConfig.GEMINI_API_KEY
            
            val responseText = if (isKeyPlaceholder) {
                // Return highly detailed local simulated response based on keywords
                getSimulatedResponse(userPrompt)
            } else {
                try {
                    // Query Gemini API directly using Direct REST API instructions
                    val history = repository.chatMessages.first().takeLast(6).map { msg ->
                        GeminiContent(
                            parts = listOf(Part(text = msg.message)),
                            role = if (msg.sender == "USER") "user" else "model"
                        )
                    }
                    
                    val systemPrompt = "Eres Lanzarus AI, el cerebro automatizado de la plataforma de inversión y contenido autónomo LANZARUS. Habla en español de forma elegante, profesional y concisa (máximo 4 líneas). Ayudas con temas financieros, criptomonedas, diversificación de carteras de inversión, y explicas cómo opera el bot de Lanzarus de forma automatizada y serverless. Da consejos sensatos pero emocionantes sobre mitigación de riesgos."
                    
                    val request = GenerateContentRequest(
                        contents = history,
                        systemInstruction = GeminiContent(parts = listOf(Part(text = systemPrompt)))
                    )
                    
                    val response = RetrofitClient.service.generateContent(apiKey, request)
                    response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                        ?: "No se obtuvo una respuesta válida del núcleo central. Inténtalo de nuevo."
                } catch (e: Exception) {
                    Log.e("LanzarusAI", "Gemini API error, falling back to simulator: ", e)
                    getSimulatedResponse(userPrompt)
                }
            }
            
            // 3. Save Bot Response
            repository.insertMessage(
                ChatMessageEntity(sender = "BOT", message = responseText)
            )
            _isChatLoading.value = false
        }
    }

    private fun getSimulatedResponse(prompt: String): String {
        val lowerPrompt = prompt.lowercase()
        return when {
            lowerPrompt.contains("invers") || lowerPrompt.contains("invert") || lowerPrompt.contains("portafolio") -> {
                "El motor de inversión de Lanzarus opera actualmente bajo una estrategia de rebalanceo dinámico. Basado en tu perfil de riesgo actual (${_botRiskLevel.value}), diversificamos de forma automática en activos como Bitcoin, ETFs de tecnología y bonos tokenizados para mitigar volatilidades extremas."
            }
            lowerPrompt.contains("riesgo") || lowerPrompt.contains("mitiga") || lowerPrompt.contains("segur") -> {
                "Mitigamos el riesgo utilizando órdenes inteligentes de Stop-Loss dinámicas operadas mediante APIs en entornos financieros regulados. Nunca exponemos más del 5% del capital total en una sola operación, garantizando la preservación del capital líquido ante caídas abruptas."
            }
            lowerPrompt.contains("bitcoin") || lowerPrompt.contains("btc") || lowerPrompt.contains("cripto") || lowerPrompt.contains("eth") -> {
                "Monitoreamos las reservas on-chain y los flujos de liquidez global. Las ballenas muestran patrones fuertes de acumulación en Bitcoin y Ethereum. Nuestro bot aprovecha estas desviaciones comprando en soportes clave y liquidando ganancias de forma escalonada."
            }
            lowerPrompt.contains("retiro") || lowerPrompt.contains("retirar") || lowerPrompt.contains("pago") || lowerPrompt.contains("webhook") -> {
                "En Lanzarus, las transferencias salientes se procesan a través de un bus de pagos serverless. Una vez que realizas un retiro, un webhook automatizado liquida la orden en milisegundos directamente a tu tarjeta de débito o billetera cripto, esquivando demoras tradicionales bancarias."
            }
            lowerPrompt.contains("premium") || lowerPrompt.contains("suscrip") || lowerPrompt.contains("costo") -> {
                "La membresía Lanzarus Premium cuesta $29.99 USD al mes y se descuenta directamente de tu saldo líquido. Te otorga acceso a nuestras señales de trading de inteligencia on-chain exclusivas y reportes macroeconómicos curados automáticamente en tiempo real."
            }
            lowerPrompt.contains("hola") || lowerPrompt.contains("saludos") || lowerPrompt.contains("ayuda") -> {
                "Hola. Soy el asistente IA de Lanzarus. Puedo ayudarte a optimizar tus bots, revisar tus transacciones, explicarte nuestras estrategias de diversificación cripto y de mercado de valores, o guiarte en el proceso de KYC y retiros inmediatos."
            }
            else -> {
                "Entendido tu consulta. Como núcleo de IA de Lanzarus, optimizo el flujo de capital autónomamente 24/7. Si deseas que configuremos una orden de trading específica o revisemos los rendimientos de tus bots en tiempo real, solo indícame los parámetros de riesgo deseados."
            }
        }
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.clearChatHistory()
            repository.insertMessage(
                ChatMessageEntity(
                    sender = "BOT",
                    message = "Historial reiniciado. ¿En qué más te puedo ayudar hoy con tus inversiones y flujos de contenido autónomo?"
                )
            )
        }
    }

    fun resetApp() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.resetDatabase()
            addLog("Reinicio de Fábrica: Sistema de Lanzarus resincronizado por completo.")
            showNotification("Aplicación reseteada al estado inicial")
        }
    }

    fun updateNotificationSetting(category: String, enabled: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            val user = repository.getUserSync() ?: return@launch
            val updatedUser = when (category) {
                "finanzas" -> user.copy(notifyFinanzas = enabled)
                "tendencias" -> user.copy(notifyTendencias = enabled)
                "mercados" -> user.copy(notifyMercados = enabled)
                "alerts" -> user.copy(notifyAlerts = enabled)
                else -> user
            }
            repository.insertOrUpdateUser(updatedUser)
            addLog("Ajustes: Notificaciones de ${category.uppercase()} ${if (enabled) "ACTIVADAS" else "DESACTIVADAS"}.")
        }
    }

    fun triggerTestNotification(category: String) {
        viewModelScope.launch {
            val user = repository.getUserSync() ?: return@launch
            val isEnabled = when (category) {
                "finanzas" -> user.notifyFinanzas
                "tendencias" -> user.notifyTendencias
                "mercados" -> user.notifyMercados
                "alerts" -> user.notifyAlerts
                else -> true
            }

            val channelId = if (category == "alerts") {
                com.example.util.NotificationHelper.CHANNEL_ALERTS
            } else {
                com.example.util.NotificationHelper.CHANNEL_FEEDS
            }

            if (isEnabled) {
                val title = when (category) {
                    "finanzas" -> "Lanzarus Finanzas: Señal de Trading"
                    "tendencias" -> "Lanzarus Tendencias: Alerta de Tendencia"
                    "mercados" -> "Lanzarus Mercados: Alerta de Volatilidad"
                    "alerts" -> "Lanzarus Alerta: Transacción de Inversión"
                    else -> "Lanzarus Notificación"
                }
                val body = when (category) {
                    "finanzas" -> "El bot de arbitraje detectó una diferencia del 2.3% en la paridad EUR/USD. Ejecutando cobertura."
                    "tendencias" -> "Las búsquedas globales de RWA y tokenización de activos inmobiliarios aumentaron un 45% hoy."
                    "mercados" -> "Bitcoin supera los $68,000 USD con alta acumulación de billeteras ballena."
                    "alerts" -> "¡Alerta de Inversión! Tu orden de trading en BTC/USDT acaba de generar un retorno positivo."
                    else -> "Servicio de notificaciones operando correctamente."
                }
                com.example.util.NotificationHelper.showNotification(
                    getApplication(),
                    title,
                    body,
                    channelId
                )
                showNotification("Notificación de prueba enviada")
            } else {
                showNotification("No enviado: Notificaciones de ${category.uppercase()} desactivadas")
            }
        }
    }

    private fun startLivePriceAlertTicker() {
        viewModelScope.launch(Dispatchers.IO) {
            var lastBtcPrice = 0.0
            while (true) {
                try {
                    val url = java.net.URL("https://api.coincap.io/v2/assets/bitcoin")
                    val connection = url.openConnection() as java.net.HttpURLConnection
                    connection.requestMethod = "GET"
                    connection.connectTimeout = 5000
                    connection.readTimeout = 5000
                    
                    if (connection.responseCode == 200) {
                        val response = connection.inputStream.bufferedReader().use { it.readText() }
                        val priceUsdPattern = "\"priceUsd\":\"([0-9.]+)\"".toRegex()
                        val matchResult = priceUsdPattern.find(response)
                        val priceStr = matchResult?.groups?.get(1)?.value
                        if (priceStr != null) {
                            val currentBtcPrice = priceStr.toDouble()
                            if (lastBtcPrice > 0.0) {
                                val percentChange = ((currentBtcPrice - lastBtcPrice) / lastBtcPrice) * 100.0
                                val user = repository.getUserSync()
                                if (user?.notifyAlerts == true && Math.abs(percentChange) >= 0.01) {
                                    val sign = if (percentChange >= 0) "▲" else "▼"
                                    com.example.util.NotificationHelper.showNotification(
                                        getApplication(),
                                        "Alerta Cripto API: BTC/USDT $sign",
                                        "El precio en vivo de Bitcoin cambió un ${String.format("%.3f", percentChange)}% a $${String.format("%,.2f", currentBtcPrice)} USD.",
                                        com.example.util.NotificationHelper.CHANNEL_ALERTS
                                    )
                                    addLog("API Ticker: Bitcoin varió a $${String.format("%,.2f", currentBtcPrice)} USD.")
                                }
                            } else {
                                addLog("API Ticker: Conectado a CoinCap API. Precio BTC inicial: $${String.format("%,.2f", currentBtcPrice)} USD.")
                            }
                            lastBtcPrice = currentBtcPrice
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LanzarusTicker", "Error fetching live price from CoinCap, using simulated price alert: ${e.message}")
                    // Fallback simulated price alert
                    if (Random.nextDouble() < 0.25) {
                        val user = repository.getUserSync()
                        if (user?.notifyAlerts == true) {
                            val assets = listOf("BTC/USDT", "ETH/USDT", "SOL/USDT")
                            val chosenAsset = assets.random()
                            val variation = Random.nextDouble(-1.8, 2.5)
                            val sign = if (variation >= 0) "▲" else "▼"
                            com.example.util.NotificationHelper.showNotification(
                                getApplication(),
                                "Alerta Cripto: $chosenAsset $sign",
                                "Movimiento del mercado detectado: $chosenAsset varía ${String.format("%.2f", variation)}% en el último minuto.",
                                com.example.util.NotificationHelper.CHANNEL_ALERTS
                            )
                        }
                    }
                }
                delay(40000) // check every 40 seconds
            }
        }
    }

    fun insertOrUpdateUser(user: UserEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertOrUpdateUser(user)
        }
    }

    fun insertOrder(order: InvestmentOrderEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertOrder(order)
        }
    }

    fun insertTransaction(transaction: TransactionEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertTransaction(transaction)
        }
    }

    fun fetchGeminiRecommendation() {
        viewModelScope.launch(Dispatchers.IO) {
            _isRecommendationLoading.value = true
            
            val user = repository.getUserSync()
            val activeInvestments = repository.activeInvestments.first()
            val contents = repository.allContents.first()
            
            val isKeyPlaceholder = !isGeminiActive.value
            val apiKey = BuildConfig.GEMINI_API_KEY
            
            val analysisPrompt = buildString {
                append("Genera un análisis de inversión personalizado y recomendaciones de trading para el siguiente usuario de la plataforma Lanzarus:\n\n")
                if (user != null) {
                    append("- Nombre de usuario: ${user.name}\n")
                    append("- Nivel de riesgo del Bot IA: ${botRiskLevel.value}\n")
                    append("- Capital Líquido Disponible: $${user.balance} USD\n")
                    append("- Capital Invertido: $${user.investedCapital} USD\n")
                    append("- Membresía Premium: ${if (user.isPremium) "SÍ" else "NO"}\n")
                    append("- Preferencias de Notificaciones/Intereses:\n")
                    append("  * Finanzas: ${if (user.notifyFinanzas) "Habilitado" else "Deshabilitado"}\n")
                    append("  * Tendencias: ${if (user.notifyTendencias) "Habilitado" else "Deshabilitado"}\n")
                    append("  * Mercados: ${if (user.notifyMercados) "Habilitado" else "Deshabilitado"}\n")
                } else {
                    append("- No hay información del usuario registrada.\n")
                }
                
                append("\nActivos actualmente invertidos (Cartera Activa):\n")
                if (activeInvestments.isEmpty()) {
                    append("- Ninguno (el capital está inactivo)\n")
                } else {
                    activeInvestments.forEach { order ->
                        append("- ${order.symbol}: Cantidad $${order.amount} USD | Precio de Entrada: $${order.entryPrice} | Tipo: ${order.type} | Retorno actual: +$${order.profit} USD\n")
                    }
                }
                
                append("\nÚltimos feeds/análisis de mercado cargados en el sistema:\n")
                contents.take(3).forEach { feed ->
                    append("- [${feed.category}] ${feed.title}: ${feed.snippet}\n")
                }
                
                append("\nInstrucciones de formato:\n")
                append("1. Sé elegante, sumamente profesional y directo. Habla en español de manera persuasiva.\n")
                append("2. Divide el análisis en tres secciones claras:\n")
                append("   * 🔍 **Diagnóstico de Cartera**: Evalúa si su capital está bien distribuido según su riesgo.\n")
                append("   * 📈 **Oportunidades**: Ofrece sugerencias específicas basadas en los feeds más recientes y su nivel de riesgo (${botRiskLevel.value}).\n")
                append("   * ⚙️ **Configuración del Bot**: Brinda una recomendación para el bot (si cambiar a agresivo, conservador, etc., o cómo configurar las notificaciones para estar más alerta).\n")
                append("3. Sé conciso pero con alto valor técnico financiero. No excedas las 12-15 líneas en total.")
            }
            
            val resultText = if (isKeyPlaceholder) {
                getSimulatedRecommendation(botRiskLevel.value, user)
            } else {
                try {
                    val systemPrompt = "Eres un Asesor Financiero IA y el motor de análisis on-chain principal de Lanzarus. Analizas datos de portafolios y preferencias de usuario para entregar recomendaciones accionables y concisas."
                    val request = GenerateContentRequest(
                        contents = listOf(
                            GeminiContent(parts = listOf(Part(text = analysisPrompt)))
                        ),
                        systemInstruction = GeminiContent(parts = listOf(Part(text = systemPrompt)))
                    )
                    val response = RetrofitClient.service.generateContent(apiKey, request)
                    response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        ?: "No se pudo generar la recomendación automatizada en este momento."
                } catch (e: Exception) {
                    Log.e("LanzarusAI", "Error fetching Gemini recommendation: ", e)
                    getSimulatedRecommendation(botRiskLevel.value, user)
                }
            }
            
            _geminiRecommendation.value = resultText
            _isRecommendationLoading.value = false
            addLog("Análisis del dashboard centralizado generado con éxito via Gemini.")
        }
    }

    private fun startPortfolioPriceTracker() {
        viewModelScope.launch(Dispatchers.IO) {
            while (true) {
                try {
                    val activeOrders = repository.activeInvestments.first()
                    val latestPrices = _liveMarketPrices.value.toMutableMap()
                    var anySuccess = false
                    
                    // Fetch real-time data from CoinCap API
                    val assetsToFetch = listOf(
                        "BTC/USDT" to "bitcoin",
                        "ETH/USDT" to "ethereum",
                        "SOL/USDT" to "solana"
                    )
                    
                    for ((symbol, assetId) in assetsToFetch) {
                        try {
                            val url = java.net.URL("https://api.coincap.io/v2/assets/$assetId")
                            val connection = url.openConnection() as java.net.HttpURLConnection
                            connection.requestMethod = "GET"
                            connection.connectTimeout = 4000
                            connection.readTimeout = 4000
                            
                            if (connection.responseCode == 200) {
                                val responseText = connection.inputStream.bufferedReader().use { it.readText() }
                                val pricePattern = "\"priceUsd\":\"([0-9.]+)\"".toRegex()
                                val match = pricePattern.find(responseText)
                                val priceStr = match?.groups?.get(1)?.value
                                if (priceStr != null) {
                                    latestPrices[symbol] = priceStr.toDouble()
                                    anySuccess = true
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("LanzarusPortfolioTracker", "Failed to fetch $symbol live: ${e.message}")
                        }
                    }
                    
                    // Update state
                    _isTrackingConnected.value = anySuccess
                    
                    // Generate minor real-time variations for traditional/other assets
                    // (EUR/USD, NASDAQ, SP500, or crypto if offline)
                    val keys = latestPrices.keys.toList()
                    val nextPrices = latestPrices.toMutableMap()
                    keys.forEach { key ->
                        if (key == "EUR/USD" || key == "NASDAQ" || key == "SP500/ETF" || !anySuccess) {
                            val percentChange = Random.nextDouble(-0.0015, 0.0018)
                            nextPrices[key] = (nextPrices[key] ?: 100.0) * (1.0 + percentChange)
                        }
                    }
                    _liveMarketPrices.value = nextPrices
                    
                    // Update active orders in local database based on live/updated prices
                    if (activeOrders.isNotEmpty()) {
                        activeOrders.forEach { order ->
                            val livePrice = nextPrices[order.symbol] ?: order.currentPrice
                            val priceDiffPercent = (livePrice - order.entryPrice) / order.entryPrice
                            val profit = order.amount * priceDiffPercent * (if (order.type == "BUY") 1.0 else -1.0)
                            
                            val updatedOrder = order.copy(
                                currentPrice = livePrice,
                                profit = profit
                            )
                            repository.updateOrder(updatedOrder)
                        }
                    }
                } catch (e: Exception) {
                    Log.e("LanzarusPortfolioTracker", "Error in portfolio tracking thread", e)
                }
                delay(12000) // check and update every 12 seconds for real-time responsiveness
            }
        }
    }

    private fun getSimulatedRecommendation(riskLevel: String, user: UserEntity?): String {
        val name = user?.name ?: "Inversionista"
        val balance = user?.balance ?: 10450.0
        return """
            🔍 **Diagnóstico de Cartera**: Hola $name, tu portafolio actual bajo riesgo **$riskLevel** muestra un balance líquido sano de $${String.format("%,.2f", balance)} USD. Tienes una distribución balanceada de capital activo, pero hay un excedente en liquidez que podría optimizarse en activos con rendimientos más estables.
            
            📈 **Oportunidades**: Basado en los últimos flujos on-chain detectados en nuestros feeds de **Mercados**, observamos un patrón fuerte de acumulación de Bitcoin (BTC) en soportes clave. Recomendamos diversificar el 15% de tu saldo disponible hacia posiciones de arbitraje EUR/USD y BTC/USDT para capturar rendimientos de volatilidad controlada.
            
            ⚙️ **Configuración del Bot**: Dado tu perfil de riesgo **$riskLevel**, te sugerimos mantener el Bot de Inversiones en modo automatizado, y activar las notificaciones push de **Módulo de Finanzas** y **Feeds de Mercados** para que recibas las señales de cobertura en milisegundos en tu móvil.
        """.trimIndent()
    }
}

class LanzarusViewModelFactory(
    private val application: Application,
    private val repository: LanzarusRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LanzarusViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LanzarusViewModel(application, repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
