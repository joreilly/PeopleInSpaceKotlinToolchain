package dev.johnoreilly.peopleinspace

import android.app.Application
import org.koin.android.ext.koin.androidContext

class PeopleInSpaceApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin { androidContext(this@PeopleInSpaceApplication) }
    }
}
