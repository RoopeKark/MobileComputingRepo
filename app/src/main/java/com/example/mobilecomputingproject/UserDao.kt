package com.example.mobilecomputingproject

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Query("DELETE FROM user_table")
    suspend fun deleteAllUsers()

    @Insert
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)

    @Query("SELECT * FROM user_table WHERE userId = :id")
    suspend fun getUserById(id: Int): User?

    @Query("SELECT * FROM user_table ORDER BY user_name ASC")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM user_table ORDER BY userId DESC LIMIT 1")
    suspend fun getUser(): User?
}

