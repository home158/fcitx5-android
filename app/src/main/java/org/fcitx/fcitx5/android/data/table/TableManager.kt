/*
 * SPDX-License-Identifier: LGPL-2.1-or-later
 * SPDX-FileCopyrightText: Copyright 2021-2023 Fcitx5 for Android Contributors
 */
package org.fcitx.fcitx5.android.data.table

import org.fcitx.fcitx5.android.R
import org.fcitx.fcitx5.android.data.table.dict.Dictionary
import org.fcitx.fcitx5.android.data.table.dict.LibIMEDictionary
import org.fcitx.fcitx5.android.utils.appContext
import org.fcitx.fcitx5.android.utils.errorRuntime
import org.fcitx.fcitx5.android.utils.extract
import org.fcitx.fcitx5.android.utils.withTempDir
import java.io.File
import java.io.InputStream
import java.util.zip.ZipInputStream

object TableManager {

    private val inputMethodDir = File(
        appContext.getExternalFilesDir(null)!!, "data/inputmethod"
    ).also { it.mkdirs() }

    private val tableDicDir = File(
        appContext.getExternalFilesDir(null)!!, "data/table"
    ).also { it.mkdirs() }

    fun inputMethods(): List<TableBasedInputMethod> =
        inputMethodDir.listFiles()?.mapNotNull { confFile ->
            runCatching {
                TableBasedInputMethod.new(confFile).apply {
                    runCatching {
                        table = LibIMEDictionary(File(tableDicDir, tableFileName))
                    }
                }
            }.getOrNull()
        } ?: emptyList()

    fun importFromZip(src: InputStream): Result<TableBasedInputMethod> =
        runCatching {
            ZipInputStream(src).use { zipStream ->
                withTempDir { tempDir ->
                    val extracted = zipStream.extract(tempDir)
                    val confFile = extracted.find { it.name.endsWith(".conf") }
                        ?: extracted.find { it.name.endsWith(".conf.in") }
                        ?: errorRuntime(R.string.exception_table_im)
                    val dictFile = extracted.find { it.name.endsWith(".dict") }
                        ?: extracted.find { it.name.endsWith(".txt") }
                        ?: errorRuntime(R.string.exception_table)
                    importFiles(confFile, dictFile)
                }
            }
        }

    fun importFromConfAndDict(
        confName: String,
        confStream: InputStream,
        dictName: String,
        dictStream: InputStream
    ): Result<TableBasedInputMethod> = runCatching {
        withTempDir { tempDir ->
            val confFile = File(tempDir, confName).also {
                it.outputStream().use { o -> confStream.use { i -> i.copyTo(o) } }
            }
            val dictFile = File(tempDir, dictName).also {
                it.outputStream().use { o -> dictStream.use { i -> i.copyTo(o) } }
            }
            importFiles(confFile, dictFile)
        }
    }

    private fun importFiles(confFile: File, dictFile: File): TableBasedInputMethod {
        val importedConfFile = File(inputMethodDir, confFile.name.removeSuffix(".in")).also {
            if (it.exists())
                errorRuntime(R.string.table_already_exists, it.name)
            confFile.copyTo(it)
        }
        val im = runCatching {
            TableBasedInputMethod.new(importedConfFile)
        }.getOrElse {
            importedConfFile.delete()
            throw it
        }
        val table = Dictionary.new(dictFile)!!
        im.tableFileName = TableBasedInputMethod.fixedTableFileName(table.name)
        runCatching {
            im.table = table.toLibIMEDictionary(File(tableDicDir, im.tableFileName))
        }.onFailure {
            im.file.delete()
            errorRuntime(R.string.invalid_table_dict, it.message)
        }
        im.save()
        reloadReverseLookupDict()
        return im
    }

    fun replaceTableDict(
        im: TableBasedInputMethod,
        dictName: String,
        dictStream: InputStream
    ): Result<LibIMEDictionary> = runCatching {
        withTempDir { tempDir ->
            val dictFile = File(tempDir, dictName).also {
                it.outputStream().use { o -> dictStream.use { i -> i.copyTo(o) } }
            }
            val dict = Dictionary.new(dictFile)!!
            runCatching {
                dict.toLibIMEDictionary(File(tempDir, im.tableFileName))
            }.onSuccess {
                it.file.copyTo(File(tableDicDir, im.tableFileName), overwrite = true)
                reloadReverseLookupDict()
            }.onFailure {
                dictFile.delete()
                errorRuntime(R.string.invalid_table_dict, it.message)
            }.getOrThrow()
        }
    }

    @JvmStatic
    external fun tableDictConv(src: String, dest: String, mode: Boolean)

    @JvmStatic
    external fun checkTableDictFormat(src: String, user: Boolean = false): Boolean

    @JvmStatic
    external fun loadReverseLookupDict(path: String): Boolean

    @JvmStatic
    external fun reverseLookup(word: String): String?

    @Volatile
    private var reverseLookupLoaded = false

    @Volatile
    var currentReverseLookupTableName: String = ""
        private set

    fun reloadReverseLookupDict() {
        reverseLookupLoaded = false
        currentReverseLookupTableName = ""
        ensureReverseLookupLoaded()
    }

    fun ensureReverseLookupLoaded() {
        if (reverseLookupLoaded) return
        val methods = runCatching { inputMethods() }.getOrNull()
        if (!methods.isNullOrEmpty()) {
            for (im in methods) {
                val dictFile = File(tableDicDir, im.tableFileName)
                if (dictFile.exists() && loadReverseLookupDict(dictFile.absolutePath)) {
                    reverseLookupLoaded = true
                    currentReverseLookupTableName = im.name
                    return
                }
            }
        }
        val dirs = listOf(
            tableDicDir,
            File(appContext.filesDir, "data/table"),
            File(appContext.filesDir, "usr/share/fcitx5/table")
        )
        for (dir in dirs) {
            if (dir.exists()) {
                val files = dir.listFiles { f -> f.name.endsWith(".main.dict") || f.name.endsWith(".dict") }
                if (!files.isNullOrEmpty()) {
                    for (file in files) {
                        if (loadReverseLookupDict(file.absolutePath)) {
                            reverseLookupLoaded = true
                            currentReverseLookupTableName = file.name.substringBefore('.')
                            return
                        }
                    }
                }
            }
        }
    }

    fun reverseLookupWord(rawText: String): String? {
        val word = rawText.substringBefore(' ').substringBefore('~').trim()
        if (word.isEmpty()) return null
        ensureReverseLookupLoaded()
        if (!reverseLookupLoaded) return null
        val direct = reverseLookup(word)
        if (!direct.isNullOrEmpty()) return direct
        if (word.codePointCount(0, word.length) > 1) {
            val sb = StringBuilder()
            var i = 0
            while (i < word.length) {
                val cp = word.codePointAt(i)
                val ch = String(Character.toChars(cp))
                val c = reverseLookup(ch)
                if (c != null) {
                    if (sb.isNotEmpty()) sb.append(" ")
                    sb.append(c)
                }
                i += Character.charCount(cp)
            }
            if (sb.isNotEmpty()) return sb.toString()
        }
        return null
    }

    const val MODE_BIN_TO_TXT = true
    const val MODE_TXT_TO_BIN = false
}