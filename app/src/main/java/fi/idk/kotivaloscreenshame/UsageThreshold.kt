package fi.idk.kotivaloscreenshame

data class UsageThreshold(
    val minutes: Long,
    val windowMinutes: Long,
    val notificationMessage: String
)