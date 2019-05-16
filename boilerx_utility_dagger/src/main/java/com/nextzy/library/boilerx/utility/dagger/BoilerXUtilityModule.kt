package com.nextzy.library.boilerx.utility.dagger

import android.content.Context
import com.nextzy.library.boilerx.utility.database.Hawk
import com.nextzy.library.boilerx.utility.datetime.DateTimeUtil
import com.nextzy.library.boilerx.utility.version.VersionUtil
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class BoilerXUtilityModule {
    @Singleton
    @Provides
    fun provideHawk(context: Context): Hawk = Hawk(context)

    @Singleton
    @Provides
    fun provideVersionUtil(): VersionUtil = VersionUtil()

    @Singleton
    @Provides
    fun provideDateTimeUtil(): DateTimeUtil = DateTimeUtil()
}
