package com.mixelte.melodorium

class Utils {

    private val _nameReplacement = mapOf(
        ' ' to "_",
        ',' to "",
        '.' to "",
        '?' to "",
        '\'' to "",
        '"' to "",
        '(' to "",
        ')' to "",
        '!' to "",
        'а' to "a",
        'б' to "b",
        'в' to "v",
        'г' to "g",
        'д' to "d",
        'е' to "e",
        'ё' to "yo",
        'ж' to "zh",
        'з' to "z",
        'и' to "i",
        'й' to "j",
        'к' to "k",
        'л' to "l",
        'м' to "m",
        'н' to "n",
        'о' to "o",
        'п' to "p",
        'р' to "r",
        'с' to "s",
        'т' to "t",
        'у' to "u",
        'ф' to "f",
        'х' to "h",
        'ц' to "c",
        'ч' to "ch",
        'ш' to "sh",
        'щ' to "sch",
        'ъ' to "j",
        'ы' to "i",
        'ь' to "j",
        'э' to "e",
        'ю' to "yu",
        'я' to "ya",
        'А' to "A",
        'Б' to "B",
        'В' to "V",
        'Г' to "G",
        'Д' to "D",
        'Е' to "E",
        'Ё' to "Yo",
        'Ж' to "Zh",
        'З' to "Z",
        'И' to "I",
        'Й' to "J",
        'К' to "K",
        'Л' to "L",
        'М' to "M",
        'Н' to "N",
        'О' to "O",
        'П' to "P",
        'Р' to "R",
        'С' to "S",
        'Т' to "T",
        'У' to "U",
        'Ф' to "F",
        'Х' to "H",
        'Ц' to "C",
        'Ч' to "Ch",
        'Ш' to "Sh",
        'Щ' to "Sch",
        'Ъ' to "J",
        'Ы' to "I",
        'Ь' to "J",
        'Э' to "E",
        'Ю' to "Yu",
        'Я' to "Ya",
    )
    private var _normalizedChars = "abcdefghijklmnopqrstuvwxyz0123456789"

//    fun CleanName(source: String): String
//    {
//        val result = new StringBuilder();
//        foreach (var letter in source)
//        result.Append(_nameReplacement.GetValueOrDefault(letter, letter.ToString()));
//        return result.ToString();
//    }
//
//    fun NormalizeName(source: String): String
//    {
//        source = source.ToLower();
//        var result = new StringBuilder();
//        foreach (var letter in source)
//        {
//            var ch = _nameReplacement.GetValueOrDefault(letter, "");
//            if (ch == "_") continue;
//            if (ch != "")
//                result.Append(ch);
//            else if (_normalizedChars.Contains(letter))
//                result.Append(letter);
//        }
//        return result.ToString();
//    }
}