package com.coderdeer.appdeer.fragments

import android.app.Fragment
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.coderdeer.appdeer.R
import com.coderdeer.appdeer.model.Response
import com.coderdeer.appdeer.model.User
import com.coderdeer.appdeer.network.NetworkUtil
import com.coderdeer.appdeer.utils.validateEmail
import com.coderdeer.appdeer.utils.validateFields
import com.google.gson.GsonBuilder
import kotlinx.android.synthetic.main.fragment_register.*
import android.support.annotation.Nullable
import retrofit2.HttpException
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.io.IOException

class RegisterFragment : Fragment(){

    companion object {
        val TAG: String = RegisterFragment::class.java.simpleName
    }


    private lateinit var mSubscriptions: CompositeSubscription

    @Nullable
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val view : View? = inflater?.inflate(R.layout.fragment_register,container,false)
        mSubscriptions = CompositeSubscription()

        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViews()
    }

    private fun initViews(){

        btn_register.setOnClickListener { register() }
        tv_login.setOnClickListener { goToLogin()}

    }

    private fun register(){
        setError()

        val name = et_name.text.toString()
        val email = et_email.text.toString()
        val password = et_password.text.toString()

        var err = 0

        if(!name.validateFields()){
            ti_name.error = "Name should not be empty !"
            err++
        }

        if (!email.validateEmail()){
            ti_email.error = "Email should be valid"
            err++
        }

        if(!password.validateFields()){
            ti_password.error = "Password should not be empty"
            err++
        }

        if (err == 0){
            val user = User(name,email,password)

            progress.visibility = View.VISIBLE
            registerProcess(user)

        }else{
            showSnackBarMessage("Enter Valid Details")

        }
    }

    private fun registerProcess(user: User) {

        mSubscriptions.add(NetworkUtil.getRetrofit().register(user)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe( this::handleResponse ,  this::handleError ))
    }

    private fun handleResponse(response: Response){
        progress.visibility = View.GONE
        showSnackBarMessage(response.message)
    }

    private fun handleError(error: Throwable){
        progress.visibility = View.GONE
        if(error is HttpException ){
            val gson = GsonBuilder().create()
            try {
                val errorBody = error.response().errorBody()?.string()
                val response = gson.fromJson<Response>(errorBody,Response::class.java)
                showSnackBarMessage(response.message)

            }catch (e:IOException){
                e.printStackTrace()
            }
        }else{
            showSnackBarMessage("Network Error")
        }
    }

    private fun showSnackBarMessage(message: String){
        if (view != null){
            Snackbar.make(view,message,Snackbar.LENGTH_SHORT).show()
        }
    }


    private fun setError(){
        ti_name.error = null
        ti_email.error = null
        ti_password.error = null
    }

    private fun goToLogin(){
        val ft = fragmentManager.beginTransaction()
        val fragment  = LoginFragment()
        ft.replace(R.id.fragmentFrame,fragment,LoginFragment.TAG)
        ft.commit()
    }

    override fun onDestroy() {
        super.onDestroy()
        mSubscriptions.unsubscribe()
    }
}