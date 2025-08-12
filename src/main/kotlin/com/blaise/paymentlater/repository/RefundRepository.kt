package com.blaise.paymentlater.repository

import com.blaise.paymentlater.domain.model.Refund
import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository

interface RefundRepository : MongoRepository<Refund, ObjectId>