package com.example.data.model

import androidx.compose.ui.graphics.Color

enum class StormRiskLevel(val label: String, val color: Color) {
    NONE("Minimal Risk", Color(0xFF10B981)),
    LOW("Low Alert", Color(0xFF38BDF8)),
    MODERATE("Moderate Risk", Color(0xFFFBBF24)),
    HIGH("High Storm Risk", Color(0xFFF97316)),
    SEVERE("Severe Weather Warning", Color(0xFFEF4444))
}

data class SkyScanResult(
    val id: String,
    val title: String,
    val cloudGenus: String,
    val cloudFamily: String,
    val estimatedAltitudeKm: String,
    val confidencePercent: Int,
    val precipitationRiskPercent: Int,
    val stormRisk: StormRiskLevel,
    val opticalPhenomenon: String,
    val meteorologicalAnalysis: String,
    val outdoorRecommendation: String,
    val flightAviationImpact: String,
    val imageDescription: String
)

object PresetSkyScenarios {
    val presets = listOf(
        SkyScanResult(
            id = "cumulonimbus",
            title = "Severe Thunderhead Cloud",
            cloudGenus = "Cumulonimbus Calvus / Incus",
            cloudFamily = "Vertical Development (High energy)",
            estimatedAltitudeKm = "1.5 km - 12.0 km",
            confidencePercent = 94,
            precipitationRiskPercent = 92,
            stormRisk = StormRiskLevel.SEVERE,
            opticalPhenomenon = "Dark anvil formation, intense virga rain shafts, lightning flashes aloft",
            meteorologicalAnalysis = "Rapid vertical thermal convection with towering updrafts. Strong updrafts support heavy rain bursts, potential hail, and severe microburst winds.",
            outdoorRecommendation = "Seek immediate sturdy shelter indoors. High lightning danger and localized flash flood risk.",
            flightAviationImpact = "Extreme low-level wind shear, severe turbulence, icing danger. Divert flight paths.",
            imageDescription = "Towering vertical anvil cloud with dense dark base and spreading anvil top."
        ),
        SkyScanResult(
            id = "altocumulus",
            title = "Mackerel Sky / Wave Clouds",
            cloudGenus = "Altocumulus Stratiformis",
            cloudFamily = "Mid-Level Tropospheric Cloud",
            estimatedAltitudeKm = "2.5 km - 6.0 km",
            confidencePercent = 89,
            precipitationRiskPercent = 38,
            stormRisk = StormRiskLevel.LOW,
            opticalPhenomenon = "Rippled wave formation, corona rings around sun or moon",
            meteorologicalAnalysis = "Shallow convection in unstable mid-troposphere. Indicates advancing warm front or moisture intrusion over the region.",
            outdoorRecommendation = "Favorable for outdoor activities currently; rain showers possible within 12-24 hours.",
            flightAviationImpact = "Light to moderate chop at cruising altitudes.",
            imageDescription = "Fluffy parallel wave sheets resembling fish scales or sheep fleece."
        ),
        SkyScanResult(
            id = "cirrus",
            title = "Mare's Tails / Ice Crystals",
            cloudGenus = "Cirrus Fibratus / Uncinus",
            cloudFamily = "High-Level Ice Cloud",
            estimatedAltitudeKm = "7.0 km - 11.5 km",
            confidencePercent = 96,
            precipitationRiskPercent = 15,
            stormRisk = StormRiskLevel.NONE,
            opticalPhenomenon = "22-degree solar halo, delicate wispy streaks, glowing parhelia (sun dogs)",
            meteorologicalAnalysis = "Composed exclusively of hexagonal ice crystals. Often heralds an approaching frontal system in 24 to 36 hours.",
            outdoorRecommendation = "Excellent clear conditions. High solar exposure; apply UV protection.",
            flightAviationImpact = "Smooth flight profile, negligible icing.",
            imageDescription = "Delicate white hair-like streaks blown by upper jet stream winds."
        ),
        SkyScanResult(
            id = "nimbostratus",
            title = "Low Dark Rain Blanket",
            cloudGenus = "Nimbostratus",
            cloudFamily = "Low Multi-Layer Precipitator",
            estimatedAltitudeKm = "0.5 km - 3.5 km",
            confidencePercent = 91,
            precipitationRiskPercent = 95,
            stormRisk = StormRiskLevel.HIGH,
            opticalPhenomenon = "Diffuse dark horizon, obscured solar disc, ragged pannus scud clouds",
            meteorologicalAnalysis = "Extensive continuous stratiform precipitation zone with high boundary layer relative humidity (>90%). Continuous steady rainfall.",
            outdoorRecommendation = "Continuous rain active. Waterproof gear essential, avoid muddy trails.",
            flightAviationImpact = "Low ceiling and reduced visibility for VFR flights.",
            imageDescription = "Uniform dark grey cloud base with no visible sunlight and continuous rain."
        ),
        SkyScanResult(
            id = "stratocumulus",
            title = "Low Rolling Cloud Banks",
            cloudGenus = "Stratocumulus Perlucidus",
            cloudFamily = "Low-Level Cloud",
            estimatedAltitudeKm = "0.8 km - 2.0 km",
            confidencePercent = 88,
            precipitationRiskPercent = 25,
            stormRisk = StormRiskLevel.NONE,
            opticalPhenomenon = "Patchy blue sky visible through gaps, soft crepuscular rays",
            meteorologicalAnalysis = "Capped by temperature inversion aloft. Weak convection prevents major vertical development. Minimal precipitation.",
            outdoorRecommendation = "Pleasant overcast conditions with cool shade. Great for running and cycling.",
            flightAviationImpact = "Minor turbulence below base; clear above cloud layer.",
            imageDescription = "Broad rolls or patches of gray-white clouds with blue sky peeking through."
        )
    )
}
