package com.example.hannapp.ui.screens

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.hannapp.R
import com.example.hannapp.data.distinct.*
import com.example.hannapp.data.model.NutritionUiModel
import com.example.hannapp.ui.button.FAB
import com.example.hannapp.ui.components.AppScaffold
import com.example.hannapp.ui.components.NutrimentCard
import com.example.hannapp.ui.dropdown.DropDownDialog
import com.example.hannapp.ui.dropdown.EmptySelectionDropDownMenu
import com.example.hannapp.ui.input.NutritionDataGroup
import com.example.hannapp.ui.theme.HannAppTheme
import com.example.hannapp.ui.viewmodel.ComponentUiState
import com.example.hannapp.ui.viewmodel.NutritionUpdateUiState
import com.example.hannapp.ui.viewmodel.NutritionUpdateViewModel
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutritionDataUpdateContent(
    pagingItems: LazyPagingItems<NutritionUiModel>,
    componentUiState: ComponentUiState,
    uiState: NutritionUpdateUiState,
    uiComponents: List<NutritionComponent>,
    onItemSelected: (NutritionUiModel) -> Unit,
    onDeleteSelected: (NutritionUiModel) -> Unit,
    onComponentValueChange: (NutritionComponent, String) -> Unit,
    onReset: (NutritionDataComponent) -> Unit,
    onUpdate: () -> Unit
) {
    AppScaffold(
        floatingActionButton = {
            FAB({ Icon(painterResource(id = R.drawable.change), "") }) { onUpdate() }
        }
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 12.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var expanded by remember { mutableStateOf(false) }

            when (uiState.cachedNutritionUiModel.isValid) {
                true -> NutrimentCard(
                    nutritionUiModel = uiState.cachedNutritionUiModel,
                    onClick = { expanded = true }
                )

                false -> EmptySelectionDropDownMenu(
                    modifier = Modifier.fillMaxWidth()
                ) { expanded = true }
            }

            if (expanded) {
                DropDownDialog(
                    pagingItems = pagingItems,
                    onDismiss = { expanded = false },
                    itemContent = { nutrition ->
                        NutrimentCard(
                            nutritionUiModel = nutrition,
                            onClick = {
                                onItemSelected(it)
                                expanded = false
                            },
                            onLongClick = { onDeleteSelected(it) }
                        )
                    }
                )
            }

            NutritionDataGroup(
                modifier = Modifier.wrapContentSize(),
                nutritionUiModel = componentUiState.nutritionUiModel,
                onComponentValueChange = onComponentValueChange,
                onReset = onReset,
                onLastItem = onUpdate,
                uiComponents = uiComponents,
                errors = componentUiState.errors,
                showErrors = componentUiState.showErrors
            )
        }
    }
}

@Composable
fun NutritionDataUpdateScreen(
    viewModel: NutritionUpdateViewModel = hiltViewModel()
) {
    val componentUiState by viewModel.uiComponentState.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val uiComponents by viewModel.uiComponents.collectAsState()
    val nutriments = viewModel.nutriments.collectAsLazyPagingItems()

    NutritionDataUpdateContent(
        pagingItems = nutriments,
        componentUiState = componentUiState,
        uiComponents = uiComponents,
        uiState = uiState,
        onItemSelected = {
            viewModel.selectItem(it)
        },
        onDeleteSelected = { viewModel.delete(it) },
        onComponentValueChange = { component, value ->
            viewModel.onNutritionChange(
                component,
                value
            )
            viewModel.validate()
        },
        onReset = { viewModel.resetError(it) },
        onUpdate = {
            if (componentUiState.isValid) {
                viewModel.update()
            } else {
                viewModel.validate()
                viewModel.showErrors()
            }
        }
    )
}

@Preview(
    showBackground = true,
    device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun NutritionDataUpdate_LightMode() {
    HannAppTheme {
        NutritionDataUpdateContent(
            pagingItems = flowOf(PagingData.from(listOf(NutritionUiModel()))).collectAsLazyPagingItems(),
            componentUiState = ComponentUiState(),
            uiState = NutritionUpdateUiState(),
            uiComponents = listOf(
                Name(),
                Kcal(),
                Protein(),
                Fat(),
                Carbohydrates(),
                Sugar(),
                Fiber(),
                Alcohol()
            ),
            onItemSelected = {},
            onDeleteSelected = {},
            onComponentValueChange = { _, _ -> },
            onReset = {},
            onUpdate = {},
        )
    }
}
