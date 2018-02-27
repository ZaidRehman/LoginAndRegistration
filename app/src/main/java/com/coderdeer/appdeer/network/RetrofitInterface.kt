package com.coderdeer.appdeer.network

import com.coderdeer.appdeer.model.Response
import com.coderdeer.appdeer.model.User
import retrofit2.http.*
import rx.Observable


/**
 * Created by qureshi on 27/01/18.
 */
interface RetrofitInterface {
    @Headers(
        "Accept:application/json",
        "Content-Type:application/json",
        "User-Agent: Your-App-Name"
    )

    @POST("users")
    fun register(@Body user: User): Observable<Response>

    @POST("authenticate")
    fun login(): Observable<Response>

    @GET("users/{email}")
    fun getProfile(@Path("email") email: String): Observable<User>

    @PUT("users/{email}")
    fun changePassword(@Path("email") email: String, @Body user: User): Observable<Response>

    @POST("users/{email}/password")
    fun resetPasswordInit(@Path("email") email: String): Observable<Response>

    @POST("users/{email}/password")
    fun resetPasswordFinish(@Path("email") email: String, @Body user: User): Observable<Response>
}
