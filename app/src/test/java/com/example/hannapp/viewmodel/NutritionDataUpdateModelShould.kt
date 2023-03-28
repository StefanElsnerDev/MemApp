package com.example.hannapp.viewmodel

import com.example.hannapp.data.distinct.*
import com.example.hannapp.data.model.Food
import com.example.hannapp.data.model.NutritionModel
import com.example.hannapp.data.model.entity.Nutrition
import com.example.hannapp.domain.DeleteNutritionUseCase
import com.example.hannapp.domain.GetFoodUseCase
import com.example.hannapp.domain.GetNutritionUseCase
import com.example.hannapp.domain.UpdateNutritionUseCase
import com.example.hannapp.ui.viewmodel.NutritionUpdateViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.*
import org.junit.jupiter.api.*
import org.mockito.Mockito.mock
import org.mockito.kotlin.any
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionDataUpdateModelShould {

    lateinit var nutritionDataUpdateViewModel: NutritionUpdateViewModel
    private val getFoodUseCase = mock(GetFoodUseCase::class.java)
    private val getNutritionUseCase = mock(GetNutritionUseCase::class.java)
    private val updateNutritionUseCase = mock(UpdateNutritionUseCase::class.java)
    private val deleteNutritionUseCase = mock(DeleteNutritionUseCase::class.java)
    private val testDispatcher = UnconfinedTestDispatcher()

    private val food = listOf(Food(100, "Apple"), Food(200, "Banana"))
    private val nutritions = listOf(
        Nutrition(uid = 100, name = "Apple", kcal = "12kcal"),
        Nutrition(uid = 200, name = "Banana", kcal = "123kcal")
    )
    private val nutritionModels = listOf(
        NutritionModel(id = 100, name = "Apple", kcal = "12kcal"),
        NutritionModel(id = 200, name = "Banana", kcal = "123kcal")
    )

    @BeforeEach
    fun beforeEach() = runTest {
        Dispatchers.setMain(testDispatcher)

        whenever(getFoodUseCase.invoke()).thenReturn(
            flowOf(food)
        )
        whenever(getNutritionUseCase.invoke(100)).thenReturn(nutritions.first())
        whenever(getNutritionUseCase.invoke(200)).thenReturn(nutritions.last())
    }

    @AfterEach
    fun afterEach() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class Instantiation {
        @Test
        fun emitsStateWithFetchedFoodOnInstantiation() {
            nutritionDataUpdateViewModel = NutritionUpdateViewModel(
                getFoodUseCase = getFoodUseCase,
                getNutritionUseCase = getNutritionUseCase,
                updateNutritionUseCase = updateNutritionUseCase,
                deleteNutritionUseCase = deleteNutritionUseCase,
                testDispatcher
            )

            verify(getFoodUseCase).invoke()
            Assertions.assertEquals(food, nutritionDataUpdateViewModel.uiState.value.foodList)
        }

        @Test
        fun emitPreselectedNutritionDataState() = runTest {
            nutritionDataUpdateViewModel = NutritionUpdateViewModel(
                getFoodUseCase = getFoodUseCase,
                getNutritionUseCase = getNutritionUseCase,
                updateNutritionUseCase = updateNutritionUseCase,
                deleteNutritionUseCase = deleteNutritionUseCase,
                testDispatcher
            )

            verify(getNutritionUseCase).invoke(100)
            Assertions.assertEquals(
                nutritionModels.first(),
                nutritionDataUpdateViewModel.uiState.value.nutritionModel
            )
        }
    }

    @Nested
    inner class Select {

        @BeforeEach
        fun beforeEach() {
            nutritionDataUpdateViewModel = NutritionUpdateViewModel(
                getFoodUseCase = getFoodUseCase,
                getNutritionUseCase = getNutritionUseCase,
                updateNutritionUseCase = updateNutritionUseCase,
                deleteNutritionUseCase = deleteNutritionUseCase,
                testDispatcher
            )
        }

        @Test
        fun invokeUseCaseToFetchSelectedFoodNutrition() = runTest {
            nutritionDataUpdateViewModel.selectItem(1)

            verify(getNutritionUseCase).invoke(200)
        }

        @Test
        fun emitStateWithSelectedFoodNutrition() = runTest {
            nutritionDataUpdateViewModel.selectItem(1)

            Assertions.assertEquals(
                nutritionModels.last(),
                nutritionDataUpdateViewModel.uiState.value.nutritionModel
            )
        }
    }

    @Nested
    inner class ChangeOnCallback {

        @BeforeEach
        fun beforeEach() = runTest {
            nutritionDataUpdateViewModel = NutritionUpdateViewModel(
                getFoodUseCase = getFoodUseCase,
                getNutritionUseCase = getNutritionUseCase,
                updateNutritionUseCase = updateNutritionUseCase,
                deleteNutritionUseCase = deleteNutritionUseCase,
                testDispatcher
            )
        }

        @Test
        fun changeUiStateOnCallback() {
            val updatedNutritionModel = NutritionModel(id = 100, name = "Strawberry", kcal = "987cal")

            nutritionDataUpdateViewModel.onNutritionChange(Name(), "Strawberry")
            nutritionDataUpdateViewModel.onNutritionChange(Kcal(), "987cal")

            Assertions.assertEquals(
                updatedNutritionModel,
                nutritionDataUpdateViewModel.uiState.value.nutritionModel
            )
        }
    }

    @Nested
    inner class Updating {
        @BeforeEach
        fun beforeEach() = runTest {
            nutritionDataUpdateViewModel = NutritionUpdateViewModel(
                getFoodUseCase = getFoodUseCase,
                getNutritionUseCase = getNutritionUseCase,
                updateNutritionUseCase = updateNutritionUseCase,
                deleteNutritionUseCase = deleteNutritionUseCase,
                testDispatcher
            )
        }

        @Test
        fun invokeUseCaseForUpdatingSelectedItem() = runTest {
            nutritionDataUpdateViewModel.selectItem(1)

            nutritionDataUpdateViewModel.update()

            Assertions.assertEquals(
                nutritionModels.last(),
                nutritionDataUpdateViewModel.uiState.value.nutritionModel
            )
            verify(updateNutritionUseCase).invoke(nutritions.last())
        }

        @Test
        fun emitFailureStateOnFailingUpdate() = runTest {
            whenever(updateNutritionUseCase.invoke(any())).thenReturn(false)

            nutritionDataUpdateViewModel.update()

            Assertions.assertEquals(
                "Update failed",
                nutritionDataUpdateViewModel.uiState.value.errorMessage
            )
        }

        @Test
        fun emitFailureStateOnThrowingUpdate() = runTest {
            val errorMessage = "Internal Error"
            whenever(updateNutritionUseCase.invoke(any())).thenThrow(RuntimeException(errorMessage))

            nutritionDataUpdateViewModel.update()

            Assertions.assertEquals(
                errorMessage,
                nutritionDataUpdateViewModel.uiState.value.errorMessage
            )
        }
    }

    @Nested
    inner class Delete {

        @BeforeEach
        fun beforeEach() = runTest {
            nutritionDataUpdateViewModel = NutritionUpdateViewModel(
                getFoodUseCase = getFoodUseCase,
                getNutritionUseCase = getNutritionUseCase,
                updateNutritionUseCase = updateNutritionUseCase,
                deleteNutritionUseCase = deleteNutritionUseCase,
                testDispatcher
            )
        }

        @Test
        fun invokeDeleteUseCase() = runTest {
            nutritionDataUpdateViewModel.delete(0)

            verify(deleteNutritionUseCase).invoke(any())
        }

        @Test
        fun deleteNutrition() = runTest {
            val expectedFoodList = listOf(food.last())

            clearInvocations(getFoodUseCase)
            whenever(getFoodUseCase.invoke()).thenReturn(
                flowOf(expectedFoodList)
            )

            nutritionDataUpdateViewModel.delete(0)

            verify(getFoodUseCase).invoke()
            Assertions.assertEquals(
                expectedFoodList,
                nutritionDataUpdateViewModel.uiState.value.foodList
            )
        }
    }
}
