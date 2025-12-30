package com.mryoda.diagnostics.api.utils;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

public class RequestContext {

    // ============================================================
    // TOKENS (MEMBER / EXISTING MEMBER / NEW USER)
    // ============================================================
    private static String memberToken;
    private static String existingMemberToken;
    private static String newUserToken;
    private static String currentFlowName = "default";

    public static void setCurrentFlowName(String name) {
        currentFlowName = name;
    }

    public static String getCurrentFlowName() {
        return currentFlowName;
    }

    // ============================================================
    // USER DETAILS (Stored per user type)
    // ============================================================
    private static String memberFirstName, memberLastName, memberUserId;
    private static String existingMemberFirstName, existingMemberLastName, existingMemberUserId;
    private static String newUserFirstName, newUserLastName, newUserUserId;

    // Generic (used by TokenManager)
    private static String token;
    private static String firstName;
    private static String lastName;
    private static String userId;
    private static String mobile;

    // ============================================================
    // LOCATION STORAGE
    // ============================================================
    private static final Map<String, String> locations = new HashMap<>();
    private static final Map<String, String> locationLatitudes = new HashMap<>();
    private static final Map<String, String> locationLongitudes = new HashMap<>();
    private static final Map<String, String> locationCities = new HashMap<>();
    private static final Map<String, String> locationStates = new HashMap<>();
    private static String selectedLocationId;

    public static void storeLocation(String title, String id) {
        locations.put(title, id);
    }

    public static void storeLocationCoordinates(String title, String latitude, String longitude) {
        locationLatitudes.put(title, latitude);
        locationLongitudes.put(title, longitude);
    }

    public static void storeLocationCityState(String title, String city, String state) {
        locationCities.put(title, city);
        locationStates.put(title, state);
    }

    public static String getLocationId(String title) {
        return locations.get(title);
    }

    public static String getLocationLatitude(String title) {
        return locationLatitudes.get(title);
    }

    public static String getLocationLongitude(String title) {
        return locationLongitudes.get(title);
    }

    public static String getLocationCity(String title) {
        return locationCities.get(title);
    }

    public static String getLocationState(String title) {
        return locationStates.get(title);
    }

    public static Map<String, String> getAllLocations() {
        return locations;
    }

    public static Map<String, String> getAllLocationLatitudes() {
        return locationLatitudes;
    }

    public static Map<String, String> getAllLocationLongitudes() {
        return locationLongitudes;
    }

    public static void setSelectedLocation(String title) {
        String id = locations.get(title);
        if (id == null) {
            throw new RuntimeException("❌ Location not found in RequestContext: " + title);
        }
        selectedLocationId = id;
    }

    public static String getSelectedLocationId() {
        return selectedLocationId;
    }

    // ============================================================
    // BRAND STORAGE
    // ============================================================
    private static final Map<String, String> brands = new HashMap<>();
    private static String selectedBrandId;

    public static void storeBrand(String title, String brandId) {
        brands.put(title, brandId);
    }

    public static String getBrandId(String title) {
        return brands.get(title);
    }

    public static Map<String, String> getAllBrands() {
        return brands;
    }

    public static void setSelectedBrand(String title) {
        String id = brands.get(title);
        if (id == null) {
            throw new RuntimeException("❌ Brand not found in RequestContext: " + title);
        }
        selectedBrandId = id;
    }

    public static String getSelectedBrandId() {
        return selectedBrandId;
    }

    // ============================================================
    // GLOBAL SEARCH TEST STORAGE
    // ============================================================
    /**
     * Each testName → details map details = { id, test_id, price, original_price,
     * discount_percentage, type }
     */
    private static final Map<String, Map<String, Object>> storedTests = new HashMap<>();

    public static void storeTestDetails(String testName, String id, String testId, int price, int originalPrice,
            int discountPercentage, String type) {

        Map<String, Object> data = new HashMap<>();
        data.put("id", id);
        data.put("test_id", testId);
        data.put("price", price);
        data.put("original_price", originalPrice);
        data.put("discount_percentage", discountPercentage);
        data.put("type", type);

        storedTests.put(testName, data);
    }

    public static Map<String, Object> getTestDetails(String testName) {
        return storedTests.get(testName);
    }

    public static Map<String, Map<String, Object>> getAllStoredTests() {
        return storedTests;
    }

    // ============================================================
    // SETTERS
    // ============================================================
    public static void setMemberToken(String v) {
        memberToken = v;
    }

    public static void setMemberFirstName(String v) {
        memberFirstName = v;
    }

    public static void setMemberLastName(String v) {
        memberLastName = v;
    }

