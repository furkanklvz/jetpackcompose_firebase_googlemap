package com.klavs.bindle.data.repo.algolia

import com.algolia.search.model.response.ResponseSearch
import com.klavs.bindle.resource.Resource

interface AlgoliaRepository {
    suspend fun searchEvent(searchQuery: String, resultSize:Long): Resource< List<ResponseSearch. Hit>>
    suspend fun searchCommunity(searchQuery: String, resultSize:Long): Resource< List<ResponseSearch. Hit>>
}