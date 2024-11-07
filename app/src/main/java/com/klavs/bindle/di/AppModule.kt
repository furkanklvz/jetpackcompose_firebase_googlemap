package com.klavs.bindle.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.klavs.bindle.data.datasource.auth.AuthDataSource
import com.klavs.bindle.data.datasource.auth.AuthDataSourceImpl
import com.klavs.bindle.data.datasource.firestore.FirestoreDataSource
import com.klavs.bindle.data.datasource.firestore.FirestoreDataSourceImpl
import com.klavs.bindle.data.datasource.location.LocationDataSource
import com.klavs.bindle.data.datasource.location.LocationDataSourceImpl
import com.klavs.bindle.data.datasource.storage.StorageDatasource
import com.klavs.bindle.data.datasource.storage.StorageDatasourceImpl
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.data.repo.auth.AuthRepositoryImpl
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.data.repo.firestore.FirestoreRepositoryImpl
import com.klavs.bindle.data.repo.location.LocationRepository
import com.klavs.bindle.data.repo.location.LocationRepositoryImpl
import com.klavs.bindle.data.repo.storage.StorageRepository
import com.klavs.bindle.data.repo.storage.StorageRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = Firebase.auth

    @Provides
    @Singleton
    fun provideFirebaseFirestore(): FirebaseFirestore = Firebase.firestore

    @Provides
    @Singleton
    fun provideFirebaseStorage(): FirebaseStorage = Firebase.storage

    @Provides
    @Singleton
    fun provideAuthDataSource(auth: FirebaseAuth): AuthDataSource = AuthDataSourceImpl(auth)

    @Provides
    @Singleton
    fun provideFirestoreDataSource(db: FirebaseFirestore, auth: FirebaseAuth): FirestoreDataSource =
        FirestoreDataSourceImpl(db, auth = auth)

    @Provides
    @Singleton
    fun provideStorageDataSource(storage: FirebaseStorage, context: Context): StorageDatasource =
        StorageDatasourceImpl(storage, context = context)

    @Provides
    @Singleton
    fun provideFirestoreRepository(firestoreDataSource: FirestoreDataSource): FirestoreRepository =
        FirestoreRepositoryImpl(firestoreDataSource)

    @Provides
    @Singleton
    fun provideAuthRepository(authDataSource: AuthDataSource): AuthRepository =
        AuthRepositoryImpl(authDataSource)

    @Provides
    @Singleton
    fun provideStorageRepository(storageDataSource: StorageDatasource): StorageRepository =
        StorageRepositoryImpl(storageDataSource)

    val Context.dataStoreFile: DataStore<Preferences> by preferencesDataStore(name = "appPreferences")

    @Provides
    @Singleton
    fun provideDataStore(context: Context): DataStore<Preferences> {
        return context.dataStoreFile
    }

    @Provides
    @Singleton
    fun provideFusedLocationProviderClient(context: Context): FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    @Provides
    @Singleton
    fun provideLocationDataSource(
        context: Context,
        locationClient: FusedLocationProviderClient
    ): LocationDataSource = LocationDataSourceImpl(locationClient,context)

    @Provides
    @Singleton
    fun provideLocationRepository(ds: LocationDataSource): LocationRepository =
        LocationRepositoryImpl(ds)


}