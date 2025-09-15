// MongoDB Initialization Script for PaymentLaterAPI Development Environment
// This script runs when MongoDB container starts for the first time
// Based on actual models: Admin, Merchant, PaymentIntent, Transaction

// Switch to the payment_later_dev database
db = db.getSiblingDB('payment_later_dev');

// Create a development user with read/write permissions
db.createUser({
  user: 'payment_later_dev_user',
  pwd: 'dev_password_123',
  roles: [
    {
      role: 'readWrite',
      db: 'payment_later_dev'
    }
  ]
});

// Create collections based on actual @Document annotations
db.createCollection('admins');
db.createCollection('merchants');
db.createCollection('payment_intents');
db.createCollection('transactions');

// Create indexes based on actual model annotations (with error handling)

// Function to safely create indexes
function safeCreateIndex(collection, indexSpec, options) {
  try {
    collection.createIndex(indexSpec, options);
    print(`✅ Created index: ${JSON.stringify(indexSpec)} on ${collection.getName()}`);
  } catch (error) {
    if (error.code === 85) {
      print(`⚠️  Index already exists: ${JSON.stringify(indexSpec)} on ${collection.getName()}`);
    } else {
      print(`❌ Error creating index: ${error.message}`);
    }
  }
}

// Admin indexes
safeCreateIndex(db.admins, { "username": 1 }, { unique: true });

// Merchant indexes (from @CompoundIndex and @Indexed annotations)
safeCreateIndex(db.merchants, { "email": 1 }, { unique: true });
safeCreateIndex(db.merchants, { "apiKey": 1 }, { unique: true });
safeCreateIndex(db.merchants, { "apiKeyDigest": 1 }, { unique: true });
safeCreateIndex(db.merchants, { "name": 1 });
safeCreateIndex(db.merchants, { "isActive": 1 });
safeCreateIndex(db.merchants, { "roles": 1 });
safeCreateIndex(db.merchants, { "createdAt": -1 });
safeCreateIndex(db.merchants, { "updatedAt": -1 });
// Compound index as defined in Merchant.kt
safeCreateIndex(db.merchants, {
  "name": 1, 
  "email": 1, 
  "isActive": 1, 
  "roles": 1, 
  "createdAt": -1, 
  "updatedAt": -1
}, { name: "name_email_isActive_roles_createdAt_updatedAt" });

// PaymentIntent indexes (from @CompoundIndex and @Indexed annotations)
safeCreateIndex(db.payment_intents, { "merchantId": 1 });
safeCreateIndex(db.payment_intents, { "currency": 1 });
safeCreateIndex(db.payment_intents, { "status": 1 });
safeCreateIndex(db.payment_intents, { "createdAt": -1 });
// Compound index as defined in PaymentIntent.kt
safeCreateIndex(db.payment_intents, {
  "currency": 1, 
  "status": 1, 
  "createdAt": -1
}, { name: "currency_status_createdAt_idx" });

// Transaction indexes
safeCreateIndex(db.transactions, { "paymentIntentId": 1 });
safeCreateIndex(db.transactions, { "parentTransactionId": 1 });
safeCreateIndex(db.transactions, { "status": 1 });
safeCreateIndex(db.transactions, { "confirmedAt": -1 });
safeCreateIndex(db.transactions, { "amount": 1 });
safeCreateIndex(db.transactions, { "currency": 1 });

// Optional: Insert minimal sample data for development (using actual enum values)
db.admins.insertOne({
  _id: ObjectId(),
  username: "admin",
  password: "$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LeJDbxWesaB5.XaWG", // bcrypt for "admin123"
  roles: ["ADMIN"],
  createdAt: new Date()
});

// Sample merchant for development
db.merchants.insertOne({
  _id: ObjectId(),
  name: "Development Merchant",
  email: "merchant@dev.paymentlater.com",
  apiKey: "dev_api_key_123456789",
  apiKeyDigest: "sha256_digest_of_api_key",
  webhookUrl: "https://localhost:3000/webhook",
  isActive: true,
  roles: ["MERCHANT"],
  createdAt: new Date(),
  updatedAt: new Date()
});

print("✅ Development database 'payment_later_dev' initialized successfully!");
print("📊 Created collections: admins, merchants, payment_intents, transactions");
print("🔍 Created indexes based on model annotations:");
print("   - Admin: unique username");
print("   - Merchant: unique email, apiKey, apiKeyDigest + compound indexes");
print("   - PaymentIntent: currency, status, createdAt + compound index");
print("   - Transaction: paymentIntentId, status, confirmedAt, etc.");
print("👤 Created development user: payment_later_dev_user");
print("🎯 Inserted sample development data for admins and merchants");