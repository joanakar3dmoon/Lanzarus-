package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.*
import com.example.ui.theme.*
import com.example.ui.viewmodel.LanzarusViewModel
import com.example.util.AdsManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: LanzarusViewModel) {
    val user by viewModel.userState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactionsState.collectAsStateWithLifecycle()
    val activeInvestments by viewModel.activeInvestmentsState.collectAsStateWithLifecycle()

    val marketPrices by viewModel.liveMarketPrices.collectAsStateWithLifecycle()
    val isAnalysisLoading by viewModel.isRecommendationLoading.collectAsStateWithLifecycle()
    val analysisResult by viewModel.geminiRecommendation.collectAsStateWithLifecycle()
    val systemLogs by viewModel.systemLogs.collectAsStateWithLifecycle()
    val notification by viewModel.notification.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(0) }
    var showExplanationDialog by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    var previousTab by remember { mutableIntStateOf(currentTab) }

    // Interstitial al cambiar pestaña
    LaunchedEffect(currentTab) {
        if (previousTab != currentTab) {
            AdsManager.loadInterstitial(context)
            delay(500)
            if (context is androidx.activity.ComponentActivity) {
                AdsManager.showInterstitial(context)
            }
        }
        previousTab = currentTab
    }

    // Notificaciones
    LaunchedEffect(notification) {
        notification?.let {
            // Show snackbar
            delay(2000)
            viewModel.clearNotification()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Lanzarus", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text("Asistente de Inversión IA",
                            fontSize = 12.sp, color = LanzarusPrimaryTeal)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = LanzarusDarkBg,
                    titleContentColor = LanzarusTextPrimary
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = LanzarusCardBg) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.TrendingUp, "Mercado") },
                    label = { Text("Mercado", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LanzarusPrimaryTeal,
                        selectedTextColor = LanzarusPrimaryTeal,
                        indicatorColor = LanzarusPrimaryTeal.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Psychology, "Análisis") },
                    label = { Text("Análisis IA", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LanzarusPrimaryTeal,
                        selectedTextColor = LanzarusPrimaryTeal,
                        indicatorColor = LanzarusPrimaryTeal.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.AccountBalanceWallet, "Cartera") },
                    label = { Text("Mi Cartera", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LanzarusPrimaryTeal,
                        selectedTextColor = LanzarusPrimaryTeal,
                        indicatorColor = LanzarusPrimaryTeal.copy(alpha = 0.15f)
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Star, "Premium") },
                    label = { Text("Premium", fontSize = 11.sp) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = LanzarusAccentGold,
                        selectedTextColor = LanzarusAccentGold,
                        indicatorColor = LanzarusAccentGold.copy(alpha = 0.15f)
                    )
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            when (currentTab) {
                0 -> MarketTab(marketPrices = marketPrices, viewModel = viewModel)
                1 -> AnalysisTab(
                    isAnalysisLoading = isAnalysisLoading,
                    analysisResult = analysisResult,
                    systemLogs = systemLogs,
                    viewModel = viewModel,
                    marketPrices = marketPrices,
                    onShowExplanation = { showExplanationDialog = it }
                )
                2 -> PortfolioTab(
                    user = user,
                    activeInvestments = activeInvestments,
                    viewModel = viewModel
                )
                3 -> PremiumTab()
            }

            // Banner Ad al pie
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(LanzarusCardBg)
            ) {
                AndroidView(
                    factory = { ctx ->
                        AdView(ctx).apply {
                            adUnitId = "ca-app-pub-4903263409458961/8928635042"
                            adSize = AdSize.BANNER
                            loadAd(AdRequest.Builder().build())
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }

    // Dialog de explicación
    showExplanationDialog?.let { text ->
        AlertDialog(
            onDismissRequest = { showExplanationDialog = null },
            title = { Text("Explicación del Análisis", color = LanzarusPrimaryTeal) },
            text = { Text(text, color = LanzarusTextSecondary) },
            confirmButton = {
                TextButton(onClick = { showExplanationDialog = null }) {
                    Text("Entendido", color = LanzarusPrimaryTeal)
                }
            },
            containerColor = LanzarusCardBg
        )
    }
}

// ========================
// TAB 1: MERCADO EN VIVO
// ========================
@Composable
fun MarketTab(marketPrices: Map<String, Double>, viewModel: LanzarusViewModel) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LanzarusDarkBg)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("Mercado en Vivo", fontWeight = FontWeight.Bold, fontSize = 18.sp,
                color = LanzarusTextPrimary)
            Text("Precios reales de criptomonedas · Fuente: CoinGecko",
                fontSize = 12.sp, color = LanzarusTextMuted)
            Spacer(Modifier.height(8.dp))
        }

        if (marketPrices.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = LanzarusCardBg)
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = LanzarusPrimaryTeal, modifier = Modifier.size(32.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Cargando precios...", color = LanzarusTextMuted, fontSize = 14.sp)
                    }
                }
            }
        }

        val sortedPrices = marketPrices.entries.sortedBy { it.key }
        items(sortedPrices) { (symbol, price) ->
            CryptoPriceCard(symbol, price)
        }

        item {
            Spacer(Modifier.height(16.dp))
            Text("⚠️ Los precios son informativos. No constituyen asesoría financiera.",
                fontSize = 11.sp, color = LanzarusTextMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun CryptoPriceCard(symbol: String, price: Double) {
    val name = symbol.replace("/USDT", "")
    val color = when {
        price > 0 -> LanzarusPrimaryTeal
        else -> LanzarusTextMuted
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = LanzarusCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(name, fontWeight = FontWeight.Bold, fontSize = 16.sp,
                    color = LanzarusTextPrimary)
                Text(symbol, fontSize = 12.sp, color = LanzarusTextMuted)
            }
            Text("$${"%,.2f".format(price)} USD",
                fontWeight = FontWeight.Bold, fontSize = 18.sp, color = color)
        }
    }
}

