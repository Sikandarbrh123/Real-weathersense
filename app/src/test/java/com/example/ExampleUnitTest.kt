package com.example

import com.example.data.model.PresetSkyScenarios
import com.example.data.model.StormRiskLevel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun testPresetSkyScenarios() {
    val presets = PresetSkyScenarios.presets
    assertTrue(presets.isNotEmpty())
    val cumulonimbus = presets.first { it.id == "cumulonimbus" }
    assertEquals(StormRiskLevel.SEVERE, cumulonimbus.stormRisk)
    assertTrue(cumulonimbus.precipitationRiskPercent > 80)
  }
}
