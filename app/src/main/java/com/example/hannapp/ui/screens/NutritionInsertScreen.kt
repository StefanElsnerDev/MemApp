package com.example.hannapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.hannapp.data.distinct.*
import com.example.hannapp.data.model.NutritionModel
import com.example.hannapp.data.model.api.Nutriments
import com.example.hannapp.data.model.api.Product
import com.example.hannapp.navigation.NavigationActions
import com.example.hannapp.ui.button.FAB
import com.example.hannapp.ui.components.AppScaffold
import com.example.hannapp.ui.components.AppTopBar
import com.example.hannapp.ui.components.NavigationBar
import com.example.hannapp.ui.input.NutritionDataGroup
import com.example.hannapp.ui.input.SearchBar
import com.example.hannapp.ui.components.ProductCard
import com.example.hannapp.ui.dropdown.DropDownDialog
import com.example.hannapp.ui.theme.HannAppTheme
import com.example.hannapp.ui.viewmodel.NutritionInsertViewModel
import kotlinx.coroutines.flow.flowOf

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun NutritionInsertContent(
    navController: NavHostController = rememberNavController(),
    nutritionModel: NutritionModel = NutritionModel(),
    uiComponents: List<NutritionComponent> = listOf(
        Name(),
        Kcal(),
        Protein(),
        Fat(),
        Carbohydrates(),
        Sugar(),
        Fiber(),
        Alcohol()
    ),
    pagingItems: LazyPagingItems<Product> = flowOf(
        PagingData.from(
            listOf(
                Product(
                    "123", "name",
                    Nutriments(1.2, 3.4, 5.5, 6.6, 7.7, 8.8, 0.0)
                )
            )
        )
    ).collectAsLazyPagingItems(),
    errors: Set<NutritionDataComponent> = emptySet(),
    showErrors: Boolean = false,
    onComponentValueChange: (NutritionComponent, String) -> Unit = { _, _ -> },
    onReset: (NutritionDataComponent) -> Unit = { _ -> },
    onAdd: () -> Unit = {},
    onSearch: (String) -> Unit = {},
    onItemSelect: (Product) -> Unit = {}
) {
    val navigationActions = remember(navController) {
        NavigationActions(navController)
    }

    var expanded by remember { mutableStateOf(false) }

    HannAppTheme {
        AppScaffold(
            topBar = {
                AppTopBar() {
                    IconButton(
                        onClick = {
                            navigationActions.navigateToUpdateData()
                        }
                    ) { Icon(Icons.Filled.Edit, null) }
                }
            },
            bottomBar = { NavigationBar(navController) },
            floatingActionButton = {
                FAB({ Icon(Icons.Filled.Add, null) }) { onAdd() }
            }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceAround,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                SearchBar(
                    modifier = Modifier.padding(horizontal = 48.dp),
                    onSearch = {
                        expanded = true
                        onSearch(it)
                    }
                )

                NutritionDataGroup(
                    onComponentValueChange = onComponentValueChange,
                    onReset = onReset,
                    uiComponents = uiComponents,
                    nutritionModel = nutritionModel,
                    errors = errors,
                    showErrors = showErrors
                )

                if (expanded) {
                    DropDownDialog(
                        pagingItems = pagingItems,
                        onDismiss = { expanded = false },
                        itemContent = {
                            ProductCard(
                                product = it,
                                onItemClick = { item ->
                                    onItemSelect(item)
                                    expanded = false
                                }
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun NutritionInsertScreen(
    viewModel: NutritionInsertViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val uiComponents by viewModel.uiComponents.collectAsState()
    val pagingItems = viewModel.products.collectAsLazyPagingItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceAround,
    ) {
        NutritionInsertContent(
            navController = navController,
            nutritionModel = uiState.nutrition,
            uiComponents = uiComponents,
            pagingItems = pagingItems,
            errors = uiState.errors,
            showErrors = uiState.showErrors,
            onComponentValueChange = { component, value ->
                viewModel.onNutritionChange(
                    component,
                    value
                )
                viewModel.validate()
            },
            onReset = { viewModel.resetError(it) },
            onAdd = {
                if (uiState.isValid) {
                    viewModel.insert()
                } else {
                    viewModel.validate()
                    viewModel.showErrors()
                }
            },
            onSearch = { viewModel.search(it) },
            onItemSelect = { viewModel.select(it) }
        )
    }
}
