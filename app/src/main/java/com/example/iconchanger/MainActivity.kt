package com.example.iconchanger

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.graphics.drawable.toBitmap

data class AppItem(val label: String, val pkg: String, val icon: Bitmap)

data class PackIcon(val name: String, val res: Int, val pkgs: List<String>)

// بسته آیکون پیکسلی: هر آیکون با نام پکیج برنامه مربوطه تطبیق داده می‌شود
val pixelPack = listOf(
    PackIcon("Discord", R.drawable.pix_discord, listOf("com.discord")),
    PackIcon("Reddit", R.drawable.pix_reddit, listOf("com.reddit.frontpage")),
    PackIcon("Twitter", R.drawable.pix_twitter, listOf("com.twitter.android")),
    PackIcon("Snapchat", R.drawable.pix_snapchat, listOf("com.snapchat.android")),
    PackIcon("WhatsApp", R.drawable.pix_whatsapp, listOf("com.whatsapp", "com.whatsapp.w4b")),
    PackIcon("Twitch", R.drawable.pix_twitch, listOf("tv.twitch.android.app")),
    PackIcon("YouTube", R.drawable.pix_youtube, listOf("com.google.android.youtube")),
    PackIcon("Facebook", R.drawable.pix_facebook, listOf("com.facebook.katana")),
    PackIcon("Instagram", R.drawable.pix_instagram, listOf("com.instagram.android")),
    PackIcon("Chrome", R.drawable.pix_chrome, listOf("com.android.chrome")),
    PackIcon("Google", R.drawable.pix_google, listOf("com.google.android.googlequicksearchbox")),
    PackIcon("Play Store", R.drawable.pix_playstore, listOf("com.android.vending")),
    PackIcon("TikTok", R.drawable.pix_tiktok, listOf("com.zhiliaoapp.musically", "com.ss.android.ugc.trill")),
    PackIcon("Gmail", R.drawable.pix_gmail, listOf("com.google.android.gm")),
    PackIcon("Docs", R.drawable.pix_docs, listOf("com.google.android.apps.docs.editors.docs")),
    PackIcon("Evernote", R.drawable.pix_evernote, listOf("com.evernote")),
    PackIcon("Word", R.drawable.pix_word, listOf("com.microsoft.office.word")),
    PackIcon("Excel", R.drawable.pix_excel, listOf("com.microsoft.office.excel")),
    PackIcon("PowerPoint", R.drawable.pix_powerpoint, listOf("com.microsoft.office.powerpoint")),
    PackIcon("OneNote", R.drawable.pix_onenote, listOf("com.microsoft.office.onenote")),
    PackIcon("Outlook", R.drawable.pix_outlook, listOf("com.microsoft.office.outlook")),
    PackIcon("Firefox", R.drawable.pix_firefox, listOf("org.mozilla.firefox")),
    PackIcon("Edge", R.drawable.pix_edge, listOf("com.microsoft.emmx")),
    PackIcon("Opera", R.drawable.pix_opera, listOf("com.opera.browser")),
    PackIcon("Steam", R.drawable.pix_steam, listOf("com.valvesoftware.android.steam.community")),
    PackIcon("Todoist", R.drawable.pix_todoist, listOf("com.todoist")),
    PackIcon("Android", R.drawable.pix_android, emptyList()),
    PackIcon("Files", R.drawable.pix_files, emptyList()),
)

fun packFor(pkg: String): PackIcon? = pixelPack.firstOrNull { pkg in it.pkgs }

fun loadPack(ctx: Context, res: Int): Bitmap =
    BitmapFactory.decodeResource(ctx.resources, res, BitmapFactory.Options().apply { inScaled = false })

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(Modifier.fillMaxSize()) { IconChangerApp() }
            }
        }
    }
}

private fun loadApps(ctx: Context): List<AppItem> {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0)
        .map { AppItem(it.loadLabel(pm).toString(), it.activityInfo.packageName, it.loadIcon(pm).toBitmap(96, 96)) }
        .distinctBy { it.pkg }
        .sortedBy { it.label.lowercase() }
}

private fun decodeImage(ctx: Context, uri: Uri): Bitmap? =
    ctx.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
        ?.let { Bitmap.createScaledBitmap(it, 192, 192, true) }

private fun pinShortcut(ctx: Context, app: AppItem, label: String, icon: Bitmap) {
    if (!ShortcutManagerCompat.isRequestPinShortcutSupported(ctx)) {
        Toast.makeText(ctx, "لانچر شما از میانبر پشتیبانی نمی‌کند", Toast.LENGTH_LONG).show()
        return
    }
    val launch = ctx.packageManager.getLaunchIntentForPackage(app.pkg) ?: return
    val info = ShortcutInfoCompat.Builder(ctx, "${app.pkg}-${System.currentTimeMillis()}")
        .setShortLabel(label.ifBlank { app.label })
        .setIcon(IconCompat.createWithBitmap(icon))
        .setIntent(launch)
        .build()
    ShortcutManagerCompat.requestPinShortcut(ctx, info, null)
}

@Composable
fun IconChangerApp() {
    val ctx = LocalContext.current
    val apps = remember { loadApps(ctx) }
    var selected by remember { mutableStateOf<AppItem?>(null) }

    BackHandler(enabled = selected != null) { selected = null }

    val app = selected
    if (app == null) {
        Column(Modifier.statusBarsPadding()) {
            Text("یک برنامه انتخاب کن", style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
            LazyColumn {
                items(apps, key = { it.pkg }) { item ->
                    Row(
                        Modifier.fillMaxWidth().clickable { selected = item }.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val shown = remember(item.pkg) {
                            packFor(item.pkg)?.let { loadPack(ctx, it.res) } ?: item.icon
                        }
                        Image(shown.asImageBitmap(), null, Modifier.size(44.dp), filterQuality = FilterQuality.None)
                        Spacer(Modifier.width(16.dp))
                        Text(item.label)
                    }
                }
            }
        }
    } else {
        Editor(app) { selected = null }
    }
}

@Composable
fun Editor(app: AppItem, onBack: () -> Unit) {
    val ctx = LocalContext.current
    var name by remember { mutableStateOf(app.label) }
    var icon by remember { mutableStateOf(packFor(app.pkg)?.let { loadPack(ctx, it.res) } ?: app.icon) }
    val packBitmaps = remember { pixelPack.map { loadPack(ctx, it.res) } }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) decodeImage(ctx, uri)?.let { icon = it }
    }

    Column(
        Modifier.fillMaxSize().statusBarsPadding().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Image(icon.asImageBitmap(), null, Modifier.size(96.dp), filterQuality = FilterQuality.None)
        OutlinedTextField(name, { name = it }, label = { Text("نام میانبر") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        Text("بسته آیکون پیکسلی")
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(pixelPack.indices.toList()) { i ->
                Image(
                    packBitmaps[i].asImageBitmap(), pixelPack[i].name,
                    Modifier.size(56.dp).clickable { icon = packBitmaps[i] },
                    filterQuality = FilterQuality.None
                )
            }
        }
        OutlinedButton(
            onClick = { picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            modifier = Modifier.fillMaxWidth()
        ) { Text("انتخاب آیکون از گالری") }
        Button(onClick = { pinShortcut(ctx, app, name, icon) }, modifier = Modifier.fillMaxWidth()) {
            Text("ساخت میانبر روی صفحه اصلی")
        }
        TextButton(onClick = onBack) { Text("بازگشت") }
    }
}
