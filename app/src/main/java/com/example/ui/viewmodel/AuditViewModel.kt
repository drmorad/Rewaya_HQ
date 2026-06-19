package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AuditRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AuditViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: AuditRepository

    // Base flows
    val hotels: StateFlow<List<Hotel>>
    val users: StateFlow<List<User>>
    val audits: StateFlow<List<Audit>>
    val correctiveActions: StateFlow<List<CorrectiveAction>>
    val hazards: StateFlow<List<QuickHazard>>

    // Session States
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> get() = _currentUser

    private val _currentLanguage = MutableStateFlow("EN") // EN or AR
    val currentLanguage: StateFlow<String> get() = _currentLanguage

    private val _selectedHotelFilter = MutableStateFlow<String?>(null) // null = All
    val selectedHotelFilter: StateFlow<String?> get() = _selectedHotelFilter

    // Gamification Points for active inspectors (simulated offline persistence for demo)
    private val _userPoints = MutableStateFlow(350)
    val userPoints: StateFlow<Int> get() = _userPoints

    init {
        val db = AppDatabase.getDatabase(application)
        repository = AuditRepository(
            hotelDao = db.hotelDao(),
            userDao = db.userDao(),
            auditDao = db.auditDao(),
            auditAnswerDao = db.auditAnswerDao(),
            correctiveActionDao = db.correctiveActionDao(),
            quickHazardDao = db.quickHazardDao()
        )

        hotels = repository.allHotels.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        users = repository.allUsers.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        audits = repository.allAudits.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        correctiveActions = repository.allCorrectiveActions.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        hazards = repository.allHazards.stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

        viewModelScope.launch {
            // Seed base values
            repository.seedDatabase()
            // Set default login as Dr. Morad (Director) on successful seeding
            users.filter { it.isNotEmpty() }.collectLatest { userList ->
                if (_currentUser.value == null) {
                    _currentUser.value = userList.firstOrNull { it.id == "user_morad_id" }
                }
            }
        }
    }

    fun switchUser(userId: String) {
        viewModelScope.launch {
            _currentUser.value = users.value.firstOrNull { it.id == userId }
        }
    }

    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    fun setHotelFilter(hotelId: String?) {
        _selectedHotelFilter.value = hotelId
    }

    // Resolve an issues
    fun resolveCorrectiveAction(actionId: String, notes: String) {
        viewModelScope.launch {
            val currentList = correctiveActions.value
            val action = currentList.firstOrNull { it.id == actionId }
            if (action != null) {
                repository.updateCorrectiveAction(
                    action.copy(
                        status = "RESOLVED",
                        resolvedAt = System.currentTimeMillis(),
                        reInspectionNotes = notes
                    )
                )
                _userPoints.value += 50 // award points on fixing issues!
            }
        }
    }

    // Report a quick hazard in under 30 seconds
    fun reportQuickHazard(hotelId: String, dept: String, zone: String, desc: String, priority: String) {
        viewModelScope.launch {
            val reporter = _currentUser.value?.fullNameWithTitle ?: "Inspector"
            val hazard = QuickHazard(
                hotelId = hotelId,
                departmentName = dept,
                zone = zone,
                description = desc,
                priority = priority,
                reporterName = reporter
            )
            repository.insertHazard(hazard)
            _userPoints.value += 20 // award points for reporting hazard
        }
    }

    // Interactive Checklist Framework
    fun submitInspection(
        hotelId: String,
        deptName: String,
        answers: List<TempAnswer>
    ) {
        viewModelScope.launch {
            val auditId = UUID.randomUUID().toString()
            val inspector = _currentUser.value

            // Compute Score
            var totalCount = 0
            var passedCount = 0
            answers.forEach { item ->
                if (item.responseType == "BINARY") {
                    if (item.binaryValue != null) {
                        totalCount++
                        if (item.binaryValue) {
                            passedCount++
                        }
                    }
                } else if (item.responseType == "RATING") {
                    if (item.ratingValue != null) {
                        totalCount++
                        // Rate 3, 4, 5 counts as passed, 1 or 2 as failed
                        if (item.ratingValue >= 3) {
                            passedCount++
                        }
                    }
                }
            }

            val score = if (totalCount > 0) (passedCount.toFloat() / totalCount.toFloat()) * 100f else 100f

            val audit = Audit(
                id = auditId,
                hotelId = hotelId,
                inspectorName = inspector?.fullNameWithTitle ?: "Inspector",
                inspectorRole = inspector?.displayRole ?: "Quality Inspector",
                departmentName = deptName,
                complianceScore = score,
                status = "COMPLETED",
                gpsLatitude = 25.0754 + (Math.random() - 0.5) * 0.005, // simulated geo variation
                gpsLongitude = 55.1887 + (Math.random() - 0.5) * 0.005,
                createdAt = System.currentTimeMillis()
            )

            val finalAnswers = answers.map { temp ->
                AuditAnswer(
                    id = UUID.randomUUID().toString(),
                    auditId = auditId,
                    questionId = temp.questionId,
                    questionTextEn = temp.questionTextEn,
                    questionTextAr = temp.questionTextAr,
                    section = temp.section,
                    responseType = temp.responseType,
                    binaryValue = temp.binaryValue,
                    ratingValue = temp.ratingValue,
                    comment = temp.comment,
                    mediaPath = temp.mediaPath,
                    maintenanceRequested = temp.maintenanceRequested,
                    maintenanceDetails = temp.maintenanceDetails
                )
            }

            repository.insertAuditWithAnswers(audit, finalAnswers)

            // Auto-Generate Corrective Action for any failed item (Binary = NO/False, Rating = 1 or 2)
            finalAnswers.forEach { ans ->
                val isFailedBinary = ans.responseType == "BINARY" && ans.binaryValue == false
                val isFailedRating = ans.responseType == "RATING" && ans.ratingValue != null && ans.ratingValue <= 2
                if (isFailedBinary || isFailedRating) {
                    val priority = if (isFailedBinary) "URGENT" else "IMPORTANT"
                    
                    // Assign to correct manager based on hotel
                    val assignedUserId = when (hotelId) {
                        "hotel_majestic_id" -> "user_hussien_id"
                        "hotel_resort_id" -> "user_sheriff_id"
                        "hotel_inn_id" -> "user_hassan_id"
                        else -> "user_morad_id"
                    }

                    val action = CorrectiveAction(
                        id = UUID.randomUUID().toString(),
                        auditId = auditId,
                        auditAnswerId = ans.id,
                        issueDescription = "[Failed during F&B Audit - ${ans.section}] ${ans.questionTextEn}. Note: ${ans.comment ?: "No comment provided."}",
                        hotelId = hotelId,
                        departmentName = deptName,
                        priority = priority,
                        assignedToUserId = assignedUserId,
                        status = "OPEN",
                        dueDate = System.currentTimeMillis() + 24 * 3600 * 1000, // Due in 24 hours
                        createdAt = System.currentTimeMillis()
                    )
                    repository.insertCorrectiveAction(action)
                }
            }

            _userPoints.value += 100 // Successful inspection rewards major points
        }
    }
}

// Temporary State Representation during audit session
data class TempAnswer(
    val questionId: String,
    val questionTextEn: String,
    val questionTextAr: String,
    val section: String,
    val responseType: String,
    val binaryValue: Boolean? = null,
    val ratingValue: Int? = null,
    val comment: String? = null,
    val mediaPath: String? = null,
    val maintenanceRequested: Boolean? = null,
    val maintenanceDetails: String? = null
)
