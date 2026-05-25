package com.mixelte.melodorium.domain.models

import android.net.Uri
import com.mixelte.melodorium.getFilename
import com.mixelte.melodorium.getFolderName
import java.io.File

class MusicFile(
    data: MusicFileData,
    val uri: Uri,
    var artworkFile: File? = null,
) {
    val name: String
    val author: String
    val nameNorm: String
    val authorNorm: String
    val rpath = data.RPath
    val folder = data.RPath.getFolderName()
    val folderWithSpaces = folder.replace('_', ' ')
    val ext: String
    val mood = data.Mood
    val like = data.Like
    val lang = data.Lang
    val emo = data.Emo
    val public = data.Public
    val publicEnum = MusicPublic.fromBool(data.Public)
    val tag = data.Tag
    val tags = data.Tag.split(";")

    init {
        val fname = rpath.getFilename()
        val exti = fname.lastIndexOf(".")
        val sname = if (exti >= 0) fname.substring(0, exti) else fname
        ext = if (exti >= 0) fname.substring(exti + 1) else ""
        if ("_-_" in sname) {
            val parts = sname.split("_-_")
            author = parts[0].replace("_", " ")
            name = parts[1].replace("_", " ")
        } else {
            author = ""
            name = sname.replace("_", " ")
        }
        nameNorm = name.lowercase()
        authorNorm = author.lowercase()
    }

    val tagsLabel: String
        get() {
            var tags = ""
            tags += " ["
            tags += mood.name.substring(0, 2)
            if (emo == MusicEmo.Happy) tags += "+"
            else if (emo == MusicEmo.Sad) tags += "-"
            tags += ";"
            tags += like.name.substring(0, 2) + ";"
            tags += lang.name.substring(0, 2)
            if (public) tags += ";P"
            if (tag != "")
                tags += "|$tag"
            tags += "]"
            return tags
        }
}