package com.blaise.paymentlater.repository

import com.blaise.paymentlater.domain.model.Transaction
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface TransactionRepository: MongoRepository<Transaction, ObjectId>
