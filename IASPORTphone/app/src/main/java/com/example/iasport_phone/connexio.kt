package com.example.iasport_phone

import android.content.Context
import android.util.Log
import com.google.android.gms.wearable.Wearable
import kotlinx.coroutines.tasks.await

object ConnectionManager
{
    private var connectedNodeId: String? = null

    suspend fun getConnectedNodeId(context: Context): String? {
        if (connectedNodeId == null) {
            val nodes = Wearable.getNodeClient(context).connectedNodes.await()
            if (nodes.isNotEmpty()) {
                connectedNodeId = nodes.first().id
                Log.d("ConnectionManager", "Node connectat: $connectedNodeId")
            } else {
                Log.e("ConnectionManager", "No s'ha trobat cap node connectat")
            }
        }
        return connectedNodeId
    }

    fun resetConnection() {
        connectedNodeId = null
        Log.d("ConnectionManager", "Connexió restablerta")
    }
}
