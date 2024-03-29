package com.example.hannapp.ui.components

import android.content.res.Configuration.UI_MODE_NIGHT_NO
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.hannapp.R
import com.example.hannapp.data.model.NavigationItem
import com.example.hannapp.navigation.Destination
import com.example.hannapp.navigation.NavigationActions
import com.example.hannapp.ui.theme.HannAppTheme

@Composable
fun NavigationBar(
    navController: NavHostController
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val currentRoute = currentRoute(navController = navController)

            navigationItems(navController).forEach { item ->
                NavigationBarItem(item, currentRoute)
            }
        }
    }
}

@Composable
private fun navigationItems(navController: NavHostController): List<NavigationItem> {
    val navigationActions = remember(navController) {
        NavigationActions(navController)
    }

    return listOf(
        NavigationItem(
            label = R.string.database,
            icon = R.drawable.food,
            destination = Destination.DATA.value,
            action = navigationActions.navigateToData
        ),
        NavigationItem(
            label = R.string.food_selection,
            icon = R.drawable.eat,
            destination = Destination.SELECTION.value,
            action = navigationActions.navigateToSelection
        ),
        NavigationItem(
            label = R.string.nutrition_references,
            icon = R.drawable.help,
            destination = Destination.REFERENCE.value,
            action = navigationActions.navigateToReference
        )
    )
}

@Composable
private fun RowScope.NavigationBarItem(
    item: NavigationItem,
    currentRoute: String?
) {
    NavigationBarItem(
        onClick = item.action,
        icon = {
            Icon(
                painterResource(id = item.icon),
                contentDescription = "",
                modifier = Modifier.size(18.dp)
            )
        },
        label = {
            Text(
                text = stringResource(id = item.label),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.labelSmall
            )
        },
        selected = currentRoute == item.destination
    )
}

@Composable
private fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Preview(uiMode = UI_MODE_NIGHT_NO)
@Composable
fun HannappBottomBar_LightMode() {
    HannAppTheme {
        NavigationBar(rememberNavController())
    }
}

@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
fun HannappBottomBar_DarkMode() {
    HannAppTheme {
        NavigationBar(rememberNavController())
    }
}
