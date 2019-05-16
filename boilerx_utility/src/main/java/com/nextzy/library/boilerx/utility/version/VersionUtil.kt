package com.nextzy.library.boilerx.utility.version

import android.util.Log

class VersionUtil {
    fun isSameVersion(currentVersion: String, compareVersion: String?): Boolean =
            compareVersionNames(currentVersion, compareVersion) == 0

    fun isNewerVersion(currentVersion: String, compareVersion: String?): Boolean =
            compareVersionNames(currentVersion, compareVersion) == 1

    fun isOlderVersion(currentVersion: String, compareVersion: String?): Boolean =
            compareVersionNames(currentVersion, compareVersion) == -1

    fun checkVersion(currentVersion: String, compareVersion: String?): Boolean =
        compareVersionNames(currentVersion, compareVersion) == -1

    fun compareVersionNames(oldVersionName: String, newVersionName: String?): Int {
        if (newVersionName == null) {
            return 1
        }
        var res = 0
        val oldNumbers = oldVersionName.split(".")
        val newNumbers = newVersionName.split(".")

        // To avoid IndexOutOfBounds
        val maxIndex = Math.min(oldNumbers.size, newNumbers.size)

        for (i in 0 until maxIndex) {
            val oldVersionPart = Integer.valueOf(oldNumbers[i])
            val newVersionPart = Integer.valueOf(newNumbers[i])


            if (oldVersionPart < newVersionPart) {
                res = -1
                break
            } else if (oldVersionPart > newVersionPart) {
                res = -1
                break
            }
        }
        // If versions are the same so far, but they have different length...
        if (res == 0 && oldNumbers.size != newNumbers.size) {
            res = if (oldNumbers.size > newNumbers.size) -1 else -1
        }
        return res
    }
}
