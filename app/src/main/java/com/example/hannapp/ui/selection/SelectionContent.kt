package com.example.hannapp.ui.selection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.hannapp.R
import com.example.hannapp.data.model.NutritionUiModel
import com.example.hannapp.ui.components.NutrimentCard
import com.example.hannapp.ui.dropdown.DropDownDialog
import com.example.hannapp.ui.dropdown.EmptySelectionDropDownMenu
import com.example.hannapp.ui.input.InputField
import com.example.hannapp.ui.theme.Constraints.PADDING
import com.example.hannapp.ui.theme.HannAppTheme
import com.example.hannapp.ui.viewmodel.NutrimentSelectContract
import kotlinx.coroutines.flow.flowOf

@Composable
fun SelectionContent(
    modifier: Modifier,
    uiState: NutrimentSelectContract.State,
    event: (NutrimentSelectContract.Event) -> Unit,
    quantity: String,
    selectedNutriment: NutritionUiModel,
    pagingItems: LazyPagingItems<NutritionUiModel>
) {
    val focusManager = LocalFocusManager.current

    Surface(
        modifier = modifier.wrapContentHeight(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .wrapContentSize()
                .padding(PADDING),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            var expanded by remember { mutableStateOf(false) }

            when (uiState.nutritionUiModel.id != null) {
                false -> EmptySelectionDropDownMenu(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    event(NutrimentSelectContract.Event.OnGetAll)
                    expanded = true
                }

                true -> NutrimentCard(
                    nutritionUiModel = selectedNutriment,
                    onClick = {
                        event(NutrimentSelectContract.Event.OnGetAll)
                        expanded = true
                    }
                )
            }

            InputField(
                value = quantity,
                onValueChange = { event(NutrimentSelectContract.Event.OnSetQuantity(it)) },
                modifier = Modifier,
                label = stringResource(id = R.string.quantity),
                isError = false,
                keyboardActions = KeyboardActions(
                    onDone = {
                        focusManager.clearFocus()
                        event(NutrimentSelectContract.Event.OnAdd)
                    }
                )
            )

            if (expanded) {
                DropDownDialog(
                    pagingItems = pagingItems,
                    onDismiss = { expanded = false },
                    itemContent = {
                        NutrimentCard(
                            nutritionUiModel = it,
                            onClick = { nutriment ->
                                event(NutrimentSelectContract.Event.OnSelect(nutriment))
                                expanded = false
                            }
                        )
                    }
                )
            }
        }
    }
}

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape")
@Composable
fun SelectionContent_LightMode() {
    HannAppTheme {
        SelectionContent(
            modifier = Modifier,
            uiState = NutrimentSelectContract.State(),
            event = {},
            quantity = "",
            pagingItems = flowOf(PagingData.from(listOf(NutritionUiModel()))).collectAsLazyPagingItems(),
            selectedNutriment = NutritionUiModel()
        )
    }
}
