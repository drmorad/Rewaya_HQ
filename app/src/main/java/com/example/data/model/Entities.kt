package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "hotels")
data class Hotel(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val nameEn: String,
    val nameAr: String,
    val hotelType: String
)

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val email: String,
    val titlePrefix: String, // "Director", "Dr.", "Mr."
    val firstName: String,
    val lastName: String,
    val displayRole: String, // e.g. "Director of Quality Control & Hygiene Department"
    val roleCode: String, // "SUPER_ADMIN", "QUALITY_MANAGER", "AUDITOR"
    val assignedHotelId: String? // Null for Super Admin Dr. Morad
) {
    val fullNameWithTitle: String get() = "$titlePrefix $firstName $lastName".trim()
}

@Entity(tableName = "audits")
data class Audit(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val hotelId: String,
    val inspectorName: String,
    val inspectorRole: String,
    val departmentName: String, // "F&B", "Housekeeping", "Back of House", "Public Areas", "Admin"
    val complianceScore: Float,
    val status: String = "COMPLETED", // "IN_PROGRESS", "COMPLETED"
    val gpsLatitude: Double? = null,
    val gpsLongitude: Double? = null,
    val signatureInspectorPath: String? = null,
    val signatureManagerPath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "audit_answers")
data class AuditAnswer(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val auditId: String,
    val questionId: String,
    val questionTextEn: String,
    val questionTextAr: String,
    val section: String, // "Receiving", "Prep Kitchens", "Hygiene", "Dining" etc.
    val responseType: String, // "BINARY", "RATING"
    val binaryValue: Boolean? = null, // true = Yes, false = No, null = N/A
    val ratingValue: Int? = null, // 1-5 for rating scale
    val comment: String? = null,
    val mediaPath: String? = null, // Photo evidence path
    val maintenanceRequested: Boolean? = null, // Conditional Field: display if Yes/No failure occurs
    val maintenanceDetails: String? = null
)

@Entity(tableName = "corrective_actions")
data class CorrectiveAction(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val auditId: String,
    val auditAnswerId: String,
    val issueDescription: String,
    val hotelId: String,
    val departmentName: String,
    val priority: String, // "URGENT", "IMPORTANT", "ROUTINE"
    val assignedToUserId: String, // User ID of responsible manager
    val status: String = "OPEN", // "OPEN", "RESOLVED"
    val dueDate: Long,
    val resolvedAt: Long? = null,
    val reInspectionNotes: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "hazards")
data class QuickHazard(
    @PrimaryKey val id: String = UUID.randomUUID().toString(),
    val hotelId: String,
    val departmentName: String,
    val zone: String,
    val description: String,
    val priority: String,
    val reporterName: String,
    val mediaPath: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val status: String = "PENDING" // "PENDING", "RESOLVED"
)
