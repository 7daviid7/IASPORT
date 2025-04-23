package com.example.iasport_phone

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.iasport_phone.ui.theme.IASPORTphoneTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.google.android.gms.wearable.MessageClient
import com.google.android.gms.wearable.MessageEvent
import com.example.iasport_phone.ConnectionManager



class MainActivity : ComponentActivity(), MessageClient.OnMessageReceivedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registrem el listener per rebre missatges del rellotge
        Wearable.getMessageClient(this).addListener(this)

        setContent {
            IASPORTphoneTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    ExercisePhaseSelector()
                }
            }
        }
    }

    override fun onMessageReceived(messageEvent: MessageEvent) {
        val path = messageEvent.path
        if (path == "/sensor_data") {  // Aquest camí ha de coincidir amb el que envia el rellotge
            val receivedData = String(messageEvent.data)  // Convertim ByteArray a String
            Log.d("WearOS", "Dades rebudes del rellotge: $receivedData")

            // Aquí podries processar les dades rebudes (per exemple, actualitzar la UI)

        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Eliminem el listener quan es destrueix l'activitat
        Wearable.getMessageClient(this).removeListener(this)
    }
}

@Composable
fun ExercisePhaseSelector() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { iniciarRecollidaDades(context, "Excèntrica") }, modifier = Modifier.fillMaxWidth()) {
            Text("Fase Excèntrica")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { iniciarRecollidaDades(context, "Concèntrica") }, modifier = Modifier.fillMaxWidth()) {
            Text("Fase Concèntrica")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { iniciarRecollidaDades(context, "Isomètrica") }, modifier = Modifier.fillMaxWidth()) {
            Text("Fase Isomètrica")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { finalitzarConnexio(context) }, modifier = Modifier.fillMaxWidth()) {
            Text("Finalitzar Connexió")
        }
    }
}


fun iniciarRecollidaDades(context: Context, phase: String) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val nodeId = ConnectionManager.getConnectedNodeId(context)
            if (nodeId != null) {
                Wearable.getMessageClient(context)
                    .sendMessage(nodeId, "/exercise_phase", phase.toByteArray())
                    .await()
                Log.d("iniciarRecollidaDades", "Missatge enviat: $phase")
            } else {
                Log.e("iniciarRecollidaDades", "No hi ha cap dispositiu connectat")
            }
        } catch (e: Exception) {
            Log.e("iniciarRecollidaDades", "Error en enviar el missatge", e)
        }
    }
}

fun finalitzarConnexio(context: Context) {
    CoroutineScope(Dispatchers.IO).launch {
        try {
            // Si tenim un node, enviem un missatge de "finalitzar" abans de desconnectar
            val nodeId = ConnectionManager.getConnectedNodeId(context)  // Obtenim l'ID del node connectat
            if (nodeId != null) {
                Wearable.getMessageClient(context)
                    .sendMessage(nodeId, "/finalitzar_connexio", "Finalitzar".toByteArray())  // Missatge de finalització
                    .await()
                Log.d("finalitzarConnexio", "Missatge de finalització enviat.")
            } else {
                Log.e("finalitzarConnexio", "No hi ha cap dispositiu connectat.")
            }

            // Aquí podries afegir més lògica per finalitzar qualsevol procés associat amb la connexió

        } catch (e: Exception) {
            Log.e("finalitzarConnexio", "Error en enviar el missatge de finalització", e)
        }
    }
}






//preview sense compilar.
@Preview(showBackground = true)
@Composable
fun ExercisePhaseSelectorPreview() {
    IASPORTphoneTheme {
        ExercisePhaseSelector()
    }
}
