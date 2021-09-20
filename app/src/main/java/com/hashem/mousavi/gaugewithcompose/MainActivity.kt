package com.hashem.mousavi.gaugewithcompose

import android.os.Bundle
import android.view.animation.BounceInterpolator
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusModifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var p by remember {
                mutableStateOf("")
            }

            var pressure by remember {
                mutableStateOf(0)
            }

            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Gauge(pressure = pressure)

                Spacer(modifier = Modifier.height(10.dp))

                TextField(
                    value = p,
                    onValueChange = { p = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    placeholder = { Text(text = "Enter pressure") }
                )

                Spacer(modifier = Modifier.height(10.dp))

                Button(onClick = {
                    if (p.toInt() < 0 || p.toInt() > 100) {
                        Toast.makeText(applicationContext, "Invalid range", Toast.LENGTH_SHORT).show()
                    } else {
                        pressure = p.toInt()
                    }
                }) {
                    Text(text = "Apply")
                }

            }
        }
    }
}

@Composable
fun Gauge(
    gaugeWidth: Dp = 40.dp,
    size: Dp = 300.dp,
    pressure: Int = 0
) {

    val path = Path()

    val scope = rememberCoroutineScope()

    val degrees = 1.6f * pressure - 170f

    val animatedPercentage = remember { Animatable(degrees, Float.VectorConverter) }

    LaunchedEffect(key1 = degrees) {
        scope.launch(Dispatchers.Main) {
            animatedPercentage.animateTo(
                targetValue = degrees,
                animationSpec = tween(
                    durationMillis = 500,
                    easing = {
                        BounceInterpolator().getInterpolation(it)
                    }
                )
            )
        }
    }

    Box(contentAlignment = Alignment.Center) {
        
        Text(
            text = ((animatedPercentage.value + 170f) / 1.6f).roundToInt().toString(),
            modifier = Modifier.offset(y = (-50).dp),
            fontSize = 30.sp
        )

        Canvas(
            modifier = Modifier
                .padding(20.dp)
                .size(size)
        ) {
            //-170->0
            //-10->160
            //sw = d + 170
            drawBackgroundIndicatorsByLevel(
                path,
                size,
                gaugeWidth,
                animatedPercentage.value + 170f
            )

            for (i in 0..4) {
                drawBackgroundIndicators(path, size, i, gaugeWidth)
            }

            drawCircle(
                color = Color.Black,
                radius = 4.dp.toPx()
            )

            rotate(degrees = animatedPercentage.value) {
                drawLine(
                    color = Color.Black,
                    start = center,
                    end = Offset(size.toPx() - gaugeWidth.toPx() - 4.dp.toPx(), size.toPx() / 2),
                    strokeWidth = 2.dp.toPx()
                )
            }

        }

    }
}

private fun DrawScope.drawBackgroundIndicators(
    path: Path,
    size: Dp,
    i: Int,
    gaugeWidth: Dp
) {
    path.reset()
    path.arcTo(
        rect = Rect(
            topLeft = Offset.Zero,
            Offset(
                size.toPx(),
                size.toPx()
            )
        ),
        -170f + 28 * i + i * 5,
        28f,
        forceMoveTo = false
    )
    path.arcTo(
        rect = Rect(
            topLeft = Offset(gaugeWidth.toPx(), gaugeWidth.toPx()),
            Offset(
                size.toPx() - gaugeWidth.toPx(),
                size.toPx() - gaugeWidth.toPx()
            )
        ),
        -142f + 28 * i + i * 5,
        -28f,
        forceMoveTo = false
    )
    path.close()
    drawPath(
        path,
        color = Color.Red,
        style = Stroke(2.dp.toPx())
    )

}

private fun DrawScope.drawBackgroundIndicatorsByLevel(
    path: Path,
    size: Dp,
    gaugeWidth: Dp,
    value: Float
) {

    path.reset()

    path.arcTo(
        rect = Rect(
            topLeft = Offset.Zero,
            Offset(
                size.toPx(),
                size.toPx()
            )
        ),
        -170f,
        value,
        forceMoveTo = false
    )
    path.arcTo(
        rect = Rect(
            topLeft = Offset(gaugeWidth.toPx(), gaugeWidth.toPx()),
            Offset(
                size.toPx() - gaugeWidth.toPx(),
                size.toPx() - gaugeWidth.toPx()
            )
        ),
        -170 + value,
        -value,
        forceMoveTo = false
    )
    path.close()
    drawPath(
        path,
        brush = Brush.sweepGradient(Pair(0.4f, Color.Green), Pair(0.8f, Color.Yellow), Pair(0.99f, Color.Red)),
        style = Fill
    )


}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    var p by remember {
        mutableStateOf("")
    }

    var pressure by remember {
        mutableStateOf(0)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Gauge(pressure = pressure)

        Spacer(modifier = Modifier.height(10.dp))

        TextField(
            value = p,
            onValueChange = { p = it },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(text = "Enter pressure") }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            if (p.toInt() < 0 || p.toInt() > 100) {
                //Toast.makeText(applicationContext, "Invalid range", Toast.LENGTH_SHORT).show()
            } else {
                pressure = p.toInt()
            }
        }) {
            Text(text = "Apply")
        }

    }

}