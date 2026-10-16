package com.example.iconchanger

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap

private fun loadLauncherApps(ctx: Context): List<AppItem> {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0)
        .map { AppItem(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.loadIcon(pm).toBitmap(144, 144)) }
        .distinctBy { it.pkg }
        .sortedBy { it.label.lowercase() }
}

class LauncherActivity : ComponentActivity() {
    private var tick by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { HomeScreen(tick) }
    }

    override fun onResume() {
        super.onResume()
        tick++
    }
}

@Composable
fun HomeScreen(tick: Int) {
    val ctx = LocalContext.current
    val apps = remember(tick) { loadLauncherApps(ctx) }
    val labelStyle = TextStyle(
        color = Color.White,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
        shadow = Shadow(Color.Black, Offset(1f, 1f), 4f)
    )

    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding(),
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(apps, key = { it.pkg }) { app ->
            val icon = remember(app.pkg) {
                packFor(app.pkg)?.let { loadPack(ctx, it.res) } ?: app.icon
            }
            Column(
                Modifier.fillMaxWidth().clickable {
                    ctx.packageManager.getLaunchIntentForPackage(app.pkg)?.let { ctx.startActivity(it) }
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(icon.asImageBitmap(), app.label, Modifier.size(56.dp), filterQuality = FilterQuality.None)
                Spacer(Modifier.height(4.dp))
                Text(app.label, style = labelStyle, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}
