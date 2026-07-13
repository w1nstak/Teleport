package com.teleport.messenger.ui.screens.premium



import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.foundation.lazy.items

import androidx.compose.foundation.shape.CircleShape

import androidx.compose.foundation.shape.RoundedCornerShape

import androidx.compose.material.icons.Icons

import androidx.compose.material.icons.filled.*

import androidx.compose.material3.*

import androidx.compose.runtime.*

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier

import androidx.compose.ui.draw.clip

import androidx.compose.ui.graphics.Brush

import androidx.compose.ui.graphics.Color

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import com.teleport.messenger.data.entity.ChatType
import com.teleport.messenger.data.entity.GiftEntity

import com.teleport.messenger.data.entity.MarketplaceListingEntity

import com.teleport.messenger.data.entity.StarTransactionEntity

import com.teleport.messenger.ui.components.TeleportAvatar

import com.teleport.messenger.ui.components.TeleportButton

import com.teleport.messenger.ui.components.TeleportTopBar


import com.teleport.messenger.ui.theme.TeleportAppTheme


import com.teleport.messenger.ui.screens.settings.SettingsGroupCard
import com.teleport.messenger.ui.screens.settings.SettingsSoftBackButton
import com.teleport.messenger.ui.screens.settings.iskryLabel
import com.teleport.messenger.ui.screens.settings.IskryPriceText

import com.teleport.messenger.ui.theme.PremiumGold

import com.teleport.messenger.viewmodel.TeleportViewModel

import java.text.SimpleDateFormat

import java.util.*



private enum class StarsTab { All, Credits, Debits }



@Composable

fun PremiumScreen(vm: TeleportViewModel, onBack: () -> Unit) {

    val user by vm.currentUser().collectAsState(initial = null)

    val features = listOf(

        "Повышенные лимиты загрузки",

        "Расшифровка голосовых сообщений",

        "Более высокая скорость загрузки файлов",

        "Дополнительные реакции и эмодзи",

        "Значок Premium в профиле",

        "Больше папок и закреплённых чатов",

    )



    Scaffold(containerColor = TeleportAppTheme.colors.screenBg) { padding ->

        Column(Modifier.fillMaxSize().padding(padding)) {

            Row(Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {

                SettingsSoftBackButton(onBack)

            }

            LazyColumn(

                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),

                verticalArrangement = Arrangement.spacedBy(16.dp),

            ) {

                item {

                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

                        Box(

                            Modifier

                                .size(80.dp)

                                .clip(CircleShape)

                                .background(Brush.linearGradient(listOf(Color(0xFFAF52DE), Color(0xFF5856D6)))),

                            contentAlignment = Alignment.Center,

                        ) {

                            Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(44.dp))

                        }

                        Spacer(Modifier.height(16.dp))

                        Text("Teleport Premium", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = TeleportAppTheme.colors.textPrimary)

                        Text(

                            "Эксклюзивные функции и приоритетная поддержка",

                            color = TeleportAppTheme.colors.textMuted,

                            textAlign = TextAlign.Center,

                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),

                        )

                    }

                }

                item {

                    SettingsGroupCard {

                        features.forEachIndexed { i, feature ->

                            Row(

                                Modifier

                                    .fillMaxWidth()

                                    .padding(horizontal = 16.dp, vertical = 14.dp),

                                verticalAlignment = Alignment.CenterVertically,

                            ) {

                                Icon(Icons.Default.CheckCircle, null, tint = TeleportAppTheme.colors.accentBlue, modifier = Modifier.size(22.dp))

                                Spacer(Modifier.width(12.dp))

                                Text(feature, fontSize = 15.sp)

                            }

                            if (i < features.lastIndex) {

                                HorizontalDivider(Modifier.padding(start = 50.dp), color = TeleportAppTheme.colors.divider)

                            }

                        }

                    }

                }

                item {

                    SettingsGroupCard {

                        Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {

                            if (user?.isPremium == true) {

                                Text("✓ Подписка активна", color = PremiumGold, fontWeight = FontWeight.SemiBold)

                            } else {

                                Button(

                                    onClick = { vm.buyPremium(1) },

                                    modifier = Modifier.fillMaxWidth().height(52.dp),

                                    shape = RoundedCornerShape(26.dp),

                                    colors = ButtonDefaults.buttonColors(containerColor = TeleportAppTheme.colors.accentBlue),

                                ) {

                                    Icon(Icons.Default.Add, null)

                                    Spacer(Modifier.width(8.dp))

                                    Text("Подписаться — 1 месяц", fontWeight = FontWeight.SemiBold)

                                }

                                Spacer(Modifier.height(10.dp))

                                TextButton(onClick = { vm.buyPremium(12) }) {

                                    Text("Годовая подписка", color = TeleportAppTheme.colors.accentBlue)

                                }

                            }

                        }

                    }

                }

            }

        }

    }

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IskryScreen(vm: TeleportViewModel, onBack: () -> Unit, onGift: () -> Unit) {
    val user by vm.currentUser().collectAsState(initial = null)
    val history by vm.starHistory(user?.id ?: "").collectAsState(initial = emptyList())
    val weekAgo = remember { System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000 }
    val weekDelta = remember(history) {
        history.filter { it.createdAt >= weekAgo && it.amount > 0 }.sumOf { it.amount }
    }

    fun buyPack(pack: IskryPack) {
        val total = pack.sparks + (pack.bonus ?: 0)
        vm.buyIskry(total.toLong())
    }

    Scaffold(containerColor = IskryV6Palette.Bg) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .background(IskryV6Palette.Bg),
        ) {
            IskryStoreTopBar(onBack)
            LazyColumn(Modifier.fillMaxSize()) {
                item {
                    IskryHeroCard(
                        balance = user?.starsBalance ?: 0L,
                        weekDelta = weekDelta,
                        onBuy = { defaultIskryPacks.firstOrNull { it.featured }?.let(::buyPack) },
                        onGift = onGift,
                    )
                }
                item {
                    IskryPackagesSection(
                        packs = defaultIskryPacks,
                        onSelect = ::buyPack,
                    )
                }
            }
        }
    }
}



