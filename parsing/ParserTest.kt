package com.example.bettingapp.parsing

// Simple test to verify parsing logic
fun main() {
    println("Testing BetParser...")
    
    val parser = BetParser(directMultiplier = 80.0, rolledMultiplier = 500.0)
    
    // Test direct bet
    println("\n1. Testing direct bet: 123=100*80")
    val directResult = parser.parseInput("123=100*80")
    println("   Valid: ${directResult.isValid}")
    println("   Total Amount: ${directResult.totalAmount}")
    println("   Entries: ${directResult.entries.size}")
    directResult.entries.forEach { entry ->
        println("   - ${entry.number} ${entry.betType}: $${entry.amount} (Payout: $${entry.potentialPayout})")
    }
    
    // Test rolled bet
    println("\n2. Testing rolled bet: 123r50")
    val rolledResult = parser.parseInput("123r50")
    println("   Valid: ${rolledResult.isValid}")
    println("   Total Amount: ${rolledResult.totalAmount}")
    println("   Entries: ${rolledResult.entries.size}")
    println("   Unique numbers: ${rolledResult.entries.map { it.number }.toSet()}")
    
    // Test multiple bets
    println("\n3. Testing multiple bets:")
    val multiResult = parser.parseInput("""
        123=100*80
        456r50
        789=200*80
    """.trimIndent())
    println("   Valid: ${multiResult.isValid}")
    println("   Total Amount: ${multiResult.totalAmount}")
    println("   Total Entries: ${multiResult.entries.size}")
    
    // Test invalid input
    println("\n4. Testing invalid input: 123=100")
    val invalidResult = parser.parseInput("123=100")
    println("   Valid: ${invalidResult.isValid}")
    println("   Error: ${invalidResult.errorMessage}")
    
    // Test empty input
    println("\n5. Testing empty input:")
    val emptyResult = parser.parseInput("")
    println("   Valid: ${emptyResult.isValid}")
    println("   Error: ${emptyResult.errorMessage}")
    
    println("\n✅ Parser test completed!")
}