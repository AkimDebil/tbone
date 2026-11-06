package com.example.ui.trainer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.example.trainer.ear.EarTrainerEngine
import com.example.trainer.rhythm.RhythmTrainerEngine
import com.example.trainer.tone.DroneSynthesizer
import com.example.trainer.tone.PitchDetector

class TrainersViewModel(application: Application) : AndroidViewModel(application) {
    val rhythmEngine = RhythmTrainerEngine(application)
    val droneSynthesizer = DroneSynthesizer()
    val pitchDetector = PitchDetector(application)
    val earTrainer = EarTrainerEngine()

    override fun onCleared() {
        super.onCleared()
        rhythmEngine.release()
        droneSynthesizer.release()
        pitchDetector.release()
        earTrainer.release()
    }
}
