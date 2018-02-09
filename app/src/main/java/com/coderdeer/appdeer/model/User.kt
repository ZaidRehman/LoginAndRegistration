package com.coderdeer.appdeer.model


class User(val name:String = "",val email: String= "",val password: String = "") {

    val createdAt: String = ""
    private var newPassword: String = ""
    private var token: String = ""

    fun setNewPassword(newPassword: String) {
        this.newPassword = newPassword
    }

    fun setToken(token: String) {
        this.token = token
    }
}