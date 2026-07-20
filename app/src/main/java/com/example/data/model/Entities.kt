package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: Int = 1,
    val name: String,
    val email: String,
    val balance: Double = 10000.0,
    val investedCapital: Double = 0.0,
    val isVerified: Boolean = false,
    val isPremium: Boolean = false,
    val monthlyEarnings: Double = 0.0,
    val dailyStreak: Int = 1,
    val notifyFinanzas: Boolean = true,
    val notifyTendencias: Boolean = true,
    val notifyMercados: Boolean = true,
    val notifyAlerts: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String,
    val amount: Double,
    val status: String = "COMPLETED",
    val timestamp: Long = System.currentTimeMillis(),
    val details: String = ""
)

@Entity(tableName = "contents")
data class ContentEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val category: String,
    val snippet: String,
    val fullText: String,
    val isPremiumOnly: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "investment_orders")
data class InvestmentOrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val symbol: String,
    val amount: Double,
    val entryPrice: Double,
    val currentPrice: Double,
    val type: String,
    val profit: Double = 0.0,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "ACTIVE"
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sender: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)
