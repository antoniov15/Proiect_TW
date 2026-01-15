package com.financeassistant.transaction.service;

import com.financeassistant.transaction.client.AIFeignClient;
import com.financeassistant.transaction.client.AccountFeignClient;
import com.financeassistant.transaction.domain.TransactionDomainService;
import com.financeassistant.transaction.dto.CreateTransactionDTO;
import com.financeassistant.transaction.dto.SmartTransactionDTO;
import com.financeassistant.transaction.dto.TransactionViewDTO;
import com.financeassistant.transaction.dto.UpdateTransactionDTO;
import com.financeassistant.transaction.entity.Category;
import com.financeassistant.transaction.entity.Transaction;
import com.financeassistant.transaction.entity.TransactionType;
import com.financeassistant.transaction.mapper.TransactionMapper;
import com.financeassistant.transaction.repository.CategoryRepository;
import com.financeassistant.transaction.repository.TransactionRepository;
import com.financeassistant.transaction.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Slf4j
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final TransactionMapper transactionMapper;
    private final TransactionDomainService transactionDomainService;
    private final AccountFeignClient accountClient;
    private final AIFeignClient aiClient;

    /**
     * Constructorul clasei TransactionService.
     * Initializeaza dependintele necesare pentru accesul la baza de date (Repositories),
     * maparea entitatilor (Mapper) si comunicarea cu alte microservicii (Feign Clients).
     *
     * @param transactionRepository Repository pentru operatiuni CRUD pe tranzactii.
     * @param categoryRepository Repository pentru gestionarea categoriilor.
     * @param transactionMapper Mapper pentru conversia intre Entity si DTO.
     * @param accountClient Client Feign pentru comunicarea cu microserviciul de Conturi.
     * @param aiClient Client Feign pentru comunicarea cu microserviciul de Inteligenta Artificiala.
     * @author Laurentiu
     */
    @Autowired
    public TransactionService(TransactionRepository transactionRepository,
                              CategoryRepository categoryRepository,
                              TransactionMapper transactionMapper,
                              AccountFeignClient accountClient,
                              AIFeignClient aiClient) {

        this.transactionRepository = transactionRepository;
        this.categoryRepository = categoryRepository;
        this.transactionMapper = transactionMapper;
        this.transactionDomainService = new TransactionDomainService();
        this.accountClient = accountClient;
        this.aiClient = aiClient;
    }

    /**
     * Genereaza un raport global pentru administrator care agrega cheltuielile tuturor utilizatorilor.
     * Aceasta metoda comunica cu microserviciul Account pentru a obtine detaliile utilizatorilor
     * (nume si email) pe baza ID-urilor din tranzactii.
     *
     * @return List&lt;String&gt; O lista de siruri de caractere, fiecare reprezentand o linie din raport.
     * @author Laurentiu
     */
    public List<String> getGlobalAdminReport() {
        List<Transaction> allTransactions = transactionRepository.findAll();

        Map<Long, BigDecimal> totalsByUser = allTransactions.stream()
                .collect(Collectors.groupingBy(
                        Transaction::getUserId,
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                Transaction::getAmount,
                                BigDecimal::add
                        )
                ));

        List<String> report = new ArrayList<>();

        totalsByUser.forEach((userId, totalSpent) -> {
            try {
                var accountDto = accountClient.getAccountById(userId);

                String line = String.format("User: %s (%s) - Total Spent: %.2f RON",
                        accountDto.getName(), accountDto.getEmail(), totalSpent);
                report.add(line);

            } catch (Exception e) {
                report.add("User ID: " + userId + " - Total Spent: " + totalSpent + " (Account Info Unavailable)");
            }
        });

        return report;
    }

    /**
     * Verifica sincronizarea dintre utilizatorul logat curent (email din token) si ID-ul din baza de date.
     * Realizeaza un apel catre microserviciul Account pentru a confirma maparea.
     *
     * @return String Un mesaj de confirmare care contine email-ul si ID-ul utilizatorului.
     * @throws IllegalStateException Daca utilizatorul nu este autentificat cu un token JWT valid.
     * @author Laurentiu
     */
    public String checkUserSync() {
        String email = getCurrentUserEmail();
        Long userId = accountClient.getUserIdByEmail(email);
        return "Sync Successful! Email: " + email + " is mapped to ID: " + userId;
    }

    /**
     * Creeaza o tranzactie inteligenta folosind AI pentru a prezice categoria.
     * Metoda trimite descrierea catre microserviciul AI, primeste o categorie sugerata,
     * verifica existenta acesteia in baza de date si salveaza tranzactia.
     *
     * @param dto Obiectul DTO care contine descrierea si suma tranzactiei.
     * @return TransactionViewDTO Detaliile tranzactiei create si salvate.
     * @throws ResourceNotFoundException Daca categoria prezisa de AI nu exista in baza de date.
     * @author Laurentiu
     */
    @Transactional
    public TransactionViewDTO createSmartTransaction(SmartTransactionDTO dto) {

        String email = getCurrentUserEmail();
        Long userId = accountClient.getUserIdByEmail(email);

        String predictedCategoryName = aiClient.predictCategory(dto.getDescription());

        String cleanName = predictedCategoryName.strip();
        Category category = categoryRepository.findByName(cleanName);

        if (category == null) {
            throw new ResourceNotFoundException("Category predicted by AI not found in DB: " + cleanName);
        }

        Transaction transaction = new Transaction();
        transaction.setUserId(userId);
        transaction.setAmount(BigDecimal.valueOf(dto.getAmount()));
        transaction.setDescription(dto.getDescription());
        transaction.setDate(LocalDate.now());
        transaction.setType(category.getType());
        transaction.setCategory(category);

        Transaction saved = transactionRepository.save(transaction);
        return transactionMapper.toViewDTO(saved);
    }

    /**
     * Extrage email-ul utilizatorului autentificat din contextul de securitate curent.
     * Verifica SecurityContextHolder pentru a gasi token-ul JWT si a citi claim-ul "email".
     * Aceasta metoda este esentiala pentru a lega cererile de utilizatorul real.
     *
     * @return String Email-ul utilizatorului extras din token.
     * @throws IllegalStateException Daca nu exista un context de autentificare valid.
     * @author Laurentiu
     */
    private String getCurrentUserEmail() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken jwtToken) {
            return (String) jwtToken.getTokenAttributes().get("email");
        }
        throw new IllegalStateException("User not authenticated with JWT");
    }

    /**
     * Creeaza o tranzactie standard pe baza datelor furnizate de utilizator.
     * Valideaza existenta categoriei si regulile de business inainte de salvare.
     *
     * @param createDto Obiectul DTO cu datele necesare crearii tranzactiei (suma, tip, categorie, userId).
     * @return TransactionViewDTO Obiectul tranzactiei salvate expus catre client.
     * @throws ResourceNotFoundException Daca categoria specificata nu este gasita.
     * @author Laurentiu
     */
    @Transactional
    public TransactionViewDTO createTransaction(CreateTransactionDTO createDto) {

        log.debug("Creating transaction for userId: {}", createDto.getUserId());

        Category category = categoryRepository.findById(createDto.getCategoryId())
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + createDto.getCategoryId()));

        transactionDomainService.validateTransactionCreation(
                createDto.getAmount(),
                createDto.getType(),
                category
        );

        Transaction transaction = transactionMapper.toEntity(createDto);
        transaction.setCategory(category);

        Transaction savedTransaction = transactionRepository.save(transaction);
        log.info("Transaction created with id: {}", savedTransaction.getId());
        return transactionMapper.toViewDTO(savedTransaction);
    }

    /**
     * Obtine detaliile unei tranzactii specifice pe baza ID-ului unic.
     *
     * @param id ID-ul tranzactiei cautate.
     * @return TransactionViewDTO Detaliile tranzactiei gasite.
     * @throws ResourceNotFoundException Daca tranzactia cu ID-ul specificat nu exista.
     * @author Laurentiu
     */
    @Transactional(readOnly = true)
    public TransactionViewDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        return transactionMapper.toViewDTO(transaction);
    }

    /**
     * Sterge o tranzactie din baza de date.
     *
     * @param id ID-ul tranzactiei care urmeaza sa fie stearsa.
     * @throws ResourceNotFoundException Daca tranzactia nu exista inainte de stergere.
     * @author Laurentiu
     */
    @Transactional
    public TransactionViewDTO updateTransaction(Long id, UpdateTransactionDTO dto) {
        Transaction existingTransaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with ID: " + id));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with ID: " + dto.getCategoryId()));

        existingTransaction.setAmount(dto.getAmount());
        existingTransaction.setDate(dto.getDate());
        existingTransaction.setDescription(dto.getDescription());
        existingTransaction.setCategory(category);

        Transaction updatedTransaction = transactionRepository.save(existingTransaction);

        return transactionMapper.toViewDTO(updatedTransaction);
    }

    /**
     * Sterge o tranzactie din baza de date.
     *
     * @param id ID-ul tranzactiei care urmeaza sa fie stearsa.
     * @throws ResourceNotFoundException Daca tranzactia nu exista inainte de stergere.
     * @author Laurentiu
     */
    @Transactional
    public void deleteTransaction(Long id) {
        if(!transactionRepository.existsById(id)) {
            throw new ResourceNotFoundException("Transaction not found with ID: " + id);
        }
        transactionRepository.deleteById(id);
    }

    /**
     * Returneaza lista tranzactiilor unui utilizator, cu optiuni de filtrare si sortare.
     *
     * @param userId ID-ul utilizatorului.
     * @param type (Optional) Tipul tranzactiei pentru filtrare (INCOME sau EXPENSE).
     * @param sortBy Campul dupa care se face sortarea (ex: "amount", "date").
     * @param order Directia sortarii ("asc" sau "desc").
     * @return List&lt;TransactionViewDTO&gt; Lista tranzactiilor filtrate si sortate.
     * @author Laurentiu
     */
    @Transactional(readOnly = true)
    public List<TransactionViewDTO> getTransactionsByUserId(Long userId, TransactionType type, String sortBy, String order) {

        List<Transaction> transactions = transactionRepository.findAllByUserId(userId);

        Stream<Transaction> transactionStream = transactions.stream();

        if (type != null) {
            transactionStream = transactionStream.filter(t -> t.getType() == type);
        }

        Comparator<Transaction> comparator = createComparator(sortBy, order);

        transactionStream = transactionStream.sorted(comparator);

        return transactionStream
                .map(transactionMapper::toViewDTO)
                .collect(Collectors.toList());
    }

    /**
     * Filtreaza un stream de tranzactii pe baza tipului specificat.
     * Aceasta este o metoda ajutatoare folosita in cadrul logicii de afisare a tranzactiilor.
     *
     * @param type Tipul dupa care se face filtrarea (INCOME, EXPENSE). Daca este null, nu se aplica filtru.
     * @return Stream&lt;Transaction&gt; Stream-ul filtrat.
     * @author Laurentiu
     */
    @Transactional(readOnly = true)
    public List<TransactionViewDTO> getTransactionsByType(TransactionType type) {

        List<Transaction> allTransactions = transactionRepository.findAll();
        return allTransactions.stream()
                .filter(t -> t.getType() == type)
                .map(transactionMapper::toViewDTO)
                .collect(Collectors.toList());
    }

    /**
     * Aplica sortarea asupra tranzactiilor si le converteste in obiecte DTO.
     * Preia un stream de tranzactii, aplica un comparator construit dinamic si mapeaza rezultatul la lista finala.
     *
     * @param sortBy Campul dupa care se face sortarea ("amount" sau "date").
     * @param order Directia sortarii ("asc" sau "desc").
     * @return List&lt;TransactionViewDTO&gt; Lista finala de tranzactii gata de trimis catre client.
     * @author Laurentiu
     */
    @Transactional(readOnly = true)
    public List<TransactionViewDTO> getSortedTransactions(String sortBy, String order) {

        List<Transaction> allTransactions = transactionRepository.findAll();

        Comparator<Transaction> comparator = createComparator(sortBy, order);

        return allTransactions.stream()
                .sorted(comparator)
                .map(transactionMapper::toViewDTO)
                .collect(Collectors.toList());
    }

    /**
     * Creeaza un obiect Comparator pentru entitatea Transaction.
     * Contine logica de switch pentru a determina campul de sortare (suma sau data)
     * si directia (crescator sau descrescator).
     *
     * @param sortBy Numele campului ("amount", "date"). Default este "date".
     * @param order Directia ("asc", "desc"). Default este "desc".
     * @return Comparator&lt;Transaction&gt; Comparatorul configurat.
     * @author Laurentiu
     */
    private Comparator<Transaction> createComparator(String sortBy, String order) {
        Comparator<Transaction> comparator = switch (sortBy.toLowerCase()) {
            case "amount" -> Comparator.comparing(Transaction::getAmount);
            case "date" -> Comparator.comparing(Transaction::getDate);
            default -> Comparator.comparing(Transaction::getDate);
        };

        if ("desc".equalsIgnoreCase(order)) {
            comparator = comparator.reversed();
        }
        return comparator;
    }

    /**
     * Calculeaza cheltuielile totale ale unui utilizator pentru o anumita luna si un anumit an.
     *
     * @param userId ID-ul utilizatorului.
     * @param month Luna pentru care se face calculul (1-12).
     * @param year Anul pentru care se face calculul.
     * @return BigDecimal Suma totala a cheltuielilor.
     * @throws IllegalArgumentException Daca luna este invalida sau userId este null.
     * @author Laurentiu
     */
    @Transactional(readOnly = true)
    public BigDecimal getMonthlyExpense(Long userId, int month, int year) {

        transactionDomainService.validateMonthlyExpense(userId, year, month);

        return transactionRepository.calculateMonthlyExpense(userId, month, year);
    }

    /**
     * Verifica daca utilizatorul se incadreaza intr-un buget specificat.
     * Compara cheltuielile totale ale utilizatorului cu limita bugetului.
     *
     * @param userId ID-ul utilizatorului.
     * @param budgetLimit Limita de buget stabilita.
     * @return String Un mesaj care indica daca bugetul a fost depasit sau nu ("Over Budget" sau "Within Budget").
     * @author Laurentiu
     */
    @Transactional(readOnly = true)
    public String checkBudgetStatus(Long userId, BigDecimal budgetLimit) {

        transactionDomainService.validateBudgetCheckRequest(userId, budgetLimit);

        return transactionRepository.checkBudgetStatus(userId, budgetLimit);
    }

    /**
     * Arhiveaza (sterge) tranzactiile mai vechi de o anumita data.
     *
     * @param cutoffDate Data limita pana la care tranzactiile sunt pastrate.
     * @return Integer Numarul de tranzactii care au fost arhivate.
     * @throws IllegalArgumentException Daca data limita este prea recenta (mai putin de 30 de zile).
     * @author Laurentiu
     */
    @Transactional
    public Integer archiveOldTransactions(LocalDate cutoffDate) {

        log.info("Request to archive transactions older than: {}", cutoffDate);

        transactionDomainService.validateArchiveRequest(cutoffDate);

        Integer deletedCount = transactionRepository.archiveOldTransactions(cutoffDate);
        log.info("Archived {} transactions older than {}", deletedCount, cutoffDate);

        return deletedCount;
    }
}