// ========================
// TAB 2: ANÁLISIS IA
// ========================
@Composable
fun AnalysisTab(
    isAnalysisLoading: Boolean,
    analysisResult: String?,
    systemLogs: List<String>,
    viewModel: LanzarusViewModel,
    marketPrices: Map<String, Double>,
    onShowExplanation: (String) -> Unit
) {
    var selectedSymbol by remember { mutableStateOf("BTC/USDT") }
    val symbols = listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "ADA/USDT", "XRP/USDT")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LanzarusDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Análisis de Mercado", fontWeight = FontWeight.Bold, fontSize = 18.sp,
            color = LanzarusTextPrimary)
        Text("La IA analiza datos reales y te da una recomendación. Tú decides.",
            fontSize = 12.sp, color = LanzarusTextMuted)
        Spacer(Modifier.height(12.dp))

        // Selector de activo
        Text("Selecciona activo:", color = LanzarusTextSecondary, fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            symbols.forEach { sym ->
                FilterChip(
                    selected = selectedSymbol == sym,
                    onClick = { selectedSymbol = sym },
                    label = { Text(sym.replace("/USDT",""), fontSize = 12.sp) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = LanzarusPrimaryTeal.copy(alpha = 0.2f),
                        selectedLabelColor = LanzarusPrimaryTeal
                    )
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Precio actual del activo seleccionado
        val currentPrice = marketPrices[selectedSymbol]
        if (currentPrice != null) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LanzarusCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Precio actual", color = LanzarusTextMuted, fontSize = 14.sp)
                    Text("$${"%,.2f".format(currentPrice)}",
                        fontWeight = FontWeight.Bold, fontSize = 20.sp, color = LanzarusPrimaryTeal)
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        // Botón de análisis
        Button(
            onClick = { viewModel.runAnalysis() },
            enabled = !isAnalysisLoading && marketPrices.isNotEmpty(),
            colors = ButtonDefaults.buttonColors(containerColor = LanzarusPrimaryTeal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            if (isAnalysisLoading) {
                CircularProgressIndicator(
                    color = Color.White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp
                )
                Spacer(Modifier.width(8.dp))
            }
            Text(if (isAnalysisLoading) "Analizando mercado..." else "🔍 Analizar con IA",
                fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.height(16.dp))

        // Resultado del análisis
        if (isAnalysisLoading) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LanzarusCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = LanzarusPrimaryTeal)
                    Spacer(Modifier.height(8.dp))
                    Text("La IA está analizando datos de mercado...",
                        color = LanzarusTextMuted, fontSize = 13.sp)
                    Text("RSI, medias móviles, tendencias y volatilidad",
                        color = LanzarusTextMuted.copy(alpha = 0.6f), fontSize = 11.sp)
                }
            }
        }

        analysisResult?.let { result ->
            Card(
                colors = CardDefaults.cardColors(containerColor = LanzarusCardBg),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("📊 Análisis de la IA", fontWeight = FontWeight.Bold,
                        color = LanzarusPrimaryTeal, fontSize = 16.sp)
                    Spacer(Modifier.height(8.dp))
                    Text(result, color = LanzarusTextSecondary, fontSize = 13.sp)

                    Spacer(Modifier.height(12.dp))
                    // Botón para explicación detallada
                    OutlinedButton(
                        onClick = { onShowExplanation(result) },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = LanzarusPrimaryTeal),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("📖 Explicar indicadores (riesgos incluidos)")
                    }

                    Spacer(Modifier.height(8.dp))
                    Text("⚠️ Esta es una recomendación basada en análisis técnico. No es asesoría financiera. Invertir conlleva riesgos.",
                        fontSize = 10.sp, color = LanzarusTextMuted)
                }
            }
        }

        // Logs del sistema
        if (systemLogs.isNotEmpty()) {
            Spacer(Modifier.height(16.dp))
            Text("Registro de análisis:", color = LanzarusTextMuted, fontSize = 11.sp)
            Spacer(Modifier.height(4.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = LanzarusCardBg.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(Modifier.padding(12.dp)) {
                    systemLogs.takeLast(5).forEach { log ->
                        Text(log, color = LanzarusTextMuted, fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace)
                    }
                }
            }
        }
    }
}