@Composable

private fun StarsTabRow(selected: StarsTab, onSelect: (StarsTab) -> Unit) {

    val tabs = listOf(StarsTab.All to "Все операции", StarsTab.Credits to "Зачисления", StarsTab.Debits to "Списания")

    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {

        tabs.forEach { (id, label) ->

            Column(

                Modifier

                    .weight(1f)

                    .clickable { onSelect(id) }

                    .padding(vertical = 8.dp),

                horizontalAlignment = Alignment.CenterHorizontally,

            ) {

                Text(

                    label,

                    fontSize = 13.sp,

                    fontWeight = if (selected == id) FontWeight.SemiBold else FontWeight.Normal,

                    color = if (selected == id) TeleportAppTheme.colors.accentBlue else TeleportAppTheme.colors.textMuted,

                )

                Spacer(Modifier.height(6.dp))

                Box(

                    Modifier

                        .height(3.dp)

                        .fillMaxWidth(0.6f)

                        .clip(RoundedCornerShape(2.dp))

                        .background(if (selected == id) TeleportAppTheme.colors.accentBlue else Color.Transparent),

                )

            }

        }

    }

}



@Composable

private fun StarTransactionRow(tx: StarTransactionEntity) {

    val date = remember(tx.createdAt) {

        SimpleDateFormat("d MMM yyyy", Locale("ru")).format(Date(tx.createdAt))

    }

    val isDebit = tx.amount < 0

    Row(

        Modifier

            .fillMaxWidth()

            .padding(horizontal = 20.dp, vertical = 12.dp),

        verticalAlignment = Alignment.CenterVertically,

    ) {

        TeleportAvatar(tx.description.take(1), size = 44.dp)

        Spacer(Modifier.width(12.dp))

        Column(Modifier.weight(1f)) {

            Text(tx.description, fontWeight = FontWeight.Medium, fontSize = 15.sp)

            Text(date, fontSize = 13.sp, color = TeleportAppTheme.colors.textMuted)

        }

        Text(

            "${if (tx.amount > 0) "+" else ""}${iskryLabel(kotlin.math.abs(tx.amount))}",

            fontWeight = FontWeight.SemiBold,

            color = if (isDebit) TeleportAppTheme.colors.debitOrange else TeleportAppTheme.colors.creditGreen,

        )

    }

}



