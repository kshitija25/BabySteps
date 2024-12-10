package com.example.babysteps

data class userMommyModel(
    val loginCredentials: LoginCredentials,
    val userProfile: UserProfile
) {
    data class LoginCredentials(
        val email: String?
    )

    data class UserProfile(
        val userId: String?,
        val userName: String?
    )
}


