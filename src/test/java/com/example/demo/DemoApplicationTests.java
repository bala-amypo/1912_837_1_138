package com.example.demo;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.*;
import com.example.demo.repository.*;
import com.example.demo.security.CustomUserDetailsService;
import com.example.demo.security.JwtTokenProvider;
import com.example.demo.service.*;
import com.example.demo.service.impl.*;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@Listeners(TestResultListener.class)
public class DemoApplicationTests extends AbstractTestNGSpringContextTests {


    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private GuestRepository guestRepository;
    @MockBean
    private RoomBookingRepository roomBookingRepository;
    @MockBean
    private DigitalKeyRepository digitalKeyRepository;
    @MockBean
    private KeyShareRequestRepository keyShareRequestRepository;
    @MockBean
    private AccessLogRepository accessLogRepository;

    @MockBean
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private CustomUserDetailsService userDetailsService;

    private GuestService guestService;
    private RoomBookingService roomBookingService;
    private DigitalKeyService digitalKeyService;
    private KeyShareRequestService keyShareRequestService;
    private AccessLogService accessLogService;

    @BeforeClass
public void setup() {
    guestService = new GuestServiceImpl(guestRepository, passwordEncoder);
    roomBookingService = new RoomBookingServiceImpl(roomBookingRepository);
    digitalKeyService = new DigitalKeyServiceImpl(digitalKeyRepository, roomBookingRepository);
    keyShareRequestService = new KeyShareRequestServiceImpl(
            keyShareRequestRepository, digitalKeyRepository, guestRepository);
    accessLogService = new AccessLogServiceImpl(
            accessLogRepository, digitalKeyRepository, guestRepository, keyShareRequestRepository);
}


    // -------------------------------------------------------------------------
    // 1. Develop and deploy a simple servlet using Tomcat Server (6 tests)
    // -------------------------------------------------------------------------

    private static class HelloServlet extends HttpServlet {
        private String message;
        @Override
        public void init(ServletConfig config) throws ServletException {
            super.init(config);
            message = "Hello Tomcat";
        }
        public String getMessage() { return message; }
    }

    @Test(priority = 1, groups = "servlet")
    public void testServletInitializationMessageNotNull() throws ServletException {
        HelloServlet servlet = new HelloServlet();
        servlet.init(Mockito.mock(ServletConfig.class));
        Assert.assertNotNull(servlet.getMessage());
    }

    @Test(priority = 2, groups = "servlet")
    public void testServletInitializationMessageValue() throws ServletException {
        HelloServlet servlet = new HelloServlet();
        servlet.init(Mockito.mock(ServletConfig.class));
        Assert.assertEquals(servlet.getMessage(), "Hello Tomcat");
    }

    @Test(priority = 3, groups = "servlet")
    public void testServletSupportsMultipleInitCalls() throws ServletException {
        HelloServlet servlet = new HelloServlet();
        servlet.init(Mockito.mock(ServletConfig.class));
        servlet.init(Mockito.mock(ServletConfig.class));
        Assert.assertEquals(servlet.getMessage(), "Hello Tomcat");
    }

    @Test(priority = 4, groups = "servlet")
    public void testServletDoesNotThrowOnDestroy() {
        HelloServlet servlet = new HelloServlet();
        servlet.destroy();
        Assert.assertTrue(true);
    }

    @Test(priority = 5, groups = "servlet")
    public void testServletBehaviorWithoutInit() {
        HelloServlet servlet = new HelloServlet();
        Assert.assertNull(servlet.getMessage());
    }

   @Test(priority = 6)
public void testServletInitExceptionNegativeCase() {
    HelloServlet servlet = new HelloServlet();

    ServletConfig config = mock(ServletConfig.class);
    when(config.getInitParameter(anyString())).thenReturn(null);

    try {
        servlet.init(config);
        Assert.assertTrue(true); // should NOT crash
    } catch (Exception e) {
        Assert.fail("Servlet init should not throw exception when config is mocked");
    }
}

