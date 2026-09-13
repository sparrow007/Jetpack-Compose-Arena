package com.example.composelearning.todo.di

import android.content.Context
import androidx.room.Room
import com.example.composelearning.todo.data.local.TodoDao
import com.example.composelearning.todo.data.local.TodoDatabase
import com.example.composelearning.todo.data.remote.TodoApi
import com.example.composelearning.todo.data.repository.DefaultTodoRepository
import com.example.composelearning.todo.data.repository.TodoRepository
import com.example.composelearning.todo.data.sync.SyncScheduler
import com.example.composelearning.todo.data.sync.WorkManagerSyncScheduler
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

@Module
@InstallIn(SingletonComponent::class)
object TodoProvidesModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): TodoDatabase =
        Room.databaseBuilder(context, TodoDatabase::class.java, TodoDatabase.NAME).build()

    @Provides
    fun dao(db: TodoDatabase): TodoDao = db.todoDao()

    @Provides
    @Singleton
    fun moshi(): Moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    @Provides
    @Singleton
    fun retrofit(moshi: Moshi): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(OkHttpClient.Builder().build())
        .addConverterFactory(MoshiConverterFactory.create(moshi))
        .build()

    @Provides
    @Singleton
    fun todoApi(retrofit: Retrofit): TodoApi = retrofit.create(TodoApi::class.java)

    @Provides
    @IoDispatcher
    fun ioDispatcher(): CoroutineDispatcher = Dispatchers.IO

    private const val BASE_URL = "https://example.com/"
}

@Module
@InstallIn(SingletonComponent::class)
interface TodoBindsModule {

    @Binds
    fun repository(impl: DefaultTodoRepository): TodoRepository

    @Binds
    fun syncScheduler(impl: WorkManagerSyncScheduler): SyncScheduler
}
