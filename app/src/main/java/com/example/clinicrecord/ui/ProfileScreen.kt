package com.example.clinicrecord.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.clinicrecord.viewmodel.ClinicViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ClinicViewModel,
    onTrashClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    val patients by viewModel.allPatients.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val deletedRecords by viewModel.recentDeletedRecords.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.purgeExpiredDeletedRecords()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = { Text("我的") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                ProfileHeader()
            }

            item {
                CreationStatsCard(
                    patientCount = patients.size,
                    folderCount = folders.size,
                    deletedCount = deletedRecords.size
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "其他",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    SettingsCard(
                        deletedCount = deletedRecords.size,
                        onTrashClick = onTrashClick,
                        onSettingsClick = onSettingsClick
                    )
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader() {
    val context = LocalContext.current
    val profilePrefs = remember(context) {
        context.getSharedPreferences(PROFILE_PREFS_NAME, Context.MODE_PRIVATE)
    }
    var nickname by rememberSaveable {
        val savedNickname = profilePrefs.getString(PROFILE_KEY_NICKNAME, DEFAULT_NICKNAME) ?: DEFAULT_NICKNAME
        mutableStateOf(if (savedNickname == OLD_DEFAULT_NICKNAME) DEFAULT_NICKNAME else savedNickname)
    }
    var nicknameDraft by rememberSaveable { mutableStateOf(nickname) }
    var nicknamePendingConfirm by rememberSaveable { mutableStateOf<String?>(null) }
    var showNicknameEditor by rememberSaveable { mutableStateOf(false) }

    var avatarUriText by rememberSaveable {
        mutableStateOf(profilePrefs.getString(PROFILE_KEY_AVATAR_URI, null))
    }
    var avatarScale by rememberSaveable {
        mutableStateOf(profilePrefs.getFloat(PROFILE_KEY_AVATAR_SCALE, 1f))
    }
    var avatarOffsetX by rememberSaveable {
        mutableStateOf(profilePrefs.getFloat(PROFILE_KEY_AVATAR_OFFSET_X, 0f))
    }
    var avatarOffsetY by rememberSaveable {
        mutableStateOf(profilePrefs.getFloat(PROFILE_KEY_AVATAR_OFFSET_Y, 0f))
    }
    var showPhotoPermission by rememberSaveable { mutableStateOf(false) }
    var editingAvatarUriText by rememberSaveable { mutableStateOf<String?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        editingAvatarUriText = uri?.toString()
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Avatar(
            uriText = avatarUriText,
            scale = avatarScale,
            offsetX = avatarOffsetX,
            offsetY = avatarOffsetY,
            modifier = Modifier.clickable { showPhotoPermission = true }
        )
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = nickname,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    nicknameDraft = nickname
                    showNicknameEditor = true
                }
            )
        }
    }

    if (showNicknameEditor) {
        NicknameDialog(
            value = nicknameDraft,
            onValueChange = { nicknameDraft = it },
            onDismiss = { showNicknameEditor = false },
            onNext = {
                val trimmed = nicknameDraft.trim()
                if (trimmed.isNotEmpty()) {
                    showNicknameEditor = false
                    nicknamePendingConfirm = trimmed
                }
            }
        )
    }

    nicknamePendingConfirm?.let { pendingNickname ->
        AlertDialog(
            onDismissRequest = { nicknamePendingConfirm = null },
            title = { Text("确认修改昵称？") },
            text = { Text("将昵称修改为“$pendingNickname”吗？") },
            confirmButton = {
                Button(
                    onClick = {
                        nickname = pendingNickname
                        profilePrefs.edit()
                            .putString(PROFILE_KEY_NICKNAME, pendingNickname)
                            .apply()
                        nicknamePendingConfirm = null
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { nicknamePendingConfirm = null }) {
                    Text("取消")
                }
            }
        )
    }

    if (showPhotoPermission) {
        AlertDialog(
            onDismissRequest = { showPhotoPermission = false },
            title = { Text("访问手机相册") },
            text = { Text("是否允许访问手机相册选择头像？") },
            confirmButton = {
                Button(
                    onClick = {
                        showPhotoPermission = false
                        launcher.launch("image/*")
                    }
                ) {
                    Text("允许")
                }
            },
            dismissButton = {
                TextButton(onClick = { showPhotoPermission = false }) {
                    Text("不允许")
                }
            }
        )
    }

    editingAvatarUriText?.let { editingUri ->
        AvatarAdjustDialog(
            uriText = editingUri,
            onDismiss = { editingAvatarUriText = null },
            onConfirm = { scale, offsetX, offsetY ->
                val savedAvatarUriText = persistAvatarImage(context, editingUri) ?: editingUri
                avatarUriText = savedAvatarUriText
                avatarScale = scale
                avatarOffsetX = offsetX
                avatarOffsetY = offsetY
                profilePrefs.edit()
                    .putString(PROFILE_KEY_AVATAR_URI, savedAvatarUriText)
                    .putFloat(PROFILE_KEY_AVATAR_SCALE, scale)
                    .putFloat(PROFILE_KEY_AVATAR_OFFSET_X, offsetX)
                    .putFloat(PROFILE_KEY_AVATAR_OFFSET_Y, offsetY)
                    .apply()
                editingAvatarUriText = null
            }
        )
    }
}

