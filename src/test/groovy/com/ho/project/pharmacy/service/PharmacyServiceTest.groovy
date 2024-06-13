package com.ho.project.pharmacy.service

import com.ho.project.AbstractIntegrationContainerBaseTest
import com.ho.project.pharmacy.entity.Pharmacy
import com.ho.project.pharmacy.repository.PharmacyRepository
import org.springframework.beans.factory.annotation.Autowired


class PharmacyServiceTest extends AbstractIntegrationContainerBaseTest {

    @Autowired
    private PharmacyService pharmacyService;

    @Autowired
    private PharmacyRepository pharmacyRepository;

    def setup(){
        pharmacyRepository.deleteAll()
    }

    def "PharmacyRepository update - dirty checking success"(){
        given:
        String inputAddress = "서울 특별시 성북구 종암동"
        String modifiedAddress = "서울 광진구 구의동"
        String name = "은혜 약국"

        def pharmacy = Pharmacy.builder()
                .pharmacyAddress(inputAddress)
                .pharmacyName(name)
                .build()

        when :
        def entity = pharmacyRepository.save(pharmacy)

        pharmacyService.updateAddress(entity.getId(), modifiedAddress)

        def result = pharmacyRepository.findAll()

        then :
        result.get(0).getPharmacyAddress() == modifiedAddress
        result.size() > 0
    }

    def "PharmacyRepository update - dirty checking fail"(){
        given:
        String inputAddress = "서울 특별시 성북구 종암동"
        String modifiedAddress = "서울 광진구 구의동"
        String name = "은혜 약국"

        def pharmacy = Pharmacy.builder()
                .pharmacyAddress(inputAddress)
                .pharmacyName(name)
                .build()

        when :
        def entity = pharmacyRepository.save(pharmacy)

        pharmacyService.updateAddressWithoutTransaction(entity.getId(), modifiedAddress)

        def result = pharmacyRepository.findAll()

        then :
        result.get(0).getPharmacyAddress() == inputAddress
    }

}