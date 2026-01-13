package com.aliernfrog.lactool

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.Color
import io.github.aliernfrog.shared.data.Social
import io.github.aliernfrog.shared.impl.CreditData

const val TAG = "LACToolLogs"
const val githubRepoURL = "https://github.com/aliernfrog/lac-tool"
const val crowdinURL = "https://crowdin.com/project/lac-tool"

object SettingsConstant {
    val socials = listOf(
        Social(
            label = "LAC",
            icon = io.github.aliernfrog.shared.R.drawable.discord,
            iconContainerColor = Color(0xFF5865F2),
            url = "https://discord.gg/aQhGqHSc3W"
        ),
        Social(
            label = "LAC Tool",
            icon = io.github.aliernfrog.shared.R.drawable.github,
            iconContainerColor = Color(0xFF104C35),
            url = githubRepoURL
        ),
        Social(
            label = "Crowdin",
            icon = Icons.Default.Translate,
            iconContainerColor = Color(0xFF263238),
            url = crowdinURL
        )
    )

    val credits = listOf(
        CreditData(
            name = "Mohammad Alizadeh",
            githubUsername = "Alizadev",
            description = R.string.settings_about_credits_gameDev,
            link = "https://discord.gg/aQhGqHSc3W"
        ),
        CreditData(
            name = "alieRN",
            githubUsername = "aliernfrog",
            description = R.string.settings_about_credits_appDev
        ),
        CreditData(
            name = "infini0083",
            githubUsername = "infini0083",
            description = R.string.settings_about_credits_ui
        ),
        CreditData(
            name = R.string.settings_about_credits_crowdin,
            githubUsername = "crowdin",
            description = R.string.settings_about_credits_translations,
            link = crowdinURL
        ),
        CreditData(
            name = "Vendetta Manager",
            githubUsername = "vendetta-mod",
            description = R.string.settings_about_credits_inspiration,
            link = "https://github.com/vendetta-mod/VendettaManager"
        ),
        CreditData(
            name = "ReVanced Manager",
            githubUsername = "ReVanced",
            description = R.string.settings_about_credits_inspiration,
            link = "https://github.com/ReVanced/revanced-manager"
        )
    )
}