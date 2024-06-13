package com.ho.project.pharmacy.service;

import com.ho.project.pharmacy.entity.Pharmacy;
import com.ho.project.pharmacy.repository.PharmacyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PharmacyService {

    private final PharmacyRepository pharmacyRepository;

    @Transactional
    public void updateAddress(Long id, String address){
        Pharmacy entity = pharmacyRepository.findById(id).orElse(null);

        if(Objects.isNull(entity)){
            log.error("[PharmacyService updateAddress] not found id : {}", id);
            return ;
        }

        entity.changePharmacyAddress(address);
    }


    //for test
    public void updateAddressWithoutTransaction(Long id, String address){
        Pharmacy entity = pharmacyRepository.findById(id).orElse(null);

        if(Objects.isNull(entity)){
            log.error("[PharmacyService updateAddressWithoutTransaction] not found id : {}", id);
            return ;
        }

        entity.changePharmacyAddress(address);
    }
}
