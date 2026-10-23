import kotlin.coroutines.Continuation
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.startCoroutine
import java.util.concurrent.CancellationException

// HTTP/session/DTO doubles only. The runner supplies the repository methods
// verbatim from MediaRepository.kt; no copy of their implementation lives here.
data class BaseItemDto(val id: String?, val name: String?, val type: String?)
data class QueryResult(val items: List<BaseItemDto>?)
data class Response(val isSuccessful: Boolean, val value: QueryResult?, val status: Int = 200) {
    fun body() = value
    fun code() = status
}
data class Request(
    val userId: String, val parentId: String?, val itemType: String?,
    val recursive: Boolean?, val sortBy: String?, val sortOrder: String?,
    val limit: Int?, val fields: String?, val enableUserData: Boolean?
)
class FakeApi {
    var response = Response(true, QueryResult(emptyList()))
    var error: Exception? = null
    val requests = mutableListOf<Request>()
    suspend fun getUserItems(
        userId: String, parentId: String?, includeItemTypes: String?, recursive: Boolean?,
        sortBy: String?, sortOrder: String?, limit: Int?, fields: String?, enableUserData: Boolean?
    ): Response {
        requests += Request(userId, parentId, includeItemTypes, recursive, sortBy, sortOrder,
            limit, fields, enableUserData)
        error?.let { throw it }
        return response
    }
}
data class Session(val api: FakeApi, val userId: String)
object R {
    object string {
        const val data_error_session_not_available = 1
        const val media_error_fetch_user_items_failed = 2
    }
}

private fun <T> runImmediate(block: suspend () -> T): T {
    var outcome: Result<T>? = null
    block.startCoroutine(object : Continuation<T> {
        override val context = EmptyCoroutineContext
        override fun resumeWith(result: Result<T>) { outcome = result }
    })
    return requireNotNull(outcome) { "Test double unexpectedly suspended" }.getOrThrow()
}

fun main() {
    var checks = 0
    fun expect(value: Boolean) { check(value); checks++ }
    for (server in listOf("Emby", "Jellyfin")) {
        val api = FakeApi()
        val harness = RecentTitlesHarness(Session(api, "$server-user"))
        for (type in listOf("Series", "Movie")) {
            api.response = Response(true, QueryResult(listOf(
                BaseItemDto("new", "Newest title", type),
                BaseItemDto("new", "Duplicate", type),
                BaseItemDto("episode", "New episode of old series", "Episode"),
                BaseItemDto(null, "Missing ID", type),
                BaseItemDto("empty", " ", type),
                BaseItemDto("wrong", "Wrong type", if (type == "Movie") "Series" else "Movie"),
                BaseItemDto("old", "Older title", type.lowercase())
            )))
            val items = runImmediate {
                if (type == "Series") harness.getRecentlyAddedSeries(24)
                else harness.getRecentlyAddedMovies(24)
            }.getOrThrow()
            expect(items.map { it.id } == listOf("new", "old"))
            val request = api.requests.last()
            expect(request.userId == "$server-user" && request.parentId == null)
            expect(request.itemType == type && request.recursive == true)
            expect(request.sortBy == "DateCreated" && request.sortOrder == "Descending")
            expect(request.limit == 24 && request.fields!!.contains("DateCreated"))
            expect(request.enableUserData == true)
        }
        expect(runImmediate { harness.getRecentlyAddedMovies(1) }.getOrThrow().size == 1)
        api.response = Response(true, QueryResult(emptyList()))
        expect(runImmediate { harness.getRecentlyAddedSeries() }.getOrThrow().isEmpty())
        api.response = Response(false, null, 503)
        expect(runImmediate { harness.getRecentlyAddedMovies() }.isFailure)
        // One failed query does not contaminate the subsequent, independent row.
        api.response = Response(true, QueryResult(listOf(BaseItemDto("ok", "Series", "Series"))))
        expect(runImmediate { harness.getRecentlyAddedSeries() }.getOrThrow().single().id == "ok")
        val before = api.requests.size
        expect(runImmediate { harness.getRecentlyAddedSeries(0) }.getOrThrow().isEmpty())
        expect(api.requests.size == before)
        api.error = CancellationException("session switched")
        try {
            runImmediate { harness.getRecentlyAddedMovies() }
            error("Cancellation was swallowed")
        } catch (_: CancellationException) { checks++ }
        api.error = IllegalStateException("offline")
        expect(runImmediate { harness.getRecentlyAddedMovies() }.isFailure)
    }
    expect(runImmediate { RecentTitlesHarness(null).getRecentlyAddedMovies() }.isFailure)
    println("$checks production repository checks passed with HTTP/session/DTO doubles.")
    println("Not an Android build or live-server verification.")
}
