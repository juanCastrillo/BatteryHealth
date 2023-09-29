package juan.batteryhealth

import android.content.Context
import android.os.BatteryManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import juan.batteryhealth.ui.theme.BatteryHealthTheme

//val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class MainActivity : ComponentActivity() {

    private var currentCapacity: Float = 0f
    private var currentChargePercentage: Int = 0
    private var currentHealth: Float = 0f
    private var maxCapacity: Float = 0f
    val SHOW_DETAILS = "showDetails"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        getBatteryHealth()
        val p = getPreferences(Context.MODE_PRIVATE)
        var showDetails = p.getBoolean(SHOW_DETAILS, false)
        setContent {
            BatteryHealthTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column (modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .statusBarsPadding()
                        .navigationBarsPadding()
                    ){

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            BatteryHealthIndicator(currentHealth, currentCapacity, maxCapacity, showDetails,
                                onShowChange = {
                                    showDetails = it
                                    p.edit().putBoolean(SHOW_DETAILS, showDetails).apply()
                                    Log.d("DETAILS INDICATOR", "saved, $showDetails")
                            })
                        }
                        if (currentChargePercentage < 100)
                            Column(modifier = Modifier.fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("Charge to 100% to get accurate results.")
                            }
                    }
                }
            }
        }
    }

    private val batteryManager: BatteryManager by lazy {
        getSystemService(BATTERY_SERVICE) as BatteryManager
    }

    private fun getBatteryHealth() {

//        val batteryStatus: Intent? = IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
//            this.registerReceiver(null, ifilter)
//        }
//
//        val batteryPct: Float? = batteryStatus?.let { intent ->
//            val level: Int = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
//            val scale: Int = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
////            val propertycapacity: Int = intent.getIntExtra(BatteryManager.EXTRA_, -1)
//
//            Log.d("BATTERY", "LEVEL: $level")
//            Log.d("BATTERY", "SCALE: $scale")
//            level * 100 / scale.toFloat()
//        }

        // Remaining battery capacity as an integer percentage of total capacity (with no fractional part).
        val property_capacity = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        Log.d("BATTERY_PROPERTY_CAPACITY", "$property_capacity") // 6

        // Battery capacity in microampere-hours, as an integer.
        val property_charge_counter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
        Log.d("BATTERY_PROPERTY_CHARGE_COUNTER", "$property_charge_counter") // 188309

        // Si 188309 es el 6%, el 100% = 3.138 mah

        // Average battery current in microamperes, as an integer.
        val property_current_average = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_AVERAGE)
        Log.d("BATTERY_PROPERTY_CURRENT_AVERAGE", "$property_current_average") // 0
        currentChargePercentage = property_capacity

        val estimated = (property_charge_counter.toFloat() * 100/ property_capacity.toFloat())/1000f
        currentCapacity = estimated

        // Samsung s20+ = 4370 mAh
        // Pixel 4 = 2800 mAh
        // Nexus 5x = 2600 mAh
        val peak_cap = 2600f
        maxCapacity = peak_cap
        val health = (estimated / peak_cap) * 100
        currentHealth = health
    }
}

@Composable
fun BatteryHealthIndicator(health: Float, currentCapacity: Float, maxCapacity: Float, showDetails: Boolean, onShowChange: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    var show by remember { mutableStateOf(showDetails) }
    Column (Modifier.clickable {
        show = !show
        onShowChange(show)
    }) {
        if (health >= 100)
            Text(
                text = "100 %",
                modifier = modifier,
                fontWeight = FontWeight.Bold,
                fontSize = 80.sp,
                color = Color(163, 210, 173)
            )
        else
            Text(
                text = "${"%.1f".format(health)}%",
                modifier = modifier,
                fontWeight = FontWeight.Bold,
                fontSize = 80.sp,
            )
        if (show) {
            Spacer(modifier = Modifier.size(10.dp))
            Text("Calculated: ${currentCapacity.toInt()} mAh")
            Text("Expected: ${maxCapacity.toInt()} mAh")
//        Text("Charge State: ")
        }
    }
}

//@Preview(showBackground = true)
//@Composable
//fun GreetingPreview() {
//    BatteryHealthTheme {
//        Greeting("Android")
//    }
//}

