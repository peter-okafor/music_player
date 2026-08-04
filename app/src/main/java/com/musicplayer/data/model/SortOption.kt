package com.musicplayer.data.model

/** Sort fields available on track lists. */
enum class TrackSort(val label: String) {
    TITLE("Title"),
    ARTIST("Artist"),
    ALBUM("Album"),
    DURATION("Duration"),
    DATE_ADDED("Date added"),
    TRACK_NUMBER("Track number")
}

/** Sort fields available on album / artist / folder / playlist grids. */
enum class CollectionSort(val label: String) {
    NAME("Name"),
    TRACK_COUNT("Number of tracks"),
    ARTIST("Artist")
}

data class SortState(
    val trackSort: TrackSort = TrackSort.TITLE,
    val descending: Boolean = false
) {
    fun serialize(): String = "${trackSort.name}:$descending"

    companion object {
        fun parse(value: String?): SortState {
            if (value.isNullOrBlank()) return SortState()
            val parts = value.split(":")
            val sort = runCatching { TrackSort.valueOf(parts[0]) }.getOrDefault(TrackSort.TITLE)
            val desc = parts.getOrNull(1)?.toBooleanStrictOrNull() ?: false
            return SortState(sort, desc)
        }
    }
}

/** Applies a [SortState] to a track list. */
fun List<Track>.applySort(state: SortState): List<Track> {
    val sorted = when (state.trackSort) {
        TrackSort.TITLE -> sortedBy { it.title.lowercase() }
        TrackSort.ARTIST -> sortedBy { it.artist.lowercase() }
        TrackSort.ALBUM -> sortedBy { it.album.lowercase() }
        TrackSort.DURATION -> sortedBy { it.duration }
        TrackSort.DATE_ADDED -> sortedBy { it.dateAdded }
        TrackSort.TRACK_NUMBER -> sortedBy { it.trackNumber }
    }
    return if (state.descending) sorted.reversed() else sorted
}
