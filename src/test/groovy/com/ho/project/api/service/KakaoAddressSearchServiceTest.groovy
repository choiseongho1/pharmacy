package com.ho.project.api.service

import com.ho.project.AbstractIntegrationContainerBaseTest
import org.springframework.beans.factory.annotation.Autowired
import spock.lang.Specification


class KakaoAddressSearchServiceTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private KakaoAddressSearchService kakaoAddressSearchService

    def "address 파라미터 값이 null인경우, requestAddressSearch 메소드는 null 리턴"(){
        given :
        String address = null

        when :
        def result = kakaoAddressSearchService.requestAddressSearch(address)

        then :
        result == null
    }

    def "주소값이 valid하면, requestAddressSearch 메소드는 정상적으로 document를 변환"(){
        given :
        def address = "서울 성북구 종암로 10길"

        when :
        def result = kakaoAddressSearchService.requestAddressSearch(address)

        then :
        result.documentDtoList.size() > 0
        result.metaDto.totalCount > 0
        result.documentDtoList.get(0).addressName != null
    }

}