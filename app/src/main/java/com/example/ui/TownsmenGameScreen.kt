package com.example.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun TownsmenGameScreen(
  viewModel: GameViewModel,
  modifier: Modifier = Modifier
) {
  val uiState by viewModel.uiState.collectAsStateWithLifecycle()

  var showBuildMenu by remember { mutableStateOf(false) }
  var showCouncilDialog by remember { mutableStateOf(false) }
  var showMilitaryDialog by remember { mutableStateOf(false) }
  var showScenarioDialog by remember { mutableStateOf(false) }

  Box(modifier = modifier.fillMaxSize()) {
    // 1. Isometric Strategy Canvas
    IsoGameCanvas(
      tiles = viewModel.tiles,
      buildings = viewModel.buildings,
      townies = viewModel.townies,
      militaryUnits = viewModel.militaryUnits,
      projectiles = viewModel.projectiles,
      season = uiState.season,
      biome = uiState.biome,
      selectedTilePos = uiState.selectedTilePos,
      buildingToPlace = uiState.buildingToPlace,
      onTileTapped = { pos ->
        viewModel.onTileTapped(pos)
      }
    )

    // 2. Medieval Strategy HUD
    GameHud(
      uiState = uiState,
      onSpeedChange = { speed -> viewModel.setGameSpeed(speed) },
      onOpenBuildMenu = { showBuildMenu = true },
      onToggleRoadMode = { viewModel.toggleRoadMode() },
      onToggleBulldozeMode = { viewModel.toggleBulldozeMode() },
      onCancelMode = { viewModel.cancelPlacement() },
      onOpenCouncil = { showCouncilDialog = true },
      onOpenMilitary = { showMilitaryDialog = true },
      onOpenScenarios = { showScenarioDialog = true }
    )

    // 3. Sheets & Modals
    if (showBuildMenu) {
      BuildMenuSheet(
        uiState = uiState,
        onSelectBuilding = { bType ->
          viewModel.selectBuildingToPlace(bType)
        },
        onDismiss = { showBuildMenu = false }
      )
    }

    if (uiState.selectedBuilding != null && !uiState.isBulldozeMode && uiState.buildingToPlace == null) {
      BuildingDetailSheet(
        building = uiState.selectedBuilding,
        onDemolish = { viewModel.demolishSelectedBuilding() },
        onDismiss = { viewModel.clearSelection() }
      )
    }

    if (showCouncilDialog) {
      CouncilDialog(
        uiState = uiState,
        onTaxRateChange = { rate -> viewModel.setTaxRate(rate) },
        onEnactDecree = { decree -> viewModel.enactDecree(decree) },
        onDismiss = { showCouncilDialog = false }
      )
    }

    if (showMilitaryDialog) {
      MilitaryDialog(
        uiState = uiState,
        onRecruitUnit = { unit -> viewModel.recruitSoldier(unit) },
        onDismiss = { showMilitaryDialog = false }
      )
    }

    if (showScenarioDialog) {
      ScenarioDialog(
        uiState = uiState,
        onStartGame = { biome, mode, scenario ->
          viewModel.startNewGame(biome, mode, scenario)
        },
        onDismiss = { showScenarioDialog = false }
      )
    }
  }
}
