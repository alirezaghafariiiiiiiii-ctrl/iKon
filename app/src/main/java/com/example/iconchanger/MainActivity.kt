package com.example.iconchanger

import android.app.Activity
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.toBitmap
import androidx.core.view.WindowCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import kotlin.math.min
import kotlin.math.roundToInt

// ---------------------------------------------------------------- Activity

class MainActivity : ComponentActivity() {

    private var refresh by mutableIntStateOf(0)
    private lateinit var host: AppWidgetHost

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        host = AppWidgetHost(this, 1024)
        setContent { LauncherScreen(refresh, host) }
    }

    override fun onStart() {
        super.onStart()
        host.startListening()
    }

    override fun onStop() {
        super.onStop()
        host.stopListening()
    }

    override fun onResume() {
        super.onResume()
        refresh++
    }
}

// ---------------------------------------------------------------- Data

data class AppItem(
    val label: String,
    val pkg: String,
    val activity: String,
    val icon: ImageBitmap,
    val pixelRes: Int
)

private val PIX_NAMES = listOf(
    "airbnb", "camera", "discord", "drive", "duolingo", "files", "finder", "gmail",
    "godot", "instagram", "netflix", "photoshop", "pinterest", "playstore", "reddit",
    "slack", "snapchat", "soundcloud", "spotify", "steam", "tinder", "trash", "twitter",
    "whatsapp", "twitch", "youtube", "facebook", "chrome", "google", "tiktok", "docs",
    "evernote", "word", "excel", "powerpoint", "onenote", "outlook", "firefox", "edge",
    "opera", "todoist", "android"
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

// ---------------------------------------------------------------- Settings store

class Store(ctx: Context) {
    private val sp = ctx.getSharedPreferences("launcher", Context.MODE_PRIVATE)
    private val dir = File(ctx.filesDir, "icons").apply { mkdirs() }
    private val awm = AppWidgetManager.getInstance(ctx)

    var pixel by mutableStateOf(sp.getBoolean("pixel", true))
        private set
    var cols by mutableIntStateOf(sp.getInt("cols", 4))
        private set
    var theme by mutableIntStateOf(sp.getInt("theme", 0)) // 0 system, 1 light, 2 dark
        private set

    val names = mutableStateMapOf<String, String>()
    val pixChoice = mutableStateMapOf<String, String>()
    val custom = mutableStateMapOf<String, ImageBitmap>()
    val widgets = mutableStateListOf<Int>()

    init {
        loadMap("names", names)
        loadMap("pixchoice", pixChoice)
        dir.listFiles()?.forEach { f ->
            if (f.extension == "png") {
                BitmapFactory.decodeFile(f.path)?.let { custom[f.nameWithoutExtension] = it.asImageBitmap() }
            }
        }
        sp.getString("widgets", "")!!.split(",").mapNotNull { it.toIntOrNull() }.forEach { id ->
            if (awm.getAppWidgetInfo(id) != null) widgets.add(id)
        }
        saveWidgets()
    }

    private fun loadMap(key: String, into: MutableMap<String, String>) {
        try {
            val o = JSONObject(sp.getString(key, "{}")!!)
            o.keys().forEach { into[it] = o.getString(it) }
        } catch (_: Exception) { }
    }

    private fun saveMap(key: String, m: Map<String, String>) {
        sp.edit().putString(key, JSONObject(m).toString()).apply()
    }

    private fun saveWidgets() {
        sp.edit().putString("widgets", widgets.joinToString(",")).apply()
    }

    fun updatePixel(v: Boolean) { pixel = v; sp.edit().putBoolean("pixel", v).apply() }
    fun updateCols(v: Int) { cols = v; sp.edit().putInt("cols", v).apply() }
    fun updateTheme(v: Int) { theme = v; sp.edit().putInt("theme", v).apply() }

    fun setName(pkg: String, name: String) {
        if (name.isBlank()) names.remove(pkg) else names[pkg] = name.trim()
        saveMap("names", names)
    }

    fun choosePix(pkg: String, pix: String) {
        deleteCustom(pkg)
        pixChoice[pkg] = pix
        saveMap("pixchoice", pixChoice)
    }

    fun setCustom(pkg: String, bmp: Bitmap) {
        pixChoice.remove(pkg)
        saveMap("pixchoice", pixChoice)
        FileOutputStream(File(dir, "$pkg.png")).use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        custom[pkg] = bmp.asImageBitmap()
    }

    private fun deleteCustom(pkg: String) {
        File(dir, "$pkg.png").delete()
        custom.remove(pkg)
    }

    fun resetIcon(pkg: String) {
        deleteCustom(pkg)
        pixChoice.remove(pkg)
        saveMap("pixchoice", pixChoice)
    }

    fun addWidget(id: Int) { widgets.add(id); saveWidgets() }
    fun removeWidget(id: Int) { widgets.remove(id); saveWidgets() }
}

// ---------------------------------------------------------------- Helpers

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
}