@Composable

fun GiftsScreen(vm: TeleportViewModel, onBack: () -> Unit, onCollection: () -> Unit, onMarketplace: () -> Unit) {

    val gifts by vm.gifts.collectAsState()
    val chats by vm.chats.collectAsState()
    val user by vm.currentUser().collectAsState(initial = null)
    var selectedGift by remember { mutableStateOf<GiftEntity?>(null) }
    var sendResult by remember { mutableStateOf<String?>(null) }



    Column(Modifier.fillMaxSize()) {

        TeleportTopBar("Подарки", onBack, actions = {

            TextButton(onClick = onCollection) { Text("Коллекция") }

            TextButton(onClick = onMarketplace) { Text("Маркет") }

        })

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            items(gifts) { gift ->

                GiftCard(gift, onSend = { selectedGift = gift })

            }

        }

    }

    selectedGift?.let { gift ->
        AlertDialog(
            onDismissRequest = { selectedGift = null },
            title = { Text("Отправить «${gift.name}»") },
            text = {
                Column {
                    Text("Выберите чат (${iskryLabel(gift.priceStars)})", color = TeleportAppTheme.colors.textMuted)
                    chats.filter { it.type == ChatType.PRIVATE }.take(10).forEach { chat ->
                        TextButton(onClick = {
                            val me = user ?: return@TextButton
                            vm.getContactForChat(chat.id, me.id) { contact ->
                                if (contact != null) {
                                    vm.sendGift(contact.id, gift.id, chat.id) { ok ->
                                        sendResult = if (ok) "Подарок отправлен!" else "Недостаточно искр"
                                        selectedGift = null
                                    }
                                }
                            }
                        }) { Text(chat.title) }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { selectedGift = null }) { Text("Отмена") } },
        )
    }

    sendResult?.let { msg ->
        AlertDialog(
            onDismissRequest = { sendResult = null },
            title = { Text(msg) },
            confirmButton = { TextButton(onClick = { sendResult = null }) { Text("OK") } },
        )
    }

}



@Composable

private fun GiftCard(gift: GiftEntity, onSend: () -> Unit = {}) {

    Card(onClick = onSend, modifier = Modifier.fillMaxWidth()) {

        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {

            Surface(Modifier.size(56.dp), color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {

                Box(contentAlignment = Alignment.Center) { Text("🎁") }

            }

            Column(Modifier.weight(1f)) {

                Text(gift.name, style = MaterialTheme.typography.titleMedium)

                Text(gift.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {

                    AssistChip(onClick = {}, label = { Text(gift.rarity) })

                    if (gift.isLimited) AssistChip(onClick = {}, label = { Text("Лимитированный") })

                    if (gift.isCollectible) AssistChip(onClick = {}, label = { Text("Коллекционный") })

                }

            }

            IskryPriceText(gift.priceStars)

        }

    }

}



@Composable

fun GiftCollectionScreen(vm: TeleportViewModel, onBack: () -> Unit) {

    val user by vm.currentUser().collectAsState(initial = null)

    val collection by vm.giftCollection(user?.id ?: "").collectAsState(initial = emptyList())



    Column(Modifier.fillMaxSize()) {

        TeleportTopBar("Коллекция подарков", onBack)

        if (collection.isEmpty()) {

            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

                Text("Пока нет подарков", color = MaterialTheme.colorScheme.onSurfaceVariant)

            }

        } else {

            LazyColumn(contentPadding = PaddingValues(16.dp)) {

                items(collection) { gift -> GiftCard(gift) }

            }

        }

    }

}



@Composable

fun MarketplaceScreen(vm: TeleportViewModel, onBack: () -> Unit) {

    val listings by vm.listings.collectAsState()
    val gifts by vm.gifts.collectAsState()

    var query by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("all") }

    val filtered = remember(listings, gifts, query, category) {
        listings.filter { listing ->
            val gift = gifts.find { it.id == listing.giftId }
            val matchesQuery = query.isBlank() ||
                gift?.name?.contains(query, ignoreCase = true) == true ||
                listing.giftId.contains(query, ignoreCase = true)
            val matchesCat = category == "all" || gift?.rarity == category
            matchesQuery && matchesCat
        }
    }

    Column(Modifier.fillMaxSize()) {

        TeleportTopBar("Маркетплейс", onBack)

        com.teleport.messenger.ui.components.TeleportTextField(query, { query = it }, "Поиск", Modifier.padding(horizontal = 16.dp))

        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {

            listOf("all", "rare", "epic", "legendary").forEach { cat ->

                FilterChip(selected = category == cat, onClick = { category = cat }, label = { Text(cat.replaceFirstChar { it.uppercase() }) })

            }

        }

        LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {

            items(filtered) { listing ->

                MarketplaceListingCard(listing, onBuy = { vm.buyListing(listing.id) {} })

            }

        }

    }

}



