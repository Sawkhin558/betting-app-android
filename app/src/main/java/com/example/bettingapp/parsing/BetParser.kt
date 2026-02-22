package com.example.bettingapp.parsing

import com.example.bettingapp.database.BetType
import com.example.bettingapp.database.EntryEntity

class BetParser(private val directMultiplier: Double = 80.0, private val rolledMultiplier: Double = 500.0) {
    
    data class ParseResult(
        val entries: List<EntryEntity>,
        val totalAmount: Double,
        val isValid: Boolean,
        val errorMessage: String? = null
    )
    
    fun parseInput(rawText: String, voucherId: Long = 0): ParseResult {
        val lines = rawText.trim().lines()
        val entries = mutableListOf<EntryEntity>()
        var totalAmount = 0.0
        
        for ((index, line) in lines.withIndex()) {
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) continue
            
            try {
                val parsedEntries = parseLine(trimmedLine, voucherId)
                entries.addAll(parsedEntries)
                totalAmount += parsedEntries.sumOf { it.amount }
            } catch (e: ParseException) {
                return ParseResult(
                    entries = emptyList(),
                    totalAmount = 0.0,
                    isValid = false,
                    errorMessage = "Line ${index + 1}: ${e.message}"
                )
            }
        }
        
        if (entries.isEmpty()) {
            return ParseResult(
                entries = emptyList(),
                totalAmount = 0.0,
                isValid = false,
                errorMessage = "No valid bets found"
            )
        }
        
        return ParseResult(
            entries = entries,
            totalAmount = totalAmount,
            isValid = true
        )
    }
    
    private fun parseLine(line: String, voucherId: Long): List<EntryEntity> {
        // Check for direct bet: 123=100*50
        if (line.contains('=') && line.contains('*')) {
            return parseDirectBet(line, voucherId)
        }
        
        // Check for rolled bet: 123r50
        if (line.contains('r') || line.contains('R')) {
            return parseRolledBet(line, voucherId)
        }
        
        throw ParseException("Invalid bet format: $line")
    }
    
    private fun parseDirectBet(line: String, voucherId: Long): List<EntryEntity> {
        val parts = line.split('=')
        if (parts.size != 2) throw ParseException("Invalid direct bet format: $line")
        
        val number = parts[0].trim()
        if (!isValidNumber(number)) throw ParseException("Invalid number: $number")
        
        val betPart = parts[1].trim()
        val betParts = betPart.split('*')
        if (betParts.size != 2) throw ParseException("Invalid bet amount format: $betPart")
        
        val amount = betParts[0].toDoubleOrNull() ?: throw ParseException("Invalid amount: ${betParts[0]}")
        val multiplier = betParts[1].toDoubleOrNull() ?: throw ParseException("Invalid multiplier: ${betParts[1]}")
        
        if (amount <= 0) throw ParseException("Amount must be positive: $amount")
        if (multiplier != directMultiplier) {
            throw ParseException("Direct bet multiplier must be $directMultiplier, got $multiplier")
        }
        
        val potentialPayout = amount * multiplier
        
        return listOf(
            EntryEntity(
                voucherId = voucherId,
                number = number,
                betType = BetType.DIRECT,
                amount = amount,
                payoutMultiplier = multiplier,
                potentialPayout = potentialPayout
            )
        )
    }
    
    private fun parseRolledBet(line: String, voucherId: Long): List<EntryEntity> {
        // Format: 123r50 or 123R50
        val regex = """(\d{3})[rR](\d+(?:\.\d+)?)""".toRegex()
        val match = regex.find(line) ?: throw ParseException("Invalid rolled bet format: $line")
        
        val number = match.groupValues[1]
        if (!isValidNumber(number)) throw ParseException("Invalid number: $number")
        
        val amount = match.groupValues[2].toDoubleOrNull() ?: throw ParseException("Invalid amount: ${match.groupValues[2]}")
        if (amount <= 0) throw ParseException("Amount must be positive: $amount")
        
        // Generate all 6 permutations for rolled bet
        val permutations = generatePermutations(number)
        
        return permutations.map { perm ->
            EntryEntity(
                voucherId = voucherId,
                number = perm,
                betType = BetType.ROLLED,
                amount = amount,
                payoutMultiplier = rolledMultiplier,
                potentialPayout = amount * rolledMultiplier
            )
        }
    }
    
    private fun generatePermutations(number: String): List<String> {
        if (number.length != 3) return listOf(number)
        
        val digits = number.toCharArray()
        val permutations = mutableSetOf<String>()
        
        // Generate all permutations of 3 digits
        for (i in 0..2) {
            for (j in 0..2) {
                for (k in 0..2) {
                    if (i != j && i != k && j != k) {
                        permutations.add("${digits[i]}${digits[j]}${digits[k]}")
                    }
                }
            }
        }
        
        return permutations.toList()
    }
    
    private fun isValidNumber(number: String): Boolean {
        return number.length == 3 && number.all { it.isDigit() }
    }
    
    class ParseException(message: String) : Exception(message)
}