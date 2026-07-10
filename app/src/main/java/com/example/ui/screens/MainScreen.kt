package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.ContentEntity
import com.example.data.model.InvestmentOrderEntity
import com.example.data.model.TransactionEntity
import com.example.data.model.UserEntity
import com.example.ui.theme.*
import com.example.ui.viewmodel.LanzarusViewModel
import com.example.util.AdsManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import kotlin.random.Random

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: LanzarusViewModel) {
    val user by viewModel.userState.collectAsStateWithLifecycle()
    val transactions by viewModel.transactionsState.collectAsStateWithLifecycle()
    val contents by viewModel.contentsState.collectAsStateWithLifecycle()
    val activeInvestments by viewModel.activeInvestmentsState.collectAsStateWithLifecycle()
    val allInvestments by viewModel.allInvestmentsState.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessagesState.collectAsStateWithLifecycle()

    val isBotActive by viewModel.isBotActive.collectAsStateWithLifecycle()
    val botRiskLevel by viewModel.botRiskLevel.collectAsStateWithLifecycle()
    val isChatLoading by viewModel.isChatLoading.collectAsStateWithLifecycle()
    val systemLogs by viewModel.systemLogs.collectAsStateWithLifecycle()
    val notification by viewModel.notification.collectAsStateWithLifecycle()

    var currentTab by remember { mutableStateOf(0) }
    var selectedArticleForDialog by remember { mutableStateOf<ContentEntity?>(null) }
    var showResetDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var previousTab by remember { mutableIntStateOf(currentTab) }

    // Mostrar anuncio intersticial al cambiar de pestaña
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

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 0.dp,
                modifier = Modifier
                    .windowInsetsPadding(WindowInsets.navigationBars)
                    .border(1.dp, LanzarusBorderColor)
                    .height(80.dp)
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Dashboard", modifier = Modifier.testTag("nav_dashboard")) },
                    label = { Text("Panel", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = LanzarusPrimaryTeal,
                        indicatorColor = LanzarusPrimaryTeal,
                        unselectedIconColor = LanzarusTextSecondary,
                        unselectedTextColor = LanzarusTextSecondary
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = { Icon(Icons.Default.Star, contentDescription = "Bot & Trade", modifier = Modifier.testTag("nav_bot")) },
                    label = { Text("Bot IA", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = LanzarusPrimaryTeal,
                        indicatorColor = LanzarusPrimaryTeal,
                        unselectedIconColor = LanzarusTextSecondary,
                        unselectedTextColor = LanzarusTextSecondary
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = { Icon(Icons.Default.List, contentDescription = "Contenidos", modifier = Modifier.testTag("nav_feeds")) },
                    label = { Text("Feeds", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = LanzarusPrimaryTeal,
                        indicatorColor = LanzarusPrimaryTeal,
                        unselectedIconColor = LanzarusTextSecondary,
                        unselectedTextColor = LanzarusTextSecondary
                    )
                )
                NavigationBarItem(
                    selected = currentTab == 3,
                    onClick = { currentTab = 3 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Billetera", modifier = Modifier.testTag("nav_finance")) },
                    label = { Text("Finanzas", fontSize = 10.sp, fontWeight = FontWeight.Bold) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = LanzarusPrimaryTeal,
                        indicatorColor = LanzarusPrimaryTeal,
                        unselectedIconColor = LanzarusTextSecondary,
                        unselectedTextColor = LanzarusTextSecondary
                    )
                )
            }
        },
        containerColor = LanzarusDarkBg
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Main Content Tab Display
            when (currentTab) {
                0 -> DashboardTab(
                    user = user,
                    isBotActive = isBotActive,
                    botRiskLevel = botRiskLevel,
                    systemLogs = systemLogs,
                    viewModel = viewModel,
                    contents = contents,
                    onResetRequest = { showResetDialog = true }
                )
                1 -> BotTab(
                    chatMessages = chatMessages,
                    isChatLoading = isChatLoading,
                    activeInvestments = activeInvestments,
                    viewModel = viewModel
                )
                2 -> FeedTab(
                    contents = contents,
                    user = user,
                    viewModel = viewModel,
                    onArticleClick = { selectedArticleForDialog = it }
                )
                3 -> FinanceTab(
                    user = user,
                    transactions = transactions,
                    viewModel = viewModel
                )
            }

            // Top Alert Notification Overlay
            AnimatedVisibility(
                visible = notification != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            ) {
                notification?.let { text ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LanzarusCardBg),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                        border = BorderStroke(1.dp, LanzarusPrimaryTeal.copy(alpha = 0.3f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.clearNotification() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(LanzarusSuccessGreen)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = text,
                                color = LanzarusTextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Close Alert",
                                tint = LanzarusSuccessGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Banner publicitario al pie
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        com.google.android.gms.ads.AdView(ctx).apply {
                            adSize = com.google.android.gms.ads.AdSize.SMART_BANNER
                            adUnitId = AdsManager.bannerAdId
                            loadAd(com.google.android.gms.ads.AdRequest.Builder().build())
                        }
                    }
                )
            }

            // Paywall & Article Details Dialog
            if (selectedArticleForDialog != null) {
                val article = selectedArticleForDialog!!
                val userIsPremium = user?.isPremium == true
                val canRead = !article.isPremiumOnly || userIsPremium

                Dialog(onDismissRequest = { selectedArticleForDialog = null }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LanzarusCardBg),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, LanzarusBorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState())
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = article.category.uppercase(),
                                    color = if (article.isPremiumOnly) LanzarusAccentGold else LanzarusPrimaryTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(
                                            (if (article.isPremiumOnly) LanzarusAccentGold else LanzarusPrimaryTeal).copy(alpha = 0.15f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                                if (article.isPremiumOnly) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = "Premium lock icon",
                                            tint = LanzarusAccentGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            "PREMIUM",
                                            color = LanzarusAccentGold,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = article.title,
                                color = LanzarusTextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            HorizontalDivider(color = LanzarusBorderColor)
                            Spacer(modifier = Modifier.height(16.dp))

                            if (canRead) {
                                Text(
                                    text = article.fullText,
                                    color = LanzarusTextPrimary.copy(alpha = 0.9f),
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { selectedArticleForDialog = null },
                                    colors = ButtonDefaults.buttonColors(containerColor = LanzarusPrimaryTeal),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Entendido", color = Color.White, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                // Locked state content blur and payment options
                                Text(
                                    text = article.snippet + "...",
                                    color = LanzarusTextPrimary.copy(alpha = 0.8f),
                                    fontSize = 14.sp,
                                    lineHeight = 22.sp
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.03f), RoundedCornerShape(16.dp))
                                        .border(BorderStroke(1.dp, LanzarusAccentGold.copy(alpha = 0.4f)), RoundedCornerShape(16.dp))
                                        .padding(16.dp)
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = "Lock Icon big",
                                            tint = LanzarusAccentGold,
                                            modifier = Modifier.size(36.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            "Contenido Exclusivo Premium",
                                            color = LanzarusTextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            "Desbloquea análisis avanzados on-chain, señales de trading automáticas de Lanzarus AI y reportes del bot.",
                                            color = LanzarusTextSecondary,
                                            fontSize = 12.sp,
                                            textAlign = TextAlign.Center,
                                            lineHeight = 16.sp
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Button(
                                            onClick = {
                                                viewModel.purchasePremium()
                                                selectedArticleForDialog = null
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = LanzarusAccentGold),
                                            shape = RoundedCornerShape(12.dp),
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("Activar Membresía ($29.99 USD)", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        TextButton(
                                            onClick = {
                                                viewModel.depositFunds(100.0)
                                                viewModel.showNotification("Se añadieron $100 Demo para pruebas")
                                            }
                                        ) {
                                            Text("Obtener Fondos Demo gratis", color = LanzarusPrimaryTeal, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Reset System Confirmation Dialog
            if (showResetDialog) {
                AlertDialog(
                    onDismissRequest = { showResetDialog = false },
                    title = { Text("¿Reiniciar Sistema Lanzarus?", color = LanzarusTextPrimary, fontWeight = FontWeight.Bold) },
                    text = { Text("Esta acción restablecerá el balance líquido, vaciará el historial de transacciones y chat, y restaurará el sistema a su estado de fábrica.", color = LanzarusTextSecondary) },
                    confirmButton = {
                        Button(
                            onClick = {
                                viewModel.resetApp()
                                showResetDialog = false
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LanzarusErrorRed)
                        ) {
                            Text("Confirmar Reinicio", color = Color.White)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showResetDialog = false }) {
                            Text("Cancelar", color = LanzarusTextSecondary)
                        }
                    },
                    containerColor = LanzarusCardBg
                )
            }
        }
    }
}

// =========================================================================
// TAB 1: DASHBOARD
// =========================================================================
@Composable
fun DashboardTab(
    user: UserEntity?,
    isBotActive: Boolean,
    botRiskLevel: String,
    systemLogs: List<String>,
    viewModel: LanzarusViewModel,
    contents: List<ContentEntity>,
    onResetRequest: () -> Unit
) {
    val geminiRecommendation by viewModel.geminiRecommendation.collectAsStateWithLifecycle()
    val isRecommendationLoading by viewModel.isRecommendationLoading.collectAsStateWithLifecycle()
    val isGeminiActive by viewModel.isGeminiActive.collectAsStateWithLifecycle()
    val liveMarketPrices by viewModel.liveMarketPrices.collectAsStateWithLifecycle()
    val isTrackingConnected by viewModel.isTrackingConnected.collectAsStateWithLifecycle()
    val activeInvestments by viewModel.activeInvestmentsState.collectAsStateWithLifecycle()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            // Professional Polish Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "SYSTEM ACTIVE",
                        color = LanzarusPrimaryTeal,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Text(
                        text = "LANZARUS",
                        color = LanzarusTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Profile dot indicator
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White)
                            .border(1.dp, LanzarusBorderColor, CircleShape)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(LanzarusSuccessGreen)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onResetRequest, modifier = Modifier.testTag("reset_button")) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reinicio de fábrica",
                            tint = LanzarusTextSecondary
                        )
                    }
                }
            }
        }

        // Total Portfolio Card (Operational Capital)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, LanzarusBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "OPERATIONAL CAPITAL",
                            color = LanzarusTextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            letterSpacing = 1.sp
                        )
                        Box(
                            modifier = Modifier
                                .background(Color(0xFFECFDF5), RoundedCornerShape(100.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "+12.4%",
                                color = LanzarusSuccessGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    // Stylized Fractional Decimals
                    val balanceVal = user?.balance ?: 5000.00
                    val wholePart = balanceVal.toInt()
                    val fractionalPart = String.format("%02d", ((balanceVal - wholePart) * 100).toInt().coerceIn(0, 99))
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$$wholePart.",
                            color = LanzarusTextPrimary,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Light,
                            letterSpacing = (-1).sp
                        )
                        Text(
                            text = fractionalPart,
                            color = LanzarusTextMuted,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Light,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Multi-colored segment bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(100.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(2f)
                                .fillMaxHeight()
                                .background(LanzarusPrimaryTeal)
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(LanzarusSuccessGreen)
                        )
                    }
                }
            }
        }

        // Mini Stats Cards
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    title = "Capital Invertido",
                    value = "$${String.format("%,.2f", user?.investedCapital ?: 0.0)}",
                    icon = Icons.Default.Star,
                    color = LanzarusSecondaryCyan,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    title = "Retorno Estimado",
                    value = "+$${String.format("%.2f", user?.monthlyEarnings ?: 0.0)}",
                    icon = Icons.Default.Notifications,
                    color = LanzarusSuccessGreen,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Real-time Portfolio Tracker Component
        item {
            PortfolioTrackingCard(
                activeInvestments = activeInvestments,
                liveMarketPrices = liveMarketPrices,
                isConnected = isTrackingConnected,
                viewModel = viewModel
            )
        }

        // Active Bot Section (High Contrast Dark Card)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = LanzarusDarkContainerBg),
                shape = RoundedCornerShape(32.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(LanzarusSecondaryCyan)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI Investment Engine",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Box(
                            modifier = Modifier
                                .background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(100.dp))
                                .clickable { viewModel.toggleBotActive() }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isBotActive) "ACTIVE" else "OFFLINE",
                                color = if (isBotActive) LanzarusSuccessGreen else Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Market scanning cosmetic indicators
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Box(modifier = Modifier.width(4.dp).height(20.dp).clip(RoundedCornerShape(100.dp)).background(LanzarusSecondaryCyan))
                        Box(modifier = Modifier.width(4.dp).height(32.dp).clip(RoundedCornerShape(100.dp)).background(LanzarusPrimaryTeal))
                        Box(modifier = Modifier.width(4.dp).height(12.dp).clip(RoundedCornerShape(100.dp)).background(Color.White.copy(alpha = 0.2f)))
                        Box(modifier = Modifier.width(4.dp).height(24.dp).clip(RoundedCornerShape(100.dp)).background(LanzarusSecondaryCyan))
                        Box(modifier = Modifier.width(4.dp).height(16.dp).clip(RoundedCornerShape(100.dp)).background(LanzarusPrimaryTeal))

                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Scanning Markets...",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 11.sp
                            )
                            Text(
                                text = "Volatility: Low",
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Custom Switch/Options Risk Selection
                    Text(
                        text = "Riesgo de Inversión",
                        color = Color.White.copy(alpha = 0.6f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val riskOptions = listOf("CONSERVADOR", "MODERADO", "AGRESIVO")
                        riskOptions.forEach { level ->
                            val isSelected = botRiskLevel == level
                            val activeColor = when (level) {
                                "CONSERVADOR" -> LanzarusSuccessGreen
                                "AGRESIVO" -> LanzarusErrorRed
                                else -> LanzarusPrimaryTeal
                            }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) activeColor.copy(alpha = 0.2f)
                                        else Color.White.copy(alpha = 0.03f)
                                    )
                                    .border(
                                        BorderStroke(
                                            width = 1.dp,
                                            color = if (isSelected) activeColor else Color.White.copy(alpha = 0.1f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { viewModel.changeRiskLevel(level) }
                                    .padding(vertical = 10.dp)
                            ) {
                                Text(
                                    text = level,
                                    color = if (isSelected) activeColor else Color.White.copy(alpha = 0.8f),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Action buttons
                    Button(
                        onClick = { viewModel.triggerForcedRebalance() },
                        colors = ButtonDefaults.buttonColors(containerColor = LanzarusPrimaryTeal),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            "OPTIMIZE YIELD",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.5.sp
                        )
                    }
                }
            }
        }

        // --- Centralized Gemini AI Dashboard recommendation card ---
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = LanzarusDarkContainerBg),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, LanzarusPrimaryTeal.copy(alpha = 0.4f)),
                modifier = Modifier.fillMaxWidth().testTag("gemini_recs_card")
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "AI central analyzer",
                                tint = LanzarusPrimaryTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "ANÁLISIS COGNITIVO GEMINI IA",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.5.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .background(
                                        if (isGeminiActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .border(
                                        BorderStroke(0.5.dp, if (isGeminiActive) Color(0xFF10B981) else Color(0xFFF59E0B)),
                                        RoundedCornerShape(6.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isGeminiActive) "ACTIVO" else "SIMULADO",
                                    color = if (isGeminiActive) Color(0xFF34D399) else Color(0xFFFBBF24),
                                    fontSize = 8.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        IconButton(
                            onClick = { viewModel.fetchGeminiRecommendation() },
                            enabled = !isRecommendationLoading,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Regenerar análisis",
                                tint = if (isRecommendationLoading) LanzarusTextMuted else LanzarusPrimaryTeal,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isRecommendationLoading) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                color = LanzarusPrimaryTeal,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Lanzando algoritmos de Gemini...\nAnalizando tu perfil de inversión",
                                color = Color.White.copy(alpha = 0.7f),
                                fontSize = 11.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 14.sp
                            )
                        }
                    } else {
                        val textToShow = geminiRecommendation ?: "Pulsa el botón de actualizar arriba para iniciar el análisis en tiempo real de tu portafolio."
                        
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            val paragraphs = textToShow.split("\n\n")
                            paragraphs.forEach { paragraph ->
                                if (paragraph.isNotBlank()) {
                                    val trimmed = paragraph.trim()
                                    if (trimmed.startsWith("🔍") || trimmed.startsWith("📈") || trimmed.startsWith("⚙️")) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Color.White.copy(alpha = 0.04f))
                                                .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.05f)), RoundedCornerShape(16.dp))
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = trimmed,
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                lineHeight = 15.sp,
                                                fontWeight = FontWeight.Normal
                                            )
                                        }
                                    } else {
                                        Text(
                                            text = trimmed,
                                            color = Color.White.copy(alpha = 0.85f),
                                            fontSize = 11.sp,
                                            lineHeight = 15.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Latest Content Pulse Stream (Content stream)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(32.dp),
                border = BorderStroke(1.dp, LanzarusBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "LATEST CONTENT PULSE",
                        color = LanzarusTextMuted,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Predefined aesthetic streams or real Room news records
                    val displayedFeed = contents.take(3)
                    if (displayedFeed.isEmpty()) {
                        CircularProgressIndicator(color = LanzarusPrimaryTeal, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else {
                        displayedFeed.forEachIndexed { idx, article ->
                            val emoji = when (article.category.lowercase()) {
                                "finanzas" -> "💳"
                                "mercados" -> "📈"
                                else -> "🤖"
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .border(1.dp, LanzarusBorderColor, RoundedCornerShape(12.dp))
                                ) {
                                    Text(emoji, fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = article.title,
                                        color = LanzarusTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = article.snippet,
                                        color = LanzarusTextSecondary,
                                        fontSize = 10.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "${idx * 6 + 2}m ago",
                                    color = LanzarusTextMuted,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            if (idx < displayedFeed.size - 1) {
                                HorizontalDivider(color = LanzarusBorderColor.copy(alpha = 0.5f), modifier = Modifier.padding(vertical = 4.dp))
                            }
                        }
                    }
                }
            }
        }

        // NOTIFICACIONES PUSH PERSONALIZADAS
        item {
            var hasNotificationPermission by remember {
                mutableStateOf(false)
            }
            val context = androidx.compose.ui.platform.LocalContext.current

            // Check permission state initially on Android 13+ (API 33+)
            LaunchedEffect(Unit) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                    hasNotificationPermission = androidx.core.content.ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ) == android.content.pm.PackageManager.PERMISSION_GRANTED
                } else {
                    hasNotificationPermission = true
                }
            }

            val permissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
                contract = androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                hasNotificationPermission = isGranted
                if (isGranted) {
                    viewModel.showNotification("Permiso de notificaciones concedido")
                } else {
                    viewModel.showNotification("Permiso denegado. Active las alertas en ajustes")
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = LanzarusCardBg),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, LanzarusBorderColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "NOTIFICACIONES PUSH REALES",
                                color = LanzarusPrimaryTeal,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.5.sp
                            )
                            Text(
                                text = "Alertas y Feeds personalizados",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Notifications active",
                            tint = if (hasNotificationPermission) LanzarusPrimaryTeal else LanzarusTextMuted,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!hasNotificationPermission && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                        Button(
                            onClick = {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LanzarusPrimaryTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                        ) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Activar",
                                tint = LanzarusDarkBg,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Habilitar Notificaciones del Sistema", color = LanzarusDarkBg, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                    }

                    Text(
                        text = "Selecciona qué categorías deseas que te notifiquen mediante push-notifications en tiempo real:",
                        color = LanzarusTextSecondary,
                        fontSize = 11.sp,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    // 1. Finanzas
                    NotificationPreferenceRow(
                        title = "Módulo de Finanzas",
                        subtitle = "Alertas de pasarelas, depósitos e inflow/outflow.",
                        checked = user?.notifyFinanzas == true,
                        onCheckedChange = { viewModel.updateNotificationSetting("finanzas", it) },
                        onTestClick = { viewModel.triggerTestNotification("finanzas") }
                    )

                    HorizontalDivider(color = LanzarusBorderColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    // 2. Tendencias
                    NotificationPreferenceRow(
                        title = "Tendencias de Mercado",
                        subtitle = "Informes del bot sobre RWA, macro y regulaciones.",
                        checked = user?.notifyTendencias == true,
                        onCheckedChange = { viewModel.updateNotificationSetting("tendencias", it) },
                        onTestClick = { viewModel.triggerTestNotification("tendencias") }
                    )

                    HorizontalDivider(color = LanzarusBorderColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    // 3. Mercados
                    NotificationPreferenceRow(
                        title = "Feeds de Mercados",
                        subtitle = "Análisis on-chain de ballenas, soportes y volatilidad.",
                        checked = user?.notifyMercados == true,
                        onCheckedChange = { viewModel.updateNotificationSetting("mercados", it) },
                        onTestClick = { viewModel.triggerTestNotification("mercados") }
                    )

                    HorizontalDivider(color = LanzarusBorderColor.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))

                    // 4. Alerts
                    NotificationPreferenceRow(
                        title = "Alertas de Inversión (Bots)",
                        subtitle = "Rebalanceos automáticos del Bot IA, fluctuaciones y API CoinCap.",
                        checked = user?.notifyAlerts == true,
                        onCheckedChange = { viewModel.updateNotificationSetting("alerts", it) },
                        onTestClick = { viewModel.triggerTestNotification("alerts") }
                    )
                }
            }
        }

        // Live Terminal Logs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF030712)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, Color(0xFF1F2937))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(LanzarusPrimaryTeal)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "TERMINAL LOG STREAM",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        if (systemLogs.isEmpty()) {
                            Text(
                                "Monitoreando sistema Lanzarus en espera de eventos...",
                                color = LanzarusTextMuted,
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.padding(4.dp)
                            )
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                items(systemLogs) { log ->
                                    Text(
                                        text = log,
                                        color = if (log.contains("Error", true)) LanzarusErrorRed else LanzarusPrimaryTeal,
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(vertical = 1.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// =========================================================================
// TAB 2: BOT & INVESTMENT PORTFOLIO
// =========================================================================
@Composable
fun BotTab(
    chatMessages: List<com.example.data.model.ChatMessageEntity>,
    isChatLoading: Boolean,
    activeInvestments: List<InvestmentOrderEntity>,
    viewModel: LanzarusViewModel
) {
    val isGeminiActive by viewModel.isGeminiActive.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    var userPrompt by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Auto scroll chat to bottom when messages list size changes
    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isNotEmpty()) {
            listState.animateScrollToItem(chatMessages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "Lanzarus AI Bot",
                        color = LanzarusPrimaryTeal,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .background(
                                if (isGeminiActive) Color(0xFF10B981).copy(alpha = 0.15f) else Color(0xFFF59E0B).copy(alpha = 0.15f),
                                RoundedCornerShape(6.dp)
                            )
                            .border(
                                BorderStroke(0.5.dp, if (isGeminiActive) Color(0xFF10B981) else Color(0xFFF59E0B)),
                                RoundedCornerShape(6.dp)
                            )
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isGeminiActive) "GEMINI ACTIVO" else "SIMULADOR",
                            color = if (isGeminiActive) Color(0xFF059669) else Color(0xFFD97706),
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Text(
                    "Asistente Autónomo y Cartera Activa",
                    color = LanzarusTextSecondary,
                    fontSize = 12.sp
                )
            }
            TextButton(
                onClick = { viewModel.clearChat() },
                colors = ButtonDefaults.textButtonColors(contentColor = LanzarusErrorRed)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Clear Chat", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Limpiar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Live Portfolio List Card
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, LanzarusBorderColor),
            modifier = Modifier.weight(0.9f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "PORTAFOLIO DE TRADING REAL",
                        color = LanzarusTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Box(
                        modifier = Modifier
                            .background(LanzarusPrimaryTeal.copy(alpha = 0.1f), RoundedCornerShape(6.dp))
                            .clickable {
                                val assets = listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "EUR/USD", "SP500/ETF", "NASDAQ")
                                val basePrices = mapOf("BTC/USDT" to 65000.0, "ETH/USDT" to 3400.0, "SOL/USDT" to 145.0, "EUR/USD" to 1.08, "SP500/ETF" to 540.0, "NASDAQ" to 490.0)
                                val chosenAsset = assets.random()
                                val startPrice = basePrices[chosenAsset] ?: 100.0
                                val size = Random.nextDouble(100.0, 1500.0)
                                viewModel.simulateOrder(
                                    symbol = chosenAsset,
                                    amount = size,
                                    entryPrice = startPrice,
                                    type = listOf("BUY", "SELL").random()
                                )
                            }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "+ Simular Orden",
                            color = LanzarusPrimaryTeal,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                
                if (activeInvestments.isEmpty()) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.02f), RoundedCornerShape(12.dp))
                    ) {
                        Text(
                            "Sin órdenes activas en cartera.\nUsa '+ Simular Orden' para colocar inversiones.",
                            color = LanzarusTextSecondary,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(activeInvestments) { order ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                                    .border(BorderStroke(1.dp, LanzarusBorderColor), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = order.symbol,
                                            color = LanzarusTextPrimary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = order.type,
                                            color = if (order.type == "BUY") LanzarusSuccessGreen else LanzarusErrorRed,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier
                                                .background(
                                                    (if (order.type == "BUY") LanzarusSuccessGreen else LanzarusErrorRed).copy(alpha = 0.12f),
                                                    RoundedCornerShape(3.dp)
                                                )
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Inversión: $${String.format("%.2f", order.amount)} | Entry: $${String.format("%.2f", order.entryPrice)}",
                                        color = LanzarusTextSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "$${String.format("%.2f", order.currentPrice)}",
                                        color = LanzarusTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    val isGain = order.profit >= 0
                                    Text(
                                        text = if (isGain) "+$${String.format("%.2f", order.profit)} USD" else "$${String.format("%.2f", order.profit)} USD",
                                        color = if (isGain) LanzarusSuccessGreen else LanzarusErrorRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Chat Conversation Card Area
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.dp, LanzarusBorderColor),
            modifier = Modifier.weight(1.1f)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Box(modifier = Modifier.weight(1f)) {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(chatMessages) { msg ->
                            val isUser = msg.sender == "USER"
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
                            ) {
                                Box(
                                    modifier = Modifier
                                        .widthIn(max = 260.dp)
                                        .background(
                                            if (isUser) LanzarusPrimaryTeal
                                            else Color(0xFFF1F5F9),
                                            shape = RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 16.dp
                                            )
                                        )
                                        .border(
                                            BorderStroke(1.dp, if (isUser) Color.Transparent else LanzarusBorderColor),
                                            shape = RoundedCornerShape(
                                                topStart = 16.dp,
                                                topEnd = 16.dp,
                                                bottomStart = if (isUser) 16.dp else 0.dp,
                                                bottomEnd = if (isUser) 0.dp else 16.dp
                                            )
                                        )
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = msg.message,
                                        color = if (isUser) Color.White else LanzarusTextPrimary,
                                        fontSize = 13.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                        if (isChatLoading) {
                            item {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(8.dp)
                                ) {
                                    CircularProgressIndicator(
                                        color = LanzarusPrimaryTeal,
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Lanzarus AI está pensando...", color = LanzarusTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Chat Input Send Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextField(
                        value = userPrompt,
                        onValueChange = { userPrompt = it },
                        placeholder = { Text("Pregunta sobre tu bot o finanzas...", color = LanzarusTextMuted, fontSize = 13.sp) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF8F9FA),
                            unfocusedContainerColor = Color(0xFFF8F9FA),
                            focusedIndicatorColor = LanzarusPrimaryTeal,
                            unfocusedIndicatorColor = Color.Transparent,
                            focusedTextColor = LanzarusTextPrimary,
                            unfocusedTextColor = LanzarusTextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Send,
                            keyboardType = KeyboardType.Text
                        ),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                if (userPrompt.isNotBlank() && !isChatLoading) {
                                    viewModel.sendMessage(userPrompt)
                                    userPrompt = ""
                                    keyboardController?.hide()
                                }
                            }
                        ),
                        modifier = Modifier.weight(1f).testTag("chat_input")
                    )
                    IconButton(
                        onClick = {
                            if (userPrompt.isNotBlank() && !isChatLoading) {
                                viewModel.sendMessage(userPrompt)
                                userPrompt = ""
                                keyboardController?.hide()
                            }
                        },
                        enabled = userPrompt.isNotBlank() && !isChatLoading,
                        modifier = Modifier
                            .background(
                                if (userPrompt.isNotBlank() && !isChatLoading) LanzarusPrimaryTeal else Color(0xFFF1F5F9),
                                RoundedCornerShape(12.dp)
                            )
                            .size(48.dp)
                            .testTag("chat_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Enviar",
                            tint = if (userPrompt.isNotBlank() && !isChatLoading) Color.White else LanzarusTextSecondary
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 3: AUTOMATED PREMIUM CONTENT FEEDS
// =========================================================================
@Composable
fun FeedTab(
    contents: List<ContentEntity>,
    user: UserEntity?,
    viewModel: LanzarusViewModel,
    onArticleClick: (ContentEntity) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    "Canal de Contenidos",
                    color = LanzarusPrimaryTeal,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Información de nicho procesada de forma autónoma",
                    color = LanzarusTextSecondary,
                    fontSize = 12.sp
                )
            }
            Button(
                onClick = { viewModel.triggerScrapeContent() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, LanzarusPrimaryTeal.copy(alpha = 0.4f)),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, tint = LanzarusPrimaryTeal, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Escanear", color = LanzarusPrimaryTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Premium subscription status card
        if (user?.isPremium != true) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFBEB)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, LanzarusAccentGold.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Acceso Premium Desactivado",
                            color = LanzarusAccentGold,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            "Suscríbete para leer informes cripto avanzados creados por IA.",
                            color = LanzarusTextSecondary,
                            fontSize = 11.sp,
                            lineHeight = 15.sp
                        )
                    }
                    Button(
                        onClick = { viewModel.purchasePremium() },
                        colors = ButtonDefaults.buttonColors(containerColor = LanzarusAccentGold),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text("Activar Premium", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }
        } else {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, LanzarusSuccessGreen.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LanzarusSuccessGreen, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        "Suscripción Lanzarus Premium Activa (Acceso Ilimitado)",
                        color = LanzarusSuccessGreen,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // List of Scraped Feed Articles
        if (contents.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White, RoundedCornerShape(24.dp))
                    .border(BorderStroke(1.dp, LanzarusBorderColor), RoundedCornerShape(24.dp))
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = LanzarusPrimaryTeal)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Cargando señales del sistema...", color = LanzarusTextSecondary, fontSize = 13.sp)
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(contents) { article ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, LanzarusBorderColor),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onArticleClick(article) }
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = article.category.uppercase(),
                                    color = if (article.isPremiumOnly) LanzarusAccentGold else LanzarusPrimaryTeal,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(
                                            (if (article.isPremiumOnly) LanzarusAccentGold else LanzarusPrimaryTeal).copy(alpha = 0.12f),
                                            RoundedCornerShape(4.dp)
                                        )
                                        .padding(horizontal = 6.dp, vertical = 3.dp)
                                )
                                if (article.isPremiumOnly) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = LanzarusAccentGold,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("PREMIUM", color = LanzarusAccentGold, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = article.title,
                                color = LanzarusTextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = article.snippet,
                                color = LanzarusTextSecondary,
                                fontSize = 12.sp,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
// TAB 4: FINANCE (DEPOSITS, INSTANT WEBHOOK WITHDRAWALS & KYC WIZARD)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaidSecureLinkDialog(
    onDismiss: () -> Unit,
    onSuccess: (bankName: String, cardHolder: String, cardNumber: String, cardBrand: String) -> Unit
) {
    var step by remember { mutableStateOf(1) } // 1: Welcome/Info, 2: Select Bank, 3: Card Details, 4: Loading/Linking, 5: Success
    var selectedBank by remember { mutableStateOf("Revolut") }
    
    var cardNumberInput by remember { mutableStateOf("") }
    var cardHolderInput by remember { mutableStateOf("") }
    var cardExpiryInput by remember { mutableStateOf("") }
    var cardCvvInput by remember { mutableStateOf("") }
    
    var linkingStatusText by remember { mutableStateOf("Iniciando conexión segura...") }
    var progress by remember { mutableStateOf(0.1f) }

    LaunchedEffect(step) {
        if (step == 4) {
            linkingStatusText = "Iniciando canal seguro SSL de 256 bits..."
            progress = 0.2f
            delay(1000)
            linkingStatusText = "Autenticando con la API segura de $selectedBank..."
            progress = 0.5f
            delay(1200)
            linkingStatusText = "Verificando vigencia de la tarjeta de débito..."
            progress = 0.8f
            delay(1000)
            linkingStatusText = "Sincronizando token de retiros automáticos (Outflow Webhook)..."
            progress = 0.95f
            delay(1000)
            step = 5
        }
    }

    Dialog(onDismissRequest = { if (step != 4) onDismiss() }) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header brand banner
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .background(Color(0xFF00C781), CircleShape) // Plaid green
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "plaid",
                        color = Color.Black,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "SECURE LINK",
                        color = Color(0xFF64748B),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))

                when (step) {
                    1 -> {
                        // Welcome Screen
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFF0FDF4), CircleShape)
                        ) {
                            Text("🛡️", fontSize = 32.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "Vincula tu tarjeta de forma segura",
                            color = Color(0xFF0F172A),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Lanzarus utiliza Plaid para que asocies tu tarjeta de débito en milisegundos sin comprometer tu seguridad bancaria.",
                            color = Color(0xFF475569),
                            fontSize = 12.sp,
                            lineHeight = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Security checkpoints
                        Column(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.Top) {
                                Text("🔒 ", fontSize = 14.sp)
                                Column {
                                    Text("Encriptación de Grado Bancario", color = Color(0xFF1E293B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Tus credenciales reales nunca son accesibles por Lanzarus.", color = Color(0xFF64748B), fontSize = 10.sp)
                                }
                            }
                            Row(verticalAlignment = Alignment.Top) {
                                Text("⚡ ", fontSize = 14.sp)
                                Column {
                                    Text("Retiros Inmediatos Activos", color = Color(0xFF1E293B), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text("Liquida tus ganancias a tu tarjeta en menos de 5 segundos.", color = Color(0xFF64748B), fontSize = 10.sp)
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Button(
                            onClick = { step = 2 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Iniciar Vinculación", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        TextButton(onClick = onDismiss) {
                            Text("Cancelar", color = Color(0xFF64748B), fontSize = 12.sp)
                        }
                    }
                    2 -> {
                        // Select Bank
                        Text(
                            text = "Selecciona tu banco o proveedor",
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        val banks = listOf(
                            "Revolut" to "🇪🇺",
                            "BBVA" to "🇪🇸",
                            "Santander" to "🇪🇸",
                            "CaixaBank" to "🇪🇸",
                            "N26" to "🇩🇪",
                            "Banco Sabadell" to "🇪🇸"
                        )
                        
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            banks.forEach { (bank, flag) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(
                                            if (selectedBank == bank) Color(0xFFF0FDF4) else Color(0xFFF8FAFC),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .border(
                                            1.dp,
                                            if (selectedBank == bank) Color(0xFF00C781) else Color(0xFFE2E8F0),
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedBank = bank }
                                        .padding(14.dp)
                                ) {
                                    Text(flag, fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = bank,
                                        color = Color(0xFF1E293B),
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (selectedBank == bank) {
                                        Text("✓", color = Color(0xFF00C781), fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = { step = 3 },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF000000)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirmar Banco", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    3 -> {
                        // Card Details Form
                        Text(
                            text = "Detalles de Tarjeta de Débito ($selectedBank)",
                            color = Color(0xFF0F172A),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        TextField(
                            value = cardHolderInput,
                            onValueChange = { cardHolderInput = it },
                            label = { Text("Nombre Completo Titular", fontSize = 11.sp) },
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        TextField(
                            value = cardNumberInput,
                            onValueChange = { input ->
                                if (input.length <= 16) cardNumberInput = input.filter { it.isDigit() }
                            },
                            label = { Text("Número de Tarjeta (16 dígitos)", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8FAFC),
                                unfocusedContainerColor = Color(0xFFF8FAFC),
                                focusedTextColor = Color(0xFF1E293B),
                                unfocusedTextColor = Color(0xFF1E293B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            TextField(
                                value = cardExpiryInput,
                                onValueChange = { if (it.length <= 5) cardExpiryInput = it },
                                label = { Text("Expiración (MM/AA)", fontSize = 11.sp) },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedTextColor = Color(0xFF1E293B),
                                    unfocusedTextColor = Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            TextField(
                                value = cardCvvInput,
                                onValueChange = { input ->
                                    if (input.length <= 3) cardCvvInput = input.filter { it.isDigit() }
                                },
                                label = { Text("CVV", fontSize = 11.sp) },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8FAFC),
                                    unfocusedContainerColor = Color(0xFFF8FAFC),
                                    focusedTextColor = Color(0xFF1E293B),
                                    unfocusedTextColor = Color(0xFF1E293B)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Button(
                            onClick = {
                                if (cardHolderInput.isNotBlank() && cardNumberInput.length == 16 && cardExpiryInput.length >= 4 && cardCvvInput.length >= 3) {
                                    step = 4
                                }
                            },
                            enabled = cardHolderInput.isNotBlank() && cardNumberInput.length == 16 && cardExpiryInput.length >= 4 && cardCvvInput.length >= 3,
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C781)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Enlazar de Forma Segura", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                    4 -> {
                        // Loading/Linking Screen
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator(
                            color = Color(0xFF00C781),
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Estableciendo Enlace Seguro",
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = linkingStatusText,
                            color = Color(0xFF64748B),
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.height(32.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            color = Color(0xFF00C781),
                            trackColor = Color(0xFFF1F5F9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                        )
                    }
                    5 -> {
                        // Success Screen
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(64.dp)
                                .background(Color(0xFFF0FDF4), CircleShape)
                        ) {
                            Text("✨", fontSize = 32.sp)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "¡Cuenta Vinculada!",
                            color = Color(0xFF1E293B),
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tu tarjeta de débito de $selectedBank ha sido autorizada de forma segura por Plaid.",
                            color = Color(0xFF64748B),
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                val maskedCard = "•••• •••• •••• " + cardNumberInput.takeLast(4)
                                val brand = if (cardNumberInput.startsWith("4")) "VISA" else "MASTERCARD"
                                onSuccess(selectedBank, cardHolderInput, maskedCard, brand)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C781)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Finalizar Enlace", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

// =========================================================================
@Composable
fun DownloadQrDialog(
    onDismiss: () -> Unit
) {
    val downloadUrl = "https://ais-dev-gvjumgtq7caxlgm3fr6euh-904278042157.europe-west2.run.app/download"
    val qrCodeUrl = "https://api.qrserver.com/v1/create-qr-code/?size=400x400&data=https%3A%2F%2Fais-dev-gvjumgtq7caxlgm3fr6euh-904278042157.europe-west2.run.app%2Fdownload&color=0d9488&bgcolor=ffffff&qzone=2"

    Dialog(onDismissRequest = onDismiss) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(28.dp),
            border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Instalar en Móvil",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = LanzarusPrimaryTeal
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cerrar",
                            tint = LanzarusTextSecondary
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Loading / QR Code Container
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(Color(0xFFF8F9FA), RoundedCornerShape(16.dp))
                        .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    coil.compose.AsyncImage(
                        model = qrCodeUrl,
                        contentDescription = "Código QR Lanzarus",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Escanea el código QR",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = LanzarusTextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = "Apunta con la cámara de tu móvil para descargar el archivo APK de forma inmediata y segura, sin cables ni USB.",
                    fontSize = 11.sp,
                    color = LanzarusTextSecondary,
                    textAlign = TextAlign.Center,
                    lineHeight = 16.sp
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Show raw download link for easy typing or reference
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF1F5F9)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Enlace de Descarga:",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = LanzarusTextSecondary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = downloadUrl,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LanzarusPrimaryTeal,
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// =========================================================================
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FinanceTab(
    user: UserEntity?,
    transactions: List<TransactionEntity>,
    viewModel: LanzarusViewModel
) {
    val linkedCardNumber by viewModel.linkedCardNumber.collectAsStateWithLifecycle()
    val linkedCardHolder by viewModel.linkedCardHolder.collectAsStateWithLifecycle()
    val linkedCardBrand by viewModel.linkedCardBrand.collectAsStateWithLifecycle()
    val linkedBankName by viewModel.linkedBankName.collectAsStateWithLifecycle()

    var showPlaidLinkDialog by remember { mutableStateOf(false) }

    var depositAmountStr by remember { mutableStateOf("") }
    var withdrawAmountStr by remember { mutableStateOf("") }
    var destinationWallet by remember { mutableStateOf("") }

    // KYC Form states
    var kycFullName by remember { mutableStateOf("") }
    var kycDocNumber by remember { mutableStateOf("") }

    // Detailed Withdrawal states
    var withdrawalMethod by remember { mutableStateOf("tarjeta") } // "tarjeta" or "banco"
    var cardNo by remember { mutableStateOf("") }
    var cardExpiry by remember { mutableStateOf("") }
    var cardCvv by remember { mutableStateOf("") }
    var bankIban by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var isProcessingWithdrawal by remember { mutableStateOf(false) }
    var processingStep by remember { mutableStateOf(0) } // 0: Start, 1: KYC check, 2: Webhook routing, 3: Completed

    val keyboardController = LocalSoftwareKeyboardController.current

    var showQrLinkDialog by remember { mutableStateOf(false) }

    if (showQrLinkDialog) {
        DownloadQrDialog(onDismiss = { showQrLinkDialog = false })
    }

    if (showPlaidLinkDialog) {
        PlaidSecureLinkDialog(
            onDismiss = { showPlaidLinkDialog = false },
            onSuccess = { bank, holder, masked, brand ->
                viewModel.linkDebitCard(bank, holder, masked, brand)
                showPlaidLinkDialog = false
            }
        )
    }

    if (isProcessingWithdrawal) {
        LaunchedEffect(Unit) {
            processingStep = 1
            delay(1500)
            processingStep = 2
            delay(1500)
            processingStep = 3
            delay(1500)
            
            val detailStr = if (linkedCardNumber != null) {
                "Plaid Express: " + linkedBankName + " (" + linkedCardNumber + ")"
            } else if (withdrawalMethod == "tarjeta") {
                "Tarjeta Débito (**** " + cardNo.takeLast(4) + ")"
            } else {
                "Cta. Bancaria (" + bankName + " - **** " + bankIban.takeLast(4) + ")"
            }
            val amt = withdrawAmountStr.toDoubleOrNull() ?: 0.0
            viewModel.withdrawFunds(amt, detailStr)
            
            // Clean up state
            isProcessingWithdrawal = false
            processingStep = 0
            withdrawAmountStr = ""
            cardNo = ""
            cardExpiry = ""
            cardCvv = ""
            bankIban = ""
            bankName = ""
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Gestión Financiera",
                color = LanzarusPrimaryTeal,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Entradas de capital, KYC y retiros liquidados vía webhook",
                color = LanzarusTextSecondary,
                fontSize = 12.sp
            )
        }

        // Wallet Balance Overview Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, LanzarusPrimaryTeal.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SALDO LÍQUIDO DISPONIBLE", color = LanzarusTextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "$${String.format("%,.2f", user?.balance ?: 0.0)} USD",
                        color = LanzarusPrimaryTeal,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Invertido", color = LanzarusTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("$${String.format("%,.2f", user?.investedCapital ?: 0.0)}", color = LanzarusTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F9FA)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Premium", color = LanzarusTextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text(if (user?.isPremium == true) "SÍ" else "NO", color = if (user?.isPremium == true) LanzarusSuccessGreen else LanzarusAccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // CARD PARA DESCARGAR APP EN MÓVIL (QR)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = LanzarusPrimaryTeal.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, LanzarusPrimaryTeal.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showQrLinkDialog = true }
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(LanzarusPrimaryTeal, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Compartir",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Instalar en tu móvil real",
                            color = LanzarusTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Escanea el código QR desde tu teléfono para descargar el instalador APK sin cables.",
                            color = LanzarusTextSecondary,
                            fontSize = 11.sp
                        )
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Ver QR",
                        tint = LanzarusPrimaryTeal
                    )
                }
            }
        }

        // PLAID SECURE CARD LINKING WIDGET
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, if (linkedCardNumber != null) Color(0xFF00C781).copy(alpha = 0.4f) else LanzarusBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (linkedCardNumber != null) Color(0xFF00C781) else Color(0xFF94A3B8), CircleShape)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "MÓDULO DE TARJETA PLAID SECURE",
                                color = LanzarusTextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                        }
                        if (linkedCardNumber != null) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFF0FDF4), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "VERIFICADO",
                                    color = Color(0xFF00C781),
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (linkedCardNumber != null) {
                        // Display beautiful credit/debit card mockup!
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)), // dark elegant card style
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                                // Background subtle accent gradient
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    drawCircle(
                                        color = Color(0xFF00C781).copy(alpha = 0.15f),
                                        radius = 180f,
                                        center = Offset(size.width * 0.85f, size.height * 0.2f)
                                    )
                                    drawCircle(
                                        color = LanzarusAccentGold.copy(alpha = 0.12f),
                                        radius = 130f,
                                        center = Offset(size.width * 0.15f, size.height * 0.9f)
                                    )
                                }

                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = linkedBankName ?: "BANCO COOPERADOR",
                                                color = Color.White,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = 1.sp
                                            )
                                            Text(
                                                text = "Tarjeta Débito Vinculada",
                                                color = Color(0xFF94A3B8),
                                                fontSize = 9.sp
                                            )
                                        }
                                        // Plaid Badge logo mock
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Box(
                                                modifier = Modifier
                                                    .size(6.dp)
                                                    .background(Color(0xFF00C781), CircleShape)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "plaid",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                letterSpacing = (-0.5).sp
                                            )
                                        }
                                    }

                                    // Card number masked
                                    Text(
                                        text = linkedCardNumber ?: "•••• •••• •••• ••••",
                                        color = Color.White,
                                        fontSize = 18.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 2.sp
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.Bottom
                                    ) {
                                        Column {
                                            Text(
                                                text = "TITULAR",
                                                color = Color(0xFF64748B),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = linkedCardHolder ?: "TITULAR CUENTA",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Text(
                                            text = linkedCardBrand ?: "VISA",
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.unlinkDebitCard() },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = LanzarusErrorRed),
                                border = BorderStroke(1.dp, LanzarusErrorRed.copy(alpha = 0.3f)),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Desvincular Tarjeta", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Unlinked State promo card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8FAFC), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(40.dp)
                                    .background(Color(0xFFE2E8F0), CircleShape)
                            ) {
                                Text("💳", fontSize = 20.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Sin tarjeta de débito asociada",
                                    color = LanzarusTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Asocie una tarjeta de débito vía Plaid para procesar retiros directos inmediatos.",
                                    color = LanzarusTextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = { showPlaidLinkDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C781)), // Plaid green
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth().testTag("plaid_link_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Vincular Tarjeta vía Plaid Secure", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // KYC Automated Verification Wizard
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, LanzarusBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "MÓDULO DE VERIFICACIÓN DE IDENTIDAD (KYC)",
                        color = LanzarusTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (user?.isVerified == true) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFECFDF5), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = LanzarusSuccessGreen, modifier = Modifier.size(24.dp))
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                textLabelBold("ESTADO: VERIFICADO", LanzarusSuccessGreen)
                                Text("Sincronizado a nombre de ${user.name}", color = LanzarusTextPrimary.copy(alpha = 0.8f), fontSize = 11.sp)
                            }
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            Text(
                                "Para desbloquear retiros inmediatos por directiva financiera, por favor asocie una identidad.",
                                color = LanzarusTextSecondary,
                                fontSize = 11.sp,
                                lineHeight = 15.sp
                            )
                            TextField(
                                value = kycFullName,
                                onValueChange = { kycFullName = it },
                                label = { Text("Nombre Completo") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8F9FA),
                                    unfocusedContainerColor = Color(0xFFF8F9FA),
                                    focusedTextColor = LanzarusTextPrimary,
                                    unfocusedTextColor = LanzarusTextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("kyc_name_input")
                            )
                            TextField(
                                value = kycDocNumber,
                                onValueChange = { kycDocNumber = it },
                                label = { Text("Nº de Identificación (DNI/NIE/Pasaporte)") },
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFFF8F9FA),
                                    unfocusedContainerColor = Color(0xFFF8F9FA),
                                    focusedTextColor = LanzarusTextPrimary,
                                    unfocusedTextColor = LanzarusTextPrimary
                                ),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth().testTag("kyc_doc_input")
                            )
                            Button(
                                onClick = {
                                    if (kycFullName.isNotBlank() && kycDocNumber.isNotBlank()) {
                                        viewModel.runKYCVerification(kycFullName, kycDocNumber)
                                        kycFullName = ""
                                        kycDocNumber = ""
                                        keyboardController?.hide()
                                    } else {
                                        viewModel.showNotification("Por favor rellene todos los campos de KYC")
                                    }
                                },
                                enabled = kycFullName.isNotBlank() && kycDocNumber.isNotBlank(),
                                colors = ButtonDefaults.buttonColors(containerColor = LanzarusPrimaryTeal),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth().testTag("kyc_submit_button")
                            ) {
                                Text("Ejecutar Validación Biométrica", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Deposit Inflow Processing Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, LanzarusBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "INGRESAR FONDOS (INFLOW)",
                        color = LanzarusTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val quickDepositAmounts = listOf(100.0, 500.0, 2000.0)
                        quickDepositAmounts.forEach { amt ->
                            Button(
                                onClick = { viewModel.depositFunds(amt) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF1F5F9)),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, LanzarusPrimaryTeal.copy(alpha = 0.25f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+$amt", color = LanzarusPrimaryTeal, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = depositAmountStr,
                            onValueChange = { depositAmountStr = it },
                            placeholder = { Text("Cantidad en USD", fontSize = 13.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color(0xFFF8F9FA),
                                unfocusedContainerColor = Color(0xFFF8F9FA),
                                focusedTextColor = LanzarusTextPrimary,
                                unfocusedTextColor = LanzarusTextPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            modifier = Modifier.weight(1f).testTag("deposit_input")
                        )
                        Button(
                            onClick = {
                                val amt = depositAmountStr.toDoubleOrNull()
                                if (amt != null && amt > 0) {
                                    viewModel.depositFunds(amt)
                                    depositAmountStr = ""
                                    keyboardController?.hide()
                                } else {
                                    viewModel.showNotification("Ingrese una cantidad válida")
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LanzarusPrimaryTeal),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("deposit_submit_button")
                        ) {
                            Text("Depositar", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }

        // Webhook Instant Withdrawal Outflow Processing Panel
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, LanzarusBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "RETIRAR FONDOS INMEDIATAMENTE (OUTFLOW)",
                        color = LanzarusTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (user?.isVerified != true) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFFEF2F2), RoundedCornerShape(12.dp))
                                .border(BorderStroke(1.dp, LanzarusErrorRed.copy(alpha = 0.2f)), RoundedCornerShape(12.dp))
                                .padding(16.dp)
                        ) {
                            Text(
                                "Retiros deshabilitados. Requiere Verificación KYC de Identidad en el módulo superior.",
                                color = LanzarusErrorRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        if (isProcessingWithdrawal) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = LanzarusPrimaryTeal,
                                    strokeWidth = 3.dp,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(20.dp))
                                Text(
                                    text = "PROCESANDO RETIRO EXPRESS",
                                    color = LanzarusPrimaryTeal,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.2.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                val stepText = when (processingStep) {
                                    1 -> "🔍 Validando estatus de verificación KYC para: " + user.name
                                    2 -> "⚡ Invocando webhook en bus de pagos serverless..."
                                    3 -> "🎉 ¡Pasarela de Pago liquidada! Verificando dispersión en cuenta..."
                                    else -> "Iniciando transferencia..."
                                }
                                
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 14.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = stepText,
                                        color = LanzarusTextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                if (linkedCardNumber != null) {
                                    // Plaid-linked instant withdrawal info card
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFECFDF5), RoundedCornerShape(12.dp))
                                            .border(BorderStroke(1.dp, Color(0xFF00C781).copy(alpha = 0.3f)), RoundedCornerShape(12.dp))
                                            .padding(12.dp)
                                    ) {
                                        Text("⚡", fontSize = 18.sp)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = "Retiro Plaid Express Activado",
                                                color = Color(0xFF065F46),
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = "Fondos transferidos directamente a tu tarjeta de $linkedBankName ($linkedCardNumber) en menos de 5 segundos.",
                                                color = Color(0xFF047857),
                                                fontSize = 11.sp,
                                                lineHeight = 14.sp
                                            )
                                        }
                                    }
                                } else {
                                    // Selector Row
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(Color(0xFFF1F5F9), RoundedCornerShape(12.dp))
                                            .padding(4.dp),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Button(
                                            onClick = { withdrawalMethod = "tarjeta" },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (withdrawalMethod == "tarjeta") Color.White else Color.Transparent,
                                                contentColor = if (withdrawalMethod == "tarjeta") LanzarusTextPrimary else LanzarusTextSecondary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            elevation = if (withdrawalMethod == "tarjeta") ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null,
                                            contentPadding = PaddingValues(vertical = 8.dp),
                                            modifier = Modifier.weight(1f).height(36.dp)
                                        ) {
                                            Text("💳 Tarjeta Débito", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        Button(
                                            onClick = { withdrawalMethod = "banco" },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (withdrawalMethod == "banco") Color.White else Color.Transparent,
                                                contentColor = if (withdrawalMethod == "banco") LanzarusTextPrimary else LanzarusTextSecondary
                                            ),
                                            shape = RoundedCornerShape(8.dp),
                                            elevation = if (withdrawalMethod == "banco") ButtonDefaults.buttonElevation(defaultElevation = 2.dp) else null,
                                            contentPadding = PaddingValues(vertical = 8.dp),
                                            modifier = Modifier.weight(1f).height(36.dp)
                                        ) {
                                            Text("🏛️ Cuenta Bancaria", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    // Verification Identity Badge
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .background(LanzarusSuccessGreen.copy(alpha = 0.08f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(6.dp)
                                                .background(LanzarusSuccessGreen, CircleShape)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = "Titular Verificado: " + user.name,
                                            color = LanzarusSuccessGreen,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
    
                                    if (withdrawalMethod == "tarjeta") {
                                        // Fields for Credit Card
                                        TextField(
                                            value = cardNo,
                                            onValueChange = { input -> if (input.length <= 16) cardNo = input.filter { it.isDigit() } },
                                            label = { Text("Número de Tarjeta (16 dígitos)") },
                                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFFF8F9FA),
                                                unfocusedContainerColor = Color(0xFFF8F9FA),
                                                focusedTextColor = LanzarusTextPrimary,
                                                unfocusedTextColor = LanzarusTextPrimary
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("withdraw_card_input")
                                        )
                                        
                                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                                            TextField(
                                                value = cardExpiry,
                                                onValueChange = { if (it.length <= 5) cardExpiry = it },
                                                label = { Text("Exp. (MM/AA)") },
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = Color(0xFFF8F9FA),
                                                    unfocusedContainerColor = Color(0xFFF8F9FA),
                                                    focusedTextColor = LanzarusTextPrimary,
                                                    unfocusedTextColor = LanzarusTextPrimary
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                singleLine = true,
                                                modifier = Modifier.weight(1f).testTag("withdraw_expiry_input")
                                            )
                                            TextField(
                                                value = cardCvv,
                                                onValueChange = { input -> if (input.length <= 3) cardCvv = input.filter { it.isDigit() } },
                                                label = { Text("CVV") },
                                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                                colors = TextFieldDefaults.colors(
                                                    focusedContainerColor = Color(0xFFF8F9FA),
                                                    unfocusedContainerColor = Color(0xFFF8F9FA),
                                                    focusedTextColor = LanzarusTextPrimary,
                                                    unfocusedTextColor = LanzarusTextPrimary
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                singleLine = true,
                                                modifier = Modifier.weight(1f).testTag("withdraw_cvv_input")
                                            )
                                        }
                                    } else {
                                        // Fields for Bank Account
                                        TextField(
                                            value = bankName,
                                            onValueChange = { bankName = it },
                                            label = { Text("Nombre del Banco") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFFF8F9FA),
                                                unfocusedContainerColor = Color(0xFFF8F9FA),
                                                focusedTextColor = LanzarusTextPrimary,
                                                unfocusedTextColor = LanzarusTextPrimary
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("withdraw_bank_name_input")
                                        )
                                        
                                        TextField(
                                            value = bankIban,
                                            onValueChange = { bankIban = it.uppercase() },
                                            label = { Text("Código IBAN de la Cuenta (ES...)") },
                                            colors = TextFieldDefaults.colors(
                                                focusedContainerColor = Color(0xFFF8F9FA),
                                                unfocusedContainerColor = Color(0xFFF8F9FA),
                                                focusedTextColor = LanzarusTextPrimary,
                                                unfocusedTextColor = LanzarusTextPrimary
                                            ),
                                            shape = RoundedCornerShape(12.dp),
                                            singleLine = true,
                                            modifier = Modifier.fillMaxWidth().testTag("withdraw_iban_input")
                                        )
                                    }
                                }
                                
                                // Amount selection and withdrawal submit
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    TextField(
                                        value = withdrawAmountStr,
                                        onValueChange = { withdrawAmountStr = it },
                                        label = { Text("Monto USD") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        colors = TextFieldDefaults.colors(
                                            focusedContainerColor = Color(0xFFF8F9FA),
                                            unfocusedContainerColor = Color(0xFFF8F9FA),
                                            focusedTextColor = LanzarusTextPrimary,
                                            unfocusedTextColor = LanzarusTextPrimary
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        singleLine = true,
                                        modifier = Modifier.weight(1f).testTag("withdraw_amount_input")
                                    )
                                    
                                    Button(
                                        onClick = {
                                            val amt = withdrawAmountStr.toDoubleOrNull()
                                            val userBal = user?.balance ?: 0.0
                                            if (amt == null || amt <= 0) {
                                                viewModel.showNotification("Por favor, ingrese un monto válido")
                                            } else if (amt > userBal) {
                                                viewModel.showNotification("Fondos insuficientes en el balance")
                                            } else if (linkedCardNumber != null) {
                                                keyboardController?.hide()
                                                isProcessingWithdrawal = true
                                            } else if (withdrawalMethod == "tarjeta" && (cardNo.length < 16 || cardExpiry.isBlank() || cardCvv.length < 3)) {
                                                viewModel.showNotification("Complete todos los datos de la tarjeta de débito")
                                            } else if (withdrawalMethod == "banco" && (bankIban.isBlank() || bankName.isBlank())) {
                                                viewModel.showNotification("Complete los datos bancarios (Banco e IBAN)")
                                            } else {
                                                keyboardController?.hide()
                                                isProcessingWithdrawal = true
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = LanzarusAccentGold),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.height(52.dp).testTag("withdraw_submit_button")
                                    ) {
                                        Text("Retirar", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(4.dp))
                                
                                Text(
                                    text = "Al solicitar el retiro, se ejecuta un webhook de dispersión inmediata de fondos. Plazo de liquidación real estimado: < 5 segundos en su cuenta bancaria/tarjeta.",
                                    color = LanzarusTextSecondary,
                                    fontSize = 10.sp,
                                    lineHeight = 13.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Historic Transaction Logs (Wallet Feed)
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, LanzarusBorderColor)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "HISTORIAL DE TRANSACCIONES",
                        color = LanzarusTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (transactions.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                        ) {
                            Text("Aún no se registran transacciones", color = LanzarusTextSecondary, fontSize = 12.sp)
                        }
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            transactions.take(15).forEach { tx ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF8F9FA), RoundedCornerShape(12.dp))
                                        .border(BorderStroke(1.dp, LanzarusBorderColor), RoundedCornerShape(12.dp))
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = tx.details,
                                            color = LanzarusTextPrimary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        val df = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
                                        Text(
                                            text = "${tx.type} | " + df.format(java.util.Date(tx.timestamp)),
                                            color = LanzarusTextSecondary,
                                            fontSize = 10.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column(horizontalAlignment = Alignment.End) {
                                        val isPositive = tx.type == "DEPOSIT" || tx.type == "REINVEST"
                                        Text(
                                            text = if (isPositive) "+$${String.format("%.2f", tx.amount)}" else "-$${String.format("%.2f", tx.amount)}",
                                            color = if (isPositive) LanzarusSuccessGreen else LanzarusErrorRed,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = tx.status,
                                            color = when (tx.status) {
                                                "COMPLETED" -> LanzarusSuccessGreen
                                                "PENDING" -> LanzarusAccentGold
                                                else -> LanzarusErrorRed
                                            },
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// Helpers for cleaner code and correct composition types
@Composable
fun textLabelBold(text: String, color: Color) {
    Text(text = text, color = color, fontWeight = FontWeight.Bold, fontSize = 13.sp)
}

@Composable
fun NotificationPreferenceRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onTestClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Text(subtitle, color = LanzarusTextSecondary, fontSize = 10.sp, lineHeight = 13.sp)
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onTestClick,
                modifier = Modifier.size(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Test Notification",
                    tint = LanzarusPrimaryTeal,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color(0xFF020617),
                    checkedTrackColor = LanzarusPrimaryTeal,
                    uncheckedThumbColor = LanzarusTextSecondary,
                    uncheckedTrackColor = Color.Black.copy(alpha = 0.3f)
                ),
                modifier = Modifier.scale(0.85f)
            )
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, LanzarusBorderColor),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    color = LanzarusTextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color.copy(alpha = 0.7f),
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                color = LanzarusTextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun PortfolioTrackingCard(
    activeInvestments: List<InvestmentOrderEntity>,
    liveMarketPrices: Map<String, Double>,
    isConnected: Boolean,
    viewModel: LanzarusViewModel
) {
    // Keep track of the last 15 profit values to draw a real-time sparkline
    val totalProfit = activeInvestments.sumOf { it.profit }
    val profitHistory = remember { mutableStateListOf<Float>() }
    
    // Add current total profit to history when it updates
    LaunchedEffect(totalProfit) {
        if (profitHistory.isEmpty() || profitHistory.last() != totalProfit.toFloat()) {
            profitHistory.add(totalProfit.toFloat())
            if (profitHistory.size > 15) {
                profitHistory.removeAt(0)
            }
        }
    }
    
    // If empty history, put a couple of dummy reference points
    if (profitHistory.isEmpty()) {
        profitHistory.addAll(listOf(0f, 0f))
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(32.dp),
        border = BorderStroke(1.dp, LanzarusBorderColor),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("portfolio_tracking_card")
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Header Section
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Pulsing Dot indicator
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(14.dp)
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                        val scale by infiniteTransition.animateFloat(
                            initialValue = 0.6f,
                            targetValue = 1.3f,
                            animationSpec = infiniteRepeatable(
                                animation = tween(800, easing = LinearEasing),
                                repeatMode = RepeatMode.Reverse
                            ),
                            label = "scale"
                        )
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background((if (isConnected) LanzarusSuccessGreen else LanzarusAccentGold).copy(alpha = 0.4f))
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isConnected) LanzarusSuccessGreen else LanzarusAccentGold)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "SEÑALES EN TIEMPO REAL",
                            color = LanzarusTextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Rendimiento del Portafolio",
                            color = LanzarusTextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                // Connection chip
                Box(
                    modifier = Modifier
                        .background(
                            (if (isConnected) LanzarusSuccessGreen else LanzarusAccentGold).copy(alpha = 0.08f),
                            RoundedCornerShape(100.dp)
                        )
                        .border(
                            1.dp,
                            (if (isConnected) LanzarusSuccessGreen else LanzarusAccentGold).copy(alpha = 0.15f),
                            RoundedCornerShape(100.dp)
                        )
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isConnected) "COINCAP LIVE" else "LOCAL SIMULATOR",
                        color = if (isConnected) LanzarusSuccessGreen else LanzarusAccentGold,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Core Metrics Row
            val totalInvested = activeInvestments.sumOf { it.amount }
            val currentPortfolioValue = totalInvested + totalProfit
            val profitPercent = if (totalInvested > 0) (totalProfit / totalInvested) * 100.0 else 0.0
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Valor del Portafolio", color = LanzarusTextSecondary, fontSize = 10.sp)
                    Text(
                        text = "$" + String.format("%,.2f", currentPortfolioValue),
                        color = LanzarusTextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                Column(horizontalAlignment = Alignment.End) {
                    Text("Beneficio Neto (PnL)", color = LanzarusTextSecondary, fontSize = 10.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = if (totalProfit >= 0) "▲ " else "▼ ",
                            color = if (totalProfit >= 0) LanzarusSuccessGreen else LanzarusErrorRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = (if (totalProfit >= 0) "+" else "") + String.format("%.2f", totalProfit) + " (" + String.format("%.2f", profitPercent) + "%)",
                            color = if (totalProfit >= 0) LanzarusSuccessGreen else LanzarusErrorRed,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sparkline Trend Chart Canvas
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFFF8F9FA), RoundedCornerShape(16.dp))
                    .border(BorderStroke(1.dp, LanzarusBorderColor), RoundedCornerShape(16.dp))
                    .padding(vertical = 8.dp, horizontal = 16.dp)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val width = size.width
                    val height = size.height
                    
                    if (profitHistory.size > 1) {
                        val minVal = profitHistory.minOrNull() ?: 0f
                        val maxVal = profitHistory.maxOrNull() ?: 1f
                        val range = if (maxVal == minVal) 1f else maxVal - minVal
                        
                        val points = profitHistory.mapIndexed { index, valF ->
                            val x = index * (width / (profitHistory.size - 1))
                            // invert Y so positive is up
                            val y = height - ((valF - minVal) / range) * height
                            Offset(x, y)
                        }
                        
                        // Create trend line path
                        val path = Path().apply {
                            moveTo(points.first().x, points.first().y)
                            for (i in 1 until points.size) {
                                lineTo(points[i].x, points[i].y)
                            }
                        }
                        
                        // Draw stroke
                        val strokeColor = if (totalProfit >= 0) LanzarusSuccessGreen else LanzarusErrorRed
                        drawPath(
                            path = path,
                            color = strokeColor,
                            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
                        )
                        
                        // Create gradient area path below trend line
                        val fillPath = Path().apply {
                            addPath(path)
                            lineTo(points.last().x, height)
                            lineTo(points.first().x, height)
                            close()
                        }
                        
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(strokeColor.copy(alpha = 0.15f), strokeColor.copy(alpha = 0.001f)),
                                startY = 0f,
                                endY = height
                            )
                        )
                        
                        // Draw pulsing end dot
                        drawCircle(
                            color = strokeColor,
                            radius = 4.dp.toPx(),
                            center = points.last()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Active assets details header
            Text(
                "DESGLOSE DE POSICIONES ACTIVAS",
                color = LanzarusTextSecondary,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            if (activeInvestments.isEmpty()) {
                // Empty state inside dashboard
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFF8F9FA), RoundedCornerShape(16.dp))
                        .border(BorderStroke(1.dp, LanzarusBorderColor), RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        "No hay órdenes o activos invertidos.",
                        color = LanzarusTextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            val assets = listOf("BTC/USDT", "ETH/USDT", "SOL/USDT", "EUR/USD", "SP500/ETF", "NASDAQ")
                            val basePrices = mapOf("BTC/USDT" to 65000.0, "ETH/USDT" to 3400.0, "SOL/USDT" to 145.0, "EUR/USD" to 1.08, "SP500/ETF" to 540.0, "NASDAQ" to 490.0)
                            val chosenAsset = assets.random()
                            val startPrice = basePrices[chosenAsset] ?: 100.0
                            val size = Random.nextDouble(100.0, 1500.0)
                            viewModel.simulateOrder(
                                symbol = chosenAsset,
                                amount = size,
                                entryPrice = startPrice,
                                type = listOf("BUY", "SELL").random()
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = LanzarusPrimaryTeal),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Text("+ Invertir Ahora", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            } else {
                // List of positions
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    activeInvestments.take(3).forEach { order ->
                        val isGain = order.profit >= 0
                        val percentProfit = (order.profit / order.amount) * 100.0
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFFF8F9FA), RoundedCornerShape(16.dp))
                                .border(BorderStroke(1.dp, LanzarusBorderColor), RoundedCornerShape(16.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                // Asset mini logo mockup / background
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background((if (order.type == "BUY") LanzarusSuccessGreen else LanzarusErrorRed).copy(alpha = 0.08f))
                                ) {
                                    Text(
                                        text = if (order.type == "BUY") "▲" else "▼",
                                        color = if (order.type == "BUY") LanzarusSuccessGreen else LanzarusErrorRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(10.dp))
                                
                                Column {
                                    Text(
                                        text = order.symbol,
                                        color = LanzarusTextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Invertido: $" + String.format("%.2f", order.amount),
                                        color = LanzarusTextSecondary,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                            
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "$" + String.format("%,.2f", order.currentPrice),
                                    color = LanzarusTextPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = (if (isGain) "+" else "") + String.format("%.2f", order.profit) + " (" + String.format("%.2f", percentProfit) + "%)",
                                    color = if (isGain) LanzarusSuccessGreen else LanzarusErrorRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    if (activeInvestments.size > 3) {
                        Text(
                            text = "Y ${activeInvestments.size - 3} posiciones más en la pestaña de Portafolio...",
                            color = LanzarusTextSecondary,
                            fontSize = 10.sp,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