@Composable

private fun MarketplaceListingCard(listing: MarketplaceListingEntity, onBuy: () -> Unit) {

    Card(Modifier.fillMaxWidth()) {

        Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {

            Column {

                Text("Подарок #${listing.giftId.takeLast(4)}", style = MaterialTheme.typography.titleMedium)

                IskryPriceText(listing.priceStars)

            }

            Button(onClick = onBuy) { Text("Купить") }

        }

    }

}



@Composable

fun CallScreen(vm: TeleportViewModel, chatId: String, type: String, onEnd: () -> Unit) {

    val chat by vm.chat(chatId).collectAsState(initial = null)

    val context = androidx.compose.ui.platform.LocalContext.current

    val app = context.applicationContext as com.teleport.messenger.TeleportApplication

    val connected by app.webRtc.connected.collectAsState()

    var muted by remember { mutableStateOf(false) }

    var videoOn by remember { mutableStateOf(true) }



    DisposableEffect(chatId, type) {

        val intent = android.content.Intent(context, com.teleport.messenger.service.CallService::class.java)

            .putExtra("type", type)

            .putExtra("chatId", chatId)

        context.startForegroundService(intent)

        onDispose {

            context.stopService(intent)

            app.webRtc.endCall()

        }

    }



    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {

        Column(horizontalAlignment = Alignment.CenterHorizontally) {

            TeleportAvatar(chat?.title ?: "?", modifier = Modifier.size(120.dp), size = 120.dp)

            Spacer(Modifier.height(24.dp))

            Text(chat?.title ?: "", style = MaterialTheme.typography.headlineMedium)

            Text(

                when {

                    connected -> "Соединено"

                    else -> if (type == "video") "Видеозвонок…" else "Звонок…"

                },

                color = MaterialTheme.colorScheme.onSurfaceVariant,

            )

            Spacer(Modifier.height(48.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {

                FilledIconButton(

                    onClick = { muted = app.webRtc.toggleMute() },

                    modifier = Modifier.size(56.dp),

                ) { Icon(if (muted) Icons.Default.MicOff else Icons.Default.Mic, null) }

                if (type == "video") {

                    FilledIconButton(

                        onClick = { videoOn = app.webRtc.toggleVideo() },

                        modifier = Modifier.size(56.dp),

                    ) { Icon(if (videoOn) Icons.Default.Videocam else Icons.Default.VideocamOff, null) }

                }

                FilledIconButton(

                    onClick = onEnd,

                    modifier = Modifier.size(56.dp),

                    colors = IconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.error),

                ) { Icon(Icons.Default.CallEnd, null) }

            }

        }

    }

}