private fun decodeSquare(ctx: Context, uri: Uri, size: Int): Bitmap? {
    return try {
        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
        var sample = 1
        while (bounds.outWidth / (sample * 2) >= size && bounds.outHeight / (sample * 2) >= size) sample *= 2
        val opts = BitmapFactory.Options().apply { inSampleSize = sample }
        val bmp = ctx.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, opts)
        } ?: return null
        val side = min(bmp.width, bmp.height)
        val sq = Bitmap.createBitmap(bmp, (bmp.width - side) / 2, (bmp.height - side) / 2, side, side)
        Bitmap.createScaledBitmap(sq, size, size, true)
    } catch (_: Exception) {
        null
    }
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

// ---------------------------------------------------------------- Main screen

@Composable
fun LauncherScreen(refresh: Int, host: AppWidgetHost) {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val store = remember { Store(ctx) }
    val awm = remember { AppWidgetManager.getInstance(ctx) }

    var query by remember { mutableStateOf("") }
    var apps by remember { mutableStateOf<List<AppItem>>(emptyList()) }

    var showSettings by remember { mutableStateOf(false) }
    var showWidgetPicker by remember { mutableStateOf(false) }
    var renameTarget by remember { mutableStateOf<AppItem?>(null) }
    var renameText by remember { mutableStateOf("") }
    var iconTarget by remember { mutableStateOf<AppItem?>(null) }
    var showPixPack by remember { mutableStateOf(false) }
    var pendingId by remember { mutableIntStateOf(-1) }

    LaunchedEffect(refresh) {
        apps = withContext(Dispatchers.Default) { loadApps(ctx) }
    }

    BackHandler(enabled = true) { query = "" }

    // ---- gallery picker for custom icon
    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        val t = iconTarget
        if (uri != null && t != null) {
            scope.launch {
                val bmp = withContext(Dispatchers.IO) { decodeSquare(ctx, uri, 192) }
                if (bmp != null) store.setCustom(t.pkg, bmp)
            }
        }
        iconTarget = null
    }

    // ---- widget flow: bind -> (configure) -> add
    val configLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        if (r.resultCode == Activity.RESULT_OK) store.addWidget(pendingId)
        else host.deleteAppWidgetId(pendingId)
        pendingId = -1
    }
    val bindLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { r ->
        val id = pendingId
        if (r.resultCode == Activity.RESULT_OK) {
            val info = awm.getAppWidgetInfo(id)
            if (info?.configure != null) {
                configLauncher.launch(
                    Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                        .setComponent(info.configure)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                )
            } else {
                store.addWidget(id)
                pendingId = -1
            }
        } else {
            host.deleteAppWidgetId(id)
            pendingId = -1
        }
    }

    fun startAddWidget(p: AppWidgetProviderInfo) {
        val id = host.allocateAppWidgetId()
        pendingId = id
        if (awm.bindAppWidgetIdIfAllowed(id, p.provider)) {
            if (p.configure != null) {
                configLauncher.launch(
                    Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE)
                        .setComponent(p.configure)
                        .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                )
            } else {
                store.addWidget(id)
                pendingId = -1
            }
        } else {
            bindLauncher.launch(
                Intent(AppWidgetManager.ACTION_APPWIDGET_BIND)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, id)
                    .putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, p.provider)
            )
        }
    }

    // ---- theme
    val dark = when (store.theme) {
        1 -> false
        2 -> true
        else -> isSystemInDarkTheme()
    }
    val fg = if (dark) Color.White else Color(0xFF111111)
    val overlay = if (dark) Color(0x99000000) else Color(0x99FFFFFF)
    val field = if (dark) Color(0x44FFFFFF) else Color(0x33000000)

    val shown = run {
        val q = query.trim()
        apps.map { it to (store.names[it.pkg] ?: it.label) }
            .filter { q.isEmpty() || it.second.contains(q, ignoreCase = true) }
            .sortedBy { it.second.lowercase() }
    }

    MaterialTheme(colorScheme = if (dark) darkColorScheme() else lightColorScheme()) {
        Column(
            Modifier
                .fillMaxSize()
                .background(overlay)
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
                        .background(field)
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    if (query.isEmpty()) Text("Search apps", color = fg.copy(alpha = 0.6f), fontSize = 15.sp)
                    BasicTextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        textStyle = TextStyle(color = fg, fontSize = 15.sp),
                        cursorBrush = SolidColor(fg),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.size(8.dp))
                Box(
                    Modifier.clip(RoundedCornerShape(12.dp)).background(field)
                        .clickable { showWidgetPicker = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) { Text("+", color = fg, fontSize = 15.sp) }
                Spacer(Modifier.size(8.dp))
                Box(
                    Modifier.clip(RoundedCornerShape(12.dp)).background(field)
                        .clickable { showSettings = true }
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) { Text("⚙", color = fg, fontSize = 15.sp) }
            }
            Spacer(Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(store.cols),
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (query.isBlank()) {
                    items(
                        store.widgets.toList(),
                        key = { "w$it" },
                        span = { GridItemSpan(maxLineSpan) }
                    ) { id ->
                        WidgetCell(id, host) {
                            host.deleteAppWidgetId(id)
                            store.removeWidget(id)
                        }
                    }
                }
                items(shown, key = { it.first.pkg + "/" + it.first.activity }) { (app, name) ->
                    AppCell(
                        app = app,
                        name = name,
                        fg = fg,
                        pixel = store.pixel,
                        custom = store.custom[app.pkg],
                        choice = store.pixChoice[app.pkg],
                        onOpen = { openApp(ctx, app); query = "" },
                        onInfo = { openInfo(ctx, app) },
                        onUninstall = { uninstall(ctx, app) },
                        onRename = { renameTarget = app; renameText = name },
                        onChangeIcon = { iconTarget = app }
                    )
                }
            }
        }

        // ---------------- Settings dialog
        if (showSettings) {
            AlertDialog(
                onDismissRequest = { showSettings = false },
                confirmButton = { TextButton(onClick = { showSettings = false }) { Text("OK") } },
                title = { Text("Settings") },
                text = {
                    Column {
                        Text("Columns: ${store.cols}")
                        Slider(
                            value = store.cols.toFloat(),
                            onValueChange = { store.updateCols(it.roundToInt()) },
                            valueRange = 3f..6f,
                            steps = 2
                        )
                        Spacer(Modifier.height(8.dp))
                        Text("Theme")
                        listOf("System", "Light", "Dark").forEachIndexed { i, label ->
                            Row(
                                Modifier.fillMaxWidth().clickable { store.updateTheme(i) },
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = store.theme == i, onClick = { store.updateTheme(i) })
                                Text(label)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Pixel icons", modifier = Modifier.weight(1f))
                            Switch(checked = store.pixel, onCheckedChange = { store.updatePixel(it) })
                        }
                    }
                }
            )
        }

        // ---------------- Widget picker dialog
        if (showWidgetPicker) {
            val providers = remember {
                awm.installedProviders.sortedBy { it.loadLabel(ctx.packageManager).lowercase() }
            }
            AlertDialog(
                onDismissRequest = { showWidgetPicker = false },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { showWidgetPicker = false }) { Text("Cancel") } },
                title = { Text("Add widget") },
                text = {
                    LazyColumn(Modifier.height(380.dp)) {
                        providers.forEach { p ->
                            item {
                                Column(
                                    Modifier.fillMaxWidth()
                                        .clickable { showWidgetPicker = false; startAddWidget(p) }
                                        .padding(vertical = 10.dp)
                                ) {
                                    Text(p.loadLabel(ctx.packageManager))
                                    Text(p.provider.packageName, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                    }
                }
            )
        }

        // ---------------- Rename dialog
        renameTarget?.let { t ->
            AlertDialog(
                onDismissRequest = { renameTarget = null },
                title = { Text("Rename app") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = renameText,
                            onValueChange = { renameText = it },
                            singleLine = true
                        )
                        Text("Leave empty to restore original name", fontSize = 11.sp)
                    }
                },
                confirmButton = {
                    TextButton(onClick = { store.setName(t.pkg, renameText); renameTarget = null }) { Text("Save") }
                },
                dismissButton = { TextButton(onClick = { renameTarget = null }) { Text("Cancel") } }
            )
        }

        // ---------------- Change icon dialog
        iconTarget?.let { t ->
            AlertDialog(
                onDismissRequest = { iconTarget = null },
                title = { Text("Change icon") },
                text = {
                    Column {
                        TextButton(onClick = { gallery.launch("image/*") }) { Text("Pick from gallery") }
                        TextButton(onClick = { showPixPack = true }) { Text("Pixel pack") }
                        TextButton(onClick = { store.resetIcon(t.pkg); iconTarget = null }) { Text("Reset to default") }
                    }
                },
                confirmButton = {},
                dismissButton = { TextButton(onClick = { iconTarget = null }) { Text("Cancel") } }
            )
            if (showPixPack) {
                AlertDialog(
                    onDismissRequest = { showPixPack = false },
                    title = { Text("Pixel pack") },
                    confirmButton = {},
                    dismissButton = { TextButton(onClick = { showPixPack = false }) { Text("Back") } },
                    text = {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(4),
                            modifier = Modifier.height(320.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(PIX_NAMES) { n ->
                                val id = ctx.resources.getIdentifier("pix_$n", "drawable", ctx.packageName)
                                if (id != 0) {
                                    Image(
                                        bitmap = ImageBitmap.imageResource(id),
                                        contentDescription = n,
                                        filterQuality = FilterQuality.None,
                                        modifier = Modifier.size(52.dp).clickable {
                                            store.choosePix(t.pkg, n)
                                            showPixPack = false
                                            iconTarget = null
                                        }
                                    )
                                }
                            }
                        }
                    }
                )
            }
        }
    }
}

