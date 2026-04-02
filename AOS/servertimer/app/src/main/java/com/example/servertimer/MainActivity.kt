package com.example.servertimer

import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.activity.viewModels
import com.example.servertimer.ui.theme.NeonAccentCyan
import com.example.servertimer.ui.theme.NeonAccentPink
import com.example.servertimer.ui.theme.NeonBackground
import com.example.servertimer.ui.theme.NeonDividerColor
import com.example.servertimer.ui.theme.NeonPanelBg
import com.example.servertimer.ui.theme.NeonTextPrimary
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen


class MainActivity : ComponentActivity() {
    private val timeViewModel: TimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        // 화면 꺼지지 않게 유지
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            NeonClockApp(timeViewModel = timeViewModel)
        }
    }

}



// 다크 네온 테마
@Composable
fun NeonClockTheme(content: @Composable () -> Unit) {
    val colorScheme = darkColorScheme(
        primary = NeonAccentCyan,
        secondary = NeonAccentPink,
        background = NeonBackground,
        surface = NeonPanelBg,
        onBackground = NeonTextPrimary,
        onSurface = NeonTextPrimary
    ).also {

        MaterialTheme(
            colorScheme = it,
            typography = Typography(),
            content = content
        )
    }
}

@Composable
fun NeonClockApp(
    timeViewModel: TimeViewModel
) {
    NeonClockTheme {
        val configuration = LocalConfiguration.current
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        // ViewModel의 상태 구독
        val utcTime by timeViewModel.utcTime.collectAsState()
        val kstTime by timeViewModel.kstTime.collectAsState()

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Transparent
        ) {
            val backgroundBrush = if (isLandscape) {
                Brush.horizontalGradient(
                    colors = listOf(
                        NeonAccentCyan.copy(alpha = 0.7f),
                        NeonBackground,
                        NeonAccentPink.copy(alpha = 0.7f)
                    )
                )
            } else {
                Brush.verticalGradient(
                    colors = listOf(
                        NeonAccentCyan.copy(alpha = 0.7f),
                        NeonBackground,
                        NeonAccentPink.copy(alpha = 0.7f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(backgroundBrush)
                    .padding(if (isLandscape) 24.dp else 16.dp)
            ) {
                if (isLandscape) {
                    Row(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        TimePanel(
                            title = "UTC",
                            dateTime = utcTime,
                            accentColor = NeonAccentCyan,
                            isLandscape = isLandscape,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(3.dp)
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(
                                            NeonAccentPink.copy(alpha = 0.1f),
                                            NeonAccentCyan.copy(alpha = 0.8f),
                                            NeonAccentPink.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                        )

                        TimePanel(
                            title = "KST (Asia/Seoul)",
                            dateTime = kstTime,
                            accentColor = NeonAccentPink,
                            isLandscape = true,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        )
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        TimePanel(
                            title = "UTC",
                            dateTime = utcTime,
                            accentColor = NeonAccentCyan,
                            isLandscape = isLandscape,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(3.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        colors = listOf(
                                            NeonAccentCyan.copy(alpha = 0.1f),
                                            NeonAccentPink.copy(alpha = 0.8f),
                                            NeonAccentCyan.copy(alpha = 0.1f)
                                        )
                                    )
                                )
                        )

                        TimePanel(
                            title = "KST (Asia/Seoul)",
                            dateTime = kstTime,
                            accentColor = NeonAccentPink,
                            isLandscape = false,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TimePanel(
    title: String,
    dateTime: ZonedDateTime,
    accentColor: Color,
    isLandscape: Boolean,
    modifier: Modifier = Modifier
) {
    val dateFormatter = remember {
        DateTimeFormatter.ofPattern("yyyy-MM-dd (EEE)")
            .withLocale(Locale.KOREA)
    }
    val timeFormatter = remember {
        DateTimeFormatter.ofPattern("HH:mm:ss")
    }

    val timeFontSize = if (isLandscape) 40.sp else 30.sp

    Box(
        modifier = modifier
            .padding(horizontal = 12.dp)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .padding(32.dp)
                .blur(40.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.7f),
                            Color.Transparent
                        ),
                        center = Offset.Zero,
                        radius = 800f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(8.dp)
                .background(
                    color = NeonPanelBg.copy(alpha = 0.9f),
                    shape = RoundedCornerShape(32.dp)
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor.copy(alpha = 0.15f),
                            accentColor.copy(alpha = 0.7f),
                            accentColor.copy(alpha = 0.15f)
                        )
                    ),
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(horizontal = 32.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = TextStyle(
                    color = accentColor,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    shadow = Shadow(
                        color = accentColor.copy(alpha = 0.85f),
                        offset = Offset(0f, 0f),
                        blurRadius = 20f
                    ),
                    letterSpacing = 3.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(18.dp))

            HorizontalDivider(
                modifier = Modifier.width(120.dp),
                thickness = 2.dp,
                color = accentColor.copy(alpha = 0.5f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .background(
                        color = Color.White.copy(alpha = 0.03f),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .border(
                        width = 1.dp,
                        color = NeonDividerColor,
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 18.dp, vertical = 8.dp)
            ) {
                Text(
                    text = dateTime.format(dateFormatter),
                    style = TextStyle(
                        color = NeonTextPrimary.copy(alpha = 0.9f),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = dateTime.format(timeFormatter),
                style = TextStyle(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            accentColor,
                            NeonAccentCyan,
                            NeonAccentPink
                        )
                    ),
                    fontSize = timeFontSize,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Monospace,
                    shadow = Shadow(
                        color = accentColor.copy(alpha = 0.9f),
                        offset = Offset(0f, 0f),
                        blurRadius = 30f
                    ),
                    letterSpacing = 4.sp
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}




