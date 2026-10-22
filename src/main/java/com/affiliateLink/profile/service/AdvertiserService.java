package com.affiliateLink.profile.service;

import com.affiliateLink.profile.dto.request.CreateAdvertiserRequest;
import com.affiliateLink.profile.dto.response.AdvertiserResponse;

import java.util.List;

/**
 * Contrat de service pour la gestion des Advertisers (annonceurs).
 */
public interface AdvertiserService {

    AdvertiserResponse createAdvertiser(CreateAdvertiserRequest request);

    AdvertiserResponse getAdvertiserById(Long id);

    List<AdvertiserResponse> getAllAdvertisers();
}
