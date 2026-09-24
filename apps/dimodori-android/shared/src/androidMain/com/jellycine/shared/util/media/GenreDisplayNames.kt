package com.jellycine.shared.util.media

import android.content.Context
import com.jellycine.shared.R
import java.text.Normalizer
import java.util.Locale

/**
 * Returns the localized display label for a server genre.
 *
 * The value returned by this class is presentation-only. Callers must continue
 * to use the server supplied id/name when building filters or requests.
 */
object GenreDisplayNames {
    private val canonicalNames = listOf(
        "action", "adventure", "animation", "comedy", "crime", "documentary",
        "drama", "family", "fantasy", "history", "horror", "music", "mystery",
        "romance", "science fiction", "thriller", "war", "western", "film noir",
        "musical", "sport"
    )

    private val aliasesByGenre = mapOf(
        "action" to setOf("Action", "Acción", "Aktion", "Azione", "Ação", "Akcja", "Акция", "Екшън", "Akční", "Märul", "Aktie", "Aksiyon", "Hành động", "动作", "アクション", "액션", "عمل"),
        "adventure" to setOf("Adventure", "Aventure", "Aventura", "Abenteuer", "Avventura", "Приключенски", "Dobrodružný", "Seiklus", "Eventyr", "Avontuur", "Avventura", "Przygodowy", "Приключения", "Macera", "Phiêu lưu", "冒险", "アドベンチャー", "모험", "مغامرة"),
        "animation" to setOf("Animation", "Animación", "Animação", "Animacao", "Animazione", "Animatie", "Animacja", "Анимация", "Animasyon", "Animatsioon", "Animeret", "Анімація", "Hoạt hình", "动画", "アニメーション", "애니메이션", "رسوم متحركة"),
        "comedy" to setOf("Comedy", "Comedia", "Komödie", "Comédie", "Commedia", "Comédia", "Komedia", "Комедия", "Komedie", "Komedija", "ตลก", "喜剧", "コメディ", "코미디", "كوميديا"),
        "crime" to setOf("Crime", "Crimen", "Krimi", "Criminal", "Kriminal", "Crimine", "Crime", "Kryminał", "Криминал", "Suç", "Tội phạm", "犯罪", "犯罪", "범죄", "جريمة"),
        "documentary" to setOf("Documentary", "Documental", "Dokumentation", "Documentaire", "Documentario", "Documentário", "Dokumentalny", "Документальный", "Belgesel", "Tài liệu", "纪录片", "ドキュメンタリー", "다큐멘터리", "وثائقي"),
        "drama" to setOf("Drama", "Dramma", "Dramat", "Драма", "Dramático", "Драматичний", "Chính kịch", "剧情", "ドラマ", "드라마", "دراما"),
        "family" to setOf("Family", "Familia", "Familie", "Famille", "Famiglia", "Família", "Familijny", "Семейный", "Aile", "Gia đình", "家庭", "ファミリー", "가족", "عائلي"),
        "fantasy" to setOf("Fantasy", "Fantasía", "Fantasie", "Fantastique", "Fantasia", "Fantastyka", "Фэнтези", "Fantastik", "Giả tưởng", "奇幻", "ファンタジー", "판타지", "فانتازيا"),
        "history" to setOf("History", "Historia", "Historie", "Histoire", "Storia", "História", "Historyczny", "История", "Tarih", "Lịch sử", "历史", "歴史", "역사", "تاريخي"),
        "horror" to setOf("Horror", "Terror", "Horreur", "Terrore", "Horrorfilm", "Užasi", "Ужасы", "Korku", "Kinh dị", "恐怖", "ホラー", "공포", "رعب"),
        "music" to setOf("Music", "Música", "Musik", "Musique", "Musica", "Muzyka", "Музыка", "Müzik", "Âm nhạc", "音乐", "音楽", "음악", "الموسيقى"),
        "mystery" to setOf("Mystery", "Misterio", "Mysterium", "Mystère", "Mistero", "Tajemnica", "Детектив", "Gizem", "Bí ẩn", "悬疑", "ミステリー", "미스터리", "غموض"),
        "romance" to setOf("Romance", "Romántico", "Romantik", "Romantique", "Romantico", "Романтика", "Romantyczny", "Romantik", "Lãng mạn", "爱情", "ロマンス", "로맨스", "رومانسي"),
        "science fiction" to setOf("Science Fiction", "Sci-Fi", "Ciencia ficción", "Science-fiction", "Sciencefiction", "Fantascienza", "Ficção científica", "Fikcja naukowa", "Фантастика", "Bilim kurgu", "Khoa học viễn tưởng", "科幻", "SF", "과학 소설", "خيال علمي"),
        "thriller" to setOf("Thriller", "Suspenso", "Spannend", "Thriller", "Thriller", "Триллер", "Gerilim", "Giật gân", "惊悚", "スリラー", "스릴러", "إثارة"),
        "war" to setOf("War", "Guerra", "Krieg", "Guerre", "Guerra", "Wojenny", "Военный", "Savaş", "Chiến tranh", "战争", "戦争", "전쟁", "حرب"),
        "western" to setOf("Western", "Wéstern", "Westerns", "Western", "Oeste", "Вестерн", "Vestern", "Cao bồi", "西部", "西部劇", "서부극", "غربي"),
        "film noir" to setOf("Film Noir", "Cine negro", "Film noir", "Film-Noir", "Film néo-noir", "Film noar", "Фильм-нуар", "フィルム・ノワール", "필름 누아르", "فيلم نوار"),
        "musical" to setOf("Musical", "Müzikal", "Musikal", "Musicale", "Muzikál", "Мюзикл", "뮤지컬", "เพลง", "音乐剧", "ミュージカル", "موسيقي"),
        "sport" to setOf("Sport", "Sports", "Deportes", "Sportovní", "Спорт", "Esporte", "Sportowy", "Спортивний", "Spor", "Thể thao", "体育", "スポーツ", "스포츠", "رياضة")
    )

    /** Null values are collisions: ambiguous aliases must not translate. */
    private val aliasToGenre: Map<String, String?> = buildMap {
        aliasesByGenre.forEach { (genre, aliases) ->
            aliases.forEach { alias ->
                val key = normalize(alias)
                if (!containsKey(key)) {
                    put(key, genre)
                } else if (get(key) != genre) {
                    put(key, null)
                }
            }
        }
    }
    private val localeLabels = mutableMapOf<String, List<String>>()
    private val localeLabelsLock = Any()

    private fun normalize(value: String): String =
        Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
            .replace("\\p{M}+".toRegex(), "")
            .lowercase(Locale.ROOT)
            .replace('-', ' ')
            .replace(Regex("\\s+"), " ")

    /**
     * [genreName] is intentionally returned unchanged when it is not a known
     * server genre (including null). This avoids translating title metadata.
     */
    fun displayName(context: Context, genreName: String?): String? {
        if (genreName == null) return null
        val key = normalize(genreName)
        val canonicalKey = aliasToGenre[key] ?: return genreName
        val canonicalIndex = canonicalNames.indexOf(canonicalKey)
        if (canonicalIndex >= 0) {
            val locale = context.resources.configuration.locales[0].toLanguageTag()
            val localizedLabels = synchronized(localeLabelsLock) {
                localeLabels.getOrPut(locale) {
                    context.getString(R.string.genre_labels).split('|')
                }
            }
            if (localizedLabels.size == canonicalNames.size) {
                return localizedLabels[canonicalIndex]
            }
        }
        return genreName
    }
}