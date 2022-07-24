import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty

/**
 * A property delegate that that is initially null, but cannot return to being null-once a non-null value is assigned.
 */
internal fun <T, V> initiallyNull(): ReadWriteProperty<T, V?> {
    return Delegates.vetoable(null) { _, _, newValue ->
        newValue != null
    }
}