    public static void setMemberUserId(String v) {
        memberUserId = v;
    }

    // NON-MEMBER setters (Mobile: 8220220227 - NOT a paid member)
    public static void setNonMemberToken(String v) {
        existingMemberToken = v;
    }

    public static void setNonMemberFirstName(String v) {
        existingMemberFirstName = v;
    }

    public static void setNonMemberLastName(String v) {
        existingMemberLastName = v;
    }

    public static void setNonMemberUserId(String v) {
        existingMemberUserId = v;
    }

    // Deprecated - use NON_MEMBER methods instead
    @Deprecated
    public static void setExistingMemberToken(String v) {
        setNonMemberToken(v);
    }

    @Deprecated
    public static void setExistingMemberFirstName(String v) {
        setNonMemberFirstName(v);
    }

    @Deprecated
    public static void setExistingMemberLastName(String v) {
        setNonMemberLastName(v);
    }

    @Deprecated
    public static void setExistingMemberUserId(String v) {
        setNonMemberUserId(v);
    }

    public static void setNewUserToken(String v) {
        newUserToken = v;
    }

    public static void setNewUserFirstName(String v) {
        newUserFirstName = v;
    }

    public static void setNewUserLastName(String v) {
        newUserLastName = v;
    }

    public static void setNewUserUserId(String v) {
        newUserUserId = v;
    }

    public static void setMobile(String v) {
        mobile = v;
    }

    public static void setToken(String v) {
        token = v;
    }

    public static void setFirstName(String v) {
        firstName = v;
    }

    public static void setLastName(String v) {
        lastName = v;
    }

    public static void setUserId(String v) {
        userId = v;
    }

    // ============================================================
    // GETTERS
    // ============================================================
    public static String getMemberToken() {
        return memberToken;
    }

    public static String getMemberFirstName() {
        return memberFirstName;
    }

    public static String getMemberLastName() {
        return memberLastName;
    }

    public static String getMemberUserId() {
        return memberUserId;
    }

    // NON-MEMBER getters (Mobile: 8220220227 - NOT a paid member)
    public static String getNonMemberToken() {
        return existingMemberToken;
    }

    public static String getNonMemberFirstName() {
        return existingMemberFirstName;
    }

    public static String getNonMemberLastName() {
        return existingMemberLastName;
    }

    public static String getNonMemberUserId() {
        return existingMemberUserId;
    }

    // Deprecated - use NON_MEMBER methods instead
    @Deprecated
    public static String getExistingMemberToken() {
        return getNonMemberToken();
    }

    @Deprecated
    public static String getExistingMemberFirstName() {
        return getNonMemberFirstName();
    }

    @Deprecated
    public static String getExistingMemberLastName() {
        return getNonMemberLastName();
    }

    @Deprecated
    public static String getExistingMemberUserId() {
        return getNonMemberUserId();
    }

    public static String getNewUserToken() {
        return newUserToken;
    }

    public static String getNewUserFirstName() {
        return newUserFirstName;
    }

    public static String getNewUserLastName() {
        return newUserLastName;
    }

    public static String getNewUserUserId() {
        return newUserUserId;
    }

    public static String getMobile() {
        return mobile;
    }

    public static String getToken() {
        return token;
    }

    public static String getFirstName() {
        return firstName;
    }

    public static String getLastName() {
        return lastName;
    }

    public static String getUserId() {
        return userId;
    }

    // ----------------------------------------------------
    // GLOBAL SEARCH TEST STORAGE
    // ----------------------------------------------------
    private static List<Map<String, Object>> globalTests = new ArrayList<>();

    public static void storeGlobalTests(List<Map<String, Object>> tests) {
        globalTests.clear();
        globalTests.addAll(tests);
    }

    public static List<Map<String, Object>> getGlobalTests() {
        return globalTests;
    }

    // Stores selected tests from global search
    private static final Map<String, Map<String, Object>> selectedTests = new HashMap<>();

    // ----------------------------------------------------
    // SELECTED TEST STORAGE
    // ----------------------------------------------------
    public static void storeTest(String testName, Map<String, Object> testData) {
        selectedTests.put(testName, testData);
    }

    public static Map<String, Object> getTest(String testName) {
        return selectedTests.get(testName);
    }

    public static Map<String, Map<String, Object>> getAllTests() {
        return selectedTests;
    }

    public static void clearAllTests() {
        selectedTests.clear();
    }

    // ============================================================
    // CART STORAGE (Separate for each user type)
    // ============================================================
    private static String memberCartId;
    private static Integer memberCartNumericId;
    private static Integer memberTotalAmount;
    private static Map<String, Object> memberAddToCartResponse;
    private static Map<String, Object> memberGetCartResponse;
    private static List<Map<String, Object>> memberCartItems;