// ========================
// TAB 3: MI CARTERA
// ========================
@Composable
fun PortfolioTab(
    user: UserEntity?,
    activeInvestments: List<InvestmentOrderEntity>,
    viewModel: LanzarusViewModel
) {
    val context = LocalContext.current
    var showAddDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LanzarusDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Mi Cartera", fontWeight = FontWeight.Bold, fontSize = 18.sp,
            color = LanzarusTextPrimary)
        Text("Registra tus inversiones manualmente y haz seguimiento",
            fontSize = 12.sp, color = LanzarusTextMuted)
        Spacer(Modifier.height(16.dp))

        // Resumen
        Card(
            colors = CardDefaults.cardColors(containerColor = LanzarusCardBg),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Capital total invertido", color = LanzarusTextMuted, fontSize = 13.sp)
                Text("$${"%,.2f".format(activeInvestments.sumOf { it.amount })}",
                    fontWeight = FontWeight.Bold, fontSize = 24.sp, color = LanzarusPrimaryTeal)
                Spacer(Modifier.height(8.dp))
                Text("Activos en cartera: ${activeInvestments.size}",
                    color = LanzarusTextSecondary, fontSize = 14.sp)
            }
        }

        Spacer(Modifier.height(16.dp))

        // Inversiones activas
        if (activeInvestments.isEmpty()) {
            Card(
                colors = CardDefaults.cardColors(containerColor = LanzarusCardBg.copy(alpha = 0.7f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.AccountBalanceWallet, contentDescription = null,
                        tint = LanzarusTextMuted, modifier = Modifier.size(48.dp))
                    Spacer(Modifier.height(8.dp))
                    Text("Aún no tienes inversiones", color = LanzarusTextMuted, fontSize = 14.sp)
                    Text("Registra tu primera inversión manualmente",
                        color = LanzarusTextMuted.copy(alpha = 0.6f), fontSize = 12.sp)
                }
            }
        } else {
            activeInvestments.forEach { inv ->
                InvestmentCard(inv)
                Spacer(Modifier.height(8.dp))
            }
        }

        Spacer(Modifier.height(16.dp))

        // Botón añadir inversión
        Button(
            onClick = { showAddDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = LanzarusPrimaryTeal),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.Add, "Añadir")
            Spacer(Modifier.width(8.dp))
            Text("Registrar Inversión")
        }

        Spacer(Modifier.height(8.dp))

        // Historial de transacciones
        Text("Historial", fontWeight = FontWeight.Bold, fontSize = 16.sp,
            color = LanzarusTextPrimary)
        Spacer(Modifier.height(8.dp))

        Text("Próximamente: historial completo de operaciones",
            color = LanzarusTextMuted, fontSize = 12.sp)
    }

    // Dialog para añadir inversión
    if (showAddDialog) {
        AddInvestmentDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { symbol, amount, price ->
                viewModel.addManualInvestment(symbol, amount, price)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun InvestmentCard(investment: InvestmentOrderEntity) {
    val profit = (investment.currentPrice - investment.entryPrice) * (investment.amount / investment.entryPrice)
    val profitPercent = if (investment.entryPrice > 0)
        ((investment.currentPrice - investment.entryPrice) / investment.entryPrice) * 100 else 0.0
    val isProfitable = profit >= 0

    Card(
        colors = CardDefaults.cardColors(containerColor = LanzarusCardBg),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(investment.symbol.replace("/USDT",""),
                    fontWeight = FontWeight.Bold, color = LanzarusTextPrimary, fontSize = 16.sp)
                Text("Invertido: $${"%.2f".format(investment.amount)}",
                    color = LanzarusTextMuted, fontSize = 12.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "${if (isProfitable) "+" else ""}$${"%.2f".format(profit)}",
                    color = if (isProfitable) LanzarusSuccessGreen else LanzarusErrorRed,
                    fontWeight = FontWeight.Bold, fontSize = 16.sp
                )
                Text(
                    "${if (isProfitable) "+" else ""}${"%.2f".format(profitPercent)}%",
                    color = if (isProfitable) LanzarusSuccessGreen else LanzarusErrorRed,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun AddInvestmentDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double, Double) -> Unit
) {
    var symbol by remember { mutableStateOf("BTC/USDT") }
    var amount by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    val symbols = listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "ADA/USDT", "XRP/USDT")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Registrar Inversión", color = LanzarusPrimaryTeal) },
        text = {
            Column {
                Text("Selecciona activo:", color = LanzarusTextSecondary, fontSize = 13.sp)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    symbols.forEach { sym ->
                        FilterChip(
                            selected = symbol == sym,
                            onClick = { symbol = sym },
                            label = { Text(sym.replace("/USDT",""), fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = LanzarusPrimaryTeal.copy(alpha = 0.2f),
                                selectedLabelColor = LanzarusPrimaryTeal
                            )
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = amount, onValueChange = { amount = it },
                    label = { Text("Cantidad invertida (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LanzarusPrimaryTeal,
                        focusedLabelColor = LanzarusPrimaryTeal,
                        unfocusedTextColor = LanzarusTextPrimary,
                        focusedTextColor = LanzarusTextPrimary
                    )
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = price, onValueChange = { price = it },
                    label = { Text("Precio de entrada (USD)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LanzarusPrimaryTeal,
                        focusedLabelColor = LanzarusPrimaryTeal,
                        unfocusedTextColor = LanzarusTextPrimary,
                        focusedTextColor = LanzarusTextPrimary
                    )
                )
                error?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(it, color = LanzarusErrorRed, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amt = amount.toDoubleOrNull()
                    val prc = price.toDoubleOrNull()
                    if (amt == null || amt <= 0) error = "Cantidad inválida"
                    else if (prc == null || prc <= 0) error = "Precio inválido"
                    else onConfirm(symbol, amt, prc)
                },
                colors = ButtonDefaults.buttonColors(containerColor = LanzarusPrimaryTeal)
            ) { Text("Registrar") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar", color = LanzarusTextMuted) }
        },
        containerColor = LanzarusCardBg
    )
}

// ========================
// TAB 4: PREMIUM
// ========================
@Composable
fun PremiumTab() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(LanzarusDarkBg)
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        Icon(Icons.Default.Star, contentDescription = null,
            tint = LanzarusAccentGold, modifier = Modifier.size(64.dp))

        Spacer(Modifier.height(16.dp))

        Text("Lanzarus Premium", fontWeight = FontWeight.Bold, fontSize = 22.sp,
            color = LanzarusAccentGold)

        Spacer(Modifier.height(8.dp))
        Text("Desbloquea el poder completo de la IA",
            color = LanzarusTextMuted, fontSize = 14.sp)

        Spacer(Modifier.height(24.dp))

        PremiumFeatureCard("📈", "Análisis Avanzado", "Indicadores técnicos en tiempo real (RSI, MACD, Bollinger Bands)")
        PremiumFeatureCard("🔔", "Alertas de Mercado", "Notificaciones cuando haya oportunidades de inversión")
        PremiumFeatureCard("📊", "Gráficos Interactivos", "Visualiza tendencias y toma mejores decisiones")
        PremiumFeatureCard("🤖", "Análisis Multi-activo", "Analiza hasta 20 activos simultáneamente")
        PremiumFeatureCard("📋", "Informes Semanales", "Resumen de mercado y oportunidades enviado a tu email")
        PremiumFeatureCard("❌", "Sin Anuncios", "Experiencia limpia y sin interrupciones")

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { /* TODO: Google Play Billing */ },
            colors = ButtonDefaults.buttonColors(containerColor = LanzarusAccentGold),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("🪙 Premium — Próximamente", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        Spacer(Modifier.height(12.dp))
        Text("Soporta el desarrollo de Lanzarus y obtén herramientas exclusivas",
            color = LanzarusTextMuted, fontSize = 11.sp, textAlign = TextAlign.Center)
    }
}

@Composable
fun PremiumFeatureCard(emoji: String, title: String, description: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = LanzarusCardBg),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, color = LanzarusTextPrimary, fontSize = 14.sp)
                Text(description, color = LanzarusTextMuted, fontSize = 11.sp)
            }
        }
    }
}