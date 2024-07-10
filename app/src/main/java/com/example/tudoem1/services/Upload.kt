package com.example.tudoem1.services

import android.app.IntentService
import android.content.Intent
import android.util.Log
import android.os.Handler
import android.os.Looper
import com.example.tudoem1.databaseUtils.DatabasePrototype
import com.example.tudoem1.databaseUtils.MeasureStructure
import com.example.tudoem1.databaseUtils.MeasureWithMetrics
import com.example.tudoem1.databaseUtils.MetricStructure
import com.example.tudoem1.webservices.Measure
import com.example.tudoem1.webservices.PostData
import com.example.tudoem1.webservices.retrofitInterface
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class UploadService : IntentService("UploadService") {

    private val db = DatabasePrototype.getDatabase(this)
    private val handler = Handler(Looper.getMainLooper())
    private val serviceScope = CoroutineScope(Dispatchers.Main) // Or another appropriate dispatcher
    lateinit var dataToUpload: List<MeasureWithMetrics>
    lateinit var postData: PostData

    private val uploadRunnable = object : Runnable {
        override fun run() {
            serviceScope.launch {
                dataToUpload = db.daoNetworkMethods().getMeasuresToUpload()

                if (dataToUpload.isNotEmpty()) {
                    dataToUpload.forEach {
                        postData = PostData(
                            acquisition = it.acquisition,
                            measures = metricStructureListToMeasureList(it.metrics)
                        )
                    }
                }

                val jsonPayload = Gson().toJson(postData)
                Log.d("RetrofitPayload", jsonPayload)


                // Perform upload operation
                val call = retrofitInterface().postData(postData)
                call.enqueue(object : Callback<Any> {
                    override fun onResponse(call: Call<Any>, response: Response<Any>) {
                        if (response.isSuccessful) {
                            Log.d("Retrofit", "Data posted successfully")
                        } else {
                            Log.d("Retrofit", "Failed to post data")
                        }
                    }

                    override fun onFailure(call: Call<Any>, t: Throwable) {
                        Log.d("Retrofit", "Error: " + t.message)
                    }
                })
            }

            // Schedule the next check after a delay
            handler.postDelayed(this, CHECK_INTERVAL_MS)
        }
    }

    override fun onHandleIntent(intent: Intent?) {
        // Start the upload task immediately
        handler.post(uploadRunnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Remove pending callbacks to stop the service properly
        handler.removeCallbacks(uploadRunnable)
    }

    private fun metricStructureListToMeasureList(metricStructureList: List<MetricStructure>): List<Measure> {
        return metricStructureList.map {
            Measure(
                timestamp = it.timeStamp,
                coordinates = it.coordinates,
                metrics = it.metrics
            )
        }
    }

    companion object {
        private const val TAG = "UploadService"
        private const val CHECK_INTERVAL_MS = 300000L // Check every 5 minutes (adjust as needed)
    }
}
