package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val balance: Double = 5000.0, // default starter demo funds
    val investedCapital: Double = 0.0,
    val isVerified: Boolean = false, // KYC Verification Status
    val isPremium: Boolean = false, // Premium Subscription Status
    val monthlyEarnings: Double = 0.0,
    val dailyStreak: Int = 1,
    val notifyFinanzas: Boolean = true,
    val notifyTendencias: Boolean = true,
    val notifyMercados: Boolean = true,
    val notifyAlerts: Boolean = true
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "DEPOSIT", "WITHDRAWAL", "REINVEST", "PREMIUM_SUB"
    val amount: Double,
    val status: String, // "COMPLETED", "PENDING", "FAILED"
    val timestamp: Long = System.currentTimeMillis(),
    val details: String
)

@Entity(tableName = "contents")
data class ContentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String, // "Finanzas", "Tendencias", "Mercados"
    val snippet: String,
    val fullText: String,
    val isPremiumOnly: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "investment_orders")
data class InvestmentOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String, // e.g. "BTC/USDT", "ETH/USDT", "NASDAQ", "EUR/USD"
    val amount: Double, // invested amount in USD
    val entryPrice: Double,
    val currentPrice: Double,
    val type: String, // "BUY", "SELL"
    val profit: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String // "ACTIVE", "CLOSED"
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String, // "USER", "BOT"
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
