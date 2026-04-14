package com.aliernfrog.lactool

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.ui.graphics.Color
import io.github.aliernfrog.shared.data.Social
import io.github.aliernfrog.shared.impl.CreditData

const val TAG = "LACToolLogs"
const val githubRepoURL = "https://github.com/aliernfrog/lac-tool"
const val crowdinURL = "https://crowdin.com/project/lac-tool"

/**
 * Link to a releases JSON file.
 * Should return a JSON array of [io.github.aliernfrog.shared.data.ReleaseInfo].
 *
 * To automate generating such file, you can use the "/generate-releases-json.js"
 * and "/.github/workflows/generate-releases-json.yml" files, which can be found in the source code of this project.
 */
const val defaultReleasesURL = "https://raw.githubusercontent.com/aliernfrog/lac-tool/refs/heads/main/releases.json"

/**
 * Link to a crash report handler API endpoint.
 * Should listen for POST request with "app" and "details" strings in the JSON body.
 *
 * Example API source code: https://github.com/aliernfrog/proxy-api (forwards the report to a Discord webhook)
 */
const val crashReportURL = "https://aliernfrog.vercel.app/crash-report"

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

    val supportLinks = listOf(
        Social(
            label = R.string.settings_about_issues_discord,
            icon = io.github.aliernfrog.shared.R.drawable.discord,
            url = "https://discord.gg/SQXqBMs"
        ),
        Social(
            label = R.string.settings_about_issues_githubIssues,
            icon = io.github.aliernfrog.shared.R.drawable.github,
            url = "$githubRepoURL/issues"
        )
    )

    val credits = listOf(
        CreditData(
            userName = "Alizadev",
            displayNameOverride = "Mohammad Alizadeh",
            description = R.string.settings_about_credits_gameDev,
            fetchFromGithub = true,
            link = "https://discord.gg/aQhGqHSc3W"
        ),
        CreditData(
            userName = "aliernfrog",
            description = R.string.settings_about_credits_appDev,
            fetchFromGithub = true
        ),
        CreditData(
            userName = "candycanezz",
            description = R.string.settings_about_credits_ui,
            fetchFromGithub = true
        ),
        CreditData(
            userName = "crowdin",
            displayNameOverride = R.string.settings_about_credits_crowdin,
            description = R.string.settings_about_credits_translations,
            fetchFromGithub = true,
            link = crowdinURL
        ),
        CreditData(
            userName = "vendetta-mod",
            displayNameOverride = "Vendetta Manager",
            description = R.string.settings_about_credits_inspiration,
            fetchFromGithub = true,
            link = "https://github.com/vendetta-mod/VendettaManager"
        ),
        CreditData(
            userName = "ReVanced",
            displayNameOverride = "ReVanced Manager",
            description = R.string.settings_about_credits_inspiration,
            fetchFromGithub = true,
            link = "https://github.com/ReVanced/revanced-manager"
        )
    )
}