    private static String existingMemberCartId;
    private static Integer existingMemberCartNumericId;
    private static Integer existingMemberTotalAmount;
    private static Map<String, Object> existingMemberAddToCartResponse;
    private static Map<String, Object> existingMemberGetCartResponse;
    private static List<Map<String, Object>> existingMemberCartItems;

    private static String newUserCartId;
    private static Integer newUserCartNumericId;
    private static Integer newUserTotalAmount;
    private static Map<String, Object> newUserAddToCartResponse;
    private static Map<String, Object> newUserGetCartResponse;
    private static List<Map<String, Object>> newUserCartItems;

    // Cart setters for Member
    public static void setMemberCartId(String id) {
        memberCartId = id;
    }

    public static void setMemberCartNumericId(Integer id) {
        memberCartNumericId = id;
    }

    public static void setMemberTotalAmount(Integer amount) {
        memberTotalAmount = amount;
    }

    public static void setMemberAddToCartResponse(Map<String, Object> response) {
        memberAddToCartResponse = response;
    }

    public static void setMemberGetCartResponse(Map<String, Object> response) {
        memberGetCartResponse = response;
    }

    public static void setMemberCartItems(List<Map<String, Object>> items) {
        memberCartItems = items;
    }

    // Cart setters for NON-MEMBER (Mobile: 8220220227)
    public static void setNonMemberCartId(String id) {
        existingMemberCartId = id;
    }

    public static void setNonMemberCartNumericId(Integer id) {
        existingMemberCartNumericId = id;
    }

    public static void setNonMemberTotalAmount(Integer amount) {
        existingMemberTotalAmount = amount;
    }

    public static void setNonMemberAddToCartResponse(Map<String, Object> response) {
        existingMemberAddToCartResponse = response;
    }

    public static void setNonMemberGetCartResponse(Map<String, Object> response) {
        existingMemberGetCartResponse = response;
    }

    public static void setNonMemberCartItems(List<Map<String, Object>> items) {
        existingMemberCartItems = items;
    }

    // Cart setters for Existing Member (Deprecated - use NON_MEMBER)
    @Deprecated
    public static void setExistingMemberCartId(String id) {
        setNonMemberCartId(id);
    }

    @Deprecated
    public static void setExistingMemberCartNumericId(Integer id) {
        setNonMemberCartNumericId(id);
    }

    @Deprecated
    public static void setExistingMemberTotalAmount(Integer amount) {
        setNonMemberTotalAmount(amount);
    }

    @Deprecated
    public static void setExistingMemberAddToCartResponse(Map<String, Object> response) {
        setNonMemberAddToCartResponse(response);
    }

    @Deprecated
    public static void setExistingMemberGetCartResponse(Map<String, Object> response) {
        setNonMemberGetCartResponse(response);
    }

    @Deprecated
    public static void setExistingMemberCartItems(List<Map<String, Object>> items) {
        setNonMemberCartItems(items);
    }

    // Cart setters for New User
    public static void setNewUserCartId(String id) {
        newUserCartId = id;
    }

    public static void setNewUserCartNumericId(Integer id) {
        newUserCartNumericId = id;
    }

    public static void setNewUserTotalAmount(Integer amount) {
        newUserTotalAmount = amount;
    }

    public static void setNewUserAddToCartResponse(Map<String, Object> response) {
        newUserAddToCartResponse = response;
    }

    public static void setNewUserGetCartResponse(Map<String, Object> response) {
        newUserGetCartResponse = response;
    }

    public static void setNewUserCartItems(List<Map<String, Object>> items) {
        newUserCartItems = items;
    }

    // Cart getters for Member
    public static String getMemberCartId() {
        return memberCartId;
    }

    public static Integer getMemberCartNumericId() {
        return memberCartNumericId;
    }

    public static Integer getMemberTotalAmount() {
        return memberTotalAmount;
    }

    public static Map<String, Object> getMemberAddToCartResponse() {
        return memberAddToCartResponse;
    }

    public static Map<String, Object> getMemberGetCartResponse() {
        return memberGetCartResponse;
    }

    public static List<Map<String, Object>> getMemberCartItems() {
        return memberCartItems;
    }

    // Cart getters for NON-MEMBER (Mobile: 8220220227)
    public static String getNonMemberCartId() {
        return existingMemberCartId;
    }

    public static Integer getNonMemberCartNumericId() {
        return existingMemberCartNumericId;
    }

    public static Integer getNonMemberTotalAmount() {
        return existingMemberTotalAmount;
    }

