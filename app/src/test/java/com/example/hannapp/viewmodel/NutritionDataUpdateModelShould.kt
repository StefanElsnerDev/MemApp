package com.example.hannapp.viewmodel

import androidx.paging.PagingData
import com.example.hannapp.data.distinct.Kcal
import com.example.hannapp.data.distinct.Name
import com.example.hannapp.data.model.NutritionUiModel
import com.example.hannapp.domain.DeleteNutritionUseCase
import com.example.hannapp.domain.GetNutritionUseCase
import com.example.hannapp.domain.UpdateNutritionUseCase
import com.example.hannapp.ui.viewmodel.NutritionUpdateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionDataUpdateModelShould {

    lateinit var nutritionDataUpdateViewModel: NutritionUpdateViewModel
    private val getNutritionUseCase = mock(GetNutritionUseCase::class.java)
    private val updateNutritionUseCase = mock(UpdateNutritionUseCase::class.java)
    private val deleteNutritionUseCase = mock(DeleteNutritionUseCase::class.java)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val nutritionUiModels = listOf(
        NutritionUiModel(id = 100, name = "Apple", kcal = "12"),
        NutritionUiModel(id = 200, name = "Banana", kcal = "3.4")
    )

    private val pagingData = PagingData.from(nutritionUiModels)
    private val nutrimentsFlow = flowOf(pagingData)

    @BeforeEach
    fun beforeEach() = runTest {
        Dispatchers.setMain(testDispatcher)

        whenever(getNutritionUseCase.getAll()).thenReturn(
            nutrimentsFlow
        )
    }

    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class Instantiation {
        @Test
        fun emitsStateWithFetchedNutrimentsOnInstantiation() = runTest {
            nutritionDataUpdateViewModel = NutritionUpdateViewModel(
                getNutritionUseCase = getNutritionUseCase,
                updateNutritionUseCase = updateNutritionUseCase,
                deleteNutritionUseCase = deleteNutritionUseCase,
                testDispatcher
            )

            verify(getNutritionUseCase).getAll()
            // TODO: Extension 'cachedIn' returns new instance of paging data with different memory reference than mocked paging data
            // Assertions.assertEquals(pagingData, nutritionDataUpdateViewModel.nutriments.first())
        }
    }

    @Nested
    inner class Select {

        @BeforeEach
        fun beforeEach() {
            nutritionDataUpdateViewModel = NutritionUpdateViewModel(
                getNutritionUseCase = getNutritionUseCase,
                updateNutritionUseCase = updateNutritionUseCase,
                deleteNutritionUseCase = deleteNutritionUseCase,
                testDispatcher
            )
        }

        @Test
        fun emitStateWithSelectedFoodNutrition() = runTest {
            Assertions.assertEquals(
                NutritionUiModel(),
                nutritionDataUpdateViewModel.uiComponentState.value.nutritionUiModel
            )

            nutritionDataUpdateViewModel.selectItem(nutritionUiModels.last())

            Assertions.assertEquals(
                nutritionUiModels.last(),
                nutritionDataUpdateViewModel.uiComponentState.value.nutritionUiModel
            )
        }

        @Test
        fun emitStateWithCachedNutritionUiModel() = runTest {
            Assertions.assertEquals(
                NutritionUiModel(),
                nutritionDataUpdateViewModel.uiState.value.cachedNutritionUiModel
            )

            nutritionDataUpdateViewModel.selectItem(nutritionUiModels.last())

            Assertions.assertEquals(
                nutritionUiModels.last(),
                nutritionDataUpdateViewModel.uiState.value.cachedNutritionUiModel
            )
        }
    }

    @Nested
    inner class ChangeOnCallback {

        @BeforeEach
        fun beforeEach() = runTest {
            nutritionDataUpdateViewModel = NutritionUpdateViewModel(
                getNutritionUseCase = getNutritionUseCase,
                updateNutritionUseCase = updateNutritionUseCase,
                deleteNutritionUseCase = deleteNutritionUseCase,
                testDispatcher
            )
        }

        @Test
        fun changeUiStateOnCallback() {
            val updatedNutritionUiModel =
                NutritionUiModel(id = null, name = "Strawberry", kcal = "987cal")

            nutritionDataUpdateViewModel.onNutritionChange(Name(), "Strawberry")
            nutritionDataUpdateViewModel.onNutritionChange(Kcal(), "987cal")

            Assertions.assertEquals(
                updatedNutritionUiModel,
                nutritionDataUpdateViewModel.uiComponentState.value.nutritionUiModel
            )
        }
    }

    @Nested
    inner class Updating {
        @BeforeEach
        fun beforeEach() = runTest {
            nutritionDataUpdateViewModel = NutritionUpdateViewModel(
                getNutritionUseCase = getNutritionUseCase,
                updateNutritionUseCase = updateNutritionUseCase,
                deleteNutritionUseCase = deleteNutritionUseCase,
                testDispatcher
            )
        }

        @Test
        fun invokeUseCaseForUpdatingSelectedItem() = runTest {
            nutritionDataUpdateViewModel.selectItem(nutritionUiModels.last())

            nutritionDataUpdateViewModel.update()

            Assertions.assertEquals(
                nutritionUiModels.last(),
                nutritionDataUpdateViewModel.uiComponentState.value.nutritionUiModel
            )
            verify(updateNutritionUseCase).invoke(nutritionUiModels.last())
        }

        @Test
        fun emitStateWithUpdatedNutritionUiModel() {
            nutritionDataUpdateViewModel.selectItem(nutritionUiModels.last())

            nutritionDataUpdateViewModel.update()

            Assertions.assertEquals(
                nutritionUiModels.last(),
                nutritionDataUpdateViewModel.uiState.value.cachedNutritionUiModel
            )
        }

        @Test
        fun emitFailureStateOnFailingUpdate() = runTest {
            whenever(updateNutritionUseCase.invoke(any())).thenReturn(false)

            nutritionDataUpdateViewModel.update()

            assertThat(nutritionDataUpdateViewModel.uiState.value.errorMessage?.messageRes).isNotNull
            assertThat(nutritionDataUpdateViewModel.uiState.value.errorMessage?.message).isNull()
        }

        @Test
        fun emitFailureStateOnThrowingUpdate() = runTest {
            val errorMessage = "Internal Error"
            whenever(updateNutritionUseCase.invoke(any())).thenThrow(RuntimeException(errorMessage))

            nutritionDataUpdateViewModel.update()

            assertThat(nutritionDataUpdateViewModel.uiState.value.errorMessage?.messageRes).isNull()
            assertThat(nutritionDataUpdateViewModel.uiState.value.errorMessage?.message).isEqualTo(errorMessage)
        }
    }

    @Nested
    inner class Delete {

        @BeforeEach
        fun beforeEach() = runTest {
            nutritionDataUpdateViewModel = NutritionUpdateViewModel(
                getNutritionUseCase = getNutritionUseCase,
                updateNutritionUseCase = updateNutritionUseCase,
                deleteNutritionUseCase = deleteNutritionUseCase,
                testDispatcher
            )
        }

        @Test
        fun invokeDeleteUseCase() = runTest {
            nutritionDataUpdateViewModel.delete(nutritionUiModels.last())

            verify(deleteNutritionUseCase).invoke(any())
        }
    }
}
