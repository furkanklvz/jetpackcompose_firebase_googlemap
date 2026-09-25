package com.klavs.bindle.data.datasource.algolia

import android.util.Log
import com.algolia.instantsearch.filter.state.filters
import com.algolia.search.client.Index
import com.algolia.search.model.filter.NumericOperator
import com.algolia.search.model.response.ResponseSearch
import com.algolia.search.model.search.Query
import com.google.firebase.Timestamp
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.klavs.bindle.R
import com.klavs.bindle.di.AppModule.EventsIndex
import com.klavs.bindle.di.AppModule.CommunitiesIndex
import com.klavs.bindle.resource.Resource
import javax.inject.Inject

class AlgoliaDataSourceImpl @Inject constructor(
    @EventsIndex private val indexEvents: Index,
    @CommunitiesIndex private val indexCommunities: Index
) : AlgoliaDataSource {
    override suspend fun searchEvent(
        searchQuery: String,
        resultSize: Long
    ): Resource<List<ResponseSearch.Hit>> {
        return try {
            val query = Query(searchQuery).apply {
                hitsPerPage = resultSize.toInt()
                filters = "date > ${Timestamp.now().toDate().time} AND privateEvent:false"
            }
            val response = indexEvents.search(query)
            val hits = response.hits
            Resource.Success(data = hits)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("error from datasource", e.message ?: "unknown error")
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }

    override suspend fun searchCommunity(
        searchQuery: String,
        resultSize: Long
    ): Resource<List<ResponseSearch.Hit>> {
        return try {
            val query = Query(searchQuery).apply {
                hitsPerPage = resultSize.toInt()
            }

            val response = indexCommunities.search(query)

            val hits = response.hits
            Resource.Success(data = hits)
        } catch (e: Exception) {
            FirebaseCrashlytics.getInstance().recordException(e)
            Log.e("error from datasource", e.message ?: "unknown error")
            Resource.Error(messageResource = R.string.something_went_wrong)
        }
    }
}
