package com.coderdeer.appdeer

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.util.Log
import com.coderdeer.appdeer.fragments.LoginFragment
import com.coderdeer.appdeer.fragments.ResetPasswordDialog
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(),ResetPasswordDialog.Listener {

    companion object {
        val TAG: String = MainActivity::class.java.simpleName
    }

    private lateinit var mLoginFragment: LoginFragment
    private lateinit var mResetPasswordDialog: ResetPasswordDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if (savedInstanceState == null){
            loadFragment()
        }
    }

    private fun loadFragment(){
        if (!::mLoginFragment.isInitialized){
            mLoginFragment = LoginFragment()
        }
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentFrame,mLoginFragment,LoginFragment.TAG).commit()
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val data = intent?.data?.lastPathSegment
        Log.d(TAG, "onNewIntent: $data")

        mResetPasswordDialog = fragmentManager
                .findFragmentByTag(ResetPasswordDialog.TAG) as ResetPasswordDialog
    }

    override fun onPasswordReset(message: String) {
        showSnackBarMessage(message)
    }

    private fun showSnackBarMessage(message : String){
        Snackbar.make(activity_main,message,Snackbar.LENGTH_SHORT).show()
    }
}