@Composable
private fun Avatar(
    uriText: String?,
    scale: Float,
    offsetX: Float,
    offsetY: Float,
    modifier: Modifier = Modifier
) {
    val bitmap by rememberBitmap(uriText)

    Surface(
        modifier = modifier
            .size(72.dp)
            .clip(CircleShape),
        color = MaterialTheme.colorScheme.secondaryContainer,
        shape = CircleShape
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap!!.asImageBitmap(),
                contentDescription = "头像",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    )
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text("头像")
            }
        }
    }
}

@Composable
private fun rememberBitmap(uriText: String?): State<android.graphics.Bitmap?> {
    val context = LocalContext.current
    return produceState<android.graphics.Bitmap?>(initialValue = null, uriText) {
        value = uriText?.let { text ->
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(text))?.use { stream ->
                    BitmapFactory.decodeStream(stream)
                }
            }.getOrNull()
        }
    }
}

private const val PROFILE_PREFS_NAME = "profile_preferences"
private const val PROFILE_KEY_NICKNAME = "nickname"
private const val PROFILE_KEY_AVATAR_URI = "avatar_uri"
private const val PROFILE_KEY_AVATAR_SCALE = "avatar_scale"
private const val PROFILE_KEY_AVATAR_OFFSET_X = "avatar_offset_x"
private const val PROFILE_KEY_AVATAR_OFFSET_Y = "avatar_offset_y"
private const val DEFAULT_NICKNAME = "点击输入昵称"
private const val OLD_DEFAULT_NICKNAME = "上传输入昵称"

private fun persistAvatarImage(context: Context, uriText: String): String? {
    return runCatching {
        val avatarFile = File(context.filesDir, "profile_avatar")
        context.contentResolver.openInputStream(Uri.parse(uriText))?.use { input ->
            avatarFile.outputStream().use { output ->
                input.copyTo(output)
            }
        } ?: return null
        Uri.fromFile(avatarFile).toString()
    }.getOrNull()
}

@Composable
private fun NicknameDialog(
    value: String,
    onValueChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onNext: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "点击输入昵称",
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text("昵称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Button(onClick = onNext) {
                        Text("下一步")
                    }
                }
            }
        }
    }
}

@Composable
private fun AvatarAdjustDialog(
    uriText: String,
    onDismiss: () -> Unit,
    onConfirm: (Float, Float, Float) -> Unit
) {
    val bitmap by rememberBitmap(uriText)
    var scale by rememberSaveable(uriText) { mutableStateOf(1f) }
    var offsetX by rememberSaveable(uriText) { mutableStateOf(0f) }
    var offsetY by rememberSaveable(uriText) { mutableStateOf(0f) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "调整头像",
                    style = MaterialTheme.typography.titleLarge
                )
                Box(
                    modifier = Modifier
                        .size(220.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer)
                        .pointerInput(uriText) {
                            detectTransformGestures { _, pan, zoom, _ ->
                                scale = (scale * zoom).coerceIn(1f, 3f)
                                offsetX += pan.x
                                offsetY += pan.y
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap!!.asImageBitmap(),
                            contentDescription = "头像预览",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .graphicsLayer(
                                    scaleX = scale,
                                    scaleY = scale,
                                    translationX = offsetX,
                                    translationY = offsetY
                                )
                        )
                    } else {
                        Text("头像")
                    }
                }
                Slider(
                    value = scale,
                    onValueChange = { scale = it },
                    valueRange = 1f..3f,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Button(onClick = { onConfirm(scale, offsetX, offsetY) }) {
                        Text("确认修改")
                    }
                }
            }
        }
    }
}

@Composable
private fun CreationStatsCard(
    patientCount: Int,
    folderCount: Int,
    deletedCount: Int
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "我的创作",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            StatItem("患者", patientCount.toString())
            StatItem("分类", folderCount.toString())
            StatItem("最近删除", deletedCount.toString())
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsCard(
    deletedCount: Int,
    onTrashClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            ProfileRow(
                title = "最近删除",
                trailing = deletedCount.toString(),
                onClick = onTrashClick
            )
            HorizontalDivider()
            ProfileRow(
                title = "设置",
                trailing = ">",
                onClick = onSettingsClick
            )
        }
    }
}

@Composable
private fun ProfileRow(
    title: String,
    trailing: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = trailing,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
