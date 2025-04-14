package com.example.calculatorapp

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var input: TextView
    private var expression: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        input = findViewById(R.id.tvInput)

        val labelInput = findViewById<EditText>(R.id.etLabel)
        val logButton = findViewById<Button>(R.id.btnLog)
        val logText = findViewById<TextView>(R.id.tvLog)
        val entryList = mutableListOf<String>()

        logButton.setOnClickListener {
            val label = labelInput.text.toString().trim()
            val value = input.text.toString().trim()

            if (label.isNotEmpty() && value.isNotEmpty()) {
                entryList.add("$label: $value")
                logText.text = entryList.joinToString("\n")
                labelInput.text.clear()
            }
        }


        val buttons = listOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9,
            R.id.btnDot, R.id.btnAdd, R.id.btnSubtract, R.id.btnMultiply,
            R.id.btnDivide, R.id.btnPercent
        )

        buttons.forEach { id ->
            findViewById<Button>(id).setOnClickListener {
                val text = (it as Button).text.toString()
                expression += text
                input.text = expression
            }
        }

        findViewById<Button>(R.id.btnClear).setOnClickListener {
            expression = ""
            input.text = "0"
            entryList.clear()
            logText.text = ""
        }

        findViewById<Button>(R.id.btnBack).setOnClickListener {
            if (expression.isNotEmpty()) {
                expression = expression.dropLast(1)
                input.text = expression
            }
        }

        findViewById<Button>(R.id.btnEquals).setOnClickListener {
            try {
                val result = eval(expression)
                input.text = result.toString()
                expression = result.toString()
            } catch (e: Exception) {
                input.text = "Error"
            }
        }
    }

    private fun eval(expr: String): Double {
        val cleanedExpr = expr
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-") // Replace Unicode minus with regular minus
            .replace("%", "/100") // Convert percent to math expression

        return try {
            object : Any() {
                var pos = -1
                var ch: Char = ' '

                fun nextChar() {
                    ch = if (++pos < cleanedExpr.length) cleanedExpr[pos] else '\u0000'
                }

                fun eat(charToEat: Char): Boolean {
                    while (ch == ' ') nextChar()
                    if (ch == charToEat) {
                        nextChar()
                        return true
                    }
                    return false
                }

                fun parse(): Double {
                    nextChar()
                    val x = parseExpression()
                    if (pos < cleanedExpr.length) throw RuntimeException("Unexpected: $ch")
                    return x
                }

                fun parseExpression(): Double {
                    var x = parseTerm()
                    while (true) {
                        x = when {
                            eat('+') -> x + parseTerm()
                            eat('-') -> x - parseTerm()
                            else -> return x
                        }
                    }
                }

                fun parseTerm(): Double {
                    var x = parseFactor()
                    while (true) {
                        x = when {
                            eat('*') -> x * parseFactor()
                            eat('/') -> x / parseFactor()
                            else -> return x
                        }
                    }
                }

                fun parseFactor(): Double {
                    if (eat('+')) return parseFactor()
                    if (eat('-')) return -parseFactor()

                    var x: Double
                    val startPos = pos
                    if (eat('(')) {
                        x = parseExpression()
                        eat(')')
                    } else if (ch in '0'..'9' || ch == '.') {
                        while (ch in '0'..'9' || ch == '.') nextChar()
                        x = cleanedExpr.substring(startPos, pos).toDouble()
                    } else {
                        throw RuntimeException("Unexpected: $ch")
                    }

                    return x
                }
            }.parse()
        } catch (e: Exception) {
            Double.NaN
        }
    }
}