// ---------------------------------------------------------------- Cells

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AppCell(
    app: AppItem,
    name: String,
    fg: Color,
    pixel: Boolean,
    custom: ImageBitmap?,
    choice: String?,
    onOpen: () -> Unit,
    onInfo: () -> Unit,
    onUninstall: () -> Unit,
    onRename: () -> Unit,
    onChangeIcon: () -> Unit
) {
    val ctx = LocalContext.current
    var menu by remember { mutableStateOf(false) }
    val resId = when {
        choice != null -> ctx.resources.getIdentifier("pix_$choice", "drawable", ctx.packageName)
        pixel && app.pixelRes != 0 -> app.pixelRes
        else -> 0
    }

    Box(Modifier.fillMaxWidth()) {
        Column(
            Modifier
                .fillMaxWidth()
                .combinedClickable(onClick = onOpen, onLongClick = { menu = true })
                .padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                custom != null -> Image(
                    bitmap = custom,
                    contentDescription = name,
                    modifier = Modifier.size(56.dp)
                )
                resId != 0 -> Image(
                    bitmap = ImageBitmap.imageResource(resId),
                    contentDescription = name,
                    filterQuality = FilterQuality.None,
                    modifier = Modifier.size(56.dp)
                )
                else -> Image(
                    bitmap = app.icon,
                    contentDescription = name,
                    modifier = Modifier.size(56.dp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(name, color = fg, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
            DropdownMenuItem(text = { Text("Rename") }, onClick = { menu = false; onRename() })
            DropdownMenuItem(text = { Text("Change icon") }, onClick = { menu = false; onChangeIcon() })
            DropdownMenuItem(text = { Text("App info") }, onClick = { menu = false; onInfo() })
            DropdownMenuItem(text = { Text("Uninstall") }, onClick = { menu = false; onUninstall() })
        }
    }
}

@Composable
private fun WidgetCell(id: Int, host: AppWidgetHost, onRemove: () -> Unit) {
    val ctx = LocalContext.current
    val info = remember(id) { AppWidgetManager.getInstance(ctx).getAppWidgetInfo(id) } ?: return
    val h = with(LocalDensity.current) { info.minHeight.coerceAtLeast(120).toDp() }

    Box(Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
        AndroidView(
            factory = { c -> host.createView(c, id, info) },
            modifier = Modifier.fillMaxWidth().height(h)
        )
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(4.dp)
                .clip(RoundedCornerShape(50))
                .background(Color(0x88000000))
                .clickable { onRemove() }
                .padding(horizontal = 8.dp, vertical = 2.dp)
        ) { Text("✕", color = Color.White, fontSize = 12.sp) }
    }
}
