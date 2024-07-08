package com.example.tudoem1.webservices

import android.text.TextUtils
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit


const val CONNECT_TIMEOUT = "CONNECT_TIMEOUT"
const val READ_TIMEOUT = "READ_TIMEOUT"
const val WRITE_TIMEOUT = "WRITE_TIMEOUT"

interface retrofitInterface {

    @POST("road-to-recovery/network/measures")
    fun postData(@Body postData: PostData?): Call<Any>

    companion object {

        private val interceptor = Interceptor { chain ->
            val request = chain.request()

            var connectTimeout = chain.connectTimeoutMillis()
            var readTimeout = chain.readTimeoutMillis()
            var writeTimeout = chain.writeTimeoutMillis()

            val connect = request.header(CONNECT_TIMEOUT)
            val read = request.header(READ_TIMEOUT)
            val write = request.header(WRITE_TIMEOUT)

            if (!TextUtils.isEmpty(connect)) {
                connectTimeout = connect!!.toInt()
            }

            if (!TextUtils.isEmpty(read)) {
                readTimeout = read!!.toInt()
            }

            if (!TextUtils.isEmpty(write)) {
                writeTimeout = write!!.toInt()
            }

            chain.withConnectTimeout(connectTimeout, TimeUnit.MILLISECONDS)
                .withReadTimeout(readTimeout, TimeUnit.MILLISECONDS)
                .withWriteTimeout(writeTimeout, TimeUnit.MILLISECONDS)
                .proceed(request)
        }

        private var okHttpClient: OkHttpClient = OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.MINUTES)
            .readTimeout(3, TimeUnit.MINUTES)
            .writeTimeout(3, TimeUnit.MINUTES)
            .addInterceptor(interceptor)
            .build()

//        private var BASE_URL = "https://tidycity.logimade.pt/server/api/"
        private var BASE_URL = "https://3e8a45907f206024d552541b0862719e.serveo.net/"


        operator fun invoke(): retrofitInterface {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient)
                .build()
                .create(retrofitInterface::class.java)
        }
    }
}