    public static Map<String, Object> getNonMemberAddToCartResponse() {
        return existingMemberAddToCartResponse;
    }

    public static Map<String, Object> getNonMemberGetCartResponse() {
        return existingMemberGetCartResponse;
    }

    public static List<Map<String, Object>> getNonMemberCartItems() {
        return existingMemberCartItems;
    }

    // Cart getters for Existing Member (Deprecated - use NON_MEMBER)
    @Deprecated
    public static String getExistingMemberCartId() {
        return getNonMemberCartId();
    }

    @Deprecated
    public static Integer getExistingMemberCartNumericId() {
        return getNonMemberCartNumericId();
    }

    @Deprecated
    public static Integer getExistingMemberTotalAmount() {
        return getNonMemberTotalAmount();
    }

    @Deprecated
    public static Map<String, Object> getExistingMemberAddToCartResponse() {
        return getNonMemberAddToCartResponse();
    }

    @Deprecated
    public static Map<String, Object> getExistingMemberGetCartResponse() {
        return getNonMemberGetCartResponse();
    }

    @Deprecated
    public static List<Map<String, Object>> getExistingMemberCartItems() {
        return getNonMemberCartItems();
    }

    // Cart getters for New User
    public static String getNewUserCartId() {
        return newUserCartId;
    }

    public static Integer getNewUserCartNumericId() {
        return newUserCartNumericId;
    }

    public static Integer getNewUserTotalAmount() {
        return newUserTotalAmount;
    }

    public static Map<String, Object> getNewUserAddToCartResponse() {
        return newUserAddToCartResponse;
    }

    public static Map<String, Object> getNewUserGetCartResponse() {
        return newUserGetCartResponse;
    }

    public static List<Map<String, Object>> getNewUserCartItems() {
        return newUserCartItems;
    }

    // Legacy methods for backward compatibility
    public static void storeCartId(String id) {
        memberCartId = id;
    }

    public static String getCartId() {
        return memberCartId;
    }

    // ============================================================
    // ADDRESS STORAGE (Separate for each user type)
    // ============================================================
    private static String memberAddressId;
    private static List<Map<String, Object>> memberAddresses;

    private static String existingMemberAddressId;
    private static List<Map<String, Object>> existingMemberAddresses;

    private static String newUserAddressId;
    private static List<Map<String, Object>> newUserAddresses;

    // Address setters for Member
    public static void setMemberAddressId(String id) {
        memberAddressId = id;
    }

    public static void setMemberAddresses(List<Map<String, Object>> addresses) {
        memberAddresses = addresses;
    }

    // Address setters for NON-MEMBER (Mobile: 8220220227)
    public static void setNonMemberAddressId(String id) {
        existingMemberAddressId = id;
    }

    public static void setNonMemberAddresses(List<Map<String, Object>> addresses) {
        existingMemberAddresses = addresses;
    }

    // Address setters for Existing Member (Deprecated - use NON_MEMBER)
    @Deprecated
    public static void setExistingMemberAddressId(String id) {
        setNonMemberAddressId(id);
    }

    @Deprecated
    public static void setExistingMemberAddresses(List<Map<String, Object>> addresses) {
        setNonMemberAddresses(addresses);
    }

    // Address setters for New User
    public static void setNewUserAddressId(String id) {
        newUserAddressId = id;
    }

    public static void setNewUserAddresses(List<Map<String, Object>> addresses) {
        newUserAddresses = addresses;
    }

    // Address getters for Member
    public static String getMemberAddressId() {
        return memberAddressId;
    }

    public static List<Map<String, Object>> getMemberAddresses() {
        return memberAddresses;
    }

    // Address getters for NON-MEMBER (Mobile: 8220220227)
    public static String getNonMemberAddressId() {
        return existingMemberAddressId;
    }

    public static List<Map<String, Object>> getNonMemberAddresses() {
        return existingMemberAddresses;
    }

    // Address getters for Existing Member (Deprecated - use NON_MEMBER)
    @Deprecated
    public static String getExistingMemberAddressId() {
        return getNonMemberAddressId();
    }

    @Deprecated
    public static List<Map<String, Object>> getExistingMemberAddresses() {
        return getNonMemberAddresses();
    }

    // Address getters for New User
    public static String getNewUserAddressId() {
        return newUserAddressId;
    }

    public static List<Map<String, Object>> getNewUserAddresses() {
        return newUserAddresses;
    }

    // ============================================================
    // SLOT STORAGE
    // ============================================================
    private static String slotStartDate;
    private static String existingMemberSlotGuid;
    private static String existingMemberSlotTime;
    private static String memberSlotGuid;
    private static String memberSlotTime;
    private static String newUserSlotGuid;
    private static String newUserSlotTime;

