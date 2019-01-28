/*
 * Copyright (c) 2018 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.di

import android.app.NotificationManager
import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.duckduckgo.app.fire.DataClearingWorker
import com.duckduckgo.app.global.view.ClearDataAction
import com.duckduckgo.app.notification.NotificationScheduler
import com.duckduckgo.app.notification.store.NotificationDao
import com.duckduckgo.app.settings.db.SettingsDataStore
import timber.log.Timber

class DaggerWorkerFactory(
    private val settingsDataStore: SettingsDataStore,
    private val clearDataAction: ClearDataAction,
    private val notificationManager: NotificationManager,
    private val notificationDao: NotificationDao
) : WorkerFactory() {

    override fun createWorker(appContext: Context, workerClassName: String, workerParameters: WorkerParameters): ListenableWorker? {

        val workerClass = Class.forName(workerClassName).asSubclass(ListenableWorker::class.java)
        val constructor = workerClass.getDeclaredConstructor(Context::class.java, WorkerParameters::class.java)
        val instance = constructor.newInstance(appContext, workerParameters)

        when (instance) {
            is DataClearingWorker -> injectDataClearWorker(instance)
            is NotificationScheduler.ShowClearDataNotification -> injectShowClearNotificationWorker(instance)
            else -> Timber.i("No injection required for worker $workerClassName")
        }

        return instance
    }

    private fun injectDataClearWorker(worker: DataClearingWorker) {
        worker.settingsDataStore = settingsDataStore
        worker.clearDataAction = clearDataAction
    }

    private fun injectShowClearNotificationWorker(worker: NotificationScheduler.ShowClearDataNotification) {
        worker.manager = notificationManager
        worker.notificationDao = notificationDao
    }
}