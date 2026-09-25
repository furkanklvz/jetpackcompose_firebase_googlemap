package com.klavs.bindle.uix.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.klavs.bindle.R
import com.klavs.bindle.data.entity.FeedbackMessage
import com.klavs.bindle.data.repo.firestore.FirestoreRepository
import com.klavs.bindle.resource.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class SupportAndFeedbackViewmodel @Inject constructor(
    private val firestoreRepo: FirestoreRepository,
    private val db: FirebaseFirestore
) : ViewModel() {

    private val _sendingResource = MutableStateFlow<Resource<FeedbackMessage>>(Resource.Idle())
    val sendingResource = _sendingResource.asStateFlow()

    fun resetSendingResource(){
        _sendingResource.value = Resource.Idle()
    }

    fun sendFeedback(feedbackMessage: FeedbackMessage) {
        _sendingResource.value = Resource.Loading()
        viewModelScope.launch(Dispatchers.Main) {
            val result = firestoreRepo.addDocument(
                collectionRef = db.collection("feedbacksAndSupportMessages"),
                data = feedbackMessage
            )
            if (result is Resource.Success){
                _sendingResource.value = Resource.Success(data = feedbackMessage.copy(id = result.data?:""))
            }else{
                _sendingResource.value = Resource.Error(messageResource = R.string.feedback_send_error)
            }
        }
    }


}