    // Slot setters
    public static void setSlotStartDate(String date) {
        slotStartDate = date;
    }

    public static void setNonMemberSlotGuid(String guid) {
        existingMemberSlotGuid = guid;
    }

    // Alias for compatibility
    public static void setNonMemberSlotId(String id) {
        setNonMemberSlotGuid(id);
    }

    public static void setNonMemberSlotTime(String time) {
        existingMemberSlotTime = time;
    }

    @Deprecated
    public static void setExistingMemberSlotGuid(String guid) {
        setNonMemberSlotGuid(guid);
    }

    public static void setMemberSlotGuid(String guid) {
        memberSlotGuid = guid;
    }

    public static void setMemberSlotId(String id) {
        setMemberSlotGuid(id);
    }

    public static void setMemberSlotTime(String time) {
        memberSlotTime = time;
    }

    public static void setNewUserSlotGuid(String guid) {
        newUserSlotGuid = guid;
    }

    public static void setNewUserSlotId(String id) {
        setNewUserSlotGuid(id);
    }

    public static void setNewUserSlotTime(String time) {
        newUserSlotTime = time;
    }

    // Slot getters
    public static String getSlotStartDate() {
        return slotStartDate;
    }

    public static String getNonMemberSlotGuid() {
        return existingMemberSlotGuid;
    }

    public static String getNonMemberSlotId() {
        return existingMemberSlotGuid;
    }

    public static String getNonMemberSlotTime() {
        return existingMemberSlotTime;
    }

    @Deprecated
    public static String getExistingMemberSlotGuid() {
        return getNonMemberSlotGuid();
    }

    public static String getMemberSlotGuid() {
        return memberSlotGuid;
    }

    public static String getMemberSlotId() {
        return memberSlotGuid;
    }

    public static String getMemberSlotTime() {
        return memberSlotTime;
    }

    public static String getNewUserSlotGuid() {
        return newUserSlotGuid;
    }

    public static String getNewUserSlotId() {
        return newUserSlotGuid;
    }

    public static String getNewUserSlotTime() {
        return newUserSlotTime;
    }

    // Generic getter for selected slot (used by all user types)
    public static String getSelectedSlotGuid() {
        // Return the first non-null slot GUID
        if (existingMemberSlotGuid != null)
            return existingMemberSlotGuid;
        if (memberSlotGuid != null)
            return memberSlotGuid;
        if (newUserSlotGuid != null)
            return newUserSlotGuid;
        return null;
    }

    // ============================================================
    // ORDER STORAGE (Separate for each user type)
    // ============================================================
    private static String memberOrderGuid;
    private static String memberOrderId;

    private static String existingMemberOrderGuid;
    private static String existingMemberOrderId;

    private static String newUserOrderGuid;
    private static String newUserOrderId;

    // Order setters for Member
    public static void setMemberOrderGuid(String guid) {
        memberOrderGuid = guid;
    }

    public static void setMemberOrderId(String id) {
        memberOrderId = id;
    }

    // Order setters for NON-MEMBER (Mobile: 8220220227)
    public static void setNonMemberOrderGuid(String guid) {
        existingMemberOrderGuid = guid;
    }

    public static void setNonMemberOrderId(String id) {
        existingMemberOrderId = id;
    }

    @Deprecated
    public static void setExistingMemberOrderGuid(String guid) {
        setNonMemberOrderGuid(guid);
    }

    @Deprecated
    public static void setExistingMemberOrderId(String id) {
        setNonMemberOrderId(id);
    }

    // Order setters for NEW USER
    public static void setNewUserOrderGuid(String guid) {
        newUserOrderGuid = guid;
    }

    public static void setNewUserOrderId(String id) {
        newUserOrderId = id;
    }

    // Order getters for Member
    public static String getMemberOrderGuid() {
        return memberOrderGuid;
    }

    public static String getMemberOrderId() {
        return memberOrderId;
    }

    // Order getters for NON-MEMBER
    public static String getNonMemberOrderGuid() {
        return existingMemberOrderGuid;
    }

    public static String getNonMemberOrderId() {
        return existingMemberOrderId;
    }

    @Deprecated
    public static String getExistingMemberOrderGuid() {
        return getNonMemberOrderGuid();
    }

    @Deprecated
    public static String getExistingMemberOrderId() {
        return getNonMemberOrderId();
    }

    // Order getters for NEW USER
    public static String getNewUserOrderGuid() {
        return newUserOrderGuid;
    }

    public static String getNewUserOrderId() {
        return newUserOrderId;
    }
}
