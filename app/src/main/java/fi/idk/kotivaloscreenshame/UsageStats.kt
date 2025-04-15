package fi.idk.kotivaloscreenshame

data class UsageStats(
    val firstTimeStamp: Long,
    val lastTimeStamp: Long,
    val totalTimeInForeground: Long,
    val packageName: String,
)
