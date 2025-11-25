package com.masenjo_android.appsensorescarlos_1
//INFORMACIÓN ESENCIAL PARA ENTENDER EL USO DE SENSORES:
//https://developer.android.com/develop/sensors-and-location/sensors/sensors_overview?hl=es-419
//https://developer.android.com/reference/android/hardware/SensorEvent#values

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

// Esta clase implementa SensorEventListener para manejar eventos de sensores
class SensoresMainActivity : AppCompatActivity(), SensorEventListener {

    // Variables para manejar el SensorManager y los sensores específicos
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lightSensor: Sensor? = null

    // TextViews para mostrar los valores
    private lateinit var accelerometerValues: TextView
    private lateinit var lightSensorValues: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main) // Enlazamos la actividad con su layout

        // Inicializamos el SensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Obtenemos el acelerómetro y el sensor de luz
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        // Referenciamos los TextViews del layout
        accelerometerValues = findViewById(R.id.accelerometer_values)
        lightSensorValues = findViewById(R.id.light_sensor_values)
    }

    override fun onResume() {
        super.onResume()
        // Registramos los listeners para los sensores
        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
        lightSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        // Desregistramos los listeners para ahorrar batería
        sensorManager.unregisterListener(this)
    }

    // Este método se llama cada vez que cambian los valores de un sensor
    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            when (event.sensor.type) {
                Sensor.TYPE_ACCELEROMETER -> {
                    val x = event.values[0]
                    val y = event.values[1]
                    val z = event.values[2]
                    // Mostramos los valores del acelerómetro
                    accelerometerValues.text = "Acelerómetro: X=$x, Y=$y, Z=$z"
                }

                Sensor.TYPE_LIGHT -> {
                    val light = event.values[0]
                    // Mostramos los valores del sensor de luz
                    lightSensorValues.text = "Luz: $light lx"
                }
            }
        }
    }

    // Este método se llama si cambia la precisión del sensor
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No lo usamos en este caso, pero se puede manejar según sea necesario
    }
}