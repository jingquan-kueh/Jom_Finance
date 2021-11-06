package com.example.jom_finance

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.StrictMode
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import com.github.zagum.speechrecognitionview.RecognitionProgressView
import com.github.zagum.speechrecognitionview.adapters.RecognitionListenerAdapter
import kotlinx.android.synthetic.main.activity_voice.*
import java.util.*
import kotlin.collections.ArrayList
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.isInvisible
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class VoiceActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent
    private lateinit var result: String
    private var voiceActive = false
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.from_bottom_anim_voice)}
    private val toBottom: Animation by lazy { AnimationUtils.loadAnimation(this,R.anim.to_bottom_anim_voice)}
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice)
        var colors = intArrayOf(
            ContextCompat.getColor(this, R.color.color1),
            ContextCompat.getColor(this, R.color.color2),
            ContextCompat.getColor(this, R.color.color3),
            ContextCompat.getColor(this, R.color.color4),
            ContextCompat.getColor(this, R.color.color5)
        )
        var heights = intArrayOf(60, 76, 58, 80, 55)

        yesBtn.setOnClickListener{
            if(result!=null){
                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val text = textClass().textClass(result)
                Toast.makeText(this, text.toString(), Toast.LENGTH_SHORT).show()
            }
        }

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        recognition_view.setSpeechRecognizer(speechRecognizer)
        recognition_view.setRecognitionListener(object : RecognitionListenerAdapter() {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onResults(results: Bundle) {
                GlobalScope.launch {
                    delay(3000)
                    runOnUiThread{
                        showResults(results)
                        recognition_view.stop()
                        voiceActive = false
                        onVoiceClick()
                        recognition_view.play()

                    }

                }

            }
        })

        recognition_view.setColors(colors)
        recognition_view.setBarMaxHeightsInDp(heights)
        recognition_view.setCircleRadiusInDp(10)
        recognition_view.setSpacingInDp(10)
        recognition_view.setIdleStateAmplitudeInDp(10)
        recognition_view.setRotationRadiusInDp(50)
        recognition_view.isInvisible = true
        recognition_view.play()

        speakBtn.setOnClickListener {
            checkVoiceCommandPermission()
            // Add transition / animation when button click, from bottom.
            if(!voiceActive){
                voiceActive = true
                onVoiceClick()
                startRecognition()
                recognition_view.postDelayed({ startRecognition() },50)
                result = ""
            }
        }
    }

    private fun startRecognition() {
        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )
        speechRecognizerIntent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            Locale.getDefault()
        )  /*ja-JP,zh-TW */
        speechRecognizer.startListening(speechRecognizerIntent)
    }


    private fun showResults(results: Bundle) {
        val matchesFound = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        if (matchesFound != null) {
            result = matchesFound.get(0)
            speechTxt.text = result
            statusTxt.text = "Status : DONE !!!"
        }
    }

    private fun checkVoiceCommandPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!(ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED)
            ) {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                finish()
            }
        }
    }
    private fun onVoiceClick() {
        setVisibility(voiceActive)
        setAnimation(voiceActive)
    }
    private fun setVisibility(voiceActive : Boolean) {
        if(!voiceActive){
            speechTxt.visibility = View.VISIBLE
            recognition_view.visibility = View.INVISIBLE
        }else{
            speechTxt.visibility = View.INVISIBLE
            recognition_view.visibility = View.VISIBLE
        }
    }
    private fun setAnimation(voiceActive : Boolean) {
        if(!voiceActive){
            speechTxt.startAnimation(fromBottom)
            recognition_view.startAnimation(toBottom)
        }else{
            speechTxt.startAnimation(toBottom)
            recognition_view.startAnimation(fromBottom)
        }
    }
}
