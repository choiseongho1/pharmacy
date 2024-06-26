package com.ho.project.api.service;

import com.ho.project.api.dto.KakaoApiResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Service
@Slf4j
@RequiredArgsConstructor
public class KakaoAddressSearchService {

    private final RestTemplate restTemplate;

    private final KakaoUriBuilderService kakaoUriBuilderService;

    @Value("${kakao.rest.api.key}")
    private String kakaoRestApiKey;


    @Retryable(
            value = {RuntimeException.class},
            maxAttempts = 2,
            backoff =  @Backoff(delay = 2000)
    )
    public KakaoApiResponseDto requestAddressSearch(String address){
        // null check
        if(ObjectUtils.isEmpty(address)) return null;

        // kakao Api 호출
        URI uri = kakaoUriBuilderService.buildUriByAddressSearch(address);

        // Header 값 Set
        HttpHeaders headers = new HttpHeaders();

        log.info(kakaoRestApiKey);
        headers.set(HttpHeaders.AUTHORIZATION, "KakaoAK " + kakaoRestApiKey);

        HttpEntity httpEntity = new HttpEntity<>(headers);

        return restTemplate.exchange(uri, HttpMethod.GET, httpEntity, KakaoApiResponseDto.class).getBody();
    }

    @Recover
    public KakaoApiResponseDto recover(RuntimeException e, String address){
        log.error("All the retries failed. address : {}, error : {}", address, e.getMessage());
        return null;
    }
}
