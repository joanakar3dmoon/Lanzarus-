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

    // Mostrar interstitial al cambiar de pestaña (excepto la primera vez)
    LaunchedEffect(currentTab) {
        if (previousTab != currentTab && currentTab != 0) {
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
                    viewModel = viewModel
                )
                3 -> WalletTab(
                    user = user,
                    transactions = transactions,
                    viewModel = viewModel
                )
            }
            
            // Banner publicitario en la parte inferior (encima de la barra de navegación)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                AdBannerView()
            }
        }
    }
    
    // Reset dialog
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Reiniciar Bot") },
            text = { Text("¿Estás seguro de que quieres reiniciar el Bot de Inversiones? Se perderá el historial de trades.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.resetBot()
                    showResetDialog = false
                }) { Text("Reiniciar", color = LanzarusErrorRed) }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

// Componente de Banner de AdMob
@Composable
fun AdBannerView() {
    val context = LocalContext.current
    AndroidView(
        modifier = Modifier.fillMaxWidth(),
        factory = { ctx ->
            AdView(ctx).apply {
                adSize = AdSize.SMART_BANNER
                adUnitId = AdsManager.bannerAdId
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
