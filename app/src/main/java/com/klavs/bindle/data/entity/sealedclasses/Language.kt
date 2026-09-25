package com.klavs.bindle.data.entity.sealedclasses

import androidx.compose.material.icons.Icons
import androidx.compose.ui.graphics.vector.ImageVector
import com.klavs.bindle.R

sealed class Language(val code: String, val nameResource: Int, val imageResource: Int, var selected:Boolean){
    data class Turkish(val isSelected: Boolean = false) : Language("tr", R.string.turkish, R.drawable.turkish_icon, selected = isSelected)
    data class English(val isSelected: Boolean = false) : Language("en", R.string.english, R.drawable.english_icon, selected = isSelected)
}
