package com.ho.project.pharmacy.service;

import com.ho.project.api.dto.DocumentDto;
import com.ho.project.api.dto.KakaoApiResponseDto;
import com.ho.project.api.service.KakaoAddressSearchService;
import com.ho.project.direction.dto.OutputDto;
import com.ho.project.direction.entity.Direction;
import com.ho.project.direction.service.Base62Service;
import com.ho.project.direction.service.DirectionService;
import com.ho.project.pharmacy.cache.PharmacyRedisTemplateService;
import com.ho.project.pharmacy.dto.PharmacyDto;
import com.ho.project.pharmacy.entity.Pharmacy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;


import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyRecommendationService {

    private final KakaoAddressSearchService kakaoAddressSearchService;

    private final DirectionService directionService;

    private final Base62Service base62Service;

    private final PharmacyRedisTemplateService pharmacyRedisTemplateService;

    private final PharmacyRepositoryService pharmacyRepositoryService;

    @Value("${pharmacy.recommendation.base.url}")
    private String baseUrl;
    private static final String ROAD_VIEW_BASE_URL = "https://map.kakao.com/link/roadview/";


    public List<OutputDto> recommendPharmacyList(String address){

        KakaoApiResponseDto kakaoApiResponseDto = kakaoAddressSearchService.requestAddressSearch(address);

        if(Objects.isNull(kakaoApiResponseDto) || CollectionUtils.isEmpty(kakaoApiResponseDto.getDocumentList())) {
            log.error("[PharmacyRecommendationService recommendPharmacyList fail] Input address: {}", address);
            return Collections.emptyList();
        }

        DocumentDto documentDto = kakaoApiResponseDto.getDocumentList().get(0);

        List<Direction> directionList = directionService.buildDirectionList(documentDto);


        // redis, db에서 조회했을때 검색이 되지 않는 경우 카카오 api검색 이후, db, redis에 저장
        if(directionList.isEmpty()) {
            directionList = directionService.buildDirectionListByCategoryApi(documentDto);

            for(Direction direction : directionList){
                System.out.println(1);

                Pharmacy pharmacy = Pharmacy.builder()
                        .pharmacyName(direction.getTargetPharmacyName())
                        .pharmacyAddress(direction.getTargetAddress())
                        .latitude(direction.getTargetLatitude())
                        .longitude(direction.getTargetLongitude())
                        .build();

                Pharmacy savePharmacy = pharmacyRepositoryService.save(pharmacy);

                pharmacyRedisTemplateService.save(PharmacyDto.builder()
                        .id(savePharmacy.getId())
                        .pharmacyName(direction.getTargetPharmacyName())
                        .pharmacyAddress(direction.getTargetAddress())
                        .latitude(direction.getTargetLatitude())
                        .longitude(direction.getTargetLongitude())
                        .build());
            }


        }
//        List<Direction> directionList = directionService.buildDirectionListByCategoryApi(documentDto);



        return directionService.saveAll(directionList)
                .stream()
                .map(this::convertToOutputDto)
                .collect(Collectors.toList());
    }

    private OutputDto convertToOutputDto(Direction direction) {



        return OutputDto.builder()
                .pharmacyName(direction.getTargetPharmacyName())
                .pharmacyAddress(direction.getTargetAddress())
                .directionUrl(baseUrl + base62Service.encodeDirectionId(direction.getId()))
                .roadViewUrl(ROAD_VIEW_BASE_URL + direction.getTargetLatitude() + "," + direction.getTargetLongitude())
                .distance(String.format("%.2f km", direction.getDistance()))
                .build();
    }
}
