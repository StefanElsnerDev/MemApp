package com.example.hannapp.repository

import com.example.hannapp.data.model.MilkReferenceUiModel
import com.example.hannapp.data.model.NutrimentLogModel
import com.example.hannapp.data.model.NutritionLimitReferenceModel
import com.example.hannapp.data.model.NutritionModel
import com.example.hannapp.data.model.entity.Nutrition
import com.example.hannapp.data.repository.MilkReferenceRepository
import com.example.hannapp.data.repository.NutrimentLogRepository
import com.example.hannapp.data.repository.NutrimentLogValidationRepository
import com.example.hannapp.data.repository.NutritionLimitsRepository
import com.example.hannapp.data.repository.SubstitutionRepository
import com.example.hannapp.ui.mood.Mood
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class NutrimentLogValidationRepositoryShould {

    private lateinit var nutrimentLogValidationRepository: NutrimentLogValidationRepository
    private val nutritionLimitsRepository = mock<NutritionLimitsRepository>()
    private val nutrimentLogRepository = mock<NutrimentLogRepository>()
    private val milkReferenceRepository = mock<MilkReferenceRepository>()
    private val substitutionRepository = mock<SubstitutionRepository>()

    private val limit = 100.0

    private val nutritionLimit = NutritionLimitReferenceModel(
        kcal = limit,
        protein = limit,
        carbohydrates = limit,
        fat = limit
    )

    private fun generateNutrimentLevelLog(
        limit: NutritionLimitReferenceModel,
        level: Float,
        logSize: Int
    ) = (1..logSize).map { index ->
        NutrimentLogModel(
            id = index.toLong(),
            nutrition = Nutrition(
                uid = index.toLong(),
                name = "",
                kcal = (level * limit.kcal).div(logSize),
                protein = (level * limit.protein).div(logSize),
                fat = (level * limit.fat).div(logSize),
                carbohydrates = (level * limit.carbohydrates).div(logSize)
            ),
            quantity = 1.0,
            createdAt = 1234567,
            modifiedAt = null
        )
    }

    private val milkReferenceUiModel = MilkReferenceUiModel(
        maxQuantity = 1060f,
        dayTimeQuantity = 340f,
        preNightQuantity = 80f,
        nightQuantity = 720f
    )

    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeEach
    fun beforeEach() {
        Dispatchers.setMain(testDispatcher)

        whenever(nutritionLimitsRepository.getPreNightShare()).thenReturn(
            flowOf(nutritionLimit)
        )

        whenever(milkReferenceRepository.emitReference()).thenReturn(
            flowOf(milkReferenceUiModel)
        )

        nutrimentLogValidationRepository = NutrimentLogValidationRepository(
            nutritionLimitsRepository = nutritionLimitsRepository,
            nutrimentLogRepository = nutrimentLogRepository,
            milkReferenceRepository = milkReferenceRepository,
            substitutionRepository = substitutionRepository
        )
    }

    @Nested
    inner class ValidatePreNight {
        @Test
        fun emitGreenMoodOnEmptyNutriments() = runTest {
            whenever(nutrimentLogRepository.getLogs()).thenReturn(
                flowOf(
                    generateNutrimentLevelLog(
                        limit = nutritionLimit,
                        level = 0.0f,
                        logSize = 5
                    )
                )
            )

            val mood = nutrimentLogValidationRepository.validatePreNight().first()

            assertThat(mood).isEqualTo(Mood.GREEN)
        }

        @Test
        fun emitGreenMoodOnNutrimentLogBelow80PercentageOfDayLimit() = runTest {
            whenever(nutrimentLogRepository.getLogs()).thenReturn(
                flowOf(
                    generateNutrimentLevelLog(
                        limit = nutritionLimit,
                        level = 0.79f,
                        logSize = 5
                    )
                )
            )

            val mood = nutrimentLogValidationRepository.validatePreNight().first()

            assertThat(mood).isEqualTo(Mood.GREEN)
        }

        @Test
        fun emitYellowMoodOnNutrimentLogOn80PercentOfDayLimit() = runTest {
            whenever(nutrimentLogRepository.getLogs()).thenReturn(
                flowOf(
                    generateNutrimentLevelLog(
                        limit = nutritionLimit,
                        level = 0.8f,
                        logSize = 5
                    )
                )
            )

            val mood = nutrimentLogValidationRepository.validatePreNight().first()

            assertThat(mood).isEqualTo(Mood.YELLOW)
        }

        @Test
        fun emitYellowMoodOnNutrimentLogOn99PercentOfDayLimit() = runTest {
            whenever(nutrimentLogRepository.getLogs()).thenReturn(
                flowOf(
                    generateNutrimentLevelLog(
                        limit = nutritionLimit,
                        level = 0.99f,
                        logSize = 5
                    )
                )
            )

            val mood = nutrimentLogValidationRepository.validatePreNight().first()

            assertThat(mood).isEqualTo(Mood.YELLOW)
        }

        @Test
        fun emitRedMoodOnNutrimentLogOnExceedingDayLimit() = runTest {
            whenever(nutrimentLogRepository.getLogs()).thenReturn(
                flowOf(
                    generateNutrimentLevelLog(
                        limit = nutritionLimit,
                        level = 1f,
                        logSize = 10
                    )
                )
            )

            val mood = nutrimentLogValidationRepository.validatePreNight().first()

            assertThat(mood).isEqualTo(Mood.RED)
        }

        @Test
        fun emitRedMoodOnNutrimentLogOnExceedingDayLimitByQuantity() = runTest {
            val share = 10.0

            whenever(nutrimentLogRepository.getLogs()).thenReturn(
                flowOf(
                    listOf(
                        NutrimentLogModel(
                            id = 1,
                            nutrition = Nutrition(
                                uid = 1,
                                name = "",
                                kcal = limit / share,
                                protein = limit / share,
                                fat = limit / share,
                                carbohydrates = limit / share
                            ),
                            quantity = share,
                            createdAt = 1234567,
                            modifiedAt = null
                        )
                    )
                )
            )

            val mood = nutrimentLogValidationRepository.validatePreNight().first()

            assertThat(mood).isEqualTo(Mood.RED)
        }
    }

    @Nested
    inner class CalculatePreNightOverFlow {

        @Test
        fun returnNoVolumeOnEmptyLog() = runTest {
            whenever(nutrimentLogRepository.getLogs()).thenReturn(
                flowOf(
                    emptyList()
                )
            )

            val volume = nutrimentLogValidationRepository.calculatePreNightMilkDiscard().first()
            assertThat(volume).isEqualTo(0.0)
        }

        @Test
        fun returnCompleteVolumeOnExhaustedLog() = runTest {
            val quantity = 10.0

            whenever(nutrimentLogRepository.getLogs()).thenReturn(
                flowOf(
                    listOf(
                        NutrimentLogModel(
                            id = 123,
                            nutrition = Nutrition(
                                protein = nutritionLimit.protein.div(quantity)
                            ),
                            quantity = quantity,
                            createdAt = 123456789,
                            modifiedAt = null
                        )
                    )
                )
            )

            val volume = nutrimentLogValidationRepository.calculatePreNightMilkDiscard().first()

            assertThat(volume).isEqualTo(milkReferenceUiModel.preNightQuantity.toDouble())
        }
    }

    @Nested
    inner class EvaluateDiscardExceedingVolume {

        @ParameterizedTest
        @ValueSource(floats = [0.0f, 0.5f, 0.9f, 0.99f])
        fun emitNoExceeding(level: Float) = runTest {
            whenever(nutrimentLogRepository.getLogs()).thenReturn(
                flowOf(
                    generateNutrimentLevelLog(
                        limit = nutritionLimit,
                        level = level,
                        logSize = 5
                    )
                )
            )

            val isExceeding = nutrimentLogValidationRepository.isPreNightDiscardExceedingVolume().first()

            assertThat(isExceeding).isFalse
        }

        @ParameterizedTest
        @ValueSource(floats = [1.01f, 1.1f, 10.0f, 100.0f])
        fun emitExceeding(level: Float) = runTest {
            whenever(nutrimentLogRepository.getLogs()).thenReturn(
                flowOf(
                    generateNutrimentLevelLog(
                        limit = nutritionLimit,
                        level = level,
                        logSize = 5
                    )
                )
            )

            val isExceeding = nutrimentLogValidationRepository.isPreNightDiscardExceedingVolume().first()

            assertThat(isExceeding).isTrue
        }
    }

    @Nested
    inner class CalculateSubstitution {

        @BeforeEach
        fun beforeEach() = runTest {
            whenever(nutrimentLogRepository.getLogs()).thenReturn(
                flowOf(
                    emptyList()
                )
            )

            whenever(substitutionRepository.getMaltoDextrin()).thenReturn(
                flowOf(NutritionModel(kcal = 1.0))
            )
        }

        @Test fun emitNoMaltoSubstitutionOnLackOfMilkOverflow() = runTest {
            val substitution = nutrimentLogValidationRepository.calculatePreNightMaltodextrinSubstitution().first()
            assertThat(substitution).isEqualTo(0.0)
        }

        @Test fun emitMaltoSubstitutionOnMilkOverflow() = runTest {
            whenever(nutrimentLogRepository.getLogs()).thenReturn(
                flowOf(
                    generateNutrimentLevelLog(
                        limit = nutritionLimit,
                        level = 0.5f,
                        logSize = 1
                    )
                )
            )

            whenever(substitutionRepository.getMaltoDextrin()).thenReturn(
                flowOf(NutritionModel(kcal = 100.0))
            )

            val maltoQuantity = nutrimentLogValidationRepository.calculatePreNightMaltodextrinSubstitution().first()
            assertThat(maltoQuantity).isEqualTo(50.0)
        }

        @Test
        fun throwOnMissingMaltoNutriments() = runTest {
            whenever(substitutionRepository.getMaltoDextrin()).thenReturn(
                flowOf(NutritionModel(kcal = null))
            )

            val exception = assertThrows<IllegalArgumentException> {
                nutrimentLogValidationRepository.calculatePreNightMaltodextrinSubstitution().first()
            }

            assertThat(exception.message).matches("\\d+")
        }
    }
}
