package moe.skd.displaynamefmt.config

import moe.skd.displaynamefmt.condition.NamedCondition

internal data class DisplayNameConfig(
    val format: String,
    val prefix: String,
    val suffix: String,
    val refreshIntervalTicks: Long,
    val conditions: Map<String, NamedCondition>,
)
