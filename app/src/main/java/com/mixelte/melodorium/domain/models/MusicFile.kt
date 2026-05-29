package com.mixelte.melodorium.domain.models

import android.net.Uri
import com.mixelte.melodorium.getFilename
import com.mixelte.melodorium.getFolderName
import java.io.File

data class MusicFile(
    val uri: Uri,
    var artworkFile: File? = null,
    val rpath: String,
    val folder: String,
    val folderWithSpaces: String,
    val mood: MusicMood,
    val like: MusicLike,
    val lang: MusicLang,
    val emo: MusicEmo,
    val public: Boolean,
    val publicEnum: MusicPublic,
    val tag: String,
    val tags: List<String>,
    val name: String,
    val author: String,
    val nameNorm: String,
    val authorNorm: String,
    val ext: String
) {
    val tagsLabel: String
        get() = buildString {
            append(" [")
            append(mood.name.take(2))
            when (emo) {
                MusicEmo.Happy -> append("+")
                MusicEmo.Sad -> append("-")
                else -> {}
            }
            append(";")
            append(like.name.take(2))
            append(";")
            append(lang.name.take(2))
            if (public) append(";P")
            if (tag.isNotEmpty()) append("|$tag")
            append("]")
        }

    companion object {
        fun create(data: MusicFileData, uri: Uri, artworkFile: File? = null): MusicFile {
            val rpath = data.RPath
            val folder = rpath.getFolderName()

            val fname = rpath.getFilename()
            val exti = fname.lastIndexOf(".")
            val sname = if (exti >= 0) fname.substring(0, exti) else fname
            val ext = if (exti >= 0) fname.substring(exti + 1) else ""

            val author: String
            val name: String
            if ("_-_" in sname) {
                val parts = sname.split("_-_")
                author = parts[0].replace("_", " ")
                name = parts[1].replace("_", " ")
            } else {
                author = ""
                name = sname.replace("_", " ")
            }

            return MusicFile(
                uri = uri,
                artworkFile = artworkFile,
                rpath = rpath,
                folder = folder,
                folderWithSpaces = folder.replace('_', ' '),
                mood = data.Mood,
                like = data.Like,
                lang = data.Lang,
                emo = data.Emo,
                public = data.Public,
                publicEnum = MusicPublic.fromBool(data.Public),
                tag = data.Tag,
                tags = data.Tag.split(";"),
                name = name,
                author = author,
                nameNorm = name.lowercase(),
                authorNorm = author.lowercase(),
                ext = ext
            )
        }
    }
}