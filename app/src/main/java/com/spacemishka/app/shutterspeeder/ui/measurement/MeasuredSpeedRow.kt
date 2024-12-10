import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlin.math.absoluteValue

@Composable
fun MeasuredSpeedRow(
    label: String,
    duration: Long,
    deviation: Double,
    reference: Long,
    warningThreshold: Double,
    errorThreshold: Double
) {
    val deviationColor = when {
        deviation.absoluteValue <= warningThreshold -> MaterialTheme.colorScheme.primary
        deviation.absoluteValue <= errorThreshold -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.error
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            "${duration}μs",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        Text(
            String.format("%.1f%%", deviation),
            style = MaterialTheme.typography.bodyMedium,
            color = deviationColor,
            modifier = Modifier.weight(1f)
        )
    }
} 