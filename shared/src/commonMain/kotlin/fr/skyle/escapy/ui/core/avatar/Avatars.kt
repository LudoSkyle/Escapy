package fr.skyle.escapy.ui.core.avatar

import fr.skyle.escapy.resources.Res
import fr.skyle.escapy.resources.avatar_01
import fr.skyle.escapy.resources.avatar_02
import fr.skyle.escapy.resources.avatar_03
import fr.skyle.escapy.resources.avatar_04
import fr.skyle.escapy.resources.avatar_05
import fr.skyle.escapy.resources.avatar_06
import fr.skyle.escapy.resources.avatar_07
import fr.skyle.escapy.resources.avatar_08
import fr.skyle.escapy.resources.avatar_09
import fr.skyle.escapy.resources.avatar_10
import fr.skyle.escapy.resources.avatar_11
import fr.skyle.escapy.resources.avatar_12
import fr.skyle.escapy.resources.avatar_13
import fr.skyle.escapy.resources.avatar_14
import fr.skyle.escapy.resources.avatar_15
import fr.skyle.escapy.resources.avatar_16
import fr.skyle.escapy.resources.avatar_17
import fr.skyle.escapy.resources.avatar_18
import fr.skyle.escapy.resources.avatar_19
import fr.skyle.escapy.resources.avatar_20
import fr.skyle.escapy.resources.avatar_21
import fr.skyle.escapy.resources.avatar_22
import fr.skyle.escapy.resources.avatar_23
import fr.skyle.escapy.resources.avatar_24
import fr.skyle.escapy.resources.avatar_25
import fr.skyle.escapy.resources.avatar_26
import fr.skyle.escapy.resources.avatar_27
import fr.skyle.escapy.resources.avatar_28
import fr.skyle.escapy.resources.avatar_29
import fr.skyle.escapy.resources.avatar_30
import fr.skyle.escapy.resources.avatar_31
import fr.skyle.escapy.resources.avatar_32
import fr.skyle.escapy.resources.avatar_33
import fr.skyle.escapy.resources.avatar_34
import fr.skyle.escapy.resources.avatar_35
import fr.skyle.escapy.resources.avatar_36
import fr.skyle.escapy.resources.avatar_37
import fr.skyle.escapy.resources.avatar_38
import fr.skyle.escapy.resources.avatar_39
import fr.skyle.escapy.resources.avatar_40
import fr.skyle.escapy.resources.avatar_41
import fr.skyle.escapy.resources.avatar_42
import fr.skyle.escapy.resources.avatar_43
import fr.skyle.escapy.resources.avatar_44
import fr.skyle.escapy.resources.avatar_45
import fr.skyle.escapy.resources.avatar_46
import fr.skyle.escapy.resources.avatar_47
import fr.skyle.escapy.resources.avatar_48
import fr.skyle.escapy.resources.avatar_49
import fr.skyle.escapy.resources.avatar_50
import org.jetbrains.compose.resources.DrawableResource

/**
 * The avatars of the app, the avatar n being at the index n - 1. Their number is the AVATAR_COUNT
 * of the Cloud Functions, which only accept these.
 */
val AVATARS: List<DrawableResource> = listOf(
    Res.drawable.avatar_01,
    Res.drawable.avatar_02,
    Res.drawable.avatar_03,
    Res.drawable.avatar_04,
    Res.drawable.avatar_05,
    Res.drawable.avatar_06,
    Res.drawable.avatar_07,
    Res.drawable.avatar_08,
    Res.drawable.avatar_09,
    Res.drawable.avatar_10,
    Res.drawable.avatar_11,
    Res.drawable.avatar_12,
    Res.drawable.avatar_13,
    Res.drawable.avatar_14,
    Res.drawable.avatar_15,
    Res.drawable.avatar_16,
    Res.drawable.avatar_17,
    Res.drawable.avatar_18,
    Res.drawable.avatar_19,
    Res.drawable.avatar_20,
    Res.drawable.avatar_21,
    Res.drawable.avatar_22,
    Res.drawable.avatar_23,
    Res.drawable.avatar_24,
    Res.drawable.avatar_25,
    Res.drawable.avatar_26,
    Res.drawable.avatar_27,
    Res.drawable.avatar_28,
    Res.drawable.avatar_29,
    Res.drawable.avatar_30,
    Res.drawable.avatar_31,
    Res.drawable.avatar_32,
    Res.drawable.avatar_33,
    Res.drawable.avatar_34,
    Res.drawable.avatar_35,
    Res.drawable.avatar_36,
    Res.drawable.avatar_37,
    Res.drawable.avatar_38,
    Res.drawable.avatar_39,
    Res.drawable.avatar_40,
    Res.drawable.avatar_41,
    Res.drawable.avatar_42,
    Res.drawable.avatar_43,
    Res.drawable.avatar_44,
    Res.drawable.avatar_45,
    Res.drawable.avatar_46,
    Res.drawable.avatar_47,
    Res.drawable.avatar_48,
    Res.drawable.avatar_49,
    Res.drawable.avatar_50,
)

/** The image of the avatar [avatarId], the first one for an id the app does not know. */
fun avatarResource(avatarId: Int): DrawableResource =
    AVATARS.getOrElse(avatarId - 1) { AVATARS.first() }
