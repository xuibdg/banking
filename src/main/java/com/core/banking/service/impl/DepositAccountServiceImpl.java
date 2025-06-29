        package com.core.banking.service.impl;
        
        import com.core.banking.config.CurrentUser;
        import com.core.banking.dto.DepositAccountRequest;
        import com.core.banking.dto.DepositAccountResponse;
        import com.core.banking.dto.EscrowAccountRequest;
        import com.core.banking.dto.UserMetaData;
        import com.core.banking.entity.*;
        import com.core.banking.enums.*;
        import com.core.banking.repository.*;
        import com.core.banking.service.DepositAccountService;
        import com.core.banking.service.EscrowAccountDetailService;
        import com.core.banking.utils.BilyetNumberGenerator;
        import com.core.banking.utils.DepositAccountNumberGenerator;
        import com.core.banking.utils.exception.BusinessException;
        import com.core.banking.utils.exception.GlobalErrorMapping;
        import org.springframework.beans.factory.annotation.Autowired;
        import org.springframework.http.HttpStatus;
        import org.springframework.stereotype.Service;
        import org.springframework.transaction.annotation.Transactional;
        import org.springframework.transaction.annotation.Propagation;
        
        import java.math.BigDecimal;
        import java.math.RoundingMode;
        import java.time.LocalDate;
        import java.time.LocalDateTime;
        import java.util.HashMap;
        import java.util.List;
        import java.util.Map;
        import java.util.stream.Collectors;
        
        @Service
        public class DepositAccountServiceImpl implements DepositAccountService {
            @Autowired
            DepositAccountRepository depositAccountRepository;
        
            @Autowired
            DepositAccountDetailRepository depositAccountDetailRepository;
        
            @Autowired
            DepositTypeConfigRepository depositTypeConfigRepository;
        
            @Autowired
            CustomerRepository customerRepository;
        
            @Autowired
            DepositAccountNumberGenerator depositAccountNumberGenerator;
        
            @Autowired
            SavingAccountRepository savingAccountRepository;
        
            @Autowired
            EscrowAccountRepository escrowAccountRepository;
        
            @Autowired
            EscrowAccountDetailService escrowAccountDetailService;
        
            @Autowired
            DepositMaturityServiceImpl depositMaturityServiceImpl;
        
            @Autowired
            BilyetNumberGenerator bilyetNumberGenerator;
    
            @Autowired
            EscrowAccountServiceImpl escrowAccountServiceImpl;

            @Override
            public List<DepositAccount> findAll() {
                return depositAccountRepository.findAll();
            }

            @Override
            @Transactional(propagation = Propagation.REQUIRED)
            public DepositAccountResponse createDepositAccount(DepositAccountRequest depositAccountRequest, UserMetaData userMetaData) {
                // Ambil savingAccountId dari request body
                String savingAccountId = depositAccountRequest.getSavingAccountId();

                Customer customer = customerRepository.findById(depositAccountRequest.getCustomerId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_NOT_FOUND));

                if (customer.getCustomerStatus() != CustomerStatus.ACTIVE) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_NOT_ACTIVE);
                }

                SavingAccount savingAccount = savingAccountRepository.findById(savingAccountId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.SAVING_ACCOUNT_NOT_FOUND));

                if (savingAccount.getAccountStatus() != SavingAccountStatus.ACTIVE) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, "Status saving account non-active."); //GlobalErrorMapping.SAVING_ACCOUNT_NOT_ACTIVE
                }

                DepositTypeConfig depositTypeConfig = depositTypeConfigRepository.findById(depositAccountRequest.getDepositTypeConfigId())
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_TYPE_CONFIG_NOT_FOUND));

                if (!depositTypeConfig.getIsActive()) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_TYPE_CONFIG_NOT_ACTIVE);
                }

                if (depositAccountRequest.getNominalDeposit().compareTo(depositTypeConfig.getMinDepositAmount()) < 0) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_AMOUNT_BELOW_MINIMUM);
                }

                LocalDate maturityDate = LocalDate.now().plusMonths(depositTypeConfig.getTermInMonths());

                String accountNumber = depositAccountNumberGenerator.generateDepositAccountNumber();

                DepositAccount depositAccount = DepositAccount.builder()
                        .accountNumber(accountNumber)
                        .customer(customer)
                        .depositTypeConfig(depositTypeConfig)
                        .principalAmount(depositAccountRequest.getNominalDeposit())
                        .maturityDate(maturityDate)
                        .createdBy(userMetaData.getUserId())
                        .accountStatus(DepositAccountStatus.ACTIVE)
                        .rolloverOption(RolloverOption.valueOf(depositAccountRequest.getRolloverOption()))
                        .openedAt(LocalDateTime.now())
                        .createdBy(userMetaData.getUserId())
                        .build();

                DepositAccount savedAccount = depositAccountRepository.save(depositAccount);

                DepositAccountDetail depositAccountDetail = DepositAccountDetail.builder()
                        .depositAccount(savedAccount)
                        .transactionType(DepositoTransactionType.INITIAL_DEPOSIT)
                        .mutationType(MutationType.CREDIT)
                        .nominalTransaction(depositAccountRequest.getNominalDeposit())
                        .beginBalance(BigDecimal.ZERO)
                        .endBalance(depositAccountRequest.getNominalDeposit())
                        .createdBy(userMetaData.getUserId())
                        .description("Setoran awal deposito")
                        .transactionAt(LocalDateTime.now())
                        .createdBy(userMetaData.getUserId())
                        .build();

                depositAccountDetailRepository.save(depositAccountDetail);

                EscrowAccountRequest escrowRequest = new EscrowAccountRequest();
                escrowRequest.setPayerCustomer(savingAccount.getCustomer().getId());
                escrowRequest.setBeneficiaryCustomer(customer.getId());
                escrowRequest.setTransactionTypeStatus(TransactionTypeStatus.DEPOSIT_PAYMENT);
                escrowRequest.setDepositAccount(savedAccount.getDepositoAccountId());
                escrowRequest.setPurpose("Pembukaan Deposito");
                escrowRequest.setSenderBank("BNI");

                String transactionReference = escrowAccountDetailService.createAndReleaseEscrowAccount(escrowRequest, depositAccountRequest.getNominalDeposit(), savingAccountId, "Pembukaan Deposito " + savedAccount.getAccountNumber(), userMetaData);

                DepositAccountResponse depositAccountResponse = new DepositAccountResponse();
                depositAccountResponse.setCustomerName(customer.getFullName());

                if (depositTypeConfig.getDepositType() != null) {
                    depositAccountResponse.setDepositTypeName(depositTypeConfig.getDepositType().getTypeName());
                }
                depositAccountResponse.setProfitSharePercentage(depositTypeConfig.getProfitSharePercentagePa());
                depositAccountResponse.setTermInMonths(depositTypeConfig.getTermInMonths());
                depositAccountResponse.setDepositoAccountId(savedAccount.getDepositoAccountId());
                depositAccountResponse.setAccountNumber(savedAccount.getAccountNumber());
                depositAccountResponse.setCustomerId(savedAccount.getCustomer().getId());
                depositAccountResponse.setPrincipalAmount(savedAccount.getPrincipalAmount());
                depositAccountResponse.setMaturityDate(savedAccount.getMaturityDate());
                depositAccountResponse.setAccountStatus(savedAccount.getAccountStatus().name());
                depositAccountResponse.setRolloverOption(savedAccount.getRolloverOption().name());
                depositAccountResponse.setOpenedAt(savedAccount.getOpenedAt());
                depositAccountResponse.setCreatedAt(savedAccount.getCreatedAt());

                depositAccountDetail.setTransactionReference(transactionReference);
                depositAccountDetailRepository.save(depositAccountDetail);

                return depositAccountResponse;
            }
        
            @Override
            @Transactional(readOnly = true)
            public DepositAccountResponse getDepositAccountById(Long depositAccountId) {
                DepositAccount depositAccount = depositAccountRepository.findById(depositAccountId)
                        .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));
                DepositAccountResponse depositAccountResponse = new DepositAccountResponse();
                depositAccountResponse.setDepositoAccountId(depositAccount.getDepositoAccountId());
                depositAccountResponse.setAccountNumber(depositAccount.getAccountNumber());
                depositAccountResponse.setCustomerId(depositAccount.getCustomer().getId());
                depositAccountResponse.setPrincipalAmount(depositAccount.getPrincipalAmount());
                depositAccountResponse.setMaturityDate(depositAccount.getMaturityDate());
                depositAccountResponse.setAccountStatus(depositAccount.getAccountStatus().name());
                depositAccountResponse.setRolloverOption(depositAccount.getRolloverOption().name());
                depositAccountResponse.setOpenedAt(depositAccount.getOpenedAt());
                depositAccountResponse.setCreatedAt(depositAccount.getCreatedAt());
                depositAccountResponse.setCustomerId(depositAccount.getCustomer().getId());
                depositAccountResponse.setCustomerName(depositAccount.getCustomer().getFullName());
                depositAccountResponse.setDepositTypeName(depositAccount.getDepositTypeConfig().getDepositType().getTypeName());
                depositAccountResponse.setProfitSharePercentage(depositAccount.getDepositTypeConfig().getProfitSharePercentagePa());
                depositAccountResponse.setTermInMonths(depositAccount.getDepositTypeConfig().getTermInMonths());
                return depositAccountResponse;
            }
        
            @Override
            public List<DepositAccountResponse> getDepositAccountsByCustomerId(String customerId) {
                customerRepository.findById(customerId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.CUSTOMER_NOT_FOUND));
                List<DepositAccountResponse> list = depositAccountRepository.findAll().stream()
                        .filter(data -> data.getCustomer().getId().equals(customerId))
                        .map(depositAccount -> {
                            DepositAccountResponse depositAccountResponse = new DepositAccountResponse();
                            depositAccountResponse.setDepositoAccountId(depositAccount.getDepositoAccountId());
                            depositAccountResponse.setAccountNumber(depositAccount.getAccountNumber());
                            depositAccountResponse.setCustomerId(depositAccount.getCustomer().getId());
                            depositAccountResponse.setPrincipalAmount(depositAccount.getPrincipalAmount());
                            depositAccountResponse.setMaturityDate(depositAccount.getMaturityDate());
                            depositAccountResponse.setAccountStatus(String.valueOf(depositAccount.getAccountStatus()));
                            depositAccountResponse.setRolloverOption(depositAccount.getRolloverOption().name());
                            depositAccountResponse.setOpenedAt(depositAccount.getOpenedAt());
                            depositAccountResponse.setCreatedAt(depositAccount.getCreatedAt());
                            depositAccountResponse.setCustomerName(depositAccount.getCustomer().getFullName());
                            depositAccountResponse.setDepositTypeName(depositAccount.getDepositTypeConfig().getDepositType().getTypeName());
                            depositAccountResponse.setProfitSharePercentage(depositAccount.getDepositTypeConfig().getProfitSharePercentagePa());
                            depositAccountResponse.setTermInMonths(depositAccount.getDepositTypeConfig().getTermInMonths());
                            return depositAccountResponse;
                        })
                        .collect(Collectors.toList());
                return list;
            }
        
            @Override
            public List<DepositAccountResponse> getDepositAccountsByStatus(DepositAccountStatus status) {
                List <DepositAccountResponse> list = depositAccountRepository.findByAccountStatus(status).stream()
                        .map(depositAccount -> {
                            DepositAccountResponse depositAccountResponse = new DepositAccountResponse();
                            depositAccountResponse.setDepositoAccountId(depositAccount.getDepositoAccountId());
                            depositAccountResponse.setAccountNumber(depositAccount.getAccountNumber());
                            depositAccountResponse.setCustomerId(depositAccount.getCustomer().getId());
                            depositAccountResponse.setPrincipalAmount(depositAccount.getPrincipalAmount());
                            depositAccountResponse.setMaturityDate(depositAccount.getMaturityDate());
                            depositAccountResponse.setAccountStatus(String.valueOf(depositAccount.getAccountStatus()));
                            depositAccountResponse.setRolloverOption(depositAccount.getRolloverOption().name());
                            depositAccountResponse.setOpenedAt(depositAccount.getOpenedAt());
                            depositAccountResponse.setCreatedAt(depositAccount.getCreatedAt());
                            depositAccountResponse.setCustomerName(depositAccount.getCustomer().getFullName());
                            depositAccountResponse.setDepositTypeName(depositAccount.getDepositTypeConfig().getDepositType().getTypeName());
                            depositAccountResponse.setProfitSharePercentage(depositAccount.getDepositTypeConfig().getProfitSharePercentagePa());
                            depositAccountResponse.setTermInMonths(depositAccount.getDepositTypeConfig().getTermInMonths());
                            return depositAccountResponse;
                        })
                        .collect(Collectors.toList());
                return list;
            }
        
            @Override
            public Map<String, Object> generateBilyet(Long depositAccountId, UserMetaData userMetaData) {
                DepositAccount depositAccount = depositAccountRepository.findById(depositAccountId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_FOUND));
        
                if (depositAccount.getAccountStatus() != DepositAccountStatus.ACTIVE) {
                    throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.DEPOSIT_ACCOUNT_NOT_ACTIVE);
                }
        
                String bilyetNumber = bilyetNumberGenerator.generateBilyetNumber(depositAccount);
        
                BigDecimal expectedProfit = calculateExpectedProfitForBilyet(depositAccount);
        
                Map<String, Object> bilyetData = new HashMap<>();
                bilyetData.put("bilyetNumber", bilyetNumber);
                bilyetData.put("accountNumber", depositAccount.getAccountNumber());
                bilyetData.put("customerName", depositAccount.getCustomer().getFullName());
                bilyetData.put("customerNik", depositAccount.getCustomer().getNik());
                bilyetData.put("principalAmount", depositAccount.getPrincipalAmount());
                bilyetData.put("profitRate", depositAccount.getDepositTypeConfig().getProfitSharePercentagePa());
                bilyetData.put("termInMonths", depositAccount.getDepositTypeConfig().getTermInMonths());
                bilyetData.put("depositTypeName", depositAccount.getDepositTypeConfig().getDepositType().getTypeName());
                bilyetData.put("openDate", depositAccount.getOpenedAt().toLocalDate());
                bilyetData.put("maturityDate", depositAccount.getMaturityDate());
                bilyetData.put("expectedProfit", expectedProfit);
                bilyetData.put("totalMaturityAmount", depositAccount.getPrincipalAmount().add(expectedProfit));
                bilyetData.put("rolloverOption", depositAccount.getRolloverOption().name());
                bilyetData.put("printDate", LocalDateTime.now());
                bilyetData.put("printedBy", userMetaData.getUserId());
        
                return bilyetData;
            }
        
            private BigDecimal calculateExpectedProfitForBilyet(DepositAccount depositAccount) {
                DepositTypeConfig config = depositAccount.getDepositTypeConfig();
                BigDecimal principal = depositAccount.getPrincipalAmount();
        
                BigDecimal annualRate = config.getProfitSharePercentagePa().divide(new BigDecimal("100"), 10, RoundingMode.HALF_UP);
                BigDecimal monthlyRate = annualRate.divide(new BigDecimal("12"), 10, RoundingMode.HALF_UP);
                BigDecimal termInMonths = new BigDecimal(config.getTermInMonths());
        
                BigDecimal expectedProfit = principal.multiply(monthlyRate).multiply(termInMonths).setScale(2, RoundingMode.HALF_UP);
        
                return expectedProfit;
            }
        }
        
        //    @Override
        //    public String deleteDepositAccount(Long depositoAccountId) {
        //        DepositAccount depositAccount = depositAccountRepository.findById(depositoAccountId).orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ERROR));
        //
        //        if (depositAccount.getAccountStatus() == DepositAccountStatus.ACTIVE) {
        //            throw new BusinessException(HttpStatus.BAD_REQUEST, GlobalErrorMapping.ERROR);
        //        }
        //
        //        depositAccountRepository.findByDepositoAccountId(depositoAccountId).map(data -> {
        //            data.setDeleted(true);
        //            depositAccountRepository.save(data);
        //            return data;
        //        });
        //        return "SUCCESS DELETE A DEPOSITO ACCOUNT";
        //    }
        
