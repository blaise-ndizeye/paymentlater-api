// MongoDB Initialization Script for PaymentLaterAPI Production Environment
// This script runs when MongoDB container starts for the first time
// Based on actual models: Admin, Merchant, PaymentIntent, Transaction

// Switch to the payment_later_prod database
db = db.getSiblingDB('payment_later_prod');

// Create collections based on actual @Document annotations
db.createCollection('admins');
db.createCollection('merchants');
db.createCollection('payment_intents');
db.createCollection('transactions');

// Create indexes based on actual model annotations

// Admin indexes
db.admins.createIndex({ "username": 1 }, { unique: true });

// Merchant indexes (from @CompoundIndex and @Indexed annotations)
db.merchants.createIndex({ "email": 1 }, { unique: true });
db.merchants.createIndex({ "apiKey": 1 }, { unique: true });
db.merchants.createIndex({ "apiKeyDigest": 1 }, { unique: true });
db.merchants.createIndex({ "name": 1 });
db.merchants.createIndex({ "isActive": 1 });
db.merchants.createIndex({ "roles": 1 });
db.merchants.createIndex({ "createdAt": -1 });
db.merchants.createIndex({ "updatedAt": -1 });
// Compound index as defined in Merchant.kt
db.merchants.createIndex({
  "name": 1, 
  "email": 1, 
  "isActive": 1, 
  "roles": 1, 
  "createdAt": -1, 
  "updatedAt": -1
}, { name: "name_email_isActive_roles_createdAt_updatedAt" });

// PaymentIntent indexes (from @CompoundIndex and @Indexed annotations)
db.payment_intents.createIndex({ "merchantId": 1 });
db.payment_intents.createIndex({ "currency": 1 });
db.payment_intents.createIndex({ "status": 1 });
db.payment_intents.createIndex({ "createdAt": -1 });
// Compound index as defined in PaymentIntent.kt
db.payment_intents.createIndex({
  "currency": 1, 
  "status": 1, 
  "createdAt": -1
}, { name: "currency_status_createdAt_idx" });

// Transaction indexes
db.transactions.createIndex({ "paymentIntentId": 1 });
db.transactions.createIndex({ "parentTransactionId": 1 });
db.transactions.createIndex({ "status": 1 });
db.transactions.createIndex({ "confirmedAt": -1 });
db.transactions.createIndex({ "amount": 1 });
db.transactions.createIndex({ "currency": 1 });

// Create production-specific indexes for performance
db.payment_intents.createIndex({ "expiresAt": 1 }, { expireAfterSeconds: 0 }); // TTL index for expired payments
db.merchants.createIndex({ "isActive": 1, "createdAt": -1 });
db.transactions.createIndex({ "status": 1, "confirmedAt": -1 });

// Security: Create read-only user for monitoring/backup
db.createUser({
  user: 'payment_later_readonly',
  pwd: passwordPrompt("Enter password for readonly user: "),
  roles: [
    {
      role: 'read',
      db: 'payment_later_prod'
    }
  ]
});

print("✅ Production database 'payment_later_prod' initialized successfully!");
print("📊 Created collections: admins, merchants, payment_intents, transactions");
print("🔍 Created indexes based on model annotations:");
print("   - Admin: unique username");
print("   - Merchant: unique email, apiKey, apiKeyDigest + compound indexes");
print("   - PaymentIntent: currency, status, createdAt + compound index + TTL");
print("   - Transaction: paymentIntentId, status, confirmedAt, etc.");
print("🔒 Created readonly user for monitoring/backup");
print("⚡ Added production-specific performance indexes");
print("🚫 No sample data inserted for production security");