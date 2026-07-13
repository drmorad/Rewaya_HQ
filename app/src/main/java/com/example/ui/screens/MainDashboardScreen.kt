package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.viewmodel.AuditViewModel
import com.example.ui.viewmodel.TempAnswer
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(
    viewModel: AuditViewModel,
    modifier: Modifier = Modifier
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val hotels by viewModel.hotels.collectAsState()
    val usersByFlow by viewModel.users.collectAsState()
    val audits by viewModel.audits.collectAsState()
    val correctiveActions by viewModel.correctiveActions.collectAsState()
    val hazards by viewModel.hazards.collectAsState()
    val isArabic = (viewModel.currentLanguage.collectAsState().value == "AR")
    val selectedHotelId by viewModel.selectedHotelFilter.collectAsState()
    val userPoints by viewModel.userPoints.collectAsState()

    // Navigation state in main state-driven container
    var currentTab by remember { mutableStateOf("dashboard") } // dashboard, audit, corrective, hazard, reports, leaderboard
    var showCreateAuditForm by remember { mutableStateOf<String?>(null) } // "FB" or "HK" etc.
    var showReportHazardDialog by remember { mutableStateOf(false) }

    // UI Colors theme configuration (Deep slate luxury and Amber accents)
    val slatePrimary = Color(0xFF0F172A)
    val slateSecondary = Color(0xFF1E293B)
    val amberAccent = Color(0xFFE2E8F0)
    val goldAccent = Color(0xFFF59E0B)

    // Helper functions for localized labels
    fun t(en: String, ar: String): String = if (isArabic) ar else en

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Hotel,
                            contentDescription = null,
                            tint = goldAccent
                        )
                        Text(
                            text = t("REWAYA OPERATIONS HQ", "مقر عمليات رواية"),
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                    }
                },
                actions = {
                    // Language Switcher
                    IconButton(onClick = {
                        viewModel.setLanguage(if (isArabic) "EN" else "AR")
                    }) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Language,
                                contentDescription = "Language",
                                tint = Color.White
                            )
                            Text(
                                text = if (isArabic) "EN" else "عربي",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Simulated Team Member/Role Simulator (essential for Dr. Morad visibility)
                    var showUserMenu by remember { mutableStateOf(false) }
                    Box {
                        Button(
                            onClick = { showUserMenu = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = slateSecondary,
                                contentColor = goldAccent
                            ),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = currentUser?.titlePrefix ?: "User",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false },
                            modifier = Modifier.background(slateSecondary)
                        ) {
                            usersByFlow.forEach { user ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = "${user.titlePrefix} ${user.firstName} ${user.lastName} (${user.displayRole})",
                                            color = Color.White,
                                            fontSize = 13.sp
                                        )
                                    },
                                    onClick = {
                                        viewModel.switchUser(user.id)
                                        showUserMenu = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = slatePrimary)
            )
        },
        bottomBar = {
            // Elegant M3 navigation bar with active pills
            NavigationBar(
                containerColor = slatePrimary,
                tonalElevation = 8.dp
            ) {
                listOf(
                    Triple("dashboard", Icons.Default.Dashboard, Pair("Dashboard", "الرئيسية")),
                    Triple("audit", Icons.Default.Assignment, Pair("Inspections", "عمليات الفحص")),
                    Triple("corrective", Icons.Default.Build, Pair("Tasks", "المهام")),
                    Triple("hazard", Icons.Default.Warning, Pair("Hazards", "المخاطر")),
                    Triple("reports", Icons.Default.Description, Pair("Reports", "التقارير")),
                    Triple("leaderboard", Icons.Default.Star, Pair("Leaderboard", "الترتيب"))
                ).forEach { (tab, icon, label) ->
                    val isSelected = currentTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            currentTab = tab
                            showCreateAuditForm = null
                        },
                        icon = {
                            Icon(
                                imageVector = icon,
                                contentDescription = label.first,
                                tint = if (isSelected) goldAccent else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        label = {
                            Text(
                                text = if (isArabic) label.second else label.first,
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) goldAccent else Color.White.copy(alpha = 0.6f)
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = slateSecondary
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Surface(
            modifier = modifier
                .padding(innerPadding)
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {

                // 2. Active User Profile HUD Banner (displays Job Title prominently at all times)
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = slatePrimary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(slatePrimary, slateSecondary)
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .clip(CircleShape)
                                        .background(goldAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "User Avatar",
                                        tint = slatePrimary,
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = currentUser?.fullNameWithTitle ?: "Guest",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 18.sp,
                                        color = Color.White
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = currentUser?.displayRole ?: "Quality Department",
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 12.sp,
                                        color = goldAccent
                                    )
                                }
                            }
                            // Gamification Badge info
                            Column(horizontalAlignment = Alignment.End) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocalActivity,
                                        contentDescription = "Points",
                                        tint = goldAccent,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "$userPoints XP",
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 16.sp,
                                        color = Color.White
                                    )
                                }
                                Text(
                                    text = t("Level 3 Quality Shield", "مستوى ٣ درع الجودة"),
                                    fontSize = 10.sp,
                                    color = Color.LightGray
                                )
                            }
                        }
                    }
                }

                // 3. Tab contents routing
                when {
                    showCreateAuditForm != null -> {
                        CreateAuditFormView(
                            formType = showCreateAuditForm!!,
                            hotels = hotels,
                            onCancel = { showCreateAuditForm = null },
                            onSubmit = { hotelId, dept, answers ->
                                viewModel.submitInspection(hotelId, dept, answers)
                                showCreateAuditForm = null
                                currentTab = "dashboard"
                            },
                            isArabic = isArabic,
                            t = ::t
                        )
                    }
                    currentTab == "dashboard" -> {
                        DashboardTabView(
                            viewModel = viewModel,
                            hotels = hotels,
                            audits = audits,
                            correctiveActions = correctiveActions,
                            selectedHotelId = selectedHotelId,
                            isArabic = isArabic,
                            t = ::t
                        )
                    }
                    currentTab == "audit" -> {
                        AuditTemplatesView(
                            isArabic = isArabic,
                            t = ::t,
                            onLaunchTemplate = { type -> showCreateAuditForm = type }
                        )
                    }
                    currentTab == "corrective" -> {
                        CorrectiveActionsTabView(
                            actions = correctiveActions,
                            currentUser = currentUser,
                            isArabic = isArabic,
                            t = ::t,
                            onResolve = { id, notes -> viewModel.resolveCorrectiveAction(id, notes) }
                        )
                    }
                    currentTab == "hazard" -> {
                        HazardsTabView(
                            hazards = hazards,
                            hotels = hotels,
                            isArabic = isArabic,
                            t = ::t,
                            onAddHazard = { hotelId, dept, zone, desc, priority ->
                                viewModel.reportQuickHazard(hotelId, dept, zone, desc, priority)
                            }
                        )
                    }
                    currentTab == "reports" -> {
                        DailyAuditPreviewReport(
                            audits = audits,
                            hotels = hotels,
                            correctiveActions = correctiveActions,
                            isArabic = isArabic,
                            t = ::t,
                            currentUser = currentUser
                        )
                    }
                    currentTab == "leaderboard" -> {
                        LeaderboardTabView(
                            users = usersByFlow,
                            isArabic = isArabic,
                            t = ::t,
                            userPoints = userPoints
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 1. DASHBOARD VIEW (MASTER AND DEPARTMENTAL)
// -------------------------------------------------------------
@Composable
fun DashboardTabView(
    viewModel: AuditViewModel,
    hotels: List<Hotel>,
    audits: List<Audit>,
    correctiveActions: List<CorrectiveAction>,
    selectedHotelId: String?,
    isArabic: Boolean,
    t: (String, String) -> String
) {
    val filteredAudits = audits.filter { selectedHotelId == null || it.hotelId == selectedHotelId }
    val filteredActions = correctiveActions.filter { selectedHotelId == null || it.hotelId == selectedHotelId }

    // Multi-hotel property selection dropdown
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = t("Filtering Property Status", "تصفية حالة الفندق"),
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
                    .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                    .clickable { }
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                var expanded by remember { mutableStateOf(false) }

                val selectedHotelName = if (selectedHotelId == null) {
                    t("All Properties (3 Hotels)", "جميع الفنادق (٣ فنادق)")
                } else {
                    val hot = hotels.firstOrNull { it.id == selectedHotelId }
                    if (isArabic) hot?.nameAr ?: "" else hot?.nameEn ?: ""
                }

                Box {
                    Text(
                        text = selectedHotelName,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.clickable { expanded = true }
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(t("All Properties (3 Hotels)", "جميع الفنادق (٣ فنادق)")) },
                            onClick = {
                                viewModel.setHotelFilter(null)
                                expanded = false
                            }
                        )
                        hotels.forEach { hotel ->
                            DropdownMenuItem(
                                text = { Text(if (isArabic) hotel.nameAr else hotel.nameEn) },
                                onClick = {
                                    viewModel.setHotelFilter(hotel.id)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = Color.Gray
                )
            }
        }
    }

    // Performance Calculations & KPIs
    val overallCompliance = if (filteredAudits.isNotEmpty()) {
        filteredAudits.map { it.complianceScore }.average().toFloat()
    } else {
        94.4f // Default based on Seeded portfolio baseline if cleared
    }

    val openUrgentCount = filteredActions.count { it.status == "OPEN" && it.priority == "URGENT" }
    val totalOpenCount = filteredActions.count { it.status == "OPEN" }
    val completedCount = filteredAudits.size

    // KPI Display panels
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val containerModifier = Modifier
            .weight(1f)
            .height(110.dp)
        
        KpiWidget(
            title = t("Avg Compliance", "متوسط الامتثال"),
            valStr = "%.1f%%".format(overallCompliance),
            subtext = "Objective: 95%+",
            color = if (overallCompliance >= 93) Color(0xFF10B981) else Color(0xFFEF4444),
            icon = Icons.Default.CheckCircle,
            modifier = containerModifier
        )
        KpiWidget(
            title = t("Pending Actions", "إجراءات معلقة"),
            valStr = "$totalOpenCount",
            subtext = t("$openUrgentCount URGENT ITEMS", "$openUrgentCount بنود عاجلة"),
            color = if (totalOpenCount > 0) Color(0xFFF59E0B) else Color(0xFF10B981),
            icon = Icons.Default.Warning,
            modifier = containerModifier
        )
    }

    Spacer(modifier = Modifier.height(8.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val containerModifier = Modifier
            .weight(1f)
            .height(110.dp)

        KpiWidget(
            title = t("Audits Logged", "الفحوصات المسجلة"),
            valStr = "$completedCount",
            subtext = t("Real-time tracked", "تتبع في الوقت الفعلي"),
            color = Color(0xFF3B82F6),
            icon = Icons.Default.Assignment,
            modifier = containerModifier
        )

        KpiWidget(
            title = t("Health Index", "مؤشر السلامة"),
            valStr = if (overallCompliance >= 90) "A+" else "B",
            subtext = t("Excellent Status", "حالة ممتازة"),
            color = Color(0xFF8B5CF6),
            icon = Icons.Default.Beenhere,
            modifier = containerModifier
        )
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Zone & Department compliance breakdown graph simulation
    Text(
        text = t("Departmental Performance Comparison", "مقارنة أداء الأقسام"),
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            listOf(
                Triple(t("Food & Beverage", "الأغذية والمشروبات"), 91.6f, Color(0xFF10B981)),
                Triple(t("Housekeeping", "الإشراف الداخلي"), 95.0f, Color(0xFF3B82F6)),
                Triple(t("Back of House", "الخدمات المساندة"), 83.2f, Color(0xFFF59E0B)),
                Triple(t("Public Areas", "المناطق العامة"), 96.0f, Color(0xFF8B5CF6))
            ).forEach { (dept, score, color) ->
                Column(modifier = Modifier.padding(vertical = 6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = dept, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(text = "%.1f%%".format(score), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = color)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE2E8F0))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(score / 100f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(color)
                        )
                    }
                }
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    // Real-time Recent Audits Stream List
    Text(
        text = t("Recent Quality Audits Map / Trace", "سجل عمليات فحص الجودة المباشر"),
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    if (filteredAudits.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = Color.LightGray,
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t("No inspections recorded yet.", "لا توجد عمليات فحص مسجلة بعد."),
                    color = Color.Gray,
                    fontSize = 13.sp
                )
            }
        }
    } else {
        filteredAudits.take(5).forEach { audit ->
            val hot = hotels.firstOrNull { it.id == audit.hotelId }
            val hotelName = if (isArabic) hot?.nameAr ?: "" else hot?.nameEn ?: ""
            val scoreColor = when {
                audit.complianceScore >= 95 -> Color(0xFF10B981)
                audit.complianceScore >= 85 -> Color(0xFF3B82F6)
                else -> Color(0xFFEF4444)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(scoreColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assignment,
                                contentDescription = null,
                                tint = scoreColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = hotelName,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color.Black
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${t("Dept:", "قسم:")} ${audit.departmentName} - ${t("by", "بواسطة")} ${audit.inspectorName}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(scoreColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "%.1f %%".format(audit.complianceScore),
                            color = scoreColor,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun KpiWidget(
    title: String,
    valStr: String,
    subtext: String,
    color: Color,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    fontSize = 11.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column {
                Text(
                    text = valStr,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = color
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtext,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// -------------------------------------------------------------
// 2. INSPECTION TEMPLATES LIST VIEW
// -------------------------------------------------------------
@Composable
fun AuditTemplatesView(
    isArabic: Boolean,
    t: (String, String) -> String,
    onLaunchTemplate: (String) -> Unit
) {
    Text(
        text = t("Select Department Inspection Checklist", "اختر نموذج فحص الجودة الخاص بالقسم"),
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    val templates = listOf(
        Triple("FB", t("Food & Beverage (F&B) Audit", "قسم الأغذية والمشروبات (F&B)"), t("Main & satellite kitchens, food storage, freezers, server lines, sanitation", "المطابخ الرئيسية والفرعية، مخازن الأغذية، غرف التجميد، النظافة")),
        Triple("HK", t("Housekeeping Audit Checklist", "قسم الإشراف الداخلي (Housekeeping)"), t("Guest rooms (all categories), corridors, linen stores, cleanliness status", "غرف النزلاء بكافة فئاتها، الممرات، مخازن البياضات، النظافة العامة")),
        Triple("BOH", t("Back of House (Support Services)", "الخدمات المساندة (BOH)"), t("Laundry rooms, equipment generators, AC system, water tanks maintenance", "غرف الغسيل، مولدات الكهرباء، الصيانة، خزانات المياه، أنظمة التبريد")),
        Triple("PA", t("Public Areas Security & Cleanliness", "المناطق العامة والنظافة"), t("Lobby, public restrooms, elevators, swimming pools & pool deck", "الردهة الرئيسية، دورات المياه العامة، المصاعد، حمامات السباحة")),
        Triple("ADMIN", t("Administrative Audit Compliance", "المكاتب الإدارية والرقابة"), t("Reception desks, file security, surveillance monitoring, communications", "مكاتب الاستقبال، أرشفة الملفات، غرف المراقبة، أنظمة الاتصال"))
    )

    templates.forEach { (type, title, desc) ->
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp)
                .clickable { onLaunchTemplate(type) },
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF59E0B))
                        )
                        Text(
                            text = title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = desc,
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                }
                IconButton(onClick = { onLaunchTemplate(type) }) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Start inspection",
                        tint = Color(0xFFF59E0B)
                    )
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 3. HYGIENE AUDIT INPUT FORM
// -------------------------------------------------------------
@Composable
fun CreateAuditFormView(
    formType: String,
    hotels: List<Hotel>,
    onCancel: () -> Unit,
    onSubmit: (String, String, List<TempAnswer>) -> Unit,
    isArabic: Boolean,
    t: (String, String) -> String
) {
    val deptName = when (formType) {
        "FB" -> "Food & Beverage"
        "HK" -> "Housekeeping"
        "BOH" -> "Back of House"
        "PA" -> "Public Areas"
        else -> "Administrative"
    }

    var selectedHotelId by remember { mutableStateOf(hotels.firstOrNull()?.id ?: "") }

    // Load templates questions. If FB, we have the complete detailed F&B questions!
    val questionsEnAr = remember(formType) {
        when (formType) {
            "FB" -> {
                listOf(
                    // Section 1
                    Quadruple("fb_1_1", "Food deliveries inspected for quality, temperature, and undamaged packaging upon arrival.", "فحص الشحنات الغذائية للتأكد من الجودة، ودرجة الحرارة، وسلامة العبوات عند الوصول.", "1. Receiving", "BINARY"),
                    Quadruple("fb_1_2", "Cold storage unit temperatures logged: Chilled food (0°C to +4°C), Frozen food (-18°C or lower).", "تسجيل درجات حرارة وحدات التخزين البارد: الأغذية المبردة (0 إلى 4 درجات مئوية)، المجمدة (-18 درجة مئوية أو أقل).", "1. Receiving", "BINARY"),
                    Quadruple("fb_1_3", "High-risk raw foods (poultry, seafood, meat) are segregated from cooked or ready-to-eat items.", "فصل الأطعمة النيئة عالية الخطورة (الدواجن، المأكولات البحرية، اللحوم) عن الأطعمة المطبوخة.", "1. Receiving", "BINARY"),
                    Quadruple("fb_1_4", "All storage items follow First-In, First-Out (FIFO) guidelines and show clear labels with prep & expiry dates.", "تتبع جميع المخزونات نظام الوارد أولاً يصرف أولاً وتظهر عليها ملصقات تاريخ التحضير والصلاحية.", "1. Receiving", "BINARY"),
                    // Section 2
                    Quadruple("fb_2_1", "Food prep surfaces, utensils, and cutting boards are clean, sanitized, and color-coded.", "أسطح تحضير الأغذية، الأدوات، وألواح التقطيع نظيفة ومعقمة ومصنفة بحسب الألوان.", "2. Kitchen Labs", "RATING"),
                    Quadruple("fb_2_2", "High-temperature dishwashing machines reach a minimum rinse temperature of 82°C.", "آلات غسيل الصحون ذات الحرارة العالية تصل لدرجة حرارة شطف دنيا تبلغ 82 درجة مئوية.", "2. Kitchen Labs", "BINARY"),
                    Quadruple("fb_2_3", "No signs of pest activity (insects, rodents) are present in prep, storage, or waste disposal areas.", "خلو مناطق التحضير، التخزين، والتخلص من النفايات من أي علامات لنشاط الآفات (الحشرات والقوارض).", "2. Kitchen Labs", "BINARY"),
                    Quadruple("fb_2_4", "Handwashing stations are fully accessible, clean, and stocked with warm water, liquid soap, and paper towels.", "محطات غسيل الأيدي سهلة الوصول ومجهزة بالكامل بالماء الدافئ والصابون السائل والمناشف الورقية.", "2. Kitchen Labs", "RATING"),
                    // Section 3
                    Quadruple("fb_3_1", "Food handlers are wearing clean, authorized uniforms, hairnets/beard snoods, and minimal jewelry.", "يرتدي متداولو الأغذية ملابس نظيفة ومعتمدة، غطاء الشعر/اللحية، مع الحد الأدنى من المجوهرات.", "3. Hygiene", "BINARY"),
                    Quadruple("fb_3_2", "Food handlers with visible cuts or wounds are wearing blue, waterproof dressings and food-grade gloves.", "العاملون الذين يعانون من جروح ظاهرة يرتدون ضمادات زرقاء مقاومة للماء وقفازات غذائية.", "3. Hygiene", "BINARY"),
                    Quadruple("fb_3_3", "Staff wash hands regularly at designated hand-wash stations (not in food-prep sinks).", "يقوم الموظفون بغسل أيديهم بانتظام في مغاسل مخصصة لذلك (وليس في أحواض تحضير الطعام).", "3. Hygiene", "BINARY"),
                    // Section 4
                    Quadruple("fb_4_1", "Hot buffet foods are held at or above 63°C; cold buffet foods are held at or below 4°C.", "حفظ أطعمة البوفيه الساخنة عند درجة حرارة 63 أو أعلى، والباردة عند 4 درجات أو أقل.", "4. Service Area", "BINARY"),
                    Quadruple("fb_4_2", "Dining area tables, chairs, and condiment holders are clean and sanitized between guest turns.", "طاولات صالة الطعام، الكراسي، وحوامل التوابل نظيفة ومعقمة بين فترات خدمة النزلاء.", "4. Service Area", "RATING"),
                    Quadruple("fb_4_3", "Guest-facing cutlery, glassware, and tableware are free of spots, cracks, or residues.", "أدوات المائدة، الأواني الزجاجية، والأطباق المخصصة للنزلاء خالية من البقع أو الشقوق أو البقايا.", "4. Service Area", "BINARY")
                )
            }
            "HK" -> {
                listOf(
                    Quadruple("hk_1_1", "Guest room mattresses, bedsheets, and pillows are clean, stained-free, and vacuumed properly.", "مراتب غرف النزلاء والملايات والوسائد نظيفة وخالية من البقع ومكنوسة بشكل ممتاز.", "1. Guest Rooms", "RATING"),
                    Quadruple("hk_1_2", "Room mini-bars and coffee stations are sanitized, restocked, and checked for expired items.", "بار الغرف الصغير ومحطة القهوة معقمة ومجهزة، والتحقق من تاريخ الصلاحية للمنتجات.", "1. Guest Rooms", "BINARY"),
                    Quadruple("hk_2_1", "Guest bathrooms disinfected: shower screen, tub, toilet bowl, and sink are spotless.", "تطهير حمامات النزلاء: زجاج الاستحمام، الحوض، والمرحاض نظيفة تماماً ولا يوجد ترسبات.", "2. Bathrooms", "RATING"),
                    Quadruple("hk_2_2", "Fresh towels, bathrobes, and luxury toiletries are stocked neatly without dust or hair.", "توفير مناشف نظيفة جديدة وأدوات النظافة الفاخرة مرتبة وخالية تماماً من الغبار والشعر.", "2. Bathrooms", "BINARY"),
                    Quadruple("hk_3_1", "Corridors vacuumed, carpet edges clean, ceiling light fixtures free of dust and cobwebs.", "الممرات مكنوسة بالكامل، نظافة أطراف السجاد، خلو الإضاءة والأسقف من الغبار وخيوط العنكبوت.", "3. Public Hallways", "RATING"),
                    Quadruple("hk_3_2", "Linen and housekeeping utility closets are locked, organized, and clean.", "مستودعات البياضات وغرف أدوات النظافة مغلقة، مرتبة، ونظيفة جداً.", "3. Public Hallways", "BINARY")
                )
            }
            "BOH" -> {
                listOf(
                    Quadruple("boh_1_1", "Laundry washers, dryers, and irons are clean, lint filters cleared, and chemical pumps operating.", "غسالات ومجففات ومكاوي المغسلة نظيفة، فلاتر الوبر نظيفة، ومضخات الكيماويات تعمل بالكامل.", "1. Laundry Ops", "BINARY"),
                    Quadruple("boh_1_2", "Soiled and clean linens are completely separated during transport, washing, and folding.", "البياضات المتسخة والنظيفة مفصولة بالكامل أثناء النقل والغسيل والطي لمنع التلوث.", "1. Laundry Ops", "BINARY"),
                    Quadruple("boh_2_1", "Waste management area bins are covered, sanitized daily, and pest treatment schedules logged.", "صناديق إدارة النفايات مغلقة، يتم تعقيمها يومياً، وجداول مكافحة الحشرات مسجلة.", "2. Waste & Pest", "RATING"),
                    Quadruple("boh_3_1", "HVAC filters clean, ventilation vents are dust-free, and water heater room dry and clean.", "فلاتر التكييف نظيفة، فتحات التهوية خالية من الغبار، وغرفة سخانات المياه جافة ونظيفة.", "3. Mechanical Zones", "BINARY")
                )
            }
            "PA" -> {
                listOf(
                    Quadruple("pa_1_1", "Main lobby floor, carpets, and high-frequency touch doors/glass are clean and spotless.", "أرضيات الردهة الرئيسية، السجاد، والأبواب والزجاج الأكثر استخداماً نظيفة تماماً.", "1. Reception & Lobby", "RATING"),
                    Quadruple("pa_1_2", "Lobby furniture, decorative pillows, and reception desks are clean, organized, and dust-free.", "أثاث الردهة والوسائد ومكتب الاستقبال نظيفة ومرتبة وخالية من أي غبار.", "1. Reception & Lobby", "BINARY"),
                    Quadruple("pa_2_1", "Public elevators are clean, interior mirrors spotless, and buttons disinfected hourly.", "المصاعد العامة نظيفة، المرايا الداخلية خالية من البقع، ويتم تعقيم الأزرار والمقابض كل ساعة.", "2. Elevators & Stairs", "BINARY"),
                    Quadruple("pa_3_1", "Swimming pool water clarity is optimal, and chemical parameters (pH/chlorine) logged hourly.", "نقاء مياه حمام السباحة مثالي، ويتم تدوين قراءات الكلور والحموضة كل ساعة بانتظام.", "3. Pools & Decks", "BINARY"),
                    Quadruple("pa_3_2", "Pool sun loungers, showers, and public towels are sanitized, fresh, and neat.", "كراسي الاسترخاء، الاستحمام، والمناشف العامة معقمة، منعشة ومرتبة بالكامل.", "3. Pools & Decks", "RATING"),
                    Quadruple("pa_4_1", "Public restrooms are smelling fresh, floors dry, soap dispensers filled, and paper hand towels stocked.", "دورات المياه العامة رائحتها طيبة، الأرضيات جافة، موزعات الصابون ممتلئة والمناشف الورقية متوفرة.", "4. Public Restrooms", "RATING")
                )
            }
            else -> {
                listOf(
                    Quadruple("admin_1_1", "Reception counters, payment terminals, and keyboards are cleaned and disinfected regularly.", "كاونتر الاستقبال، أجهزة الدفع، ولوحات المفاتيح نظيفة ومطهرة بشكل دوري.", "1. Front Desks", "BINARY"),
                    Quadruple("admin_2_1", "Archive cupboards and guest logs are stored securely, confidential paperwork locked.", "خزائن الأرشيف وسجلات النزلاء مخزنة بشكل آمن، والأوراق السرية مقفلة.", "2. Office Security", "BINARY"),
                    Quadruple("admin_3_1", "Staff dining room, tea kitchen, microwave, and coffee maker are clean, tidy, and sanitized.", "غرفة طعام الموظفين، مطبخ الشاي، المايكروويف، وآلة القهوة نظيفة، مرتبة، ومعقمة.", "3. Employee Spaces", "RATING"),
                    Quadruple("admin_3_2", "Staff locker rooms and toilets are clean, smelling fresh, and stocked with hand sanitizers.", "خزانات الموظفين ودورات المياه الخاصة بهم نظيفة، رائحتها منعشة، ومجهزة بمعقمات الأيدي.", "3. Employee Spaces", "BINARY")
                )
            }
        }
    }

    // Keep active map of temp answers
    val activeAnswers = remember {
        mutableStateMapOf<String, TempAnswer>().apply {
            questionsEnAr.forEach { (id, en, ar, sec, type) ->
                put(id, TempAnswer(id, en, ar, sec, type))
            }
        }
    }

    var inspectorSign by remember { mutableStateOf("") }
    var managerSign by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        // Back Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        ) {
            IconButton(onClick = onCancel) {
                Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "${t("New Audit Form:", "نموذج فحص جديد:")} ${t(deptName, "قسم فحص الإدارة")}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        Divider(modifier = Modifier.padding(bottom = 16.dp))

        // Property select for audit
        Text(text = t("Audited Hotel Property", "فندق الفحص"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(4.dp))
        var showingHotelSpin by remember { mutableStateOf(false) }
        val currentHotel = hotels.firstOrNull { it.id == selectedHotelId }
        val currentHotelName = if (isArabic) currentHotel?.nameAr ?: "" else currentHotel?.nameEn ?: ""

        Box(modifier = Modifier.fillMaxWidth()) {
            OutlinedButton(
                onClick = { showingHotelSpin = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
            ) {
                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Text(text = currentHotelName, fontWeight = FontWeight.Bold)
                    Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
            DropdownMenu(expanded = showingHotelSpin, onDismissRequest = { showingHotelSpin = false }) {
                hotels.forEach { h ->
                    DropdownMenuItem(
                        text = { Text(if (isArabic) h.nameAr else h.nameEn) },
                        onClick = {
                            selectedHotelId = h.id
                            showingHotelSpin = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Questions Loop grouped by Section
        val sections = questionsEnAr.groupBy { it.fourth }
        sections.forEach { (sectionName, itemsList) ->
            Text(
                text = sectionName,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFF59E0B),
                modifier = Modifier.padding(vertical = 12.dp)
            )

            itemsList.forEach { q ->
                val ans = activeAnswers[q.first]!!
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                    border = BorderStroke(1.dp, Color(0xFFE2E8F0))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = if (isArabic) q.third else q.second,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        // Dynamic inputs based on type
                        if (q.fifth == "BINARY") {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = { activeAnswers[q.first] = ans.copy(binaryValue = true) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (ans.binaryValue == true) Color(0xFF10B981) else Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = t("YES (Pass)", "نعم (مقبول)"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                                Button(
                                    onClick = { activeAnswers[q.first] = ans.copy(binaryValue = false) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (ans.binaryValue == false) Color(0xFFEF4444) else Color.LightGray
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(text = t("NO (Fail)", "لا (مخالفة)"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        } else if (q.fifth == "RATING") {
                            // 1-5 rate stars selector
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                (1..5).forEach { stars ->
                                    val isLit = (ans.ratingValue ?: 0) >= stars
                                    IconButton(onClick = { activeAnswers[q.first] = ans.copy(ratingValue = stars) }) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = null,
                                            tint = if (isLit) Color(0xFFF59E0B) else Color.LightGray,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // CONDITIONAL LOGICAL FIELD MANDATORY COMMENT ON FAIL / NO
                        val isFailed = (ans.responseType == "BINARY" && ans.binaryValue == false) ||
                                (ans.responseType == "RATING" && ans.ratingValue != null && ans.ratingValue <= 2)

                        if (isFailed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = t("MANDATORY FIELD: Specify violation notes below", "حقل إلزامي: يرجى تحديد تفاصيل المخالفة"),
                                color = Color(0xFFEF4444),
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            OutlinedTextField(
                                value = ans.comment ?: "",
                                onValueChange = { activeAnswers[q.first] = ans.copy(comment = it) },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text(text = t("What exactly failed / is damaged?", "ما الذي تلف بالضبط أو يحتاج إلى معالجة؟"), fontSize = 11.sp) },
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                            )

                            // Action items: Maintenance trigger
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = t("Request Immediate Maintenance?", "طلب صيانة فورية؟"), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Switch(
                                    checked = ans.maintenanceRequested == true,
                                    onCheckedChange = { activeAnswers[q.first] = ans.copy(maintenanceRequested = it) }
                                )
                            }
                            if (ans.maintenanceRequested == true) {
                                Spacer(modifier = Modifier.height(4.dp))
                                OutlinedTextField(
                                    value = ans.maintenanceDetails ?: "",
                                    onValueChange = { activeAnswers[q.first] = ans.copy(maintenanceDetails = it) },
                                    modifier = Modifier.fillMaxWidth(),
                                    placeholder = { Text(text = t("Maintenance priority and team directives", "تصفية الأولويات والتفاصيل لفريق الصيانة"), fontSize = 11.sp) },
                                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Dual Digital E-Signatures Canvas Captures Simple Representation
        Text(text = t("Dual Electronic Signatures", "التوقيع الإلكتروني الثنائي"), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = inspectorSign,
                onValueChange = { inspectorSign = it },
                label = { Text(t("Inspector Sign-off Code", "رمز توقيع المفتش"), fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
            )
            OutlinedTextField(
                value = managerSign,
                onValueChange = { managerSign = it },
                label = { Text(t("Dept Representative Sign", "توقيع ممثل القسم"), fontSize = 11.sp) },
                modifier = Modifier.weight(1f),
                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button Action
        Button(
            onClick = {
                val listAnswers = activeAnswers.values.toList()
                onSubmit(selectedHotelId, deptName, listAnswers)
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF59E0B))
        ) {
            Text(
                text = t("SUBMIT COMPLETED QUALITY AUDIT", "إرسال تقرير فحص الجودة المكتمل"),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }
}

// -------------------------------------------------------------
// 4. CORRECTIVE ACTIONS TASK WORKFLOW MANAGER
// -------------------------------------------------------------
@Composable
fun CorrectiveActionsTabView(
    actions: List<CorrectiveAction>,
    currentUser: User?,
    isArabic: Boolean,
    t: (String, String) -> String,
    onResolve: (actionId: String, notes: String) -> Unit
) {
    // Quality Managers or Super Admins filter tasks
    val isSuperAdmin = currentUser?.roleCode == "SUPER_ADMIN"
    val assignedTasks = if (isSuperAdmin) {
        actions
    } else {
        actions.filter { it.assignedToUserId == currentUser?.id }
    }

    Text(
        text = if (isSuperAdmin) t("All Group Corrective Actions Pipeline", "جميع الإجراءات التصحيحية للمجموعة")
               else t("My Assigned Safety Actions", "المهام التصحيحية المعينة لي"),
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 12.dp)
    )

    if (assignedTasks.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t("No active corrective actions. Safe operations!", "لا توجد إجراءات تصحيحية معلقة. عمليات آمنة وممتازة!"),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        assignedTasks.forEach { task ->
            var responseNotes by remember { mutableStateOf("") }
            val priorityColor = when (task.priority) {
                "URGENT" -> Color(0xFFEF4444)
                "IMPORTANT" -> Color(0xFFF59E0B)
                else -> Color(0xFF3B82F6)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, if (task.status == "OPEN") priorityColor.copy(alpha = 0.5f) else Color.LightGray)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${t("Hotel:", "فندق:")} ${if (task.hotelId == "hotel_majestic_id") "Rewaya Majestic" else if (task.hotelId == "hotel_resort_id") "Rewaya Luxury Hotel & Resort" else "Rewaya Inn"}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(priorityColor.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = task.priority,
                                color = priorityColor,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = task.issueDescription,
                        fontSize = 12.sp,
                        color = Color.Black
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Divider()

                    Spacer(modifier = Modifier.height(8.dp))

                    if (task.status == "OPEN") {
                        Text(
                            text = t("Resolve corrective task:", "معالجة وإغلاق هذه المهمة:"),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = responseNotes,
                                onValueChange = { responseNotes = it },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text(t("Action taken details", "تفاصيل الإجراء التصحيحي المتخذ"), fontSize = 11.sp) },
                                textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                            )
                            Button(
                                onClick = { onResolve(task.id, responseNotes) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = t("Close", "إغلاق"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    } else {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = t("STATUS: RESOLVED ✅", "الحالة: تم الحل وإغلاق الملف ✅"),
                                    color = Color(0xFF10B981),
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 11.sp
                                )
                                val resolvedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date(task.resolvedAt ?: 0))
                                Text(text = resolvedDate, fontSize = 10.sp, color = Color.Gray)
                            }
                            if (task.reInspectionNotes?.isNotEmpty() == true) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "${t("Verification Notes:", "ملاحظات الفحص:")} ${task.reInspectionNotes}",
                                    fontSize = 11.sp,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 5. QUICK HAZARD REPORT WORKFLOW (IN UNDER 30 SECONDS)
// -------------------------------------------------------------
@Composable
fun HazardsTabView(
    hazards: List<QuickHazard>,
    hotels: List<Hotel>,
    isArabic: Boolean,
    t: (String, String) -> String,
    onAddHazard: (hotelId: String, dept: String, zone: String, desc: String, priority: String) -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    var hHotelId by remember { mutableStateOf("hotel_majestic_id") }
    var hDept by remember { mutableStateOf("Food & Beverage") }
    var hZone by remember { mutableStateOf("") }
    var hDesc by remember { mutableStateOf("") }
    var hPriority by remember { mutableStateOf("URGENT") }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = t("Quick Hazards Feed (Real-Time Safety)", "رصد المخاوف السريعة الفورية"),
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = Color.Black
        )
        Button(
            onClick = { showDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)), // red alert hazard buttons
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = t("Report Hazard (30s)", "إبلاغ فوري عن خطر"), fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    // Form Box if open
    if (showDialog) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
            border = BorderStroke(1.dp, Color(0xFFFECDD3))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = t("URGENT: Rapid Safety Hazard Report Form", "عاجل: نموذج إبلاغ سريع عن خطر سلامة"),
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF991B1B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Select Hotel dropdown
                val activeH = hotels.firstOrNull { it.id == hHotelId }
                val activeHName = if (isArabic) activeH?.nameAr ?: "" else activeH?.nameEn ?: ""
                var spin by remember { mutableStateOf(false) }
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedButton(
                        onClick = { spin = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
                    ) {
                        Text(text = activeHName, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                    DropdownMenu(expanded = spin, onDismissRequest = { spin = false }) {
                        hotels.forEach { h ->
                            DropdownMenuItem(
                                text = { Text(if (isArabic) h.nameAr else h.nameEn) },
                                onClick = {
                                    hHotelId = h.id
                                    spin = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Department
                OutlinedTextField(
                    value = hDept,
                    onValueChange = { hDept = it },
                    label = { Text(t("Department Name", "اسم القسم"), fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Zone details
                OutlinedTextField(
                    value = hZone,
                    onValueChange = { hZone = it },
                    label = { Text(t("Which specific Area/Room?", "تحديد الغرفة/المكان بالضبط؟"), fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Hazard Description
                OutlinedTextField(
                    value = hDesc,
                    onValueChange = { hDesc = it },
                    label = { Text(t("Describe the risk or defect visible", "صف الخلل المشاهد ومستوى الخطر"), fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = LocalTextStyle.current.copy(fontSize = 11.sp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Priority levels
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("ROUTINE", "IMPORTANT", "URGENT").forEach { level ->
                        val isSel = hPriority == level
                        Button(
                            onClick = { hPriority = level },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isSel) Color(0xFFE11D48) else Color.LightGray
                            ),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = level, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDialog = false },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = t("Cancel", "إلغاء"))
                    }
                    Button(
                        onClick = {
                            if (hDesc.isNotEmpty()) {
                                onAddHazard(hHotelId, hDept, hZone, hDesc, hPriority)
                                hDesc = ""
                                hZone = ""
                                showDialog = false
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(text = t("SUBMIT ALERT", "إرسال تصنيف الخطر"), color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // List of active Quick Hazards reported
    if (hazards.isEmpty()) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = Color(0xFF10B981),
                    modifier = Modifier.size(54.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = t("All parameters in check. No open hazards reported.", "جميع المؤشرات مستقرة. لا توجد مخاطر مسجلة."),
                    color = Color.Gray,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        hazards.forEach { hz ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                border = BorderStroke(1.dp, Color(0xFFFECDD3))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${t("Risk at", "خطر في:")} ${if (hz.hotelId == "hotel_majestic_id") "Rewaya Majestic" else if (hz.hotelId == "hotel_resort_id") "Rewaya Luxury Resort" else "Rewaya Inn"}",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 12.sp,
                            color = Color(0xFF991B1B)
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFFEF2F2))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = hz.priority,
                                color = Color(0xFFE11D48),
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 9.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = hz.description,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${t("Zone:", "المنطقة:")} ${hz.zone} (${hz.departmentName})",
                            fontSize = 10.sp,
                            color = Color.Gray,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${t("Reported by", "مبلغ:")} ${hz.reporterName}",
                            fontSize = 9.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 6. DAILY AUDIT PDF REPORT PREVIEW VISUALIZER
// -------------------------------------------------------------
@Composable
fun DailyAuditPreviewReport(
    audits: List<Audit>,
    hotels: List<Hotel>,
    correctiveActions: List<CorrectiveAction>,
    isArabic: Boolean,
    t: (String, String) -> String,
    currentUser: User?
) {
    var showSuccessToast by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = t("Executive Reports Hub", "مركز التقارير التنفيذية"),
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color.Black
            )
            Text(
                text = t("Click to export Daily PDF/Excel log", "انقر للتصدير الفوري اليومي للتقارير بصيغة PDF/Excel"),
                fontSize = 11.sp,
                color = Color.Gray
            )
        }
        Button(
            onClick = { showSuccessToast = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(imageVector = Icons.Default.Download, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = t("Export", "تصدير"), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }

    Spacer(modifier = Modifier.height(12.dp))

    if (showSuccessToast) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFECFDF5)),
            border = BorderStroke(1.dp, Color(0xFF10B981))
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = null, tint = Color(0xFF10B981))
                Text(
                    text = t(
                        "SUCCESS: Daily Quality Audit PDF & Excel generated successfully for Dr. Morad (Director) approval!",
                        "تم النجاح: تم إنشاء تقارير فحص الجودة بصيغة PDF و Excel بنجاح للموافقة والتوقيع من الدكتور مراد (المدير)!"
                    ),
                    color = Color(0xFF065F46),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }

    // High fidelity "Daily Quality Report PDF" simulation UI layout
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFCFDFD)),
        border = BorderStroke(2.dp, Color.Gray.copy(alpha = 0.3f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "REWAYA HOTELS & RESORTS GROUP",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.DarkGray
            )
            Text(
                text = "DAILY QUALITY & HYGIENE STATUS REPORT",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            // EXECUTIVE PREPARED CARD
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFF1F5F9))
                    .padding(10.dp)
            ) {
                Column {
                    Text(text = "PREPARED FOR:", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = "Dr. Morad", fontSize = 14.sp, fontWeight = FontWeight.ExtraBold, color = Color.Black)
                    Text(
                        text = "Director of Quality Control & Hygiene Department",
                        fontSize = 11.sp,
                        color = Color(0xFFF59E0B),
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Report Date: June 18, 2026 | Generated: 18:00 (GMT+2)", fontSize = 9.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // 1. PORTFOLIO BREAKDOWN
            Text(text = "1. PORTFOLIO COMPLIANCE OVERVIEW", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(6.dp))

            listOf(
                Triple("Rewaya Majestic", "96.4%", "1 Urgent"),
                Triple("Rewaya Inn", "91.0%", "3 Routine"),
                Triple("Rewaya Luxury Hotel & Resort", "95.8%", "1 Important")
            ).forEach { (hName, comp, openCount) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = hName, fontSize = 11.sp, color = Color.Black)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(text = comp, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text(text = openCount, fontSize = 11.sp, color = Color(0xFFEF4444))
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFE2E8F0))
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "PORTFOLIO AVERAGE COMPLIANCE RATE: 94.4%", fontWeight = FontWeight.ExtraBold, fontSize = 11.sp, color = Color.Black)
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // 2. OPERATIONAL ROLES FEEDS
            Text(text = "2. OPERATIONAL SUMMARY BY RESPONSIBLE MANAGERS", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(8.dp))

            listOf(
                Triple("REWAYA MAJESTIC", "Dr. Mohamed Hussien", "Completed Audits: F&B Main Kitchen (Pass), Housekeeping Zone 2A (Pass)"),
                Triple("REWAYA INN", "Mr. Mohamed Hassan", "Completed Audits: Public Reception Area (Pass), Back of House Laundry (Fail)"),
                Triple("REWAYA RESORT", "Dr. Ahmed Sheriff", "Completed Audits: Main Pool Guard Post (Pass), Italian Restaurant (Pass)")
            ).forEach { (hotel, manager, summary) ->
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Text(text = "* $hotel", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = Color.Black)
                    Text(text = "Responsible: $manager", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text(text = summary, fontSize = 10.sp, color = Color.DarkGray)
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // 3. ACTION ITEMS
            Text(text = "3. CRITICAL ACTION PLAN PLAN", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Color.Black)
            Spacer(modifier = Modifier.height(6.dp))

            correctiveActions.take(3).forEach { act ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = act.issueDescription.take(45) + "...", fontSize = 9.sp, color = Color.DarkGray, modifier = Modifier.weight(1f))
                    Text(text = act.priority, color = if (act.priority == "URGENT") Color.Red else Color(0xFFFF9800), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()
            Spacer(modifier = Modifier.height(12.dp))

            // Footers details
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Director Approval Status:", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.Gray)
                            .padding(4.dp)
                    ) {
                        Text(text = " [  ] APPROVED ", fontSize = 9.sp)
                    }
                    Box(
                        modifier = Modifier
                            .border(1.dp, Color.Gray)
                            .padding(4.dp)
                    ) {
                        Text(text = " [  ] REVIEW REQUIRED ", fontSize = 9.sp)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// 7. GAMIFICATION & ENGAGEMENT LEADERBOARD
// -------------------------------------------------------------
@Composable
fun LeaderboardTabView(
    users: List<User>,
    isArabic: Boolean,
    t: (String, String) -> String,
    userPoints: Int
) {
    Text(
        text = t("Rewaya Safety Leadership Shield", "لوحة شرف فرسان الجودة والامتثال"),
        fontWeight = FontWeight.Bold,
        fontSize = 15.sp,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = Color(0xFFF59E0B),
                modifier = Modifier.size(54.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = t("Your Active Score:", "نقاط الجودة والنشاط الخاصة بك:"),
                color = Color.White,
                fontSize = 13.sp
            )
            Text(
                text = "$userPoints XP",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFF59E0B)
            )
            Text(
                text = t("Lead the group with fast corrective response!", "كن في مقدمة فرسان الفحص عبر سرعة الاستجابة التصحيحية!"),
                fontSize = 10.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = t("Active Standings Board", "لوحة الترتيب للمفتشين والمدراء"),
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 8.dp)
    )

    val standings = listOf(
        Pair("Dr. Mohamed Hussien", 480),
        Pair("Dr. Morad", 410),
        Pair("Dr. Ahmed Sheriff", 350),
        Pair("Mr. Mohamed Hassan", 200)
    )

    standings.forEachIndexed { idx, player ->
        val rankColor = when (idx) {
            0 -> Color(0xFFF59E0B) // Gold
            1 -> Color(0xFF94A3B8) // Silver
            2 -> Color(0xFFB45309) // Bronze
            else -> Color.Gray
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(rankColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${idx + 1}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                    Text(
                        text = player.first,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
                Text(
                    text = "${player.second} XP",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 13.sp,
                    color = Color(0xFF0F172A)
                )
            }
        }
    }
}

// Custom data quadruple tuple
data class Quadruple<A, B, C, D, E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E
)
