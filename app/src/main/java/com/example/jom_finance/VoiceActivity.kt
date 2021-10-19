package com.example.jom_finance

import android.Manifest
import android.Manifest.permission.RECORD_AUDIO
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import kotlinx.android.synthetic.main.activity_voice.*
import java.util.*
import kotlin.collections.ArrayList

class VoiceActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var speechRecognizerIntent: Intent
    private lateinit var result : String
    private var voiceOn = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice)
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)

        speechRecognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"zh-TW")  /*ja-JP,zh-TW */
        speechRecognizer.setRecognitionListener(object : RecognitionListener{
            override fun onReadyForSpeech(p0: Bundle?) {

            }

            override fun onBeginningOfSpeech() {

            }

            override fun onRmsChanged(p0: Float) {

            }

            override fun onBufferReceived(p0: ByteArray?) {

            }

            override fun onEndOfSpeech() {

            }

            override fun onError(p0: Int) {

            }

            override fun onResults(p0: Bundle?) {
                val matchesFound = p0?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if(matchesFound != null){
                    result = matchesFound.get(0)
                    speechTxt.text = result
                    statusTxt.text = "Status : DONE !!!"
                }
            }

            override fun onPartialResults(p0: Bundle?) {

            }

            override fun onEvent(p0: Int, p1: Bundle?) {

            }

        })
        speakBtn.setOnClickListener {
            if(!voiceOn){
                checkVoiceCommandPermission()
                speechRecognizer.startListening(speechRecognizerIntent)
                statusTxt.text = "Status : Start Listening..."
                result = ""
            }else{
                speechRecognizer.stopListening()
                statusTxt.text = "Status : Processing..."
            }


        }

    }

    private fun checkVoiceCommandPermission(){
        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
            if(!(ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED)){
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
                startActivity(intent)
                finish()
            }
        }
    }
}