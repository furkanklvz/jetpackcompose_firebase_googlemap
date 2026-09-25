package com.klavs.bindle.data.repo.algolia

import com.algolia.search.model.response.ResponseSearch
import com.klavs.bindle.data.datasource.algolia.AlgoliaDataSource
import com.klavs.bindle.resource.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class AlgoliaRepositoryImpl @Inject constructor(val ds: AlgoliaDataSource) : AlgoliaRepository {
    override suspend fun searchEvent(
        searchQuery: String,
        resultSize: Long
    ): Resource<List<ResponseSearch.Hit>> =
        withContext(Dispatchers.IO){ds.searchEvent(
            searchQuery = searchQuery,
            resultSize = resultSize
        )}

    override suspend fun searchCommunity(
        searchQuery: String,
        resultSize: Long
    ): Resource<List<ResponseSearch.Hit>> =
        withContext(Dispatchers.IO){ds.searchCommunity(
            searchQuery = searchQuery,
            resultSize = resultSize
        )}
}