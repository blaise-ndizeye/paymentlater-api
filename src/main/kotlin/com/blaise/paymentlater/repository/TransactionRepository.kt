package com.blaise.paymentlater.repository

import com.blaise.paymentlater.domain.model.Transaction
import com.blaise.paymentlater.repository.sub.TransactionExtensionRepository
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface TransactionRepository: MongoRepository<Transaction, ObjectId>, TransactionExtensionRepository
