package com.nexxlabs.chhotu

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Application class for Chhotu voice assistant.
 * Annotated with @HiltAndroidApp to enable Hilt dependency injection.
 */
@HiltAndroidApp
class ChhotuApplication : Application()
