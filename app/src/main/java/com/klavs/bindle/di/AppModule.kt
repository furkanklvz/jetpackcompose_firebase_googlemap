package com.klavs.bindle.di

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.algolia.search.client.ClientSearch
import com.algolia.search.client.Index
import com.algolia.search.model.APIKey
import com.algolia.search.model.ApplicationID
import com.algolia.search.model.IndexName
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.PendingPurchasesParams
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.messaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storage
import com.klavs.bindle.R
import com.klavs.bindle.data.PurchasesUpdatedListenerImpl
import com.klavs.bindle.data.datasource.algolia.AlgoliaDataSource
import com.klavs.bindle.data.datasource.algolia.AlgoliaDataSourceImpl
import com.klavs.bindle.data.datasource.auth.AuthDataSource
import com.klavs.bindle.data.datasource.auth.AuthDataSourceImpl
import com.klavs.bindle.data.datasource.firestore.FirestoreDataSource
import com.klavs.bindle.data.datasource.firestore.FirestoreDataSourceImpl
import com.klavs.bindle.data.datasource.googleplaybilling.GooglePlayBillingDataSource
import com.klavs.bindle.data.datasource.googleplaybilling.GooglePlayBillingDataSourceImpl
import com.klavs.bindle.data.datasource.location.LocationDataSource
import com.klavs.bindle.data.datasource.location.LocationDataSourceImpl
import com.klavs.bindle.data.datasource.messaging.MessagingDataSource
import com.klavs.bindle.data.datasource.messaging.MessagingDataSourceImpl
import com.klavs.bindle.data.datasource.storage.StorageDatasource
import com.klavs.bindle.data.datasource.storage.StorageDatasourceImpl
import com.klavs.bindle.data.repo.algolia.AlgoliaRepository
import com.klavs.bindle.data.repo.algolia.AlgoliaRepositoryImpl
import com.klavs.bindle.data.repo.auth.AuthRepository
import com.klavs.bindle.data.repo.auth.AuthRepositoryImpl
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.data.repo.firestore.FirestoreRepositoryImpl
import com.klavs.bindle.data.repo.googleplaybilling.GooglePlayBillingRepository
import com.klavs.bindle.data.repo.googleplaybilling.GooglePlayBillingRepositoryImpl
import com.klavs.bindle.data.repo.location.LocationRepository
import com.klavs.bindle.data.repo.location.LocationRepositoryImpl
import com.klavs.bindle.data.repo.messaging.MessagingRepository
import com.klavs.bindle.data.repo.messaging.MessagingRepositoryImpl
import com.klavs.bindle.data.repo.storage.StorageRepository
import com.klavs.bindle.data.repo.storage.StorageRepositoryImpl
import com.klavs.bindle.util.startdestination.StartDestinationProvider
import com.klavs.bindle.util.startdestination.StartDestinationProviderImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
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
    fun provideFirebaseMessaging(): FirebaseMessaging = Firebase.messaging

    @Provides
    @Singleton
    fun provideAuthDataSource(
        auth: FirebaseAuth,
        db: FirebaseFirestore,
        storage: FirebaseStorage
    ): AuthDataSource =
        AuthDataSourceImpl(
            auth,
            db,
            storage
        )

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
    fun provideMessagingDataSource(
        db: FirebaseFirestore,
        messaging: FirebaseMessaging,
        context: Context
    ): MessagingDataSource =
        MessagingDataSourceImpl(
            context = context,
            messaging = messaging,
            db = db
        )

    @Provides
    @Singleton
    fun provideAlgoliaDataSource(
        @EventsIndex indexEvents: Index,
        @CommunitiesIndex indexCommunities: Index
    ): AlgoliaDataSource =
        AlgoliaDataSourceImpl(
            indexCommunities = indexCommunities,
            indexEvents = indexEvents
        )

    @Provides
    @Singleton
    fun provideGooglePlayBillingDataSource(
        billingClient: BillingClient,
        context: Context,
        db: FirebaseFirestore,
        firestoreRepo: FirestoreRepository
    ): GooglePlayBillingDataSource =
        GooglePlayBillingDataSourceImpl(
            billingClient = billingClient,
            context = context,
            firestoreRepo = firestoreRepo,
            db = db
        )

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

    @Provides
    @Singleton
    fun provideMessagingRepository(ds: MessagingDataSource): MessagingRepository =
        MessagingRepositoryImpl(
            ds = ds
        )

    @Provides
    @Singleton
    fun provideAlgoliaRepository(ds: AlgoliaDataSource): AlgoliaRepository =
        AlgoliaRepositoryImpl(
            ds = ds
        )

    @Provides
    @Singleton
    fun provideGooglePlayBillingRepository(ds: GooglePlayBillingDataSource): GooglePlayBillingRepository =
        GooglePlayBillingRepositoryImpl(
            ds = ds
        )

    private val Context.dataStoreFile: DataStore<Preferences> by preferencesDataStore(name = "appPreferences")

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
    ): LocationDataSource = LocationDataSourceImpl(locationClient, context)

    @Provides
    @Singleton
    fun provideLocationRepository(ds: LocationDataSource): LocationRepository =
        LocationRepositoryImpl(ds)

    @Provides
    @Singleton
    fun provideAlgoliaClient(context: Context): ClientSearch {
        return try {
            ClientSearch(
                ApplicationID(context.getString(R.string.algolia_application_id)),
                APIKey(context.getString(R.string.algolia_search_only_api_key))
            )
        } catch (e: Exception) {
            Log.e("error from datasource", "Failed to initialize Algolia client: ${e.message}")
            FirebaseCrashlytics.getInstance().recordException(e)
            throw e
        }
    }

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class EventsIndex

    @Provides
    @Singleton
    @EventsIndex
    fun provideAlgoliaIndexEvents(client: ClientSearch, context: Context): Index {
        return client.initIndex(
            indexName = IndexName(context.getString(R.string.algolia_index_events))
        )
    }

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class CommunitiesIndex

    @Provides
    @Singleton
    @CommunitiesIndex
    fun provideAlgoliaIndexCommunities(client: ClientSearch, context: Context): Index {
        return client.initIndex(
            indexName = IndexName(context.getString(R.string.algolia_index_communities))
        )
    }

    @Provides
    @Singleton
    fun provideStartDestinationProvider(): StartDestinationProvider {
        return StartDestinationProviderImpl()
    }

    @Provides
    @Singleton
    fun providePurchasesUpdatedListener(): PurchasesUpdatedListenerImpl {
        return PurchasesUpdatedListenerImpl()
    }

    @Provides
    @Singleton
    fun provideBillingClient(
        context: Context,
        purchasesUpdatedListener: PurchasesUpdatedListenerImpl
    ): BillingClient {
        val purchasesParams = PendingPurchasesParams.newBuilder()
            .enableOneTimeProducts()
            .build()

        return BillingClient.newBuilder(context)
            .enablePendingPurchases(purchasesParams)
            .setListener(purchasesUpdatedListener)
            .build()
    }

    @Provides
    @Singleton
    fun provideAdRequest(): AdRequest {
        return AdRequest.Builder().build()
    }

}