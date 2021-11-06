package com.example.jom_finance

import android.util.Log
import com.google.mlkit.nl.entityextraction.*

class Voice {
    private val TAG = "Voice"
    fun setClient(voiceText: String) {
        val entityExtractor =
            EntityExtraction.getClient(
                EntityExtractorOptions.Builder(EntityExtractorOptions.ENGLISH)
                    .build())
        entityExtractor
            .downloadModelIfNeeded()
            .addOnSuccessListener { _ ->
                val params =
                    EntityExtractionParams.Builder(voiceText)
                        .build()
                entityExtractor
                    .annotate(params)
                    .addOnSuccessListener {

                        //step4
                        for (entityAnnotation in it) {
                            val entities: List<Entity> = entityAnnotation.entities
                            Log.d(TAG, "Range: ${entityAnnotation.start} - ${entityAnnotation.end}")
                            for (entity in entities) {
                                when (entity) {
                                    is MoneyEntity -> {
                                        Log.d(TAG, "Currency: ${entity.unnormalizedCurrency}")
                                        Log.d(TAG, "Integer part: ${entity.integerPart}")
                                        Log.d(TAG, "Fractional Part: ${entity.fractionalPart}")

                                    }
                                    else -> {
                                        Log.d(TAG, "  $entity")
                                    }
                                }
                            }
                        }
                    }
                    .addOnFailureListener {
                        Log.d(TAG, it.message.toString())}
            }
    }
}
