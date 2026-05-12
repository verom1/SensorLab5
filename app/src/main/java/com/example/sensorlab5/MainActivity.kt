package com.example.sensorlab5

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    LevelScreen(context = this)
                }
            }
        }
    }
}

@Composable
fun LevelScreen(context: Context) {
    var sensorData by remember { mutableStateOf(Triple(0f, 0f, 0f)) }

    DisposableEffect(Unit) {
        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                    sensorData = Triple(event.values[0], event.values[1], event.values[2])
                }
            }
            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
        }

        sensorManager.registerListener(listener, accelerometer, SensorManager.SENSOR_DELAY_UI)

        onDispose {
            sensorManager.unregisterListener(listener)
        }
    }

    val (x, y, z) = sensorData

    val maxGravity = 9.81f
    val normalizedX = (x / maxGravity).coerceIn(-1f, 1f)
    val normalizedY = (y / maxGravity).coerceIn(-1f, 1f)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Будівельний рівень",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(250.dp)
                .background(Color(0xFFE0E0E0), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val radius = size.width / 2
                val center = Offset(size.width / 2, size.height / 2)

                drawCircle(
                    color = Color.DarkGray,
                    radius = radius,
                    style = Stroke(width = 4.dp.toPx())
                )
                drawCircle(
                    color = Color.Red,
                    radius = radius * 0.15f,
                    style = Stroke(width = 2.dp.toPx())
                )

                val bubbleRadius = radius * 0.12f
                val bubbleOffset = Offset(
                    x = center.x - (normalizedX * (radius - bubbleRadius)),
                    y = center.y + (normalizedY * (radius - bubbleRadius))
                )

                val isLevel = Math.abs(x) < 0.5f && Math.abs(y) < 0.5f
                drawCircle(
                    color = if (isLevel) Color(0xFF4CAF50) else Color(0xFF2196F3),
                    radius = bubbleRadius,
                    center = bubbleOffset
                )
            }
        }

        Spacer(modifier = Modifier.height(40.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Сирі дані акселерометра (м/с²)", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("X (вліво/вправо): ${"%.2f".format(x)}")
                Text("Y (вгору/вниз): ${"%.2f".format(y)}")
                Text("Z (обличчям вгору): ${"%.2f".format(z)}")

                Divider(modifier = Modifier.padding(vertical = 16.dp))

                Text("Обчислені кути нахилу:", fontWeight = FontWeight.Bold)
                val angleX = (normalizedX * 90).roundToInt()
                val angleY = (normalizedY * 90).roundToInt()
                Text("Нахил по осі X: $angleX°")
                Text("Нахил по осі Y: $angleY°")
            }
        }
    }
}