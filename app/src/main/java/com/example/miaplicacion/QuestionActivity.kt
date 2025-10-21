package com.example.miaplicacion

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random

class QuestionActivity : AppCompatActivity() {

    data class Question(
        val id: Int,
        val question: String,
        val options: List<String>,
        val correctIndex: Int
    )

    private lateinit var tvQuestion: TextView
    private lateinit var btnOpt1: Button
    private lateinit var btnOpt2: Button
    private lateinit var btnOpt3: Button
    private lateinit var btnOpt4: Button
    private lateinit var btnNext: Button
    private lateinit var tvScore: TextView

    private var questions: MutableList<Question> = mutableListOf()
    private var currentIndex = 0
    private var score = 0
    private var answered = false

    companion object {
        private const val KEY_INDEX = "key_index"
        private const val KEY_SCORE = "key_score"
        private const val QUESTIONS_TO_SHOW = 10
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.preguntas)

        tvQuestion = findViewById(R.id.tv_question)
        btnOpt1 = findViewById(R.id.btn_option_1)
        btnOpt2 = findViewById(R.id.btn_option_2)
        btnOpt3 = findViewById(R.id.btn_option_3)
        btnOpt4 = findViewById(R.id.btn_option_4)
        btnNext = findViewById(R.id.btn_next)
        tvScore = findViewById(R.id.tv_score)

        if (savedInstanceState != null) {
            currentIndex = savedInstanceState.getInt(KEY_INDEX, 0)
            score = savedInstanceState.getInt(KEY_SCORE, 0)
        }

        loadQuestionsFromAssets("questions.json")

        if (questions.isEmpty()) {
            Toast.makeText(this, "No hay preguntas en el JSON.", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        btnOpt1.setOnClickListener { onOptionSelected(0, btnOpt1) }
        btnOpt2.setOnClickListener { onOptionSelected(1, btnOpt2) }
        btnOpt3.setOnClickListener { onOptionSelected(2, btnOpt3) }
        btnOpt4.setOnClickListener { onOptionSelected(3, btnOpt4) }

        btnNext.setOnClickListener {
            if (!answered) {
                Toast.makeText(this, "Selecciona una opción antes de continuar", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            currentIndex++
            if (currentIndex < questions.size) {
                loadQuestion(currentIndex)
            } else {
                showResult()
            }
        }

        btnNext.isEnabled = false

        if (currentIndex < questions.size) {
            loadQuestion(currentIndex)
        } else {
            showResult()
        }

        updateScoreText()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt(KEY_INDEX, currentIndex)
        outState.putInt(KEY_SCORE, score)
    }

    private fun loadQuestionsFromAssets(filename: String) {
        try {
            val input = assets.open(filename)
            val reader = BufferedReader(InputStreamReader(input))
            val sb = StringBuilder()
            var line: String? = reader.readLine()
            while (line != null) {
                sb.append(line)
                line = reader.readLine()
            }
            reader.close()
            val jsonArray = JSONArray(sb.toString())

            val allQuestions = mutableListOf<Question>()

            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val id = obj.optInt("id", i)
                val question = obj.optString("question", "Pregunta sin texto")
                val optionsJson = obj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optionsJson.length()) {
                    options.add(optionsJson.getString(j))
                }
                val correct = obj.optInt("correctIndex", 0)
                while (options.size < 4) options.add("")
                allQuestions.add(Question(id, question, options, correct))
            }

            questions = selectRandomQuestions(allQuestions, QUESTIONS_TO_SHOW)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error leyendo JSON: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun selectRandomQuestions(allQuestions: List<Question>, count: Int): MutableList<Question> {
        return if (allQuestions.size <= count) {
            allQuestions.toMutableList()
        } else {
            allQuestions.shuffled().take(count).toMutableList()
        }
    }

    private fun loadQuestion(index: Int) {
        resetOptionStyles()
        answered = false
        val q = questions[index]
        tvQuestion.text = q.question

        btnOpt1.text = q.options.getOrNull(0) ?: ""
        btnOpt2.text = q.options.getOrNull(1) ?: ""
        btnOpt3.text = q.options.getOrNull(2) ?: ""
        btnOpt4.text = q.options.getOrNull(3) ?: ""

        setOptionsEnabled(true)
        btnNext.isEnabled = false

        btnNext.text = if (index == questions.size - 1) "Bukatu" else "Hurrengoa"
        updateScoreText()
    }

    private fun onOptionSelected(selectedIndex: Int, selectedButton: Button) {
        if (answered) return
        answered = true
        val q = questions[currentIndex]

        setOptionsEnabled(false)

        btnNext.isEnabled = true

        if (selectedIndex == q.correctIndex) {
            score++
            highlightButtonAsCorrect(selectedButton)
        } else {
            highlightButtonAsIncorrect(selectedButton)
            val correctBtn = getButtonByIndex(q.correctIndex)
            highlightButtonAsCorrect(correctBtn)
        }
        updateScoreText()
    }

    private fun getButtonByIndex(index: Int): Button {
        return when (index) {
            0 -> btnOpt1
            1 -> btnOpt2
            2 -> btnOpt3
            3 -> btnOpt4
            else -> btnOpt1
        }
    }

    private fun highlightButtonAsCorrect(button: Button) {
        val green = ContextCompat.getColor(this, android.R.color.holo_green_light)
        button.backgroundTintList = ColorStateList.valueOf(green)
    }

    private fun highlightButtonAsIncorrect(button: Button) {
        val red = ContextCompat.getColor(this, android.R.color.holo_red_light)
        button.backgroundTintList = ColorStateList.valueOf(red)
    }

    private fun resetOptionStyles() {
        btnOpt1.backgroundTintList = null
        btnOpt2.backgroundTintList = null
        btnOpt3.backgroundTintList = null
        btnOpt4.backgroundTintList = null
    }

    private fun setOptionsEnabled(enabled: Boolean) {
        btnOpt1.isEnabled = enabled
        btnOpt2.isEnabled = enabled
        btnOpt3.isEnabled = enabled
        btnOpt4.isEnabled = enabled
    }

    private fun updateScoreText() {
        tvScore.text = "Puntuazioa: $score / ${questions.size}"
    }

    private fun showResult() {

        Toast.makeText(this, "Puntuazioa: $score/${questions.size}", Toast.LENGTH_LONG).show()
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        startActivity(intent)
        finish()
    }
}