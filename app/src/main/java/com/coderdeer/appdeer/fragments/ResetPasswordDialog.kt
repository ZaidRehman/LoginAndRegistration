package com.coderdeer.appdeer.fragments

import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.support.annotation.Nullable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coderdeer.appdeer.MainActivity
import com.coderdeer.appdeer.R
import com.coderdeer.appdeer.model.Response
import com.coderdeer.appdeer.model.User
import com.coderdeer.appdeer.network.NetworkUtil
import com.coderdeer.appdeer.utils.validateEmail
import com.coderdeer.appdeer.utils.validateFields
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.dialog_reset_password.*
import retrofit2.HttpException
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.IOException


class ResetPasswordDialog : DialogFragment(){
    interface Listener{
        fun onPasswordReset(message: String)
    }

    companion object {
        val TAG: String = ResetPasswordDialog::class.java.simpleName
    }

    private lateinit var mSubscriptions : CompositeSubscription
    private lateinit var mEmail: String
    private var isInit = true
    private lateinit var mListener: Listener

    @Nullable
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.dialog_reset_password,container,false)
        mSubscriptions = CompositeSubscription()

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as MainActivity
    }

    private  fun initViews(){
        btn_reset_password.setOnClickListener{
            if(isInit) resetPasswordInit()
            else resetPasswordFinish()
        }
    }

    private fun setEmptyFields(){
        ti_email.error = null
        ti_token.error = null
        ti_password.error = null
        tv_message.text = null
    }

    private fun setToken (token: String){
        et_token.setText(token)
    }

    private fun resetPasswordInit(){
        setEmptyFields()
        mEmail = et_email.text.toString()
        var err = 0

        if(!mEmail.validateEmail()){
            err++
            ti_email.error = "Email Should be Valid!"
        }

        if (err == 0){
            progress.visibility = View.VISIBLE
            resetPasswordInitProgress(mEmail)
        }

    }

    private fun resetPasswordFinish(){
        setEmptyFields()

        val token = et_token.text.toString()
        val password = et_password.text.toString()

        var err= 0
        if(!token.validateFields()){
            err++
            ti_token.error = "Token Should not be empty"
        }

        if (!password.validateFields()){
            err++
            ti_password.error = "Password Should not be empty!"
        }

        if (err==0){
            progress.visibility = View.VISIBLE

            val user = User(password = password)
            user.setToken(token)
            resetPasswordFinishProgress(user)
        }
    }

    private fun resetPasswordInitProgress(email : String){
        mSubscriptions.add(NetworkUtil.getRetrofit().resetPasswordInit(email)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError))
    }

    private fun resetPasswordFinishProgress(user :User){
        mSubscriptions.add(NetworkUtil.getRetrofit().resetPasswordFinish(mEmail,user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError))
    }

    private fun handleResponse(response: Response){
        progress.visibility = View.GONE

        if(isInit){

            isInit = false
            showMessage(response.message)
            ti_email.visibility = View.GONE
            ti_token.visibility = View.VISIBLE
            ti_password.visibility = View.VISIBLE

        }else{

            mListener.onPasswordReset(response.message)
            dismiss()
        }
    }

    private fun handleError(error: Throwable){
        progress.visibility = View.GONE
        if (error is HttpException){
            val gson = GsonBuilder().create()
            try {
                val errorBody = error.response().errorBody()?.string()
                val response = gson.fromJson<Response>(errorBody,Response::class.java)
                showMessage(response.message)

            }catch (e:IOException){
                e.printStackTrace()
            }
        }else{
            showMessage("Network Error !")
        }
    }

    private fun showMessage(message: String){
        tv_message.visibility = View.VISIBLE
        tv_message.text = message
    }

    override fun onDestroy() {
        super.onDestroy()
        mSubscriptions.unsubscribe()
    }
}