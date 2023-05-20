package io.github.xamdr.noties.ui.helpers

import android.app.ActivityManager
import android.content.Context
import androidx.core.content.getSystemService
import java.util.*

enum class PerformanceClass {
	LOW,
	MEDIUM,
	HIGH
}

object DevicePerformanceClassifier {

	private const val COMMAND = "cat /proc/cpuinfo"

	fun getDevicePerformanceClass(context: Context): PerformanceClass {
		val cpuCount = Runtime.getRuntime().availableProcessors()
		val cpuFrequency = getCpuMaxFrequency()
		val ramSize = getTotalMemory(context)
		return when {
			cpuFrequency < 1500000 && ramSize < 2000000000 && cpuCount <= 4 -> PerformanceClass.LOW
			cpuFrequency >= 1500000 && ramSize >= 2000000000 && cpuCount >= 8 -> PerformanceClass.HIGH
			else -> PerformanceClass.MEDIUM
		}
	}

	private fun getCpuMaxFrequency(): Int {
		val cpuInfo = Runtime.getRuntime().exec(COMMAND)
		Scanner(cpuInfo.inputStream).apply {
			useDelimiter("\n")
			while (hasNext()) {
				val line = next()
				if (line.contains("cpu MHz")) {
					val frequency = line.split(":")[1].trim().toDouble()
					return (frequency * 1000000).toInt()
				}
			}
		}
		return 0
	}

	private fun getTotalMemory(context: Context): Long {
		val activityManager = context.getSystemService<ActivityManager>()
		val memoryInfo = ActivityManager.MemoryInfo()
		activityManager?.getMemoryInfo(memoryInfo)
		return memoryInfo.totalMem
	}
}