package com.klavs.bindle.util

import android.content.Context
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.firebase.Timestamp
import com.klavs.bindle.R
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class TimeFunctions {

    fun calculateLeftTime(timestamp: Timestamp): Pair<Pair<Int, Long?>, Boolean> {
        val currentTime = Timestamp.now().toDate().time
        val diff = timestamp.toDate().time - currentTime

        if (diff <= 0){
            return (R.string.event_has_started to null) to true
        }else{
            val minutes = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
            val hours = TimeUnit.MILLISECONDS.toHours(diff) % 24
            val days = TimeUnit.MILLISECONDS.toDays(diff)

            return when{
                days >= 4 -> (R.string.event_left_days to days) to false
                days > 0 -> (R.string.event_left_days to days) to true
                hours > 0 -> (R.string.event_left_hours to hours) to true
                minutes > 0 -> (R.string.event_left_minutes to minutes) to true
                else -> (R.string.about_to_start to null) to true
            }
        }
    }


    fun mergeDateAndTime(dateTimestamp: Timestamp, hour: Int, minute: Int): Timestamp {
        // Eski tarih bilgisini al
        val date = dateTimestamp.toDate()

        // Saat ve dakikayı sıfırlıyoruz
        val calendar = Calendar.getInstance()
        calendar.time = date
        calendar.set(Calendar.HOUR_OF_DAY, hour)  // Yeni saati ayarla
        calendar.set(Calendar.MINUTE, minute)     // Yeni dakikayı ayarla
        calendar.set(Calendar.SECOND, 0)          // Saniyeyi sıfırla
        calendar.set(Calendar.MILLISECOND, 0)     // Milisaniyeyi sıfırla

        // Yeni tarih ve saat bilgisini Timestamp olarak döndür
        return Timestamp(calendar.time)
    }

    fun convertTimestampToLocalizeTime(timestamp: Timestamp, context: Context): String {
        val currentTime = Timestamp.now().toDate().time

        val diff = currentTime - timestamp.toDate().time

        val minutes = if (diff >= 0) TimeUnit.MILLISECONDS.toMinutes(diff) else TimeUnit.MILLISECONDS.toMinutes(-diff)
        val hours = if (diff >= 0) TimeUnit.MILLISECONDS.toHours(diff) else TimeUnit.MILLISECONDS.toHours(-diff)
        val days = if (diff >= 0) TimeUnit.MILLISECONDS.toDays(diff) else TimeUnit.MILLISECONDS.toDays(-diff)

        return if (diff >= 0) when {
            days > 7 -> TimeFunctions().convertTimestampToDate(timestamp)
            days > 0 -> context.getString(R.string.x_day_ago, days)
            hours > 0 -> context.getString(R.string.x_hours_ago, hours)
            minutes > 0 -> context.getString(R.string.x_minutes_ago, minutes)
            else -> context.getString(R.string.now)
        } else when {
            days > 7 -> TimeFunctions().convertTimestampToDate(timestamp)
            days > 0 -> context.getString(R.string.x_days_left, days)
            hours > 0 -> context.getString(R.string.x_hours_left, hours)
            minutes > 0 -> context.getString(R.string.x_minutes_left, minutes)
            else -> context.getString(R.string.now)
        }
    }

    fun convertTimestampToDate(timestamp: Timestamp): String {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        return formatter.format(timestamp.toDate())
    }

    fun convertTimestampToLocalizeDate(timestamp: Timestamp, singleLine: Boolean = true): String {
        val currentTime = Timestamp.now().toDate().time
        val diff = abs(currentTime - timestamp.toDate().time)
        val years = diff / (1000L * 60 * 60 * 24 * 365) // Milisaniyeleri yıllara çevir

        val formatter = if (years >= 1f) {
            SimpleDateFormat(if (singleLine) "dd/MMM/yyyy"  else "dd\nMMM\nyy", Locale.getDefault())
        } else {
            SimpleDateFormat(if (singleLine) "dd MMM" else "dd\nMMM", Locale.getDefault())
        }

        return formatter.format(timestamp.toDate())
    }


    fun getHourAndMinuteFromTimestamp(timestamp: Timestamp): Pair<Int, Int> {
        val date = timestamp.toDate()

        val calendar = Calendar.getInstance()
        calendar.time = date

        val hour = calendar.get(Calendar.HOUR_OF_DAY) // Saat
        val minute = calendar.get(Calendar.MINUTE) // Dakika

        return Pair(hour, minute)
    }

    fun convertDatePickerTimeToTimestamp(millis: Long): Timestamp {
        // Millis değeri UTC zaman diliminde LocalDateTime'a dönüştürülür
        val instant = Instant.ofEpochMilli(millis)
        val dateTime = LocalDateTime.ofInstant(
            instant,
            ZoneId.systemDefault()
        ) // Sistem zaman dilimini kullanarak LocalDateTime'a çeviriyoruz

        // LocalDateTime'ı Timestamp'e dönüştürmek için, UTC'ye dönüştürüp Timestamp'e çeviriyoruz
        val utcInstant = dateTime.atZone(ZoneId.systemDefault())
            .toInstant()  // Yine sistem saat diliminden UTC'ye
        return Timestamp(Date.from(utcInstant))
    }
}