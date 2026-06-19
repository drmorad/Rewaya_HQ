package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class AuditRepository(
    private val hotelDao: HotelDao,
    private val userDao: UserDao,
    private val auditDao: AuditDao,
    private val auditAnswerDao: AuditAnswerDao,
    private val correctiveActionDao: CorrectiveActionDao,
    private val quickHazardDao: QuickHazardDao
) {
    val allHotels: Flow<List<Hotel>> = hotelDao.getAllHotels()
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    val allAudits: Flow<List<Audit>> = auditDao.getAllAudits()
    val allCorrectiveActions: Flow<List<CorrectiveAction>> = correctiveActionDao.getAllCorrectiveActions()
    val allHazards: Flow<List<QuickHazard>> = quickHazardDao.getAllHazards()

    fun getAnswersForAudit(auditId: String): Flow<List<AuditAnswer>> =
        auditAnswerDao.getAnswersForAudit(auditId)

    suspend fun insertAuditWithAnswers(audit: Audit, answers: List<AuditAnswer>) {
        auditDao.insertAudit(audit)
        auditAnswerDao.insertAnswers(answers)
    }

    suspend fun insertCorrectiveAction(action: CorrectiveAction) {
        correctiveActionDao.insertCorrectiveAction(action)
    }

    suspend fun updateCorrectiveAction(action: CorrectiveAction) {
        correctiveActionDao.updateCorrectiveAction(action)
    }

    suspend fun insertHazard(hazard: QuickHazard) {
        quickHazardDao.insertHazard(hazard)
    }

    suspend fun updateHazard(hazard: QuickHazard) {
        quickHazardDao.updateHazard(hazard)
    }

    suspend fun deleteAudit(auditId: String) {
        auditDao.deleteAudit(auditId)
    }

    suspend fun seedDatabase() {
        val existingHotels = hotelDao.getAllHotelsList()
        if (existingHotels.isNotEmpty()) return

        // 1. Seed Hotels with fixed UUIDs
        val majestic = Hotel(id = "hotel_majestic_id", nameEn = "Rewaya Majestic", nameAr = "رواية ماجستيك", hotelType = "Luxury Hotel")
        val inn = Hotel(id = "hotel_inn_id", nameEn = "Rewaya Inn", nameAr = "رواية إن", hotelType = "Budget Hotel")
        val resort = Hotel(id = "hotel_resort_id", nameEn = "Rewaya Luxury Hotel & Resort", nameAr = "رواية لوزري أوتيل & ريزورت", hotelType = "Luxury Resort")
        hotelDao.insertHotels(listOf(majestic, inn, resort))

        // 2. Seed Users
        val morad = User(
            id = "user_morad_id",
            email = "dr.morad@rewayahotels.com",
            titlePrefix = "Director",
            firstName = "Morad",
            lastName = "",
            displayRole = "Director of Quality Control & Hygiene Department",
            roleCode = "SUPER_ADMIN",
            assignedHotelId = null
        )
        val hussien = User(
            id = "user_hussien_id",
            email = "dr.mohamed.hussien@rewayahotels.com",
            titlePrefix = "Dr.",
            firstName = "Mohamed",
            lastName = "Hussien",
            displayRole = "Quality and Hygiene Manager",
            roleCode = "QUALITY_MANAGER",
            assignedHotelId = "hotel_majestic_id"
        )
        val sheriff = User(
            id = "user_sheriff_id",
            email = "dr.ahmed.sheriff@rewayahotels.com",
            titlePrefix = "Dr.",
            firstName = "Ahmed",
            lastName = "Sheriff",
            displayRole = "Quality and Hygiene Manager",
            roleCode = "QUALITY_MANAGER",
            assignedHotelId = "hotel_resort_id"
        )
        val hassan = User(
            id = "user_hassan_id",
            email = "mr.mohamed.hassan@rewayahotels.com",
            titlePrefix = "Mr.",
            firstName = "Mohamed",
            lastName = "Hassan",
            displayRole = "Quality and Hygiene Manager",
            roleCode = "QUALITY_MANAGER",
            assignedHotelId = "hotel_inn_id"
        )
        val auditor = User(
            id = "user_auditor_id",
            email = "inspector.ali@rewayahotels.com",
            titlePrefix = "Mr.",
            firstName = "Ali",
            lastName = "Farouk",
            displayRole = "Senior Quality Auditor",
            roleCode = "AUDITOR",
            assignedHotelId = "hotel_majestic_id"
        )
        userDao.insertUsers(listOf(morad, hussien, sheriff, hassan, auditor))

        // 3. Seed some initial historical audits for realistic dashboard UI
        val audit1Details = Audit(
            id = "audit_1_id",
            hotelId = "hotel_majestic_id",
            inspectorName = "Dr. Mohamed Hussien",
            inspectorRole = "Quality and Hygiene Manager",
            departmentName = "Food & Beverage",
            complianceScore = 91.6f,
            status = "COMPLETED",
            gpsLatitude = 25.0754,
            gpsLongitude = 55.1887,
            createdAt = System.currentTimeMillis() - 4 * 3600 * 1000 // 4 hours ago
        )
        val audit2Details = Audit(
            id = "audit_2_id",
            hotelId = "hotel_resort_id",
            inspectorName = "Dr. Ahmed Sheriff",
            inspectorRole = "Quality and Hygiene Manager",
            departmentName = "Housekeeping",
            complianceScore = 95.0f,
            status = "COMPLETED",
            gpsLatitude = 25.0760,
            gpsLongitude = 55.1901,
            createdAt = System.currentTimeMillis() - 12 * 3600 * 1000 // 12 hours ago
        )
        val audit3Details = Audit(
            id = "audit_3_id",
            hotelId = "hotel_inn_id",
            inspectorName = "Mr. Mohamed Hassan",
            inspectorRole = "Quality and Hygiene Manager",
            departmentName = "Back of House",
            complianceScore = 78.5f,
            status = "COMPLETED",
            gpsLatitude = 25.0741,
            gpsLongitude = 55.1872,
            createdAt = System.currentTimeMillis() - 24 * 3600 * 1000 // 24 hours ago
        )

        auditDao.insertAudit(audit1Details)
        auditDao.insertAudit(audit2Details)
        auditDao.insertAudit(audit3Details)

        // 4. Seed Audit Answers for historical items
        val answers1 = listOf(
            AuditAnswer(
                id = "ans_1_1", auditId = "audit_1_id", questionId = "fb_1",
                questionTextEn = "Food deliveries inspected for quality, temperature, and undamaged packaging upon arrival.",
                questionTextAr = "فحص الشحنات الغذائية للتأكد من الجودة، ودرجة الحرارة، وسلامة العبوات عند الوصول.",
                section = "Receiving & Storage", responseType = "BINARY", binaryValue = true
            ),
            AuditAnswer(
                id = "ans_1_2", auditId = "audit_1_id", questionId = "fb_2",
                questionTextEn = "Cold storage unit temperatures logged: Chilled food (0°C to +4°C), Frozen food (-18°C or lower).",
                questionTextAr = "تسجيل درجات حرارة وحدات التخزين البارد: الأغذية المبردة (0 إلى 4 درجات مئوية)، المجمدة (-18 درجة مئوية أو أقل).",
                section = "Receiving & Storage", responseType = "BINARY", binaryValue = false,
                comment = "Main Kitchen Walk-In Freezer temperature reading is -12°C, which is too warm.",
                maintenanceRequested = true, maintenanceDetails = "Adjust compressor system - freezer is running too warm."
            ),
            AuditAnswer(
                id = "ans_1_3", auditId = "audit_1_id", questionId = "fb_3",
                questionTextEn = "Staff hygiene practices, uniforms, hairnets and overall cleanliness meet guidelines.",
                questionTextAr = "ممارسات النظافة الشخصية للعاملين، الزي الرسمي، غطاء الشعر والنظافة العامة مطابقة للإرشادات.",
                section = "Staff Hygiene", responseType = "RATING", ratingValue = 4
            )
        )
        val answers2 = listOf(
            AuditAnswer(
                id = "ans_2_1", auditId = "audit_2_id", questionId = "hk_1",
                questionTextEn = "Guest rooms cleanliness, dust-free surfaces, and correct furniture layout checked.",
                questionTextAr = "التحقق من نظافة غرف النزلاء وخلو الأسطح من الأتربة والترتيب الصحيح للأثاث.",
                section = "Guest Rooms", responseType = "BINARY", binaryValue = true
            ),
            AuditAnswer(
                id = "ans_2_2", auditId = "audit_2_id", questionId = "hk_2",
                questionTextEn = "Emergency lighting, door/window safety status verified.",
                questionTextAr = "التحقق من حالة إضاءة الطوارئ وسلامة الأبواب والنوافذ.",
                section = "Public Areas & Safety", responseType = "RATING", ratingValue = 5
            )
        )
        val answers3 = listOf(
            AuditAnswer(
                id = "ans_3_1", auditId = "audit_3_id", questionId = "boh_1",
                questionTextEn = "Electrical rooms and generators are clear of debris, with visual maintenance tags active.",
                questionTextAr = "المولدات وغرف الكهرباء خالية من العوائد، وبطاقات الصيانة المرئية مفعلة.",
                section = "Mechanical & Electrical", responseType = "BINARY", binaryValue = false,
                comment = "Secondary generator room contains stacked broken linen carts.",
                maintenanceRequested = true, maintenanceDetails = "Clear broken carts immediately."
            ),
            AuditAnswer(
                id = "ans_3_2", auditId = "audit_3_id", questionId = "boh_2",
                questionTextEn = "Ventilation, safety guards, and noise level in laundry room within boundaries.",
                questionTextAr = "التهوية، وحواجز السلامة، ومستوى الضوضاء في غرفة الغسيل ضمن الحدود المقبولة.",
                section = "Laundry & Housekeeping Support", responseType = "BINARY", binaryValue = true
            )
        )

        auditAnswerDao.insertAnswers(answers1)
        auditAnswerDao.insertAnswers(answers2)
        auditAnswerDao.insertAnswers(answers3)

        // 5. Seed Open Corrective Actions automatically representing the failed items
        val action1 = CorrectiveAction(
            id = "action_1_id",
            auditId = "audit_1_id",
            auditAnswerId = "ans_1_2",
            issueDescription = "Main Kitchen Walk-In Freezer temperature reading is -12°C, which is too warm.",
            hotelId = "hotel_majestic_id",
            departmentName = "Food & Beverage",
            priority = "URGENT",
            assignedToUserId = "user_hussien_id",
            dueDate = System.currentTimeMillis() + 12 * 3600 * 1000, // Due in 12 hours
            createdAt = System.currentTimeMillis() - 4 * 3600 * 1000
        )
        val action2 = CorrectiveAction(
            id = "action_2_id",
            auditId = "audit_3_id",
            auditAnswerId = "ans_3_1",
            issueDescription = "Secondary generator room contains stacked broken linen carts.",
            hotelId = "hotel_inn_id",
            departmentName = "Back of House",
            priority = "IMPORTANT",
            assignedToUserId = "user_hassan_id",
            dueDate = System.currentTimeMillis() + 48 * 3600 * 1000, // Due in 48 hours,
            createdAt = System.currentTimeMillis() - 24 * 3600 * 1000
        )

        correctiveActionDao.insertCorrectiveAction(action1)
        correctiveActionDao.insertCorrectiveAction(action2)

        // 6. Seed high priority Quick Hazard
        val hazard1 = QuickHazard(
            id = "hazard_1_id",
            hotelId = "hotel_resort_id",
            departmentName = "Public Areas",
            zone = "Gardens & Swimmimg Pools",
            description = "Loose pool tiles discovered near the shallow-end water steps.",
            priority = "IMPORTANT",
            reporterName = "Dr. Ahmed Sheriff",
            createdAt = System.currentTimeMillis() - 2 * 3600 * 1000,
            status = "PENDING"
        )
        quickHazardDao.insertHazard(hazard1)
    }
}
