package com.example

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.example.data.local.AlarmEntity
import com.example.ui.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun home_screen_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        HomeScreen(
          alarms = listOf(
            AlarmEntity(
              id = 1L,
              hour = 6,
              minute = 0,
              isEnabled = true,
              label = "Bangun Pagi Subuh",
              missionType = "PHOTO",
              photoTargetPlace = "TOILET",
              photoTargetLabel = "Toilet / Kamar Mandi",
              daysOfWeek = "1,2,3,4,5,6,7"
            )
          ),
          onAddAlarmClick = {},
          onEditAlarmClick = {},
          onToggleAlarm = { _, _ -> },
          onDeleteAlarm = {},
          onTestAlarmClick = {},
          onOpenPhotoSpots = {}
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/home_screen.png")
  }
}
