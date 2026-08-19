package com.teleport.messenger.ui.screens.settings

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

/** Stroke icons matching teleport_settings_2026.html / settings-preview. */
object SettingsIcons {
    val User: ImageVector by lazy {
        ImageVector.Builder("User", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                // circle cx=12 cy=8 r=4
                moveTo(16f, 8f)
                arcToRelative(4f, 4f, 0f, true, true, -8f, 0f)
                arcToRelative(4f, 4f, 0f, true, true, 8f, 0f)
                // path M4 20c0-4 4-6 8-6s8 2 8 6
                moveTo(4f, 20f)
                curveTo(4f, 16f, 8f, 14f, 12f, 14f)
                curveTo(16f, 14f, 20f, 16f, 20f, 20f)
            }
        }.build()
    }

    val Shield: ImageVector by lazy {
        ImageVector.Builder("Shield", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                moveTo(12f, 3f)
                lineTo(19f, 6f)
                verticalLineTo(11f)
                curveTo(19f, 16f, 16f, 19f, 12f, 21f)
                curveTo(8f, 19f, 5f, 16f, 5f, 11f)
                verticalLineTo(6f)
                close()
            }
        }.build()
    }

    val Bell: ImageVector by lazy {
        ImageVector.Builder("Bell", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                moveTo(12f, 3f)
                curveTo(9.24f, 3f, 7f, 5.24f, 7f, 8f)
                verticalLineTo(11f)
                curveTo(7f, 12f, 6.5f, 13f, 5.5f, 14f)
                horizontalLineTo(18.5f)
                curveTo(17.5f, 13f, 17f, 12f, 17f, 11f)
                verticalLineTo(8f)
                curveTo(17f, 5.24f, 14.76f, 3f, 12f, 3f)
                close()
                moveTo(9.5f, 20f)
                arcToRelative(2.5f, 2.5f, 0f, false, false, 5f, 0f)
            }
        }.build()
    }

    val Message: ImageVector by lazy {
        ImageVector.Builder("Message", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                moveTo(21f, 11.5f)
                curveTo(21f, 13.8f, 20.1f, 16f, 18.5f, 17.5f)
                curveTo(16.7f, 19.3f, 14.2f, 20.5f, 11.4f, 20.5f)
                curveTo(9.7f, 20.5f, 8f, 20f, 6.5f, 19.2f)
                lineTo(3f, 21f)
                lineTo(4.9f, 15.3f)
                curveTo(4.3f, 13.9f, 4f, 12.3f, 4f, 10.7f)
                curveTo(4f, 6.7f, 7.8f, 3.5f, 12.5f, 3.5f)
                curveTo(17.2f, 3.5f, 21f, 6.7f, 21f, 10.7f)
                verticalLineTo(11.5f)
                close()
            }
        }.build()
    }

    val Database: ImageVector by lazy {
        ImageVector.Builder("Database", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                moveTo(4f, 5.5f)
                arcToRelative(8f, 3f, 0f, true, true, 16f, 0f)
                arcToRelative(8f, 3f, 0f, true, true, -16f, 0f)
                moveTo(4f, 5.5f)
                verticalLineTo(18.5f)
                curveTo(4f, 20.2f, 7.6f, 21.5f, 12f, 21.5f)
                curveTo(16.4f, 21.5f, 20f, 20.2f, 20f, 18.5f)
                verticalLineTo(5.5f)
                moveTo(4f, 12f)
                curveTo(4f, 13.7f, 7.6f, 15f, 12f, 15f)
                curveTo(16.4f, 15f, 20f, 13.7f, 20f, 12f)
            }
        }.build()
    }

    val Moon: ImageVector by lazy {
        ImageVector.Builder("Moon", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                moveTo(20f, 14.5f)
                arcToRelative(8.5f, 8.5f, 0f, false, true, -10.5f, -10.5f)
                arcToRelative(8.5f, 8.5f, 0f, true, false, 10.5f, 10.5f)
                close()
            }
        }.build()
    }

    val Image: ImageVector by lazy {
        ImageVector.Builder("Image", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                moveTo(6f, 4f)
                horizontalLineTo(18f)
                arcToRelative(3f, 3f, 0f, false, true, 3f, 3f)
                verticalLineTo(17f)
                arcToRelative(3f, 3f, 0f, false, true, -3f, 3f)
                horizontalLineTo(6f)
                arcToRelative(3f, 3f, 0f, false, true, -3f, -3f)
                verticalLineTo(7f)
                arcToRelative(3f, 3f, 0f, false, true, 3f, -3f)
                close()
                moveTo(8.5f, 9.5f)
                arcToRelative(1.5f, 1.5f, 0f, true, true, 0f, 0.01f)
                moveTo(21f, 16f)
                lineTo(16f, 11f)
                lineTo(12f, 15f)
                lineTo(9f, 12f)
                lineTo(4f, 17f)
            }
        }.build()
    }

    val Sliders: ImageVector by lazy {
        ImageVector.Builder("Sliders", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                fill = null,
            ) {
                moveTo(4f, 6f); lineTo(20f, 6f)
                moveTo(4f, 12f); lineTo(20f, 12f)
                moveTo(4f, 18f); lineTo(20f, 18f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(9f, 6f); arcToRelative(2f, 2f, 0f, true, true, 0.01f, 0f)
                moveTo(15f, 12f); arcToRelative(2f, 2f, 0f, true, true, 0.01f, 0f)
                moveTo(7f, 18f); arcToRelative(2f, 2f, 0f, true, true, 0.01f, 0f)
            }
        }.build()
    }

    val Help: ImageVector by lazy {
        ImageVector.Builder("Help", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                moveTo(12f, 3f)
                arcToRelative(9f, 9f, 0f, true, true, 0f, 18f)
                arcToRelative(9f, 9f, 0f, true, true, 0f, -18f)
                moveTo(9.5f, 9f)
                curveTo(9.5f, 7.6f, 10.6f, 6.5f, 12f, 6.5f)
                curveTo(13.4f, 6.5f, 14.5f, 7.6f, 14.5f, 9f)
                curveTo(14.5f, 10.2f, 13.7f, 11f, 12.7f, 11.3f)
                curveTo(11.9f, 11.7f, 11.4f, 12.3f, 11.4f, 13.2f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 17f); arcToRelative(0.6f, 0.6f, 0f, true, true, 0.01f, 0f)
            }
        }.build()
    }

    val Info: ImageVector by lazy {
        ImageVector.Builder("Info", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                fill = null,
            ) {
                moveTo(12f, 3f)
                arcToRelative(9f, 9f, 0f, true, true, 0f, 18f)
                arcToRelative(9f, 9f, 0f, true, true, 0f, -18f)
                moveTo(12f, 11f); lineTo(12f, 16f)
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(12f, 7.7f); arcToRelative(0.6f, 0.6f, 0f, true, true, 0.01f, 0f)
            }
        }.build()
    }

    val UserPlus: ImageVector by lazy {
        ImageVector.Builder("UserPlus", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                moveTo(9f, 8f)
                arcToRelative(4f, 4f, 0f, true, true, 0.01f, 0f)
                moveTo(2f, 21f)
                verticalLineTo(20f)
                curveTo(2f, 16.7f, 4.7f, 14f, 8f, 14f)
                horizontalLineTo(10f)
                curveTo(13.3f, 14f, 16f, 16.7f, 16f, 20f)
                verticalLineTo(21f)
                moveTo(19f, 8f); lineTo(19f, 14f)
                moveTo(16f, 11f); lineTo(22f, 11f)
            }
        }.build()
    }

    val Logout: ImageVector by lazy {
        ImageVector.Builder("Logout", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                moveTo(9f, 21f)
                horizontalLineTo(6f)
                arcToRelative(2f, 2f, 0f, false, true, -2f, -2f)
                verticalLineTo(5f)
                arcToRelative(2f, 2f, 0f, false, true, 2f, -2f)
                horizontalLineTo(9f)
                moveTo(16f, 17f)
                lineTo(21f, 12f)
                lineTo(16f, 7f)
                moveTo(21f, 12f)
                horizontalLineTo(9f)
            }
        }.build()
    }

    val Admin: ImageVector by lazy {
        ImageVector.Builder("Admin", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                moveTo(4.5f, 4.5f)
                horizontalLineTo(8.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 1.5f, 1.5f)
                verticalLineTo(9.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, 1.5f)
                horizontalLineTo(4.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, -1.5f)
                verticalLineTo(6f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 1.5f, -1.5f)
                close()
                moveTo(15.5f, 4.5f)
                horizontalLineTo(19.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 1.5f, 1.5f)
                verticalLineTo(9.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, 1.5f)
                horizontalLineTo(15.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, -1.5f)
                verticalLineTo(6f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 1.5f, -1.5f)
                close()
                moveTo(4.5f, 15.5f)
                horizontalLineTo(8.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 1.5f, 1.5f)
                verticalLineTo(20.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, 1.5f)
                horizontalLineTo(4.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, -1.5f)
                verticalLineTo(17f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 1.5f, -1.5f)
                close()
                moveTo(15.5f, 15.5f)
                horizontalLineTo(19.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 1.5f, 1.5f)
                verticalLineTo(20.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, 1.5f)
                horizontalLineTo(15.5f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, -1.5f, -1.5f)
                verticalLineTo(17f)
                arcToRelative(1.5f, 1.5f, 0f, false, true, 1.5f, -1.5f)
                close()
            }
        }.build()
    }

    val Chevron: ImageVector by lazy {
        ImageVector.Builder("Chevron", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 2f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                moveTo(9f, 6f)
                lineTo(15f, 12f)
                lineTo(9f, 18f)
            }
        }.build()
    }

    val Contacts: ImageVector by lazy {
        ImageVector.Builder("Contacts", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                moveTo(9f, 8f)
                arcToRelative(3.4f, 3.4f, 0f, true, true, 0.01f, 0f)
                moveTo(3.5f, 20f)
                curveTo(3.5f, 16.8f, 6f, 14.5f, 9f, 14.5f)
                curveTo(12f, 14.5f, 14.5f, 16.8f, 14.5f, 20f)
                moveTo(17f, 8.5f)
                arcToRelative(2.6f, 2.6f, 0f, true, true, 0.01f, 0f)
                moveTo(15.5f, 14.7f)
                curveTo(18.1f, 15.1f, 20f, 17.1f, 20f, 20f)
            }
        }.build()
    }

    val Chats: ImageVector by lazy {
        ImageVector.Builder("Chats", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                moveTo(20f, 12f)
                curveTo(20f, 16f, 16.2f, 19f, 11.5f, 19f)
                curveTo(10.5f, 19f, 9.5f, 18.87f, 8.6f, 18.62f)
                lineTo(4f, 20f)
                lineTo(5.15f, 16.6f)
                curveTo(4.42f, 15.4f, 4f, 13.75f, 4f, 12f)
                curveTo(4f, 8f, 7.8f, 5f, 12.5f, 5f)
                curveTo(17.2f, 5f, 20f, 8f, 20f, 12f)
                close()
            }
            path(fill = SolidColor(Color.White)) {
                moveTo(8.7f, 12f); arcToRelative(0.9f, 0.9f, 0f, true, true, 0.01f, 0f)
                moveTo(12f, 12f); arcToRelative(0.9f, 0.9f, 0f, true, true, 0.01f, 0f)
                moveTo(15.3f, 12f); arcToRelative(0.9f, 0.9f, 0f, true, true, 0.01f, 0f)
            }
        }.build()
    }

    val Gear: ImageVector by lazy {
        ImageVector.Builder("Gear", 24.dp, 24.dp, 24f, 24f).apply {
            path(
                stroke = SolidColor(Color.White),
                strokeLineWidth = 1.7f,
                strokeLineCap = StrokeCap.Round,
                strokeLineJoin = StrokeJoin.Round,
                fill = null,
            ) {
                // Center hole
                moveTo(12f, 15f)
                arcToRelative(3f, 3f, 0f, true, true, 0.01f, 0f)
                // Classic settings cog (Lucide/Feather)
                moveTo(19.4f, 15f)
                curveTo(19.48f, 15.32f, 19.53f, 15.64f, 19.53f, 15.97f)
                curveTo(19.53f, 16.42f, 19.39f, 16.85f, 19.13f, 17.21f)
                lineTo(19.19f, 17.27f)
                arcToRelative(2f, 2f, 0f, true, true, -2.83f, 2.83f)
                lineTo(16.3f, 20.04f)
                curveTo(15.94f, 20.3f, 15.51f, 20.44f, 15.06f, 20.44f)
                curveTo(14.72f, 20.44f, 14.39f, 20.35f, 14.09f, 20.22f)
                curveTo(13.62f, 20.02f, 13.09f, 20.17f, 12.8f, 20.57f)
                verticalLineTo(21f)
                arcToRelative(2f, 2f, 0f, true, true, -4f, 0f)
                verticalLineTo(20.57f)
                curveTo(8.51f, 20.17f, 7.98f, 20.02f, 7.51f, 20.22f)
                curveTo(7.21f, 20.35f, 6.88f, 20.44f, 6.54f, 20.44f)
                curveTo(6.09f, 20.44f, 5.66f, 20.3f, 5.3f, 20.04f)
                lineTo(5.24f, 20.1f)
                arcToRelative(2f, 2f, 0f, true, true, -2.83f, -2.83f)
                lineTo(2.47f, 17.21f)
                curveTo(2.21f, 16.85f, 2.07f, 16.42f, 2.07f, 15.97f)
                curveTo(2.07f, 15.63f, 2.16f, 15.3f, 2.29f, 15f)
                curveTo(2.49f, 14.53f, 2.34f, 14f, 1.94f, 13.71f)
                horizontalLineTo(1.5f)
                arcToRelative(2f, 2f, 0f, true, true, 0f, -4f)
                horizontalLineTo(1.94f)
                curveTo(2.34f, 9.42f, 2.49f, 8.89f, 2.29f, 8.42f)
                curveTo(2.16f, 8.12f, 2.07f, 7.79f, 2.07f, 7.45f)
                curveTo(2.07f, 7f, 2.21f, 6.57f, 2.47f, 6.21f)
                lineTo(2.41f, 6.15f)
                arcToRelative(2f, 2f, 0f, true, true, 2.83f, -2.83f)
                lineTo(5.3f, 3.38f)
                curveTo(5.66f, 3.12f, 6.09f, 2.98f, 6.54f, 2.98f)
                curveTo(6.88f, 2.98f, 7.21f, 3.07f, 7.51f, 3.2f)
                curveTo(7.98f, 3.4f, 8.51f, 3.25f, 8.8f, 2.85f)
                verticalLineTo(2.42f)
                arcToRelative(2f, 2f, 0f, true, true, 4f, 0f)
                verticalLineTo(2.85f)
                curveTo(13.09f, 3.25f, 13.62f, 3.4f, 14.09f, 3.2f)
                curveTo(14.39f, 3.07f, 14.72f, 2.98f, 15.06f, 2.98f)
                curveTo(15.51f, 2.98f, 15.94f, 3.12f, 16.3f, 3.38f)
                lineTo(16.36f, 3.32f)
                arcToRelative(2f, 2f, 0f, true, true, 2.83f, 2.83f)
                lineTo(19.13f, 6.21f)
                curveTo(19.39f, 6.57f, 19.53f, 7f, 19.53f, 7.45f)
                curveTo(19.53f, 7.79f, 19.44f, 8.12f, 19.31f, 8.42f)
                curveTo(19.11f, 8.89f, 19.26f, 9.42f, 19.66f, 9.71f)
                horizontalLineTo(20.1f)
                arcToRelative(2f, 2f, 0f, true, true, 0f, 4f)
                horizontalLineTo(19.66f)
                curveTo(19.26f, 14f, 19.11f, 14.53f, 19.31f, 15f)
                close()
            }
        }.build()
    }
}
