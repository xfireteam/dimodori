import com.dimodori.app.ui.screens.dashboard.shouldReturnDashboardHome
import com.dimodori.app.ui.screens.player.supportsWindowHdrHeadroom

/** Executes production policy functions; not an Android window/media test. */
fun main() {
    var checks = 0
    for (sdk in 0..37) {
        check(supportsWindowHdrHeadroom(sdk) == (sdk >= 35))
        checks++
    }
    for (route in listOf(null, "home", "search", "favorites", "settings")) {
        for (resumed in listOf(false, true)) {
            for (overlay in listOf(false, true)) {
                check(shouldReturnDashboardHome(route, "home", resumed, overlay) ==
                    (resumed && !overlay && route != null && route != "home"))
                checks++
            }
        }
    }
    check(!shouldReturnDashboardHome("downloads-home", "downloads-home", true, false))
    checks++
    println("$checks production HDR/dashboard policy checks passed (not native UI tests).")
}
