import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import com.google.android.gms.wearable.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.tasks.await

class MainActivity : ComponentActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var sendingData = false
    private var currentPhase: String = "Ninguna"
    private lateinit var messageClient: MessageClient
    private val mobileNodeId = MutableStateFlow<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicialitza sensors
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        // Configura la recepció de missatges
        messageClient = Wearable.getMessageClient(this)
        messageClient.addListener { messageEvent ->
            when (messageEvent.path) {
                "/exercise_phase" -> {
                    currentPhase = String(messageEvent.data)  // Rebem la fase
                    Log.d("WearOS", "Fase rebuda: $currentPhase")
                    startSendingData()
                }
                "/stop" -> {
                    stopSendingData()
                }
            }
        }

        // Obtenir ID del mòbil connectat
        CoroutineScope(Dispatchers.IO).launch {
            val nodes = Wearable.getNodeClient(this@MainActivity).connectedNodes.await()
            mobileNodeId.value = nodes.firstOrNull()?.id
        }
    }

    private fun startSendingData() {
        if (!sendingData) {
            sendingData = true
            Log.d("WearOS", "Iniciant enviament de dades en fase: $currentPhase")
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, gyroscope, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private fun stopSendingData() {
        if (sendingData) {
            sendingData = false
            Log.d("WearOS", "Aturant enviament de dades...")
            sensorManager.unregisterListener(this)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null || !sendingData) return

        val data = when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> "ACC ($currentPhase): x=${event.values[0]}, y=${event.values[1]}, z=${event.values[2]}"
            Sensor.TYPE_GYROSCOPE -> "GYRO ($currentPhase): x=${event.values[0]}, y=${event.values[1]}, z=${event.values[2]}"
            else -> return
        }

        Log.d("WearOS", "Enviant: $data")
        mobileNodeId.value?.let { nodeId ->
            CoroutineScope(Dispatchers.IO).launch
                Wearable.getMessageClient(this@MainActivity)
                    .sendMessage(nodeId, "/sensor_data", data.toByteArray())
                    .await()
            }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}


