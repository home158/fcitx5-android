/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2024 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.input.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.view.View
import androidx.annotation.Keep
import androidx.core.view.allViews
import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.core.FcitxKeyMapping
import org.fcitx.fcitx5.android.core.InputMethodEntry
import org.fcitx.fcitx5.android.core.KeySym
import org.fcitx.fcitx5.android.data.prefs.AppPrefs
import org.fcitx.fcitx5.android.data.prefs.ManagedPreference
import org.fcitx.fcitx5.android.data.theme.Theme
import org.fcitx.fcitx5.android.input.popup.PopupAction
import splitties.views.imageResource

class ZhuyinKey(
    val bopomofo: String,
    val act: String,
    val sym: Int,
    percentWidth: Float = 0.0909f,
    variant: KeyDef.Appearance.Variant = KeyDef.Appearance.Variant.Normal,
    popup: Array<Popup>? = null
) : KeyDef(
    Appearance.Text(
        displayText = bopomofo,
        textSize = 24f,
        percentWidth = percentWidth,
        variant = variant
    ),
    setOf(
        Behavior.Press(KeyAction.SymAction(KeySym(sym)))
    ),
    popup ?: arrayOf(
        Popup.Preview(bopomofo)
    )
)

@SuppressLint("ViewConstructor")
class ZhuyinKeyboard(
    context: Context,
    theme: Theme
) : BaseKeyboard(context, theme, Layout) {

    companion object {
        const val Name = "Zhuyin"

        val Layout: List<List<KeyDef>> = listOf(
            // Row 1: 11 keys (ㄅ ㄉ ˇ ˋ ㄓ ˊ ˙ ㄚ ㄞ ㄢ ㄦ) -> 1 2 3 4 5 6 7 8 9 0 -
            listOf(
                ZhuyinKey("ㄅ", "1", FcitxKeyMapping.FcitxKey_1, 0.0909f),
                ZhuyinKey("ㄉ", "2", FcitxKeyMapping.FcitxKey_2, 0.0909f),
                ZhuyinKey("ˇ", "3", FcitxKeyMapping.FcitxKey_3, 0.0909f),
                ZhuyinKey("ˋ", "4", FcitxKeyMapping.FcitxKey_4, 0.0909f),
                ZhuyinKey("ㄓ", "5", FcitxKeyMapping.FcitxKey_5, 0.0909f),
                ZhuyinKey("ˊ", "6", FcitxKeyMapping.FcitxKey_6, 0.0909f),
                ZhuyinKey("˙", "7", FcitxKeyMapping.FcitxKey_7, 0.0909f),
                ZhuyinKey("ㄚ", "8", FcitxKeyMapping.FcitxKey_8, 0.0909f),
                ZhuyinKey("ㄞ", "9", FcitxKeyMapping.FcitxKey_9, 0.0909f),
                ZhuyinKey("ㄢ", "0", FcitxKeyMapping.FcitxKey_0, 0.0909f),
                ZhuyinKey("ㄦ", "-", FcitxKeyMapping.FcitxKey_minus, 0.091f)
            ),
            // Row 2: 10 keys (ㄆ ㄊ ㄍ ㄐ ㄔ ㄗ ㄧ ㄛ ㄟ ㄣ) -> q w e r t y u i o p
            listOf(
                ZhuyinKey("ㄆ", "q", FcitxKeyMapping.FcitxKey_q, 0.1f),
                ZhuyinKey("ㄊ", "w", FcitxKeyMapping.FcitxKey_w, 0.1f),
                ZhuyinKey("ㄍ", "e", FcitxKeyMapping.FcitxKey_e, 0.1f),
                ZhuyinKey("ㄐ", "r", FcitxKeyMapping.FcitxKey_r, 0.1f),
                ZhuyinKey("ㄔ", "t", FcitxKeyMapping.FcitxKey_t, 0.1f),
                ZhuyinKey("ㄗ", "y", FcitxKeyMapping.FcitxKey_y, 0.1f),
                ZhuyinKey("ㄧ", "u", FcitxKeyMapping.FcitxKey_u, 0.1f),
                ZhuyinKey("ㄛ", "i", FcitxKeyMapping.FcitxKey_i, 0.1f),
                ZhuyinKey("ㄟ", "o", FcitxKeyMapping.FcitxKey_o, 0.1f),
                ZhuyinKey("ㄣ", "p", FcitxKeyMapping.FcitxKey_p, 0.1f)
            ),
            // Row 3: 10 keys (ㄇ ㄋ ㄎ ㄑ ㄕ ㄘ ㄨ ㄜ ㄠ ㄤ) -> a s d f g h j k l ;
            listOf(
                ZhuyinKey("ㄇ", "a", FcitxKeyMapping.FcitxKey_a, 0.1f),
                ZhuyinKey("ㄋ", "s", FcitxKeyMapping.FcitxKey_s, 0.1f),
                ZhuyinKey("ㄎ", "d", FcitxKeyMapping.FcitxKey_d, 0.1f),
                ZhuyinKey("ㄑ", "f", FcitxKeyMapping.FcitxKey_f, 0.1f),
                ZhuyinKey("ㄕ", "g", FcitxKeyMapping.FcitxKey_g, 0.1f),
                ZhuyinKey("ㄘ", "h", FcitxKeyMapping.FcitxKey_h, 0.1f),
                ZhuyinKey("ㄨ", "j", FcitxKeyMapping.FcitxKey_j, 0.1f),
                ZhuyinKey("ㄜ", "k", FcitxKeyMapping.FcitxKey_k, 0.1f),
                ZhuyinKey("ㄠ", "l", FcitxKeyMapping.FcitxKey_l, 0.1f),
                ZhuyinKey("ㄤ", ";", FcitxKeyMapping.FcitxKey_semicolon, 0.1f)
            ),
            // Row 4: Switch English + 10 keys (ㄈ ㄌ ㄏ ㄒ ㄖ ㄙ ㄩ ㄝ ㄡ ㄥ) + Backspace
            // 0.11f + 10 * 0.078f + 0.11f = 1.0f
            listOf(
                LayoutSwitchKey("EN", TextKeyboard.Name, 0.11f, KeyDef.Appearance.Variant.Alternative),
                ZhuyinKey("ㄈ", "z", FcitxKeyMapping.FcitxKey_z, 0.078f),
                ZhuyinKey("ㄌ", "x", FcitxKeyMapping.FcitxKey_x, 0.078f),
                ZhuyinKey("ㄏ", "c", FcitxKeyMapping.FcitxKey_c, 0.078f),
                ZhuyinKey("ㄒ", "v", FcitxKeyMapping.FcitxKey_v, 0.078f),
                ZhuyinKey("ㄖ", "b", FcitxKeyMapping.FcitxKey_b, 0.078f),
                ZhuyinKey("ㄙ", "n", FcitxKeyMapping.FcitxKey_n, 0.078f),
                ZhuyinKey("ㄩ", "m", FcitxKeyMapping.FcitxKey_m, 0.078f),
                ZhuyinKey("ㄝ", ",", FcitxKeyMapping.FcitxKey_comma, 0.078f),
                ZhuyinKey("ㄡ", ".", FcitxKeyMapping.FcitxKey_period, 0.078f),
                ZhuyinKey("ㄥ", "/", FcitxKeyMapping.FcitxKey_slash, 0.078f),
                BackspaceKey(0.11f)
            ),
            // Row 5: ?123, Comma, Lang, Space (一聲), ., Return
            listOf(
                LayoutSwitchKey("?123", ""),
                CommaKey(0.1f, KeyDef.Appearance.Variant.Alternative),
                LanguageKey(),
                SpaceKey(),
                SymbolKey(".", 0.1f, KeyDef.Appearance.Variant.Alternative),
                ReturnKey()
            )
        )
    }

    val backspace: ImageKeyView by lazy { findViewById(R.id.button_backspace) }
    val quickphrase: ImageKeyView by lazy { findViewById(R.id.button_quickphrase) }
    val lang: ImageKeyView by lazy { findViewById(R.id.button_lang) }
    val space: TextKeyView by lazy { findViewById(R.id.button_space) }
    val `return`: ImageKeyView by lazy { findViewById(R.id.button_return) }

    private val showLangSwitchKey = AppPrefs.getInstance().keyboard.showLangSwitchKey

    @Keep
    private val showLangSwitchKeyListener = ManagedPreference.OnChangeListener<Boolean> { _, v ->
        updateLangSwitchKey(v)
    }

    init {
        updateLangSwitchKey(showLangSwitchKey.getValue())
        showLangSwitchKey.registerOnChangeListener(showLangSwitchKeyListener)
    }

    private val textKeys: List<TextKeyView> by lazy {
        allViews.filterIsInstance(TextKeyView::class.java).toList()
    }

    private var punctuationMapping: Map<String, String> = mapOf()
    private fun transformPunctuation(p: String) = punctuationMapping.getOrDefault(p, p)

    override fun onReturnDrawableUpdate(returnDrawable: Int) {
        `return`.img.imageResource = returnDrawable
    }

    override fun onPunctuationUpdate(mapping: Map<String, String>) {
        punctuationMapping = mapping
        updatePunctuationKeys()
    }

    override fun onInputMethodUpdate(ime: InputMethodEntry) {
        space.mainText.text = buildString {
            append(ime.displayName)
            ime.subMode.run { label.ifEmpty { name.ifEmpty { null } } }?.let { append(" ($it)") }
        }
    }

    override fun onPopupAction(action: PopupAction) {
        super.onPopupAction(action)
    }

    private fun updateLangSwitchKey(visible: Boolean) {
        lang.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun updatePunctuationKeys() {
        textKeys.forEach {
            if (it is AltTextKeyView) {
                it.def as KeyDef.Appearance.AltText
                it.altText.text = transformPunctuation(it.def.altText)
            } else {
                it.def as KeyDef.Appearance.Text
                it.mainText.text = it.def.displayText.let { str ->
                    if (str.length == 1 && str[0] == '.') transformPunctuation(str) else str
                }
            }
        }
    }
}
