package com.klavs.bindle.util.startdestination

import android.content.Intent
import android.os.Bundle

interface StartDestinationProvider {

    fun determineStartDestination(intent: Intent?): Pair<String, String?>

}