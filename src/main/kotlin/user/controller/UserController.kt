package com.wafflestudio.seminar.spring2023.user.controller

import com.wafflestudio.seminar.spring2023.user.service.*
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
class UserController(
    private val userService: UserService,
) {

    @PostMapping("/api/v1/signup")
    fun signup(
        @RequestBody request: SignUpRequest,
    ): ResponseEntity<Unit> {
        val (username, password, image) = request
        try {
            userService.signUp(username = username, password = password, image = image)
        } catch (e: SignUpBadUsernameException) {
            return ResponseEntity.badRequest().build()
        } catch (e: SignUpBadPasswordException) {
            return ResponseEntity.badRequest().build()
        } catch (e: SignUpUsernameConflictException) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build()
        }
        return ResponseEntity.ok().build()
    }

    @PostMapping("/api/v1/signin")
    fun signIn(
        @RequestBody request: SignInRequest,
    ): ResponseEntity<SignInResponse> {
        val (username, password) = request
        val user = try {
            userService.signIn(username = username, password = password)
        } catch (e: SignInUserNotFoundException) {
            return ResponseEntity.notFound().build()
        } catch (e: SignInInvalidPasswordException) {
            return ResponseEntity.notFound().build()
        }
        return ResponseEntity.ok(SignInResponse(accessToken = user.getAccessToken()))
    }

    @GetMapping("/api/v1/users/me")
    fun me(
        @RequestHeader(name = "Authorization", required = false) authorizationHeader: String?,
    ): ResponseEntity<UserMeResponse> {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        val token = authorizationHeader.substring(7)
        val (username, image) = try {
            userService.authenticate(accessToken = token)
        } catch (e: AuthenticateException) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        return ResponseEntity.ok(UserMeResponse(username = username, image = image))
    }

}

data class UserMeResponse(
    val username: String,
    val image: String,
)

data class SignUpRequest(
    val username: String,
    val password: String,
    val image: String,
)

data class SignInRequest(
    val username: String,
    val password: String,
)

data class SignInResponse(
    val accessToken: String,
)
