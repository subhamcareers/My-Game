package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.GameEngine
import com.example.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GameUiState(
  val biome: Biome = Biome.RIVERLAND,
  val gameMode: GameMode = GameMode.SANDBOX,
  val currentScenario: ScenarioId? = null,
  val season: Season = Season.SPRING,
  val seasonDay: Int = 1,
  val yearCount: Int = 1,
  val totalDays: Int = 1,
  val resources: KingdomResources = KingdomResources(),
  val population: Int = 4,
  val maxPopulation: Int = 9,
  val averageHappiness: Float = 85f,
  val approvalRating: Float = 85f,
  val electionDaysRemaining: Int = 100,
  val taxRate: TaxRate = TaxRate.NORMAL,
  val activeDecrees: List<ActiveDecree> = emptyList(),
  val isRaidWarning: Boolean = false,
  val isRaidActive: Boolean = false,
  val raidCountdown: Int = 75,
  val raidWave: Int = 0,
  val bannerMessage: String? = null,
  val gameSpeed: Float = 1.0f,
  val selectedTilePos: GridPos? = null,
  val selectedBuilding: BuildingInstance? = null,
  val buildingToPlace: BuildingType? = null,
  val isBulldozeMode: Boolean = false,
  val isRoadMode: Boolean = false,
  val scenarioComplete: Boolean = false
)

class GameViewModel : ViewModel() {
  private var engine = GameEngine(Biome.RIVERLAND, GameMode.SANDBOX, null)

  private val _uiState = MutableStateFlow(GameUiState())
  val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

  val tiles: Array<Array<MapTile>> get() = engine.tiles
  val buildings: List<BuildingInstance> get() = engine.buildings
  val townies: List<TownieUnit> get() = engine.townies
  val militaryUnits: List<MilitaryUnit> get() = engine.militaryUnits
  val projectiles: List<ArrowProjectile> get() = engine.projectiles
  val scenarioProgress: ScenarioProgress get() = engine.scenarioProgress

  init {
    startSimulationLoop()
  }

  private fun startSimulationLoop() {
    viewModelScope.launch {
      var lastTime = System.nanoTime()
      while (true) {
        val now = System.nanoTime()
        val dt = ((now - lastTime) / 1_000_000_000f).coerceIn(0.01f, 0.1f)
        lastTime = now

        engine.update(dt, _uiState.value.gameSpeed)
        syncUiState()
        delay(100) // 10 updates per second for UI state refresh
      }
    }
  }

  private fun syncUiState() {
    val cur = _uiState.value
    val maxPop = 4 + engine.buildings.count { it.type == BuildingType.HOUSE && it.hp > 0 } * 5
    _uiState.value = cur.copy(
      biome = engine.biome,
      gameMode = engine.gameMode,
      currentScenario = engine.currentScenario,
      season = engine.currentSeason,
      seasonDay = engine.seasonDay,
      yearCount = engine.yearCount,
      totalDays = engine.totalDays,
      resources = engine.resources.copy(),
      population = engine.townies.size,
      maxPopulation = maxPop,
      averageHappiness = engine.getAverageHappiness(),
      approvalRating = engine.popularApprovalRating,
      electionDaysRemaining = engine.electionDaysRemaining,
      taxRate = engine.taxRate,
      activeDecrees = engine.activeDecrees.map { it.copy() },
      isRaidWarning = engine.isRaidWarningActive,
      isRaidActive = engine.isRaidActive,
      raidCountdown = engine.raidCountdownSeconds,
      raidWave = engine.raidWaveCount,
      bannerMessage = engine.bannerMessage,
      scenarioComplete = engine.scenarioProgress.isComplete
    )
  }

  fun setGameSpeed(speed: Float) {
    _uiState.value = _uiState.value.copy(gameSpeed = speed)
  }

  fun onTileTapped(pos: GridPos) {
    val state = _uiState.value
    if (state.isBulldozeMode) {
      val b = engine.buildings.firstOrNull { it.occupies(pos) }
      if (b != null) {
        engine.demolish(b)
        _uiState.value = _uiState.value.copy(selectedBuilding = null, selectedTilePos = null)
      }
      return
    }

    if (state.isRoadMode) {
      engine.build(BuildingType.ROAD, pos)
      return
    }

    if (state.buildingToPlace != null) {
      val success = engine.build(state.buildingToPlace, pos)
      if (success) {
        _uiState.value = _uiState.value.copy(buildingToPlace = null, selectedTilePos = pos)
      }
      return
    }

    // Inspect tile or building
    val b = engine.buildings.firstOrNull { it.occupies(pos) }
    _uiState.value = _uiState.value.copy(
      selectedTilePos = pos,
      selectedBuilding = b
    )
  }

  fun selectBuildingToPlace(type: BuildingType) {
    _uiState.value = _uiState.value.copy(
      buildingToPlace = type,
      isBulldozeMode = false,
      isRoadMode = false,
      selectedBuilding = null
    )
  }

  fun toggleBulldozeMode() {
    val next = !_uiState.value.isBulldozeMode
    _uiState.value = _uiState.value.copy(
      isBulldozeMode = next,
      buildingToPlace = null,
      isRoadMode = false
    )
  }

  fun toggleRoadMode() {
    val next = !_uiState.value.isRoadMode
    _uiState.value = _uiState.value.copy(
      isRoadMode = next,
      isBulldozeMode = false,
      buildingToPlace = null
    )
  }

  fun cancelPlacement() {
    _uiState.value = _uiState.value.copy(
      buildingToPlace = null,
      isBulldozeMode = false,
      isRoadMode = false
    )
  }

  fun clearSelection() {
    _uiState.value = _uiState.value.copy(
      selectedTilePos = null,
      selectedBuilding = null
    )
  }

  fun setTaxRate(taxRate: TaxRate) {
    engine.taxRate = taxRate
    engine.setBanner("Royal Tax Rate set to ${taxRate.label}.")
    syncUiState()
  }

  fun enactDecree(decree: DecreeType) {
    engine.enactDecree(decree)
    syncUiState()
  }

  fun recruitSoldier(unitType: UnitType) {
    engine.recruitUnit(unitType)
    syncUiState()
  }

  fun demolishSelectedBuilding() {
    val b = _uiState.value.selectedBuilding ?: return
    engine.demolish(b)
    _uiState.value = _uiState.value.copy(selectedBuilding = null, selectedTilePos = null)
  }

  fun startNewGame(biome: Biome, mode: GameMode, scenario: ScenarioId?) {
    engine = GameEngine(biome, mode, scenario)
    _uiState.value = GameUiState(
      biome = biome,
      gameMode = mode,
      currentScenario = scenario
    )
    syncUiState()
  }
}
