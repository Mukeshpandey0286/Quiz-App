package com.example.quizz

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.quizz.databinding.FragmentQuizBinding
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader

class QuizFragment : Fragment() {
    private lateinit var binding: FragmentQuizBinding
    private lateinit var viewModel: QuizViewModel
    private lateinit var timer: CountDownTimer
    private var timeLeftInMillis: Long = 600000 // 10 minutes in milliseconds

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentQuizBinding.inflate(inflater, container, false)
        viewModel = ViewModelProvider(this).get(QuizViewModel::class.java)

        val questions = loadQuestionsFromJson()
        if (questions.isEmpty()) {
            Toast.makeText(requireContext(), "No questions loaded", Toast.LENGTH_LONG).show()
            return binding.root
        }

        viewModel.setQuestions(questions)

        binding.nextButton.setOnClickListener { onNextClicked() }

        // Restore state
        val sharedPreferences = requireActivity().getSharedPreferences("quiz_prefs", 0)
        val currentQuestionIndex = sharedPreferences.getInt("current_question_index", 0)
        val quizFinished = sharedPreferences.getBoolean("quiz_finished", false)

        // If quiz is finished, reset the index and timer
        if (quizFinished) {
            viewModel.setCurrentQuestionIndex(0)
            timeLeftInMillis = 600000
            sharedPreferences.edit().putBoolean("quiz_finished", false).apply()
        } else {
            timeLeftInMillis = sharedPreferences.getLong("time_left", 600000)
            viewModel.setCurrentQuestionIndex(currentQuestionIndex)
        }

        startTimer(timeLeftInMillis)

        updateQuestionView()

        return binding.root
    }

    private fun loadQuestionsFromJson(): List<Question> {
        val json: String?
        try {
            val inputStream = requireContext().assets.open("questions.json")
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val stringBuilder = StringBuilder()
            var line: String? = bufferedReader.readLine()
            while (line != null) {
                stringBuilder.append(line)
                line = bufferedReader.readLine()
            }
            bufferedReader.close()
            json = stringBuilder.toString()
        } catch (ex: IOException) {
            ex.printStackTrace()
            return emptyList()
        }

        val gson = Gson()
        return gson.fromJson(json, object : TypeToken<List<Question>>() {}.type)
    }

    private fun startTimer(timeInMillis: Long) {
        timer = object : CountDownTimer(timeInMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                timeLeftInMillis = millisUntilFinished
                updateTimerView()
            }

            override fun onFinish() {
                endQuiz()
            }
        }.start()
    }

    private fun updateTimerView() {
        val minutes = (timeLeftInMillis / 1000) / 60
        val seconds = (timeLeftInMillis / 1000) % 60
        binding.timerTextView.text = String.format("%02d:%02d", minutes, seconds)
    }

    private fun onNextClicked() {
        val selectedOption = view?.findViewById<RadioButton>(binding.optionsRadioGroup.checkedRadioButtonId)?.text
        if (selectedOption != null) {
            viewModel.submitAnswer(selectedOption.toString())
            if (!viewModel.isQuizFinished()) {
                viewModel.moveNextQuestion()
                updateQuestionView()
            } else {
                endQuiz()
            }
        } else {
            Toast.makeText(requireContext(), "Please select an option", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateQuestionView() {
        val question = viewModel.getCurrentQuestion()
        binding.questionTextView.text = question.question
        binding.optionsRadioGroup.removeAllViews()

        for ((key, value) in question.options) {
            val radioButton = RadioButton(requireContext()).apply {
                text = "$key. $value"
                id = View.generateViewId()
                setTextColor(ContextCompat.getColor(requireContext(), R.color.black)) // Set your desired color here
            }
            binding.optionsRadioGroup.addView(radioButton)
        }
        binding.optionsRadioGroup.clearCheck() // Clear any previous selection
    }



    private fun endQuiz() {
        val sharedPreferences = requireActivity().getSharedPreferences("quiz_prefs", 0)
        with(sharedPreferences.edit()) {
            putInt("current_question_index", 0)
            putLong("time_left", 600000)
            putBoolean("quiz_finished", true)
            apply()
        }

        val intent = Intent(requireContext(), ResultActivity::class.java)
        intent.putExtra("score", viewModel.getScore())
        intent.putExtra("totalQuestions", viewModel.getTotalQuestions())
        startActivity(intent)
        requireActivity().finish() // Finish the current activity to prevent the user from going back to the quiz
    }



    override fun onPause() {
        super.onPause()
        // Save state
        val sharedPreferences = requireActivity().getSharedPreferences("quiz_prefs", 0)
        with(sharedPreferences.edit()) {
            putInt("current_question_index", viewModel.getCurrentQuestionIndex())
            putLong("time_left", timeLeftInMillis)
            apply()
        }
        timer.cancel()
    }
}
