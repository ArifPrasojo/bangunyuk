package com.example.mission

import kotlin.random.Random

data class MathProblem(
    val id: Int,
    val question: String,
    val answer: Int
)

object MathProblemGenerator {
    fun generate(count: Int, difficulty: String): List<MathProblem> {
        val problems = mutableListOf<MathProblem>()
        for (i in 1..count) {
            problems.add(generateSingleProblem(i, difficulty))
        }
        return problems
    }

    private fun generateSingleProblem(id: Int, difficulty: String): MathProblem {
        return when (difficulty.uppercase()) {
            "EASY" -> {
                val a = Random.nextInt(11, 49)
                val b = Random.nextInt(7, 39)
                if (Random.nextBoolean()) {
                    MathProblem(id, "$a + $b", a + b)
                } else {
                    val high = maxOf(a, b)
                    val low = minOf(a, b)
                    MathProblem(id, "$high - $low", high - low)
                }
            }
            "HARD" -> {
                val a = Random.nextInt(12, 28)
                val b = Random.nextInt(6, 15)
                val c = Random.nextInt(15, 60)
                if (Random.nextBoolean()) {
                    MathProblem(id, "($a × $b) - $c", (a * b) - c)
                } else {
                    MathProblem(id, "($a × $b) + $c", (a * b) + c)
                }
            }
            else -> { // MEDIUM default
                val a = Random.nextInt(6, 16)
                val b = Random.nextInt(4, 12)
                val c = Random.nextInt(5, 30)
                if (Random.nextBoolean()) {
                    MathProblem(id, "$a × $b + $c", (a * b) + c)
                } else {
                    val mult = a * b
                    val sub = Random.nextInt(5, mult)
                    MathProblem(id, "$a × $b - $sub", mult - sub)
                }
            }
        }
    }
}
