package com.coderdeer.appdeer.fragments

import android.app.Fragment
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coderdeer.appdeer.Profile
import com.coderdeer.appdeer.R
import com.coderdeer.appdeer.model.Response
import com.coderdeer.appdeer.network.NetworkUtil
import com.coderdeer.appdeer.utils.EMAIL
import com.coderdeer.appdeer.utils.TOKEN
import com.coderdeer.appdeer.utils.validateEmail
import com.coderdeer.appdeer.utils.validateFields
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_login.*
import android.support.annotation.Nullable
import retrofit2.HttpException
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.IOException

class LoginFragment : Fragment(){
    //val TAG: String = LoginFragment::class.java.simpleName
    companion object {
        val TAG: String = LoginFragment::class.java.simpleName
    }

    private lateinit var mSubscriptions: CompositeSubscription
    private lateinit var mSharedPreferences: SharedPreferences


    @Nullable
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.fragment_login,container,false)
        mSubscriptions = CompositeSubscription()

        initSharedPreferences()
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews(){
        btn_login.setOnClickListener{ login() }
        tv_register.setOnClickListener { goToRegister() }
        tv_forgot_password.setOnClickListener { showDialog() }
    }

    private fun login(){
        setError()

        val email = et_email.text.toString()
        val password = et_password.text.toString()

        var err = 0
        if(!email.validateEmail()){
            err++
            ti_email.error = "Email should be valid!"
        }

        if(!password.validateFields()){
            err++
            ti_password.error = "Password should not be empty!"
        }

        if(err == 0){
            loginProcess(email,password)
            progress.visibility = View.VISIBLE
        }else {
            showSnackBarMessage("Enter Valid Details !")
        }
    }

    private fun setError(){
        ti_email.error = null
        ti_password.error = null
    }

    private fun loginProcess(email: String,password: String){
        mSubscriptions.add(NetworkUtil.getRetrofit(email,password).login()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError))
    }

    private fun handleResponse(response: Response){
        progress.visibility = View.GONE

        val editor = mSharedPreferences.edit()
        editor.putString(TOKEN,response.token)
        editor.putString(EMAIL,response.message)
        editor.apply()

        et_email.text = null
        et_password.text = null

        val intent = Intent(activity,Profile::class.java)
        startActivity(intent)

    }

    private fun handleError(error: Throwable){
        progress.visibility = View.GONE

        if(error is HttpException){
            val gson = GsonBuilder().create()
            try {
                val errorBody = error.response().errorBody()?.string()
                val response = gson.fromJson<Response>(errorBody,Response::class.java)
                showSnackBarMessage(response.message)

            }catch (e:IOException){
                e.printStackTrace()
            }
        }
    }

    private fun goToRegister(){

        val ft = fragmentManager.beginTransaction()
        val fragment = RegisterFragment()
        ft.replace(R.id.fragmentFrame,fragment,RegisterFragment.TAG)
        ft.commit()
    }

    private fun showDialog(){
        val fragment = ResetPasswordDialog()
        fragment.show(fragmentManager,ResetPasswordDialog.TAG)
    }

    private fun showSnackBarMessage(message : String){
        if (view != null){
            Snackbar.make(view,message,Snackbar.LENGTH_SHORT).show()
        }
    }

    private fun initSharedPreferences(){
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        mSubscriptions.unsubscribe()
    }
}