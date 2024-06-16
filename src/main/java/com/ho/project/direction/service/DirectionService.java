package com.ho.project.direction.service;

import com.ho.project.api.dto.DocumentDto;
import com.ho.project.api.dto.KakaoApiResponseDto;
import com.ho.project.api.service.KakaoCategorySearchService;
import com.ho.project.direction.entity.Direction;
import com.ho.project.direction.repository.DirectionRepository;
import com.ho.project.pharmacy.dto.PharmacyDto;
import com.ho.project.pharmacy.entity.Pharmacy;
import com.ho.project.pharmacy.service.PharmacySearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DirectionService {

    private static final String DIRECTION_BASE_URL = "https://map.kakao.com/link/map/";


    private static final int MAX_SEARCH_COUNT = 3; // 최대 검색 갯수
    private static final double RADIUS_KM = 10.0; // 반경 10km

    private final PharmacySearchService pharmacySearchService;
    private final DirectionRepository directionRepository;
    private final KakaoCategorySearchService kakaoCategorySearchService;
    private final Base62Service base62Service;

    @Transactional
    public List<Direction> saveAll(List<Direction> directionList){
        if(CollectionUtils.isEmpty(directionList)) return Collections.emptyList();

        return directionRepository.saveAll(directionList);

    }

    public String findDirectionUrlById(String encodeId){
        Long decodeId = base62Service.decodeDirectionId(encodeId);
        Direction direction = directionRepository.findById(decodeId).orElse(null);

        String params = String.join(",", direction.getTargetPharmacyName(), String.valueOf(direction.getTargetLatitude()), String.valueOf(direction.getTargetLongitude()));

        String result = UriComponentsBuilder.fromHttpUrl(DIRECTION_BASE_URL + params).toUriString();

        return result;
    }

    public List<Direction> buildDirectionList(DocumentDto documentDto){

        if(Objects.isNull(documentDto)) return Collections.emptyList();

        // 약국 데이터 조회

        return pharmacySearchService.searchPharmacyDtoList()
                .stream().map(pharmacyDto ->
                    Direction.builder()
                            .inputAddress(documentDto.getAddressName())
                            .inputLatitude(documentDto.getLatitude())
                            .inputLongitude(documentDto.getLongitude())

                            .targetPharmacyName(pharmacyDto.getPharmacyName())
                            .targetAddress(pharmacyDto.getPharmacyAddress())
                            .targetLatitude(pharmacyDto.getLatitude())
                            .targetLongitude(pharmacyDto.getLongitude())

                            .distance(
                                    calculateDistance(documentDto.getLatitude(), documentDto.getLongitude(), pharmacyDto.getLatitude(), pharmacyDto.getLongitude())
                            )
                            .build())
                .filter(direction -> direction.getDistance() <= RADIUS_KM)
                .sorted(Comparator.comparing(Direction::getDistance))
                .limit(MAX_SEARCH_COUNT)
                .collect(Collectors.toList());
        // 거리계산 알고리즘을 이용하여, 고객과 약국 사이의 거리를 계산하고 sort

    }

    // pharmacy search by category kakao api
    public List<Direction> buildDirectionListByCategoryApi(DocumentDto inputDocumentDto) {

        log.info("[DirectionService buildDirectionListByCategoryApi 조회된 결과가 존재하지 않아, 카카오 재검색]");
        if(Objects.isNull(inputDocumentDto)) return Collections.emptyList();

        KakaoApiResponseDto responseDto = kakaoCategorySearchService.requestPharmacyCategorySearch(inputDocumentDto.getLatitude(), inputDocumentDto.getLongitude(), RADIUS_KM);

        return responseDto.getDocumentList()
                .stream().map(resultDocumentDto ->
                        Direction.builder()
                                .inputAddress(inputDocumentDto.getAddressName())
                                .inputLatitude(inputDocumentDto.getLatitude())
                                .inputLongitude(inputDocumentDto.getLongitude())
                                .targetPharmacyName(resultDocumentDto.getPlaceName())
                                .targetAddress(resultDocumentDto.getAddressName())
                                .targetLatitude(resultDocumentDto.getLatitude())
                                .targetLongitude(resultDocumentDto.getLongitude())
                                .distance(resultDocumentDto.getDistance() * 0.001) // km 단위
                                .build())
                .limit(MAX_SEARCH_COUNT)
                .collect(Collectors.toList());
    }

    // Haversine formula
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        lat1 = Math.toRadians(lat1);
        lon1 = Math.toRadians(lon1);
        lat2 = Math.toRadians(lat2);
        lon2 = Math.toRadians(lon2);

        double earthRadius = 6371; //Kilometers
        return earthRadius * Math.acos(Math.sin(lat1) * Math.sin(lat2) + Math.cos(lat1) * Math.cos(lat2) * Math.cos(lon1 - lon2));
    }
}
