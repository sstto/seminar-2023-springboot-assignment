package com.wafflestudio.seminar.spring2023.user.service

import com.wafflestudio.seminar.spring2023.user.repository.UserEntity
import com.wafflestudio.seminar.spring2023.user.repository.UserRepository
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    private val userRepository: UserRepository,
) : UserService {

    override fun signUp(username: String, password: String, image: String): User {
        if (username.length < 4) {
            throw SignUpBadUsernameException()
        }
        if (password.length < 4) {
            throw SignUpBadPasswordException()
        }
        userRepository.findByUsername(username)?.let { throw SignUpUsernameConflictException() }
        userRepository.save(UserEntity(username = username, password = password, image = image))
        return User(username, image)
    }

    override fun signIn(username: String, password: String): User {
        val user = userRepository.findByUsername(username) ?: throw SignInUserNotFoundException()
        if (password != user.password) {
            throw SignInInvalidPasswordException()
        }
        return User(username, user.image)
    }

    override fun authenticate(accessToken: String): User {
        val users = userRepository.findAll()
            .map { User(it.username, it.image) }
            .find { it.getAccessToken() == accessToken }
        return users ?: throw AuthenticateException()
    }
    
}
