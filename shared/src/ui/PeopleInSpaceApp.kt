package dev.johnoreilly.peopleinspace.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffold
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.layout.PaneAdaptedValue
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.navigationevent.NavigationEventInfo
import androidx.navigationevent.compose.NavigationBackHandler
import androidx.navigationevent.compose.rememberNavigationEventState
import androidx.compose.ui.graphics.vector.ImageVector
import kotlinx.coroutines.launch

enum class AppDestinations(val label: String, val icon: ImageVector) {
    PERSON_LIST("People", Icons.Default.Person),
    ISS_POSITION("ISS Position", Icons.Default.LocationOn),
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun PeopleInSpaceApp() {
    var currentDestination by rememberSaveable { mutableStateOf(AppDestinations.PERSON_LIST) }
    // Keyed by name: only the name needs to survive a configuration change.
    val navigator = rememberListDetailPaneScaffoldNavigator<String>()
    val scope = rememberCoroutineScope()

    // System back (Android back, iOS edge swipe) closes the detail pane
    NavigationBackHandler(
        state = rememberNavigationEventState(NavigationEventInfo.None),
        isBackEnabled = navigator.canNavigateBack(),
        onBackCompleted = { scope.launch { navigator.navigateBack() } },
    )

    PeopleInSpaceTheme {
        NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = { Icon(it.icon, contentDescription = it.label) },
                        label = { Text(it.label) },
                        selected = it == currentDestination,
                        onClick = { currentDestination = it },
                    )
                }
            }
        ) {
            when (currentDestination) {
                AppDestinations.PERSON_LIST -> {
                    ListDetailPaneScaffold(
                        directive = navigator.scaffoldDirective,
                        value = navigator.scaffoldValue,
                        listPane = {
                            PersonListRoute { person ->
                                scope.launch { navigator.navigateTo(ListDetailPaneScaffoldRole.Detail, person.name) }
                            }
                        },
                        detailPane = {
                            navigator.currentDestination?.contentKey?.let {
                                PersonDetailsRoute(
                                    personName = it,
                                    showBackButton = !navigator.isListPaneVisible(),
                                    popBack = { scope.launch { navigator.navigateBack() } },
                                )
                            }
                        },
                    )
                }
                AppDestinations.ISS_POSITION -> ISSPositionRoute()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
private fun <T> ThreePaneScaffoldNavigator<T>.isListPaneVisible(): Boolean =
    scaffoldValue[ListDetailPaneScaffoldRole.List] == PaneAdaptedValue.Expanded
