package com.example.iconchanger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Color as AColor
import android.os.BatteryManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat

private val emojiColors = mapOf(
    'A' to "#FDD235", 'B' to "#73D2F1", 'C' to "#593631", 'D' to "#F9EEE5",
    'E' to "#9968DF", 'F' to "#F8837D", 'H' to "#8C4219", 'I' to "#8E5C48",
    'J' to "#A692E9", 'K' to "#AA7577", 'L' to "#7ED0BD"
)

// ۵ شکلک ۱۶×۱۶: ۱۰۰٪ ، ۸۰٪ ، ۵۰٪ ، ۲۰٪ ، ۱٪
private val emojiArt = listOf(
    listOf(
        ".....HHHHHH.....",
        "...HHAAAAAAHH...",
        "..HAHHAAAAHHAH..",
        ".HAAAAAAAAAAAAH.",
        ".HAAAAAAAAAAAAH.",
        "HCCCCKCCCCCCKCCH",
        "HACCKCCAACCKCCAH",
        "HACKCCCAACKCCCAH",
        "HAACCCAAAACCCAAH",
        "HAAAAAAAAAAAAAAH",
        ".AAAAAAAAAHAAAI.",
        ".HAAAAHHHHAAAAH.",
        ".HAAAAAAAAAAAA..",
        "..HAAAAAAAAAAH..",
        "...HHAAAAAAHH...",
        ".....HHHHHH....."
    ),
    listOf(
        ".....HHHHHH.....",
        "...HHAAAAAAHH...",
        "..HAAAAAAAAAAH..",
        ".HAAHHAAAAHHAAH.",
        ".HAAAAAAAAAAAAH.",
        "HAAAHHAAAAHHAAAH",
        "HAAHAAHAAHAAHAAH",
        "HAAHAAHAAHAAHAAH",
        "HAAHAAHAAHAAHAAH",
        "HAAAAAAAAAAAAAAH",
        "HAAAAHHHHHHAAAAH",
        ".HAAAHFFFFHAAAH.",
        ".HAAAHFFFFHAAAH.",
        "..HAAAHHHHAAAH..",
        "...HHAAAAAAHH...",
        ".....HHHHHH....."
    ),
    listOf(
        ".....HHHHHH.....",
        "...HHAAAAAAHH...",
        "..HAAAAAAAAAAH..",
        ".HAAAAAAAAAAAAK.",
        ".HAAHHAAAAHHAAH.",
        "HAAAAAAAAAAAAAAH",
        "HAAAAAAAAAAAAAAH",
        "HAHAAAHAAHAAAHAH",
        "HAHAAAHAAHAAAHAH",
        "HFFHHHAAAAHHHFFH",
        "HFFFAAAAAAAAFFFH",
        ".HFFAAHAAHAAFFH.",
        ".HAAAAAHHAAAAA..",
        "..HAAAAAAAAAAH..",
        "...HHAAAAAAHH...",
        ".....HHHHHH....."
    ),
    listOf(
        ".....HHHHHH.....",
        "...HHAAAAAAHH...",
        "..HAAAAAAAAAAH..",
        ".HAAAHAAAAHAAAH.",
        ".HAAHAAAAAAHAAH.",
        "HAAAAAAAAAAAAAAH",
        "HAAAAAAAAAAAAAAH",
        "HAAHHHAAAAHHHAAH",
        "HAABAAAAAAAABAAH",
        "HABBAAAHHAAABLAH",
        "HABBAAHFFHAABLAH",
        ".HBAAAHFFHAAALH.",
        ".HAAAAHHHHAAAAI.",
        "..HAAAAAAAAAAH..",
        "...HHAAAAAAHH...",
        ".....HHHHHH....."
    ),
    listOf(
        ".....HHHHHH.....",
        "...HHJJJJEJHH...",
        "..HJJHJJJEHEJH..",
        ".HJJHJJJJEJIJKI.",
        ".HJJJJJJJEJEJEH.",
        "HJJHHHJJJEHHHEKH",
        "HJHDDDHJJHDDDIEH",
        "HJHDCDHJJHDCDHKH",
        "HAHDDDHAAHDDDHAH",
        "HAAHHHAAAAHHHAAH",
        "HAAAAAAHHAAAAAAH",
        ".HAAAAHFFHAAAAH.",
        ".HAAAAHHHHAAAA..",
        "..HAAAAAAAAAAH..",
        "...HHAAAAAAHI...",
        ".....HHHHHH....."
    )
)

private fun emojiBitmap(art: List<String>): Bitmap {
    val bmp = Bitmap.createBitmap(16, 16, Bitmap.Config.ARGB_8888)
    art.forEachIndexed { y, row ->
        row.forEachIndexed { x, ch ->
            emojiColors[ch]?.let { bmp.setPixel(x, y, AColor.parseColor(it)) }
        }
    }
    return bmp
}

private fun readBattery(i: Intent?): Pair<Int, Boolean> {
    if (i == null) return 0 to false
    val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
    val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
    val status = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
    val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
        status == BatteryManager.BATTERY_STATUS_FULL
    val pct = if (level >= 0 && scale > 0) level * 100 / scale else 0
    return pct to charging
}

@Composable
fun rememberBattery(): Pair<Int, Boolean> {
    val ctx = LocalContext.current
    var state by remember {
        mutableStateOf(readBattery(ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))))
    }
    DisposableEffect(ctx) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context, i: Intent) {
                state = readBattery(i)
            }
        }
        ContextCompat.registerReceiver(
            ctx, receiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        onDispose { ctx.unregisterReceiver(receiver) }
    }
    return state
}

@Composable
fun BatteryBar() {
    val (level, charging) = rememberBattery()
    val faceIndex = when {
        level >= 90 -> 0
        level >= 65 -> 1
        level >= 35 -> 2
        level >= 10 -> 3
        else -> 4
    }
    val face = remember(faceIndex) { emojiBitmap(emojiArt[faceIndex]) }
    val fill = when {
        level >= 90 -> Color(0xFF4FC3F7)
        level >= 65 -> Color(0xFF5BC236)
        level >= 35 -> Color(0xFFF5C536)
        level >= 10 -> Color(0xFFFF9D45)
        else -> Color(0xFFE5484D)
    }
    val frame = Color(0xFF7A1F1F)
    val filled = (level + 9) / 10
    val textStyle = TextStyle(
        color = Color.White,
        fontSize = 16.sp,
        fontWeight = FontWeight.Bold,
        shadow = Shadow(Color.Black, Offset(1f, 1f), 4f)
    )

    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(face.asImageBitmap(), null, Modifier.size(44.dp), filterQuality = FilterQuality.None)
        Spacer(Modifier.width(12.dp))
        Row(
            Modifier.border(3.dp, frame).background(Color(0xFFE6E0DC)).padding(3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(10) { i ->
                Box(
                    Modifier.width(13.dp).height(18.dp)
                        .background(if (i < filled) fill else Color.Transparent)
                )
                if (i < 9) Spacer(Modifier.width(2.dp))
            }
        }
        Box(Modifier.width(4.dp).height(10.dp).background(frame))
        Spacer(Modifier.width(10.dp))
        Text(if (charging) "$level% ⚡" else "$level%", style = textStyle)
    }
}
