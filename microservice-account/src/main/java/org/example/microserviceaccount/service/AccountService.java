package org.example.microserviceaccount.service;

import jakarta.validation.Valid;
import org.example.microserviceaccount.domain.AccountDomainService;
import org.example.microserviceaccount.dto.AccountCreateDTO;
import org.example.microserviceaccount.dto.AccountResponseDTO;
import org.example.microserviceaccount.dto.LoginRequestDTO;
import org.example.microserviceaccount.entity.Account;
import org.example.microserviceaccount.exception.ResourceNotFoundException;
import org.example.microserviceaccount.mapper.AccountMapper;
import org.example.microserviceaccount.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.microserviceaccount.client.TransactionClient;
import org.example.microserviceaccount.dto.AccountSummaryDTO;
import org.example.microserviceaccount.dto.external.TransactionDTO;
import java.math.BigDecimal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviciul principal pentru gestionarea logicii de business a conturilor.
 * Se ocupa de operatii CRUD, autentificare, si comunicare cu alte microservicii.
 *
 * @author [Vicas Antonio]
 */
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;
    private final PasswordEncoder passwordEncoder;
    private final AccountDomainService accountDomainService;
    private final TransactionClient transactionClient;

    @Autowired
    public AccountService(
            AccountRepository accountRepository,
            AccountMapper accountMapper,
            PasswordEncoder passwordEncoder,
            AccountDomainService accountDomainService,
            TransactionClient transactionClient) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
        this.passwordEncoder = passwordEncoder;
        this.accountDomainService = accountDomainService;
        this.transactionClient = transactionClient;
    }

    /// CRUD
    /**
     * Creeaza un cont nou in baza de date dupa validarea unicitatii email-ului si a username-ului.
     * Parola este criptata inainte de salvare.
     *
     * @param createDTO Obiectul DTO care contine datele necesare pentru crearea contului.
     * @return AccountResponseDTO Detaliile contului creat.
     * @throws IllegalArgumentException Daca email-ul sau username-ul exista deja.
     * @author [Vicas Antonio]
     */
    @Transactional
    public AccountResponseDTO createAccount(AccountCreateDTO createDTO) {
        // verif mail si username
        accountRepository.findByEmail(createDTO.getEmail()).ifPresent(account -> {
            throw new IllegalArgumentException("Email " + createDTO.getEmail() + " already in use");
        });

        accountRepository.findByUserName(createDTO.getUserName()).ifPresent(account -> {
            throw new IllegalArgumentException("Username " + createDTO.getUserName() + " already in use");
        });

        Account newAccount = accountMapper.accountCreateDTOToAccount(createDTO);

        newAccount.setPassword(passwordEncoder.encode(newAccount.getPassword()));

        Account savedAccount = accountRepository.save(newAccount);
        return accountMapper.accountToAccountResponseDTO(savedAccount);
    }

    /**
     * Cauta un cont dupa ID.
     *
     * @param id Identificatorul unic al contului.
     * @return AccountResponseDTO Detaliile contului gasit.
     * @throws ResourceNotFoundException Daca nu exista niciun cont cu ID-ul specificat.
     * @author [Vicas Antonio]
     */
    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountById(Long id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " not found"));
        return accountMapper.accountToAccountResponseDTO(account);
    }

    /**
     * Returneaza o lista cu toate conturile existente, ordonate crescator dupa ID.
     *
     * @return O lista de obiecte AccountResponseDTO.
     * @author [Vicas Antonio]
     */
    @Transactional(readOnly = true)
    public List<AccountResponseDTO> getAllAccounts() {
        List<Account> accounts = accountRepository.findAll(Sort.by(Sort.Direction.ASC, "id"));
        return accountMapper.accountsToAccountResponseDTOs(accounts);
    }

    /**
     * Actualizeaza informatiile unui cont existent (Username si Email).
     * Verifica daca noile valori nu sunt deja utilizate de alte conturi.
     *
     * @param id ID-ul contului de actualizat.
     * @param updateDTO Noile date ale contului.
     * @return AccountResponseDTO Contul actualizat.
     * @throws ResourceNotFoundException Daca contul nu este gasit.
     * @throws IllegalArgumentException Daca email-ul sau username-ul sunt deja folosite de alt cont.
     * @author [Vicas Antonio]
     */
    @Transactional
    public AccountResponseDTO updateAccount(Long id, AccountCreateDTO updateDTO) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " not found"));

        accountRepository.findByEmail(updateDTO.getEmail()).ifPresent(account -> {
            if (!account.getId().equals(id)) {
                throw new IllegalArgumentException("Email " + updateDTO.getEmail() + " already in use");
            }
        });

        accountRepository.findByUserName(updateDTO.getUserName()).ifPresent(account -> {
            if (!account.getId().equals(id)) {
                throw new IllegalArgumentException("Username " + updateDTO.getUserName() + " already in use");
            }
        });

        existingAccount.setUserName(updateDTO.getUserName());
        existingAccount.setEmail(updateDTO.getEmail());

        Account updatedAccount = accountRepository.save(existingAccount);

        return accountMapper.accountToAccountResponseDTO(updatedAccount);
    }

    /**
     * Sterge un cont din baza de date pe baza ID-ului.
     *
     * @param id ID-ul contului de sters.
     * @throws ResourceNotFoundException Daca contul nu este gasit.
     * @author [Vicas Antonio]
     */
    @Transactional
    public void deleteAccount(Long id) {
        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + id + " not found"));
        accountRepository.delete(existingAccount);
    }

    /**
     * Cauta un cont pe baza adresei de email.
     *
     * @param email Adresa de email cautata.
     * @return AccountResponseDTO Detaliile contului.
     * @throws ResourceNotFoundException Daca nu exista cont cu acest email.
     * @author [Vicas Antonio]
     */
    @Transactional(readOnly = true)
    public AccountResponseDTO getAccountByEmail(String email) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Contul cu email-ul " + email + " nu a fost găsit."));

        return accountMapper.accountToAccountResponseDTO(account);
    }

    /**
     * Returneaza conturile sortate dupa data crearii.
     *
     * @param direction Directia sortarii ("asc" sau "desc").
     * @return Lista de conturi sortata.
     * @author [Vicas Antonio]
     */
    @Transactional(readOnly = true)
    public List<AccountResponseDTO> getAccountsSortedByCreationDate(String direction) {
        Sort.Direction sortDirection = "desc".equalsIgnoreCase(direction)
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sort = Sort.by(sortDirection, "createdAt");

        List<Account> accounts = accountRepository.findAll(sort);
        return accountMapper.accountsToAccountResponseDTOs(accounts);
    }

    /**
     * Cauta conturi care contin un anumit fragment de text in username (case-insensitive).
     *
     * @param usernameFragment Textul cautat in username.
     * @return Lista conturilor care se potrivesc criteriului.
     * @author [Vicas Antonio]
     */
    @Transactional(readOnly = true)
    public List<AccountResponseDTO> findAccountsByUsernameContaining(String usernameFragment) {
        List<Account> accounts = accountRepository.findByUserNameContainingIgnoreCase(usernameFragment);
        return accountMapper.accountsToAccountResponseDTOs(accounts);
    }

    /**
     * Autentifica un utilizator pe baza email-ului/username-ului si a parolei.
     *
     * @param loginDTO Datele de login (identifier si parola).
     * @return AccountResponseDTO Daca autentificarea are succes.
     * @throws ResourceNotFoundException Daca credentialele sunt invalide.
     * @author [Vicas Antonio]
     */
    @Transactional(readOnly = true)
    public AccountResponseDTO login(LoginRequestDTO loginDTO) {
        Account account = accountRepository.findByEmail(loginDTO.getLoginIdentifier())
                .or(() -> accountRepository.findByUserName(loginDTO.getLoginIdentifier()))
                .orElseThrow(() -> new ResourceNotFoundException("Invalid credentials"));

        if (passwordEncoder.matches(loginDTO.getPassword(), account.getPassword())) {
            return accountMapper.accountToAccountResponseDTO(account);
        } else {
            throw new ResourceNotFoundException("Invalid credentials");
        }
    }

    /**
     * Reseteaza parola unui utilizator.
     *
     * @param email Email-ul contului.
     * @param newPassword Noua parola (necriptata).
     * @return AccountResponseDTO Contul actualizat.
     * @throws ResourceNotFoundException Daca contul nu este gasit.
     * @author [Vicas Antonio]
     */
    @Transactional
    public AccountResponseDTO resetPassword(String email, String newPassword) {
        Account account = accountRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Account with email " + email + " not found"));

        account.setPassword(passwordEncoder.encode(newPassword));

        Account updatedAccount = accountRepository.save(account);
        return accountMapper.accountToAccountResponseDTO(updatedAccount);
    }

    /**
     * Numara utilizatorii noi creati intr-un interval de timp folosind o procedura stocata.
     *
     * @param startDate Data de inceput.
     * @param endDate Data de sfarsit.
     * @return Numarul de utilizatori noi.
     * @author [Vicas Antonio]
     */
    @Transactional(readOnly = true)
    public Integer countNewUsers(LocalDate startDate, LocalDate endDate) {
        // Domain validation for date range
        accountDomainService.validateDateRange(startDate, endDate);

        return accountRepository.countNewUsers(startDate, endDate);
    }

    /**
     * Anonimizeaza datele unui utilizator (GDPR request) folosind o procedura stocata.
     *
     * @param userId ID-ul utilizatorului.
     * @return Mesaj de confirmare din baza de date.
     * @author [Vicas Antonio]
     */
    @Transactional
    public String anonymizeUserData(Long userId) {
        // Domain validation for anonymization
        accountDomainService.validateAnonymizationRequest(userId);

        return accountRepository.anonymizeUserData(userId);
    }

    /**
     * Verifica disponibilitatea unui cont (email si username) folosind o procedura stocata.
     *
     * @param email Email-ul de verificat.
     * @param username Username-ul de verificat.
     * @return Mesaj care indica disponibilitatea.
     * @author [Vicas Antonio]
     */
    @Transactional(readOnly = true)
    public String checkAccountAvailability(String email, String username) {
        // Domain validation
        accountDomainService.validateEmail(email);
        accountDomainService.validateUsername(username);

        return accountRepository.checkAccountAvailability(email, username);
    }

    // viza 3, GET

    /**
     * (Viza 3) Calculeaza balanta totala a contului interogand microserviciul de Tranzactii.
     * Aceasta metoda agrega date din surse externe.
     *
     * @param accountId ID-ul contului pentru care se face sumarul.
     * @return AccountSummaryDTO Un obiect complex ce contine datele contului si tranzactiile recente.
     * @throws ResourceNotFoundException Daca contul nu este gasit.
     * @author [Vicas Antonio]
     */
    @Transactional(readOnly = true)
    public AccountSummaryDTO getAccountSummary(Long accountId) {
        // account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + accountId + " not found"));

        // apelare Transactions
        // ID-ul contului din acest serviciu corespunde userId-ului din Transactions
        List<TransactionDTO> transactions = transactionClient.getTransactionsByUserId(accountId);

        // procesare
        double totalBalance = transactions.stream()
                .filter(t -> t.getAmount() != null && t.getType() != null) // Siguranță împotriva null
                .mapToDouble(t -> {
                    if ("INCOME".equalsIgnoreCase(t.getType())) {
                        return t.getAmount().doubleValue();
                    } else {
                        return -t.getAmount().doubleValue();
                    }
                })
                .sum();

        AccountSummaryDTO summary = new AccountSummaryDTO();
        summary.setAccountId(account.getId());
        summary.setUserName(account.getUserName());
        summary.setEmail(account.getEmail());
        summary.setRecentTransactions(transactions);
        summary.setTotalBalanceCalculated(totalBalance);

        return summary;
    }

    /**
     * (Viza 3) Verifica tranzactiile utilizatorului si il promoveaza la VIP daca veniturile depasesc un prag.
     * Aceasta metoda demonstreaza logica de business bazata pe date din alt microserviciu.
     *
     * @param accountId ID-ul contului de verificat.
     * @return AccountResponseDTO Contul, posibil actualizat cu noul nume (VIP).
     * @throws ResourceNotFoundException Daca contul nu este gasit.
     * @author [Vicas Antonio]
     */
    @Transactional
    public AccountResponseDTO checkAndPromoteToVip(Long accountId) {
        // account
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account with id " + accountId + " not found"));

        // tranzactiile din microserviciul extern
        List<TransactionDTO> transactions = transactionClient.getTransactionsByUserId(accountId);

        // calcul INCOME
        double totalIncome = transactions.stream()
                .filter(t -> "INCOME".equalsIgnoreCase(t.getType()))
                .mapToDouble(t -> t.getAmount().doubleValue())
                .sum();

        // Daca are peste 1000, devine VIP
        if (totalIncome > 1000) {
            String currentName = account.getUserName();
            if (!currentName.endsWith(" (VIP)")) {
                account.setUserName(currentName + " (VIP)");
                account = accountRepository.save(account);
            }
        }

        return accountMapper.accountToAccountResponseDTO(account);
    }

    @Transactional
    public Long syncUserByEmail(String email) {
        return accountRepository.findByEmail(email)
                .map(Account::getId)
                .orElseGet(() -> {
                    return createNewAccount(email);
                });
    }

    private Long createNewAccount(String email) {
        Account newAccount = new Account();
        newAccount.setEmail(email);
        newAccount.setUserName(extractNameFromEmail(email));
        newAccount.setRole("ROLE_USER");
        newAccount.setCreatedAt(LocalDate.from(LocalDateTime.now()));
        newAccount.setPassword("GOOGLE_OAUTH_ACCOUNT");

        Account saved = accountRepository.save(newAccount);
        return saved.getId();
    }

    private String extractNameFromEmail(String email) {
        if (email.contains("@")) {
            return email.substring(0, email.indexOf("@"));
        }
        return "Unknown User";
    }
}