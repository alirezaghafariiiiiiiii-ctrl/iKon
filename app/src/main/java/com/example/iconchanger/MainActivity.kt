package com.example.iconchanger

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private var refresh by mutableIntStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { LauncherScreen(refresh) }
    }

    override fun onResume() {
        super.onResume()
        refresh++
    }
}

data class AppItem(
    val label: String,
    val pkg: String,
    val activity: String,
    val icon: ImageBitmap,
    val pixelRes: Int
)

private val PIXEL = mapOf(
    "com.discord" to "discord",
    "com.reddit.frontpage" to "reddit",
    "com.twitter.android" to "twitter",
    "com.twitter.android.lite" to "twitter",
    "com.snapchat.android" to "snapchat",
    "com.whatsapp" to "whatsapp",
    "com.whatsapp.w4b" to "whatsapp",
    "tv.twitch.android.app" to "twitch",
    "com.google.android.youtube" to "youtube",
    "com.facebook.katana" to "facebook",
    "com.facebook.lite" to "facebook",
    "com.instagram.android" to "instagram",
    "com.android.chrome" to "chrome",
    "com.google.android.googlequicksearchbox" to "google",
    "com.android.vending" to "playstore",
    "com.zhiliaoapp.musically" to "tiktok",
    "com.ss.android.ugc.trill" to "tiktok",
    "com.google.android.gm" to "gmail",
    "com.google.android.apps.docs" to "drive",
    "com.google.android.apps.docs.editors.docs" to "docs",
    "com.evernote" to "evernote",
    "com.microsoft.office.word" to "word",
    "com.microsoft.office.excel" to "excel",
    "com.microsoft.office.powerpoint" to "powerpoint",
    "com.microsoft.office.onenote" to "onenote",
    "com.microsoft.office.outlook" to "outlook",
    "org.mozilla.firefox" to "firefox",
    "com.microsoft.emmx" to "edge",
    "com.opera.browser" to "opera",
    "com.todoist" to "todoist",
    "com.valvesoftware.android.steam.community" to "steam",
    "com.spotify.music" to "spotify",
    "com.netflix.mediaclient" to "netflix",
    "com.pinterest" to "pinterest",
    "com.slack" to "slack",
    "com.soundcloud.android" to "soundcloud",
    "com.tinder" to "tinder",
    "com.airbnb.android" to "airbnb",
    "com.duolingo" to "duolingo",
    "com.android.camera" to "camera",
    "com.android.camera2" to "camera",
    "com.google.android.GoogleCamera" to "camera",
    "com.sec.android.app.camera" to "camera",
    "com.google.android.apps.nbu.files" to "files",
    "com.google.android.documentsui" to "files",
    "com.android.documentsui" to "files",
    "com.sec.android.app.myfiles" to "files"
)

private fun loadApps(ctx: Context): List<AppItem> {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0)
        .filter { it.activityInfo.packageName != ctx.packageName }
        .map { ri ->
            val pkg = ri.activityInfo.packageName
            val name = PIXEL[pkg]
            val resId = if (name != null) {
                ctx.resources.getIdentifier("pix_$name", "drawable", ctx.packageName)
            } else 0
            AppItem(
                label = ri.loadLabel(pm).toString(),
                pkg = pkg,
                activity = ri.activityInfo.name,
                icon = ri.loadIcon(pm).toBitmap(128, 128).asImageBitmap(),
                pixelRes = resId
            )
        }
        .sortedBy { it.label.lowercase() }
}

private fun openApp(ctx: Context, app: AppItem) {
    val i = Intent(Intent.ACTION_MAIN)
        .addCategory(Intent.CATEGORY_LAUNCHER)
        .setClassName(app.pkg, app.activity)
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
    try { ctx.startActivity(i) } catch (_: Exception) { }
}

private fun openInfo(ctx: Context, app: AppItem) {
    val i = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${app.pkg}"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try { ctx.startActivity(i) } catch (_: Exception) { }
}

private fun uninstall(ctx: Context, app: AppItem) {
    val i = Intent(Intent.ACTION_DELETE, Uri.parse("package:${app.pkg}"))
        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    try { ctx.startActivity(i) } catch (_: Exception) { }
}

@Composable
fun LauncherScreen(refresh: Int) {
    val ctx = LocalContext.current
    val prefs = remember { ctx.getSharedPreferences("launcher", Context.MODE_PRIVATE) }
    var pixel by remember { mutableStateOf(prefs.getBoolean("pixel", true)) }
    var query by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }

    LaunchedEffect(refresh) {
        apps = withContext(Dispatchers.Default) { loadApps(ctx) }
    }

    BackHandler(enabled = true) { query = "" }

    val shown = remember(apps, query) {
        val q = query.trim()
        if (q.isEmpty()) apps else apps.filter { it.label.contains(q, ignoreCase = true) }
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0x99000000))
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp)
    ) {
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0x44FFFFFF))
                    .padding(horizontal = 14.dp, vertical = 12.dp)
            ) {
                if (query.isEmpty()) {
                    Text("Search apps", color = Color(0xAAFFFFFF), fontSize = 15.sp)
                }
                BasicTextField(
                    value = query,
                    onValueChange = { query = it },
                    singleLine = true,
                    textStyle = TextStyle(color = Color.White, fontSize = 15.sp),
                    cursorBrush = SolidColor(Color.White),
                    modifier = Modifier.fillMaxWidth()
                )
            }
            Spacer(Modifier.size(8.dp))
            Box(
                Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (pixel) Color(0xFF3DDC84) else Color(0x44FFFFFF))
                    .clickable {
                        pixel = !pixel
                        prefs.edit().putBoolean("pixel", pixel).apply()
                    }
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Text(
                    "PIXEL",
                    color = if (pixel) Color.Black else Color.White,
                    fontSize = 13.sp
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(4),
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            items(shown, key = { it.pkg + "/" + it.activity }) { app ->
                AppCell(
                    app = app,
                    pixel = pixel,
                    onOpen = { openApp(ctx, app); query = "" },
                    onInfo = { openInfo(ctx, app) },
                    onUninstall = { uninstall(ctx, app) }
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCell(
    app: AppItem,
    pixel: Boolean,
    onOpen: () -> Unit,
    onInfo: () -> Unit,
    onUninstall: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    Box(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onOpen, onLongClick = { menu = true })
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (pixel && app.pixelRes != 0) {
                Image(
                    bitmap = ImageBitmap.imageResource(app.pixelRes),
                    contentDescription = app.label,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.size(56.dp)
                )
            } else {
                Image(
                    bitmap = app.icon,
                    contentDescription = app.label,
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                app.label,
                color = Color.White,
                fontSize = 11.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(
                text = { Text("App info") },
                onClick = { menu = false; onInfo() }
            )
            DropdownMenuItem(
                text = { Text("Uninstall") },
                onClick = { menu = false; onUninstall() }
            )
        }
    }
}
