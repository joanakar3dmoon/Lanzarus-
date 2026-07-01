package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

class LanzarusRepository(
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val contentDao: ContentDao,
    private val investmentDao: InvestmentDao,
    private val chatDao: ChatDao
) {
    val userFlow: Flow<UserEntity?> = userDao.getUserFlow()
    val allTransactions: Flow<List<TransactionEntity>> = transactionDao.getAllTransactionsFlow()
    val allContents: Flow<List<ContentEntity>> = contentDao.getAllContentsFlow()
    val activeInvestments: Flow<List<InvestmentOrderEntity>> = investmentDao.getActiveOrdersFlow()
    val allInvestments: Flow<List<InvestmentOrderEntity>> = investmentDao.getAllOrdersFlow()
    val chatMessages: Flow<List<ChatMessageEntity>> = chatDao.getAllMessagesFlow()

    suspend fun getUserSync(): UserEntity? = userDao.getUserSync()

    suspend fun insertOrUpdateUser(user: UserEntity) = userDao.insertOrUpdateUser(user)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun insertTransaction(transaction: TransactionEntity) =
        transactionDao.insertTransaction(transaction)

    suspend fun insertContent(content: ContentEntity) =
        contentDao.insertContent(content)

    suspend fun insertContents(contents: List<ContentEntity>) =
        contentDao.insertContents(contents)

    suspend fun insertOrder(order: InvestmentOrderEntity) =
        investmentDao.insertOrder(order)

    suspend fun updateOrder(order: InvestmentOrderEntity) =
        investmentDao.updateOrder(order)

    suspend fun insertMessage(message: ChatMessageEntity) =
        chatDao.insertMessage(message)

    suspend fun clearChatHistory() = chatDao.clearHistory()

    suspend fun resetDatabase() {
        transactionDao.clearTransactions()
        contentDao.clearContents()
        investmentDao.clearOrders()
        chatDao.clearHistory()
        // Reset user to default state
        val adminEmail = if (com.example.BuildConfig.LANZARUS_ADMIN_UID.isNullOrBlank()) "admin@lanzarus.ai" else com.example.BuildConfig.LANZARUS_ADMIN_UID
        userDao.insertOrUpdateUser(
            UserEntity(
                name = "Demo Admin",
                email = adminEmail,
                balance = 12500.0,
                investedCapital = 4200.0,
                isVerified = true,
                isPremium = true,
                monthlyEarnings = 1450.0,
                dailyStreak = 3
            )
        )
    }
}
