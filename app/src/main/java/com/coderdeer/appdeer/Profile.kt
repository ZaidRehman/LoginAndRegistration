package com.coderdeer.appdeer

import android.content.SharedPreferences
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.view.View
import com.coderdeer.appdeer.fragments.ChangePasswordDialog
import com.coderdeer.appdeer.model.Response
import com.coderdeer.appdeer.model.User
import com.coderdeer.appdeer.network.NetworkUtil
import com.coderdeer.appdeer.utils.EMAIL
import com.coderdeer.appdeer.utils.TOKEN
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.activity_profile.*
import retrofit2.HttpException
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.IOException

class Profile : AppCompatActivity(), ChangePasswordDialog.Listener {

    companion object {
        val TAG : String = Profile::class.java.simpleName
    }

    private lateinit var mSubscription: CompositeSubscription
    private lateinit var mToken : String
    private lateinit var mEmail: String
    private lateinit var mSharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        initViews()
        initSharedPreferences()
        loadProfile()
    }

    private fun initViews(){
        btn_change_password.setOnClickListener{ showDialog() }
        btn_logout.setOnClickListener{ logout() }
    }

    private fun initSharedPreferences(){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        mToken = mSharedPreferences.getString(TOKEN,"")
        mEmail = mSharedPreferences.getString(EMAIL,"")
    }

    private fun logout(){

        val editor = mSharedPreferences.edit()
        editor.putString(EMAIL,"")
        editor.putString(TOKEN,"")
        editor.apply()
        finish()
    }

    private fun showDialog(){
        val fragment = ChangePasswordDialog()
        val bundle = Bundle()
        bundle.putString(EMAIL,"")
        bundle.putString(TOKEN,"")
        fragment.arguments = bundle

        fragment.show(fragmentManager,ChangePasswordDialog.TAG)
    }

    private fun loadProfile(){
        mSubscription.add(NetworkUtil.getRetrofit(mToken).getProfile(mEmail)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError))
    }

    private fun handleResponse(user: User){
        progress.visibility = View.GONE
        tv_name.text = user.name
        tv_email.text = user.email
        tv_date.text = user.createdAt
    }

    private fun handleError(error: Throwable){
        progress.visibility = View.GONE
        if(error is HttpException){
            val gson = GsonBuilder().create()
            try {
                val errorBody = error.response().errorBody()?.string()
                val response = gson.fromJson<Response>(errorBody,Response::class.java)
                showSnackBarMessage(response.message)
            }catch (e: IOException){
                e.printStackTrace()
            }
        }else{
            showSnackBarMessage("Network Error !")
        }
    }

    private fun showSnackBarMessage(message: String){
        Snackbar.make(activity_profile,message,Snackbar.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSubscription.unsubscribe()
    }

    override fun onPasswordChanged() {
        showSnackBarMessage("Password Changed Successfully !")
    }

}
