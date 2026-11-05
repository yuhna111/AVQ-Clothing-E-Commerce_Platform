package vn.avq.shop.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.avq.shop.common.BusinessException;
import vn.avq.shop.domain.*;
import vn.avq.shop.dto.AddressDto;
import vn.avq.shop.repository.CustomerRepository;
import java.util.List;

@Service
public class AccountService {
    private final CustomerRepository customerRepo;
    public AccountService(CustomerRepository customerRepo) { this.customerRepo = customerRepo; }

    @Transactional(readOnly = true)
    public List<AddressDto> addresses(Long customerId) {
        return customerRepo.findById(customerId).orElseThrow().getAddresses().stream()
                .map(a -> new AddressDto(a.getId(), a.getReceiverName(), a.getReceiverPhone(), a.getAddressLine())).toList();
    }

    @Transactional
    public List<AddressDto> addAddress(Long customerId, AddressDto d) {
        if (d.receiverName() == null || d.receiverName().isBlank() || d.receiverPhone() == null || d.receiverPhone().isBlank()
                || d.addressLine() == null || d.addressLine().isBlank())
            throw new BusinessException("VALIDATION_ERROR", "Vui lòng nhập đủ tên, số điện thoại, địa chỉ");
        Customer c = customerRepo.findById(customerId).orElseThrow();
        c.addAddress(new Address(d.receiverName().trim(), d.receiverPhone().trim(), d.addressLine().trim()));
        customerRepo.save(c);
        return addresses(customerId);
    }
}
