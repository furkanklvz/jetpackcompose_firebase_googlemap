package com.klavs.bindle.resource

sealed class RewardedAdResource(val rewardAmount: Int? = null, val rewardType: String? = null) {
    class onAdClicked : RewardedAdResource()
    class onAdDismissedFullScreenContent : RewardedAdResource()
    class onAdFailedToShowFullScreenContent : RewardedAdResource()
    class onAdImpression : RewardedAdResource()
    class onAdShowedFullScreenContent : RewardedAdResource()
    class OnUserEarnedReward(rewardAmount: Int, rewardType: String) : RewardedAdResource(rewardAmount,rewardType)
}