package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface HotelDao {
    @Query("SELECT * FROM hotels")
    fun getAllHotels(): Flow<List<Hotel>>

    @Query("SELECT * FROM hotels")
    suspend fun getAllHotelsList(): List<Hotel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHotels(hotels: List<Hotel>)
}

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users")
    suspend fun getAllUsersList(): List<User>

    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>)
}

@Dao
interface AuditDao {
    @Query("SELECT * FROM audits ORDER BY createdAt DESC")
    fun getAllAudits(): Flow<List<Audit>>

    @Query("SELECT * FROM audits WHERE hotelId = :hotelId ORDER BY createdAt DESC")
    fun getAuditsByHotel(hotelId: String): Flow<List<Audit>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAudit(audit: Audit)

    @Query("DELETE FROM audits WHERE id = :auditId")
    suspend fun deleteAudit(auditId: String)
}

@Dao
interface AuditAnswerDao {
    @Query("SELECT * FROM audit_answers WHERE auditId = :auditId")
    fun getAnswersForAudit(auditId: String): Flow<List<AuditAnswer>>

    @Query("SELECT * FROM audit_answers WHERE auditId = :auditId")
    suspend fun getAnswersForAuditList(auditId: String): List<AuditAnswer>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnswers(answers: List<AuditAnswer>)
}

@Dao
interface CorrectiveActionDao {
    @Query("SELECT * FROM corrective_actions ORDER BY createdAt DESC")
    fun getAllCorrectiveActions(): Flow<List<CorrectiveAction>>

    @Query("SELECT * FROM corrective_actions WHERE assignedToUserId = :userId ORDER BY createdAt DESC")
    fun getCorrectiveActionsForUser(userId: String): Flow<List<CorrectiveAction>>

    @Query("SELECT * FROM corrective_actions WHERE hotelId = :hotelId ORDER BY createdAt DESC")
    fun getCorrectiveActionsForHotel(hotelId: String): Flow<List<CorrectiveAction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCorrectiveAction(action: CorrectiveAction)

    @Update
    suspend fun updateCorrectiveAction(action: CorrectiveAction)
}

@Dao
interface QuickHazardDao {
    @Query("SELECT * FROM hazards ORDER BY createdAt DESC")
    fun getAllHazards(): Flow<List<QuickHazard>>

    @Query("SELECT * FROM hazards WHERE hotelId = :hotelId ORDER BY createdAt DESC")
    fun getHazardsForHotel(hotelId: String): Flow<List<QuickHazard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHazard(hazard: QuickHazard)

    @Update
    suspend fun updateHazard(hazard: QuickHazard)
}
