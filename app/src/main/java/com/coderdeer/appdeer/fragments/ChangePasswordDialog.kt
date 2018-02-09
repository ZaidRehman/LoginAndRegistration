package com.coderdeer.appdeer.fragments

import android.app.DialogFragment
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coderdeer.appdeer.R
import android.support.annotation.Nullable
import com.coderdeer.appdeer.Profile
import com.coderdeer.appdeer.model.Response
import com.coderdeer.appdeer.model.User
import com.coderdeer.appdeer.network.NetworkUtil
import com.coderdeer.appdeer.utils.EMAIL
import com.coderdeer.appdeer.utils.TOKEN
import com.coderdeer.appdeer.utils.validateFields
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.dialog_change_password.*
import retrofit2.HttpException
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.IOException


class ChangePasswordDialog : DialogFragment() {

    interface Listener {

        fun onPasswordChanged()
    }

    companion object {
        val TAG = ChangePasswordDialog::class.java.simpleName
    }

    private lateinit var mSubscriptions: CompositeSubscription
    private lateinit var mToken: String
    private lateinit var mEmail: String
    private lateinit var mListener: Listener


    @Nullable
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater?.inflate(R.layout.dialog_change_password,container,false)
        mSubscriptions = CompositeSubscription()
        getData()

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun getData(){
        val bundle = arguments
        mToken = bundle.getString(TOKEN)
        mEmail = bundle.getString(EMAIL)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mListener = context as Profile
    }

    private fun initViews(){

        btn_change_password.setOnClickListener{ changePassword() }
        btn_cancel.setOnClickListener{ dismiss() }

    }

    private fun changePassword(){
        setError()

        val oldPassword = et_old_password.text.toString()
        val newPassword = et_new_password.text.toString()

        var err = 0

        if(!oldPassword.validateFields()){
            err++
            ti_old_password.error = "Password should not be empty!"
        }
        if (!newPassword.validateFields()){
            err ++
            ti_new_password.error = "Password should not be empty!"
        }

        if (err == 0){
            val user = User(password = oldPassword)
            user.setNewPassword(newPassword)

            changePasswordProgress(user)
            progress.visibility = View.VISIBLE
        }
    }

    private fun setError(){
        ti_old_password.error = null
        ti_new_password.error = null
    }

    private fun changePasswordProgress(user : User){
        mSubscriptions.add(NetworkUtil.getRetrofit(mToken).changePassword(mEmail,user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(this::handleResponse,this::handleError))
    }

    private fun handleResponse(response : Response){
        progress.visibility = View.GONE
        mListener.onPasswordChanged()
        dismiss()
    }

    private fun handleError(error : Throwable){
        progress.visibility = View.GONE

        if(error is HttpException){
            val gson = GsonBuilder().create()
            try {
                val errorBody = error.response().errorBody()?.string()
                val response = gson.fromJson<Response>(errorBody,Response::class.java)
                showMessage(response.message)
            }catch (e: IOException){
                showMessage("Network Error!")
            }
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