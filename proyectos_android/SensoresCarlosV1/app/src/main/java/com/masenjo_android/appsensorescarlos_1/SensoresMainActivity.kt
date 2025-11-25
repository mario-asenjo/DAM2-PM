package com.masenjo_android.appsensorescarlos_1

import android.animation.ArgbEvaluator
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    // SensorManager y sensores
    private lateinit var sensorManager: SensorManager
    private var accelerometer: Sensor? = null
    private var lightSensor: Sensor? = null

    // Vistas
    private lateinit var rootLayout: LinearLayout
    private lateinit var accelerometerValues: TextView
    private lateinit var orientationLabel: TextView
    private lateinit var lightSensorValues: TextView

    // Colores para interpolar el fondo según la luz
    private var darkColor: Int = 0
    private var lightColor: Int = 0

    // Para suavizar cambios de luz (filtro muy simple)
    private var lastLightValue: Float? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.tarjetas_sensores)

        // Inicializamos SensorManager
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        // Obtenemos sensores
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        // Referencias a vistas
        rootLayout = findViewById(R.id.root_layout)
        accelerometerValues = findViewById(R.id.accelerometer_values)
        orientationLabel = findViewById(R.id.orientation_label)
        lightSensorValues = findViewById(R.id.light_sensor_values)

        // Define colores de fondo
        darkColor = 0xFF121212.toInt()   // fondo muy oscuro
        lightColor = 0xFFFFFFFF.toInt()  // fondo blanco

        // Si algún sensor no existe, avisamos en pantalla
        if (accelerometer == null) {
            accelerometerValues.text = getString(R.string.not_accelerometter_warning)
        }
        if (lightSensor == null) {
            lightSensorValues.text = getString(R.string.not_light_sensor_warning)
        }
    }

    override fun onResume() {
        super.onResume()

        accelerometer?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }

        lightSensor?.also { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(event)
            Sensor.TYPE_LIGHT -> handleLight(event)
        }
    }

    private fun handleAccelerometer(event: SensorEvent) {
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        // Módulo del vector aceleración
        val magnitude = sqrt(x * x + y * y + z * z)

        accelerometerValues.text = """
            X: ${"%.2f".format(x)} m/s²
            Y: ${"%.2f".format(y)} m/s²
            Z: ${"%.2f".format(z)} m/s²
            Módulo: ${"%.2f".format(magnitude)} m/s²
        """.trimIndent()

        // Orientación aproximada del móvil según ejes
        val orientation = when {
            abs(x) < 3 && y > 6 -> "Vertical (modo retrato)"
            abs(x) < 3 && y < -6 -> "Vertical invertido"
            x > 6 -> "Horizontal (apaisado izquierda)"
            x < -6 -> "Horizontal (apaisado derecha)"
            else -> "Apoyado / en movimiento"
        }

        orientationLabel.text = "Orientación: $orientation"
    }

    private fun handleLight(event: SensorEvent) {
        val rawLight = event.values[0]

        // Suavizamos un poco los cambios de medición para que el fondo no "parpadee"
        val light = if (lastLightValue == null) {
            rawLight
        } else {
            // filtro muy sencillo: media ponderada
            lastLightValue!! * 0.8f + rawLight * 0.2f
        }
        lastLightValue = light

        // Texto descriptivo del nivel de luz
        val nivel = when {
            light < 10f -> "Muy oscuro"
            light < 100f -> "Oscuro / interior tenue"
            light < 1000f -> "Interior normal"
            light < 10000f -> "Muy iluminado"
            else -> "Luz exterior intensa"
        }

        lightSensorValues.text = "Luz: ${"%.1f".format(light)} lx\nNivel: $nivel"

        // Ajuste del fondo: interpolamos entre oscuro (poca luz) y claro (mucha luz)
        val maxLightForCalc = 20000f
        val normalized = (light.coerceIn(0f, maxLightForCalc)) / maxLightForCalc

        val evaluator = ArgbEvaluator()
        val backgroundColor = evaluator.evaluate(normalized, darkColor, lightColor) as Int
        rootLayout.setBackgroundColor(backgroundColor)

        // Cambiar color del texto para que siempre haya contraste aceptable
        val textColor = if (normalized < 0.5f) {
            // Fondo tirando a oscuro => texto claro
            0xFFFFFFFF.toInt()
        } else {
            // Fondo tirando a claro => texto oscuro
            0xFF000000.toInt()
        }

        setAllTextColors(rootLayout, textColor)
    }

    // Recorre el árbol de vistas y cambia el color de texto de todos los TextView
    private fun setAllTextColors(view: View, color: Int) {
        if (view is TextView) {
            view.setTextColor(color)
        } else if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                setAllTextColors(view.getChildAt(i), color)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No lo usamos por ahora
    }
}