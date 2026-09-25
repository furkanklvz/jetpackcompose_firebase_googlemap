package com.klavs.bindle.data.datasource.algolia

import com.algolia.search.model.response.ResponseSearch
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.klavs.bindle.resource.Resource

interface AlgoliaDataSource {
    suspend fun searchEvent(searchQuery: String, resultSize:Long): Resource< List<ResponseSearch. Hit>>
    suspend fun searchCommunity(searchQuery: String, resultSize:Long): Resource< List<ResponseSearch. Hit>>
}