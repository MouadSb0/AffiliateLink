package com.affiliateLink.profile.service;

import com.affiliateLink.profile.dto.request.CreatePublisherRequest;
import com.affiliateLink.profile.dto.response.PublisherResponse;

import java.util.List;

/**
 * Contrat de service pour la gestion des Publishers (éditeurs).
 */
public interface PublisherService {

    PublisherResponse createPublisher(CreatePublisherRequest request);

    PublisherResponse getPublisherById(Long id);

    List<PublisherResponse> getAllPublishers();
}
