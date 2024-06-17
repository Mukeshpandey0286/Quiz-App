package com.example.quizz

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.quizz.databinding.ActivityResultBinding

class ResultActivity : AppCompatActivity() {
    private lateinit var binding: ActivityResultBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val score = intent.getIntExtra("score", 0)
        val totalQuestions = intent.getIntExtra("totalQuestions", 0)

        binding.congratulationsTextView.text = "Congratulations!"
        binding.scoreTextView.text = "You scored $score out of $totalQuestions."

        binding.finishButton.setOnClickListener {
            finish()
        }
    }
}
