package com.mayaxdev.simnumbersetter.repositories

import com.topjohnwu.superuser.Shell
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

interface RootRepository {

    suspend fun isRooted(): Boolean
    suspend fun runRootCommand(command: String): Shell.Result
    suspend fun restartTelephony(): Boolean
    suspend fun getLine1NumberViaRoot(slotIndex: Int): String?
    suspend fun setLine1NumberViaRoot(slotIndex: Int, number: String): Boolean

}

class RootRepositoryImpl: RootRepository {

    override suspend fun isRooted(): Boolean {
        return withContext(Dispatchers.IO) {
            Shell.rootAccess()
        }
    }

    override suspend fun runRootCommand(command: String): Shell.Result {
        return withContext(Dispatchers.IO){
            Shell.su(command).exec()
        }
    }

    override suspend fun restartTelephony(): Boolean {
        return withContext(Dispatchers.IO) {
            val result = Shell.su("killall com.android.phone || pkill -f com.android.phone").exec()
            result.isSuccess
        }
    }

    override suspend fun getLine1NumberViaRoot(slotIndex: Int): String? {
        return withContext(Dispatchers.IO) {
            try {
                val result = Shell.su("content query --uri content://telephony/siminfo --projection sim_id:number").exec()
                if (!result.isSuccess) return@withContext null
                for (line in result.out) {
                    // Output format: Row: 0 sim_id=0, number=91XXXXXXXXXX
                    if (line.contains("sim_id=$slotIndex")) {
                        val regex = Regex("number=([^,\\s]*)")
                        val match = regex.find(line)
                        if (match != null) {
                            val num = match.groupValues[1]
                            return@withContext if (num == "null" || num == "NULL") "" else num
                        }
                    }
                }
                ""
            } catch (t: Throwable) {
                null
            }
        }
    }

    override suspend fun setLine1NumberViaRoot(slotIndex: Int, number: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val cmd = "content update --uri content://telephony/siminfo --bind number:s:$number --where sim_id=$slotIndex"
                val result = Shell.su(cmd).exec()
                result.isSuccess
            } catch (t: Throwable) {
                false
            }
        }
    }

}