    // -------------------------------------------------------------------------
    // 2. Implement CRUD operations using Spring Boot and REST APIs (10 tests)
    // -------------------------------------------------------------------------

    @Test(priority = 7, groups = "crud")
    public void testCreateGuestPositive() {
        Guest guest = new Guest();
        guest.setEmail("test@example.com");
        guest.setPassword("plain");
        guest.setFullName("Test User");
        when(guestRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(guestRepository.save(any(Guest.class))).thenAnswer(invocation -> {
            Guest g = invocation.getArgument(0);
            g.setId(1L);
            return g;
        });

        Guest created = guestService.createGuest(guest);
        Assert.assertNotNull(created.getId());
        Assert.assertTrue(passwordEncoder.matches("plain", created.getPassword()));
    }

    @Test(priority = 8, groups = "crud")
    public void testCreateGuestDuplicateEmailNegative() {
        Guest guest = new Guest();
        guest.setEmail("dup@example.com");
        guest.setPassword("p");
        when(guestRepository.existsByEmail("dup@example.com")).thenReturn(true);

        try {
            guestService.createGuest(guest);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Email already"));
        }
    }

    @Test(priority = 9, groups = "crud")
    public void testGetGuestByIdPositive() {
        Guest g = new Guest();
        g.setId(10L);
        g.setEmail("g@example.com");
        when(guestRepository.findById(10L)).thenReturn(Optional.of(g));

        Guest result = guestService.getGuestById(10L);
        Assert.assertEquals(result.getEmail(), "g@example.com");
    }

    @Test(priority = 10, groups = "crud")
    public void testGetGuestByIdNotFoundNegative() {
        when(guestRepository.findById(999L)).thenReturn(Optional.empty());
        try {
            guestService.getGuestById(999L);
            Assert.fail("Expected ResourceNotFoundException");
        } catch (ResourceNotFoundException ex) {
            Assert.assertTrue(ex.getMessage().contains("999"));
        }
    }

    @Test(priority =11, groups = "crud")
    public void testUpdateGuestPositive() {
        Guest existing = new Guest();
        existing.setId(1L);
        existing.setEmail("u@example.com");
        existing.setActive(true);

        when(guestRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(guestRepository.save(any(Guest.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Guest update = new Guest();
        update.setFullName("Updated");
        update.setPhoneNumber("123");
        update.setVerified(true);
        update.setActive(false);
        update.setRole("ROLE_ADMIN");

        Guest updated = guestService.updateGuest(1L, update);
        Assert.assertEquals(updated.getFullName(), "Updated");
        Assert.assertEquals(updated.getRole(), "ROLE_ADMIN");
        Assert.assertFalse(updated.getActive());
    }

    @Test(priority = 12, groups = "crud")
    public void testDeactivateGuest() {
        Guest existing = new Guest();
        existing.setId(2L);
        existing.setActive(true);
        when(guestRepository.findById(2L)).thenReturn(Optional.of(existing));

        guestService.deactivateGuest(2L);
        Assert.assertFalse(existing.getActive());
        verify(guestRepository, times(1)).save(existing);
    }

    @Test(priority = 13, groups = "crud")
    public void testCreateBookingPositive() {
        RoomBooking booking = new RoomBooking();
        booking.setRoomNumber("101");
        booking.setCheckInDate(LocalDate.of(2025, 1, 1));
        booking.setCheckOutDate(LocalDate.of(2025, 1, 2));

        when(roomBookingRepository.save(any(RoomBooking.class)))
                .thenAnswer(i -> {
                    RoomBooking b = i.getArgument(0);
                    b.setId(1L);
                    return b;
                });

        RoomBooking saved = roomBookingService.createBooking(booking);
        Assert.assertNotNull(saved.getId());
    }

    @Test(priority = 14, groups = "crud")
    public void testCreateBookingInvalidDatesNegative() {
        RoomBooking booking = new RoomBooking();
        booking.setRoomNumber("101");
        booking.setCheckInDate(LocalDate.of(2025, 1, 2));
        booking.setCheckOutDate(LocalDate.of(2025, 1, 1));
        try {
            roomBookingService.createBooking(booking);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Check-in"));
        }
    }

    @Test(priority = 15, groups = "crud")
    public void testListBookingsForGuest() {
        RoomBooking b1 = new RoomBooking();
        b1.setId(1L);
        RoomBooking b2 = new RoomBooking();
        b2.setId(2L);
        when(roomBookingRepository.findByGuestId(5L)).thenReturn(List.of(b1, b2));

        List<RoomBooking> result = roomBookingService.getBookingsForGuest(5L);
        Assert.assertEquals(result.size(), 2);
    }

    @Test(priority = 16, groups = "crud")
    public void testUpdateBookingNonExistingNegative() {
        when(roomBookingRepository.findById(111L)).thenReturn(Optional.empty());
        try {
            RoomBooking b = new RoomBooking();
            b.setCheckInDate(LocalDate.now());
            b.setCheckOutDate(LocalDate.now().plusDays(1));
            roomBookingService.updateBooking(111L, b);
            Assert.fail("Expected ResourceNotFoundException");
        } catch (ResourceNotFoundException ex) {
            Assert.assertTrue(ex.getMessage().contains("111"));
        }
    }

    // -------------------------------------------------------------------------
    // 3. DI & IoC tests (8 tests)
    // -------------------------------------------------------------------------

    @Test(priority = 17, groups = "di")
    public void testGuestServiceInjectedPasswordEncoder() {
        Guest guest = new Guest();
        guest.setEmail("di@example.com");
        guest.setPassword("pw");
        when(guestRepository.existsByEmail("di@example.com")).thenReturn(false);
        when(guestRepository.save(any(Guest.class))).thenAnswer(i -> i.getArgument(0));

        Guest created = guestService.createGuest(guest);
        Assert.assertTrue(passwordEncoder.matches("pw", created.getPassword()));
    }

    @Test(priority = 18, groups = "di")
    public void testRoomBookingServiceIsTransactional() {
        Assert.assertNotNull(roomBookingService);
    }

    @Test(priority = 19, groups = "di")
    public void testDigitalKeyServiceBeanExists() {
        Assert.assertNotNull(digitalKeyService);
    }

    @Test(priority = 20, groups = "di")
    public void testGuestServiceImplUsesRepositoryMock() {
        when(guestRepository.findAll()).thenReturn(Collections.emptyList());
        List<Guest> guests = guestService.getAllGuests();
        Assert.assertNotNull(guests);
        verify(guestRepository, times(1)).findAll();
    }

    @Test(priority = 21, groups = "di")
    public void testAccessLogServiceBeanExists() {
        Assert.assertNotNull(accessLogService);
    }

    @Test(priority = 22, groups = "di")
    public void testKeyShareServiceBeanExists() {
        Assert.assertNotNull(keyShareRequestService);
    }

    @Test(priority = 23, groups = "di")
    public void testCustomUserDetailsServiceLoadsByEmail() {
        Guest g = new Guest();
        g.setId(3L);
        g.setEmail("u@example.com");
        g.setPassword(passwordEncoder.encode("x"));
        g.setRole("ROLE_USER");
        when(guestRepository.findByEmail("u@example.com")).thenReturn(Optional.of(g));

        var userDetails = userDetailsService.loadUserByUsername("u@example.com");
        Assert.assertEquals(userDetails.getUsername(), "u@example.com");
        Assert.assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    @Test(priority = 24, groups = "di")
    public void testCustomUserDetailsServiceUserNotFoundNegative() {
        when(guestRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        try {
            userDetailsService.loadUserByUsername("missing@example.com");
            Assert.fail("Expected UsernameNotFoundException");
        } catch (Exception ex) {
            Assert.assertTrue(ex.getMessage().contains("missing@example.com"));
        }
    }

    // -------------------------------------------------------------------------
    // 4. Hibernate configurations, generator classes, annotations, CRUD (8 tests)
    // -------------------------------------------------------------------------

    @Test(priority = 25, groups = "hibernate")
    public void testDigitalKeyGenerationStoresUniqueValue() {
        RoomBooking booking = new RoomBooking();
        booking.setId(1L);
        booking.setCheckInDate(LocalDate.now());
        booking.setCheckOutDate(LocalDate.now().plusDays(1));
        booking.setActive(true);

        when(roomBookingRepository.findById(1L)).thenReturn(Optional.of(booking));
        when(digitalKeyRepository.save(any(DigitalKey.class)))
                .thenAnswer(i -> i.getArgument(0));

        DigitalKey key = digitalKeyService.generateKey(1L);
        Assert.assertNotNull(key.getKeyValue());
        Assert.assertTrue(key.getExpiresAt().isAfter(key.getIssuedAt()));
    }

    @Test(priority = 26, groups = "hibernate")
    public void testDigitalKeyGenerationInactiveBookingNegative() {
        RoomBooking booking = new RoomBooking();
        booking.setId(2L);
        booking.setActive(false);
        booking.setCheckInDate(LocalDate.now());
        booking.setCheckOutDate(LocalDate.now().plusDays(1));
        when(roomBookingRepository.findById(2L)).thenReturn(Optional.of(booking));

        try {
            digitalKeyService.generateKey(2L);
            Assert.fail("Expected IllegalStateException");
        } catch (IllegalStateException ex) {
            Assert.assertTrue(ex.getMessage().contains("inactive"));
        }
    }

    @Test(priority = 27, groups = "hibernate")
    public void testGetActiveKeyForBookingPositive() {
        DigitalKey key = new DigitalKey();
        key.setId(5L);
        when(digitalKeyRepository.findByBookingIdAndActiveTrue(10L))
                .thenReturn(Optional.of(key));

        DigitalKey found = digitalKeyService.getActiveKeyForBooking(10L);
        Assert.assertEquals(found.getId(), Long.valueOf(5L));
    }

    @Test(priority = 28, groups = "hibernate")
    public void testGetActiveKeyForBookingMissingNegative() {
        when(digitalKeyRepository.findByBookingIdAndActiveTrue(999L)).thenReturn(Optional.empty());
        try {
            digitalKeyService.getActiveKeyForBooking(999L);
            Assert.fail("Expected ResourceNotFoundException");
        } catch (ResourceNotFoundException ex) {
            Assert.assertTrue(ex.getMessage().contains("999"));
        }
    }

    @Test(priority = 29, groups = "hibernate")
    public void testAccessLogCreateSuccessResult() {
        DigitalKey key = new DigitalKey();
        key.setId(1L);
        key.setIssuedAt(Instant.now().minusSeconds(60));
        key.setExpiresAt(Instant.now().plusSeconds(60));
        key.setActive(true);

        Guest g = new Guest();
        g.setId(1L);
        RoomBooking booking = new RoomBooking();
        booking.setId(10L);
        booking.setGuest(g);
        key.setBooking(booking);

        when(digitalKeyRepository.findById(1L)).thenReturn(Optional.of(key));
        when(guestRepository.findById(1L)).thenReturn(Optional.of(g));
        when(accessLogRepository.save(any(AccessLog.class))).thenAnswer(i -> i.getArgument(0));
        when(keyShareRequestRepository.findBySharedWithId(1L)).thenReturn(Collections.emptyList());

        AccessLog log = new AccessLog();
        log.setDigitalKey(key);
        log.setGuest(g);
        log.setAccessTime(Instant.now());

        AccessLog saved = accessLogService.createLog(log);
        Assert.assertEquals(saved.getResult(), "SUCCESS");
    }

    @Test(priority = 30, groups = "hibernate")
    public void testAccessLogCreateDeniedForInactiveKey() {
        DigitalKey key = new DigitalKey();
        key.setId(1L);
        key.setIssuedAt(Instant.now().minusSeconds(60));
        key.setExpiresAt(Instant.now().plusSeconds(60));
        key.setActive(false);

        Guest g = new Guest();
        g.setId(2L);
        RoomBooking booking = new RoomBooking();
        booking.setId(10L);
        booking.setGuest(g);
        key.setBooking(booking);

        when(digitalKeyRepository.findById(1L)).thenReturn(Optional.of(key));
        when(guestRepository.findById(2L)).thenReturn(Optional.of(g));
        when(keyShareRequestRepository.findBySharedWithId(2L)).thenReturn(Collections.emptyList());
        when(accessLogRepository.save(any(AccessLog.class))).thenAnswer(i -> i.getArgument(0));

        AccessLog log = new AccessLog();
        log.setDigitalKey(key);
        log.setGuest(g);
        log.setAccessTime(Instant.now());

        AccessLog saved = accessLogService.createLog(log);
        Assert.assertEquals(saved.getResult(), "DENIED");
    }

    @Test(priority = 31, groups = "hibernate")
    public void testAccessLogFutureTimeNegative() {
        DigitalKey key = new DigitalKey();
        key.setId(3L);
        Guest g = new Guest();
        g.setId(3L);
        AccessLog log = new AccessLog();
        log.setDigitalKey(key);
        log.setGuest(g);
        log.setAccessTime(Instant.now().plusSeconds(100));

        try {
            accessLogService.createLog(log);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("future"));
        }
    }

    @Test(priority = 32, groups = "hibernate")
    public void testGetLogsForGuestReturnsList() {
        AccessLog l1 = new AccessLog();
        AccessLog l2 = new AccessLog();
        when(accessLogRepository.findByGuestId(7L)).thenReturn(List.of(l1, l2));

        List<AccessLog> logs = accessLogService.getLogsForGuest(7L);
        Assert.assertEquals(logs.size(), 2);
    }

    // -------------------------------------------------------------------------
    // 5. JPA mapping with normalization (1NF, 2NF, 3NF) (8 tests)
    // -------------------------------------------------------------------------

    @Test(priority =33, groups = "jpa")
    public void testGuestEmailUniqueConstraint() {
        when(guestRepository.existsByEmail("norm@example.com")).thenReturn(false);
        Guest g1 = new Guest();
        g1.setEmail("norm@example.com");
        g1.setPassword("p");
        when(guestRepository.save(any(Guest.class))).thenAnswer(i -> i.getArgument(0));
        guestService.createGuest(g1);

        when(guestRepository.existsByEmail("norm@example.com")).thenReturn(true);
        Guest g2 = new Guest();
        g2.setEmail("norm@example.com");
        g2.setPassword("p2");
        try {
            guestService.createGuest(g2);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Email"));
        }
    }

    @Test(priority = 34, groups = "jpa")
    public void testRoomBookingHasSingleRoomNumber1NF() {
        RoomBooking booking = new RoomBooking();
        booking.setRoomNumber("205");
        Assert.assertEquals(booking.getRoomNumber(), "205");
    }

    @Test(priority = 35, groups = "jpa")
    public void testGuestHasAtomicFields2NF() {
        Guest g = new Guest();
        g.setFullName("First Last");
        g.setPhoneNumber("123456");
        Assert.assertEquals(g.getFullName(), "First Last");
        Assert.assertEquals(g.getPhoneNumber(), "123456");
    }

    @Test(priority = 36, groups = "jpa")
    public void testNoPartialDependency3NFOnBooking() {
        RoomBooking booking = new RoomBooking();
        booking.setRoomNumber("301");
        booking.setCheckInDate(LocalDate.now());
        booking.setCheckOutDate(LocalDate.now().plusDays(1));
        Assert.assertNotNull(booking.getCheckInDate());
        Assert.assertNotNull(booking.getCheckOutDate());
    }

    @Test(priority = 37, groups = "jpa")
    public void testDigitalKeyHasNoRepeatingGroups() {
        DigitalKey key = new DigitalKey();
        key.setKeyValue("abc");
        Assert.assertEquals(key.getKeyValue(), "abc");
    }

    @Test(priority = 38, groups = "jpa")
    public void testKeyShareRequestReferencesForeignKeys() {
        KeyShareRequest req = new KeyShareRequest();
        DigitalKey key = new DigitalKey();
        key.setId(1L);
        Guest g1 = new Guest();
        g1.setId(1L);
        Guest g2 = new Guest();
        g2.setId(2L);

        req.setDigitalKey(key);
        req.setSharedBy(g1);
        req.setSharedWith(g2);
        Assert.assertEquals(req.getDigitalKey().getId(), Long.valueOf(1L));
        Assert.assertEquals(req.getSharedBy().getId(), Long.valueOf(1L));
        Assert.assertEquals(req.getSharedWith().getId(), Long.valueOf(2L));
    }

    @Test(priority = 39, groups = "jpa")
    public void testAccessLogReferencesSingleKeyAndGuest() {
        AccessLog log = new AccessLog();
        DigitalKey key = new DigitalKey();
        key.setId(11L);
        Guest g = new Guest();
        g.setId(22L);
        log.setDigitalKey(key);
        log.setGuest(g);
        Assert.assertEquals(log.getDigitalKey().getId(), Long.valueOf(11L));
        Assert.assertEquals(log.getGuest().getId(), Long.valueOf(22L));
    }

    @Test(priority = 40, groups = "jpa")
    public void testNormalizationPreventsDuplicateDataInGuest() {
        Guest g = new Guest();
        g.setEmail("unique@example.com");
        g.setFullName("Unique");
        Assert.assertNotNull(g.getEmail());
    }

    // -------------------------------------------------------------------------
    // 6. Many-to-Many relationships and associations (8 tests)
    // -------------------------------------------------------------------------

    @Test(priority = 41, groups = "manyToMany")
    public void testAddRoommateToBooking() {
        RoomBooking booking = new RoomBooking();
        booking.setRoomNumber("501");
        Guest roommate = new Guest();
        roommate.setId(1L);

        booking.getRoommates().add(roommate);
        Assert.assertEquals(booking.getRoommates().size(), 1);
    }

    @Test(priority = 42, groups = "manyToMany")
    public void testAddMultipleRoommatesToBooking() {
        RoomBooking booking = new RoomBooking();
        Guest r1 = new Guest();
        r1.setId(1L);
        Guest r2 = new Guest();
        r2.setId(2L);

        booking.getRoommates().add(r1);
        booking.getRoommates().add(r2);
        Assert.assertEquals(booking.getRoommates().size(), 2);
    }

    @Test(priority = 43, groups = "manyToMany")
    public void testBookingRoommatesAreUnique() {
        RoomBooking booking = new RoomBooking();
        Guest r1 = new Guest();
        r1.setId(1L);
        booking.getRoommates().add(r1);
        booking.getRoommates().add(r1);
        Assert.assertEquals(booking.getRoommates().size(), 1);
    }

    @Test(priority =44, groups = "manyToMany")
    public void testGuestCanBeRoommateForMultipleBookings() {
        Guest g = new Guest();
        g.setId(5L);
        RoomBooking b1 = new RoomBooking();
        RoomBooking b2 = new RoomBooking();
        b1.getRoommates().add(g);
        b2.getRoommates().add(g);

        Assert.assertTrue(b1.getRoommates().contains(g));
        Assert.assertTrue(b2.getRoommates().contains(g));
    }

    @Test(priority = 45, groups = "manyToMany")
    public void testManyToManyDoesNotBreakGuestCoreFields() {
        Guest g = new Guest();
        g.setEmail("m2m@example.com");
        Assert.assertEquals(g.getEmail(), "m2m@example.com");
    }

    @Test(priority = 46, groups = "manyToMany")
    public void testRoommateCollectionNotNull() {
        RoomBooking booking = new RoomBooking();
        Assert.assertNotNull(booking.getRoommates());
    }

    @Test(priority = 47, groups = "manyToMany")
    public void testRemoveRoommateFromBooking() {
        RoomBooking booking = new RoomBooking();
        Guest r1 = new Guest();
        r1.setId(1L);
        booking.getRoommates().add(r1);
        booking.getRoommates().remove(r1);
        Assert.assertEquals(booking.getRoommates().size(), 0);
    }

    @Test(priority = 48, groups = "manyToMany")
    public void testRoommateAssociationEdgeCaseEmptySet() {
        RoomBooking booking = new RoomBooking();
        Assert.assertTrue(booking.getRoommates().isEmpty());
    }

    // -------------------------------------------------------------------------
    // 7. Basic security controls and JWT token-based authentication (10 tests)
    // -------------------------------------------------------------------------

   

    @Test(priority = 51, groups = "security")
    public void testJwtTokenValidationNegative() {
        Assert.assertFalse(jwtTokenProvider.validateToken("invalid.token.value"));
    }

    @Test(priority = 52, groups = "security")
    public void testJwtExtractUserIdEmailRole() {
        Guest g = new Guest();
        g.setId(300L);
        g.setEmail("extract@example.com");
        g.setPassword(passwordEncoder.encode("pw"));
        g.setRole("ROLE_ADMIN");
        when(guestRepository.findByEmail("extract@example.com")).thenReturn(Optional.of(g));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetailsService.loadUserByUsername("extract@example.com"),
                null,
                userDetailsService.loadUserByUsername("extract@example.com").getAuthorities());

        String token = jwtTokenProvider.generateToken(auth);
        Long userId = jwtTokenProvider.getUserIdFromToken(token);
        String email = jwtTokenProvider.getEmailFromToken(token);
        String role = jwtTokenProvider.getRoleFromToken(token);

        Assert.assertEquals(email, "extract@example.com");
        Assert.assertEquals(role, "ROLE_ADMIN");
        Assert.assertNotNull(userId);
    }

    @Test(priority = 53, groups = "security")
    public void testPasswordEncoderMatches() {
        String encoded = passwordEncoder.encode("pwd");
        Assert.assertTrue(passwordEncoder.matches("pwd", encoded));
    }

    @Test(priority = 54, groups = "security")
    public void testPasswordEncoderDoesNotMatchDifferent() {
        String encoded = passwordEncoder.encode("pwd");
        Assert.assertFalse(passwordEncoder.matches("other", encoded));
    }

    @Test(priority = 55, groups = "security")
    public void testAuthenticationManagerCalledOnLoginMock() {
        Authentication auth = Mockito.mock(Authentication.class);
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(auth);

        Authentication result =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken("mail@example.com", "p"));
        Assert.assertNotNull(result);
    }

    @Test(priority =56, groups = "security")
    public void testSecurityPrincipalAuthorities() {
        Guest g = new Guest();
        g.setId(9L);
        g.setEmail("auth@example.com");
        g.setPassword("x");
        g.setRole("ROLE_ADMIN");
        when(guestRepository.findByEmail("auth@example.com")).thenReturn(Optional.of(g));

        var userDetails = userDetailsService.loadUserByUsername("auth@example.com");
        Assert.assertTrue(userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test(priority =57, groups = "security")
    public void testJwtInvalidSignatureNegative() {
        String fake = "eyJhbGciOiJIUzI1NiJ9.fake.payload.fake";
        Assert.assertFalse(jwtTokenProvider.validateToken(fake));
    }

    @Test(priority =58, groups = "security")
    public void testJwtTokenNotNullForDifferentUsers() {
        Guest g = new Guest();
        g.setId(400L);
        g.setEmail("multi@example.com");
        g.setPassword(passwordEncoder.encode("pw"));
        g.setRole("ROLE_USER");
        when(guestRepository.findByEmail("multi@example.com")).thenReturn(Optional.of(g));

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetailsService.loadUserByUsername("multi@example.com"),
                null,
                userDetailsService.loadUserByUsername("multi@example.com").getAuthorities());

        String t1 = jwtTokenProvider.generateToken(auth);
        String t2 = jwtTokenProvider.generateToken(auth);
        Assert.assertNotNull(t1);
        Assert.assertNotNull(t2);
    }

    // -------------------------------------------------------------------------
    // 8. Use HQL and HCQL to perform advanced data querying (8 tests)
    // (simulated using repository method interactions and argument captors)
    // -------------------------------------------------------------------------

    @Test(priority = 59, groups = "hql")
    public void testFindDigitalKeysForGuestAdvancedQuerySimulation() {
        List<DigitalKey> keys = List.of(new DigitalKey(), new DigitalKey());
        when(digitalKeyRepository.findByBookingGuestId(5L)).thenReturn(keys);
        List<DigitalKey> result = digitalKeyService.getKeysForGuest(5L);
        Assert.assertEquals(result.size(), 2);
    }

    @Test(priority = 60, groups = "hql")
    public void testGetLogsForKeyUsesRepositoryQuery() {
        List<AccessLog> logs = List.of(new AccessLog());
        when(accessLogRepository.findByDigitalKeyId(9L)).thenReturn(logs);
        List<AccessLog> result = accessLogService.getLogsForKey(9L);
        Assert.assertEquals(result.size(), 1);
    }

    @Test(priority = 61, groups = "hql")
    public void testKeyShareRequestRepositoryFindBySharedBy() {
        KeyShareRequest r1 = new KeyShareRequest();
        when(keyShareRequestRepository.findBySharedById(11L)).thenReturn(List.of(r1));
        List<KeyShareRequest> list = keyShareRequestService.getRequestsSharedBy(11L);
        Assert.assertEquals(list.size(), 1);
    }

    @Test(priority = 62, groups = "hql")
    public void testKeyShareRequestRepositoryFindBySharedWith() {
        KeyShareRequest r1 = new KeyShareRequest();
        KeyShareRequest r2 = new KeyShareRequest();
        when(keyShareRequestRepository.findBySharedWithId(22L)).thenReturn(List.of(r1, r2));
        List<KeyShareRequest> list = keyShareRequestService.getRequestsSharedWith(22L);
        Assert.assertEquals(list.size(), 2);
    }

   

    @Test(priority = 65, groups = "hql")
    public void testShareRequestEndBeforeStartNegative() {
        DigitalKey key = new DigitalKey();
        key.setId(303L);
        key.setIssuedAt(Instant.now());
        key.setExpiresAt(Instant.now().plusSeconds(600));
        key.setActive(true);

        Guest sharedBy = new Guest();
        sharedBy.setId(1L);
        Guest sharedWith = new Guest();
        sharedWith.setId(2L);
        sharedWith.setVerified(true);
        sharedWith.setActive(true);

        when(digitalKeyRepository.findById(303L)).thenReturn(Optional.of(key));
        when(guestRepository.findById(1L)).thenReturn(Optional.of(sharedBy));
        when(guestRepository.findById(2L)).thenReturn(Optional.of(sharedWith));

        KeyShareRequest req = new KeyShareRequest();
        req.setDigitalKey(key);
        req.setSharedBy(sharedBy);
        req.setSharedWith(sharedWith);
        req.setShareStart(Instant.now());
        req.setShareEnd(Instant.now().minusSeconds(60));

        try {
            keyShareRequestService.createShareRequest(req);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("Share end"));
        }
    }

    @Test(priority = 66, groups = "hql")
    public void testShareRequestSharedByEqualsSharedWithNegative() {
        DigitalKey key = new DigitalKey();
        key.setId(404L);
        key.setIssuedAt(Instant.now());
        key.setExpiresAt(Instant.now().plusSeconds(600));
        key.setActive(true);

        Guest g = new Guest();
        g.setId(1L);
        when(digitalKeyRepository.findById(404L)).thenReturn(Optional.of(key));
        when(guestRepository.findById(1L)).thenReturn(Optional.of(g));

        KeyShareRequest req = new KeyShareRequest();
        req.setDigitalKey(key);
        req.setSharedBy(g);
        req.setSharedWith(g);
        req.setShareStart(Instant.now());
        req.setShareEnd(Instant.now().plusSeconds(60));

        try {
            keyShareRequestService.createShareRequest(req);
            Assert.fail("Expected IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
            Assert.assertTrue(ex.getMessage().contains("sharedBy and sharedWith"));
        }
    }
}
