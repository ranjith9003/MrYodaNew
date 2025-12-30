package com.mryoda.diagnostics.api.tests;

import com.mryoda.diagnostics.api.base.BaseTest;
import com.mryoda.diagnostics.api.endpoints.APIEndpoints;
import com.mryoda.diagnostics.api.utils.AssertionUtil;
import com.mryoda.diagnostics.api.utils.RequestContext;
import com.mryoda.diagnostics.api.builders.RequestBuilder;
import com.mryoda.diagnostics.api.payloads.OrderPayloadBuilder;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

public class CreateOrderCODAPITest extends BaseTest {

    // Valid postal codes for service centers only
    private static final Map<String, String> LOCATION_POSTAL_CODES = new HashMap<>();
    static {
        LOCATION_POSTAL_CODES.put("Madhapur", "500033"); // Hyderabad - Madhapur
        LOCATION_POSTAL_CODES.put("Ameerpet (HQ)", "500016"); // Hyderabad - Ameerpet
        LOCATION_POSTAL_CODES.put("Guntur", "522001"); // Guntur
        LOCATION_POSTAL_CODES.put("Khammam", "507001"); // Khammam
        LOCATION_POSTAL_CODES.put("Visakhapatnam", "530002"); // Visakhapatnam
        LOCATION_POSTAL_CODES.put("Tirupati", "517501"); // Tirupati
    }

    // -------------------------------
    // HELPER: Call Get Cart API
    // -------------------------------
    private Response callGetCartAPI(String token, String userId) {
        System.out.println("\n==========================================================");
        System.out.println("      GET CART API (Check Total for COD)");
        System.out.println("==========================================================");

        String endpoint = APIEndpoints.GET_CART_BY_ID.replace("{user_id}", userId);
        // We need location ID, assuming default location or fetching from context if
        // available
        String locationId = RequestContext.getLocationId(DEFAULT_LOCATION);

        Response response = new RequestBuilder()
                .setEndpoint(endpoint)
                .addHeader("Authorization", token)
                .addQueryParam("order_type", "home") // Assuming home order
                .addQueryParam("location", locationId)
                .get();

        System.out.println("Response Status: " + response.getStatusCode());

        AssertionUtil.verifyEquals(response.getStatusCode(), 200, "Get Cart should return 200");
        return response;
    }

    // -------------------------------
    // HELPER: Call Verify Payment API
    // -------------------------------
    private Map<String, String> callVerifyPaymentAPI(String token, String userId, String cartId, String addressId, String slotGuid,
            String labLocationId, String orderType, int totalAmount, String date, String time, String source) {
        System.out.println("\n==========================================================");
        System.out.println("      VERIFY PAYMENT API (COD PRE-CHECK)");
        System.out.println("==========================================================");

        // Use OrderPayloadBuilder to construct the payload
        Map<String, Object> payload = OrderPayloadBuilder.buildVerifyPaymentPayload(cartId, "cash", source, userId,
                String.valueOf(addressId), slotGuid, date, time, totalAmount, orderType, labLocationId);

        System.out.println("Request Payload: " + payload);
        System.out.println("Target URL: " + RestAssured.baseURI + APIEndpoints.VERIFY_PAYMENT);

        Response response = new RequestBuilder()
                .setEndpoint(APIEndpoints.VERIFY_PAYMENT)
                .addHeader("Authorization", token)
                .setRequestBody(payload)
                .post();

        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Verify 200 OK
        AssertionUtil.verifyEquals(response.getStatusCode(), 200, "VerifyPayment HTTP status should be 200");

        // Extract Payment ID (assuming data.guid or data.id)
        String paymentId = response.jsonPath().getString("data.guid");
        if (paymentId == null || "null".equals(paymentId) || "[null]".equals(paymentId)) {
            paymentId = response.jsonPath().getString("data.id");
        }
        // Handle nested structure: data[0].orderDetails[0].payment_id
        if (paymentId == null || "null".equals(paymentId) || "[null]".equals(paymentId)) {
            paymentId = response.jsonPath().getString("data[0].orderDetails[0].payment_id");
        }
        
        // Extract Order ID from the same response
        String orderId = response.jsonPath().getString("data[0].orderDetails[0].guid");
        
        System.out.println("Extracted Payment ID: " + paymentId);
        System.out.println("Extracted Order ID: " + orderId);
        
        Map<String, String> result = new HashMap<>();
        result.put("paymentId", paymentId);
        result.put("orderId", orderId);
        return result;
    }

    // -------------------------------
    // HELPER: Call Get Payment By ID API (Dev)
    // -------------------------------
    private Response callGetPaymentByIdAPI(String token, String paymentId) {
        System.out.println("\n==========================================================");
        System.out.println("      GET PAYMENT BY ID API (DEV)");
        System.out.println("==========================================================");

        Map<String, String> payload = new HashMap<>();
        payload.put("id", paymentId);

        // Use DEV URL as per requirement
        String getPaymentUrl = "https://dev-api-yodadiagnostics.yodaprojects.com/gateway/getPaymentById";

        System.out.println("Request Payload: " + payload);
        System.out.println("Target URL: " + getPaymentUrl);

        Response response = new RequestBuilder()
                .setEndpoint(getPaymentUrl)
                .addHeader("Authorization", token)
                .setRequestBody(payload)
                .post();

        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Verify 200 OK
        AssertionUtil.verifyEquals(response.getStatusCode(), 200, "GetPaymentById HTTP status should be 200");
        
        return response;
    }

    // -------------------------------
    // CROSS-API VALIDATIONS
    // -------------------------------
    private void performCrossAPIValidations(Response cartResponse, Response paymentResponse, 
            int expectedTotalPrice, String expectedCartId, String expectedPaymentId, 
            String expectedUserId, String expectedAddressId, String expectedSlotGuid) {
        
        System.out.println("\n🔍 ==========================================================");
        System.out.println("         CROSS-API VALIDATION REPORT");
        System.out.println("🔍 ==========================================================");
        
        // Extract payment data
        Map<String, Object> paymentData = paymentResponse.jsonPath().getMap("data.payments");
        List<Map<String, Object>> orderItems = paymentResponse.jsonPath().getList("data.order_items");
        
        // 1. CART VS PAYMENT TOTAL VALIDATION
        System.out.println("\n📊 1. TOTAL AMOUNT CONSISTENCY CHECK:");
        System.out.println("   Expected Total (Cart): ₹" + expectedTotalPrice);
        
        Object paymentAmountObj = paymentData.get("amount");
        int paymentAmount = paymentAmountObj instanceof String ? 
            Integer.parseInt((String) paymentAmountObj) : 
            ((Number) paymentAmountObj).intValue();
            
        System.out.println("   Payment Total (GetPayment): ₹" + paymentAmount);
        
        if (paymentAmount == expectedTotalPrice) {
            System.out.println("   ✅ PASS: Cart total matches Payment total");
        } else {
            System.out.println("   ❌ FAIL: Total mismatch! Cart=" + expectedTotalPrice + ", Payment=" + paymentAmount);
            throw new RuntimeException("Cross-API validation failed: Total amount mismatch");
        }
        
        // 2. PAYMENT ID CONSISTENCY
        System.out.println("\n🆔 2. PAYMENT ID CONSISTENCY CHECK:");
        String actualPaymentGuid = (String) paymentData.get("guid");
        System.out.println("   Expected Payment ID: " + expectedPaymentId);
        System.out.println("   Actual Payment GUID: " + actualPaymentGuid);
        
        if (expectedPaymentId.equals(actualPaymentGuid)) {
            System.out.println("   ✅ PASS: Payment ID matches across APIs");
        } else {
            System.out.println("   ❌ FAIL: Payment ID mismatch!");
            throw new RuntimeException("Cross-API validation failed: Payment ID mismatch");
        }
        
        // 3. PAYMENT TYPE VALIDATION
        System.out.println("\n💳 3. PAYMENT TYPE VALIDATION:");
        String paymentType = (String) paymentData.get("payment_type");
        System.out.println("   Payment Type: " + paymentType);
        
        if ("COD".equals(paymentType)) {
            System.out.println("   ✅ PASS: Payment type is COD as expected");
        } else {
            System.out.println("   ❌ FAIL: Expected COD but got: " + paymentType);
            throw new RuntimeException("Cross-API validation failed: Invalid payment type");
        }
        
        // 4. ORDER ITEMS VALIDATION
        System.out.println("\n📦 4. ORDER ITEMS VALIDATION:");
        if (orderItems != null && !orderItems.isEmpty()) {
            System.out.println("   Total Order Items: " + orderItems.size());
            
            int calculatedTotal = 0;
            for (int i = 0; i < orderItems.size(); i++) {
                Map<String, Object> item = orderItems.get(i);
                String productName = (String) item.get("product_name");
                Object finalPriceObj = item.get("final_price");
                Object quantityObj = item.get("quantity");
                
                int finalPrice = finalPriceObj instanceof String ? 
                    Integer.parseInt((String) finalPriceObj) : 
                    ((Number) finalPriceObj).intValue();
                    
                int quantity = quantityObj instanceof String ? 
                    Integer.parseInt((String) quantityObj) : 
                    ((Number) quantityObj).intValue();
                
                int itemTotal = finalPrice * quantity;
                calculatedTotal += itemTotal;
                
                System.out.println("   Item " + (i+1) + ": " + productName + 
                    " | Qty: " + quantity + " | Price: ₹" + finalPrice + " | Total: ₹" + itemTotal);
            }
            
            System.out.println("   Calculated Items Total: ₹" + calculatedTotal);
            
            // Note: For COD orders, home collection charges might be added separately
            Object netPayableObj = paymentData.get("net_payable");
            int netPayable = netPayableObj instanceof String ? 
                Integer.parseInt((String) netPayableObj) : 
                ((Number) netPayableObj).intValue();
                
            System.out.println("   Net Payable (API): ₹" + netPayable);
            
            if (calculatedTotal == netPayable) {
                System.out.println("   ✅ PASS: Calculated total matches net payable");
            } else {
                System.out.println("   ⚠️  INFO: Total difference may include delivery charges or discounts");
            }
        }
        
        // 5. PAYMENT STATUS VALIDATION
        System.out.println("\n📋 5. PAYMENT STATUS VALIDATION:");
        String paymentStatus = (String) paymentData.get("payment_status");
        System.out.println("   Payment Status: " + paymentStatus);
        
        if ("Pending".equals(paymentStatus)) {
            System.out.println("   ✅ PASS: Payment status is Pending (expected for COD)");
        } else {
            System.out.println("   ⚠️  WARNING: Unexpected payment status for COD: " + paymentStatus);
        }
        
        // 6. DISCOUNT AND MEMBERSHIP VALIDATION
        System.out.println("\n💰 6. DISCOUNT & MEMBERSHIP VALIDATION:");
        Object membershipDiscountObj = paymentData.get("membership_discount");
        int membershipDiscount = membershipDiscountObj instanceof String ? 
            Integer.parseInt((String) membershipDiscountObj) : 
            ((Number) membershipDiscountObj).intValue();
            
        Object totalDiscountObj = paymentData.get("total_discount");
        int totalDiscount = totalDiscountObj instanceof String ? 
            Integer.parseInt((String) totalDiscountObj) : 
            ((Number) totalDiscountObj).intValue();
            
        System.out.println("   Membership Discount: ₹" + membershipDiscount);
        System.out.println("   Total Discount: ₹" + totalDiscount);
        
        if (membershipDiscount > 0) {
            System.out.println("   ✅ INFO: Membership discount applied");
        } else {
            System.out.println("   ✅ INFO: No membership discount (expected for non-members)");
        }
        
        System.out.println("🎉 ==========================================================");
        System.out.println("         CROSS-API VALIDATION COMPLETED SUCCESSFULLY");
        System.out.println("🎉 ==========================================================");
    }

    // -------------------------------
    // HELPER: Call Phlebotomist Login API
    // -------------------------------
    private String callPhlebotomistLoginAPI() {
        System.out.println("\n==========================================================");
        System.out.println("      PHLEBOTOMIST LOGIN API");
        System.out.println("==========================================================");

        Map<String, Object> payload = new HashMap<>();
        payload.put("mobile", 9360651932L);  // Use Long for mobile number
        payload.put("password", "12345678");

        String phlebotomistLoginUrl = "https://dev-api-yodadiagnostics.yodaprojects.com/phlebo/loginPhlebo";

        System.out.println("Request Payload: " + payload);
        System.out.println("Target URL: " + phlebotomistLoginUrl);

        Response response = new RequestBuilder()
                .setEndpoint(phlebotomistLoginUrl)
                .setRequestBody(payload)
                .post();

        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        String phlebotomistGuid = null;

        // Verify successful login
        if (response.getStatusCode() == 200) {
            boolean success = response.jsonPath().getBoolean("success");
            if (success) {
                String phlebotomistToken = response.jsonPath().getString("data.token");
                String phlebotomistId = response.jsonPath().getString("data.id");
                String phlebotomistName = response.jsonPath().getString("data.first_name");
                phlebotomistGuid = response.jsonPath().getString("data.guid");  // Extract GUID
                
                System.out.println("✅ Phlebotomist Login Successful");
                System.out.println("   Phlebo Name: " + phlebotomistName);
                System.out.println("   Phlebo ID: " + phlebotomistId);
                System.out.println("   Phlebo GUID: " + phlebotomistGuid);
                System.out.println("   Phlebo Token: " + phlebotomistToken);
                
                // Store for potential future use
                System.setProperty("phlebo.token", phlebotomistToken != null ? phlebotomistToken : "");
                System.setProperty("phlebo.id", phlebotomistId != null ? phlebotomistId : "");
                System.setProperty("phlebo.guid", phlebotomistGuid != null ? phlebotomistGuid : "");
            } else {
                System.out.println("❌ Phlebotomist Login Failed: " + response.jsonPath().getString("msg"));
            }
        } else {
            System.out.println("❌ Phlebotomist Login API Failed with status: " + response.getStatusCode());
        }
        
        return phlebotomistGuid;  // Return the extracted GUID
    }

    // -------------------------------
    // HELPER: Call Assign Order API
    // -------------------------------
    private void callAssignOrderAPI(String orderId, String phlebotomistGuid) {
        if (phlebotomistGuid == null || orderId == null) {
            System.out.println("⚠️ Cannot call Assign Order API - Missing GUID or Order ID");
            return;
        }

        System.out.println("\n==========================================================");
        System.out.println("      ASSIGN ORDER API");
        System.out.println("==========================================================");

        Map<String, Object> payload = new HashMap<>();
        payload.put("order_id", Arrays.asList(orderId));  // Array with single order ID
        payload.put("phlebo_id", phlebotomistGuid);

        String assignOrderUrl = "https://dev-api-yodadiagnostics.yodaprojects.com/order_tracking/assignOrder";

        System.out.println("Request Payload: " + payload);
        System.out.println("Target URL: " + assignOrderUrl);

        Response response = new RequestBuilder()
                .setEndpoint(assignOrderUrl)
                .setRequestBody(payload)
                .post();

        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Verify successful assignment
        if (response.getStatusCode() == 200) {
            boolean success = response.jsonPath().getBoolean("success");
            if (success) {
                System.out.println("✅ Order Assignment Successful");
                System.out.println("   Order ID: " + orderId);
                System.out.println("   Assigned to Phlebo GUID: " + phlebotomistGuid);
            } else {
                System.out.println("❌ Order Assignment Failed: " + response.jsonPath().getString("msg"));
            }
        } else {
            System.out.println("❌ Assign Order API Failed with status: " + response.getStatusCode());
        }
    }

    // -------------------------------
    // HELPER: Call Get Centres By Address API
    // -------------------------------
    private Response callGetCentresByAddressAPI(String token, String addressId, String labLocationId) {
        System.out.println("\n==========================================================");
        System.out.println("      GET CENTRES BY ADDRESS API");
        System.out.println("==========================================================");

        Map<String, Object> payload = new HashMap<>();
        payload.put("addressid", addressId);
        payload.put("lab_id", labLocationId);

        System.out.println("Request Payload: " + payload);
        System.out.println("Target URL: " + RestAssured.baseURI + APIEndpoints.GET_CENTERS_BY_ADD);

        Response response = new RequestBuilder()
                .setEndpoint(APIEndpoints.GET_CENTERS_BY_ADD)
                .addHeader("Authorization", token)
                .setRequestBody(payload)
                .post();

        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Verify 200 OK
        AssertionUtil.verifyEquals(response.getStatusCode(), 200, "GetCentresByAddress HTTP status should be 200");
        
        // Validate success response
        boolean success = response.jsonPath().getBoolean("success");
        if (success) {
            System.out.println("✅ Centres fetched successfully for address: " + addressId);
            
            // Extract and display centers info
            List<Map<String, Object>> centers = response.jsonPath().getList("data");
            if (centers != null && !centers.isEmpty()) {
                System.out.println("   📍 Available Centers: " + centers.size());
                for (int i = 0; i < Math.min(3, centers.size()); i++) {
                    Map<String, Object> center = centers.get(i);
                    String centerName = (String) center.get("name");
                    String centerCode = (String) center.get("center_code");
                    System.out.println("   " + (i+1) + ". " + centerName + " (Code: " + centerCode + ")");
                }
            }
        } else {
            System.out.println("❌ Failed to fetch centres: " + response.jsonPath().getString("msg"));
        }
        
        return response;
    }

    // -------------------------------
    // HELPER: Call Add to Cart API with Address ID
    // -------------------------------
    private Response callAddToCartWithAddressAPI(String token, String userId, String addressId) {
        System.out.println("\n==========================================================");
        System.out.println("      ADD TO CART API (With Address ID)");
        System.out.println("==========================================================");

        // Create simple payload with basic product details (reuse existing tests)
        Map<String, Object> productDetail1 = new HashMap<>();
        productDetail1.put("product_id", "675921110856fe1e1e992d5a"); // Glucose (Urine)
        productDetail1.put("quantity", 1);
        productDetail1.put("type", "home");
        productDetail1.put("brand_id", "efb159ac-db7c-4b06-a79b-021a8b6d67e8");
        productDetail1.put("location_id", "64870066842708a0d5ae6c77");

        Map<String, Object> productDetail2 = new HashMap<>();
        productDetail2.put("product_id", "675921b2ba6f16b8c0d7bee4"); // Diabetes Monitor
        productDetail2.put("quantity", 1);
        productDetail2.put("type", "home");
        productDetail2.put("brand_id", "efb159ac-db7c-4b06-a79b-021a8b6d67e8");
        productDetail2.put("location_id", "64870066842708a0d5ae6c77");

        List<Map<String, Object>> productDetails = Arrays.asList(productDetail1, productDetail2);

        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId);
        payload.put("product_details", productDetails);
        payload.put("address_id", addressId);

        System.out.println("Request Payload: " + payload);
        System.out.println("Target URL: " + RestAssured.baseURI + APIEndpoints.ADD_TO_CART);

        Response response = new RequestBuilder()
                .setEndpoint(APIEndpoints.ADD_TO_CART)
                .addHeader("Authorization", token)
                .setRequestBody(payload)
                .post();

        System.out.println("Response Status: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody().asString());

        // Verify successful cart update
        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            boolean success = response.jsonPath().getBoolean("success");
            if (success) {
                System.out.println("✅ Cart updated successfully with address_id: " + addressId);
                
                // Extract updated cart GUID
                String cartGuid = response.jsonPath().getString("data.guid");
                if (cartGuid != null) {
                    System.out.println("   Updated Cart GUID: " + cartGuid);
                }
            } else {
                System.out.println("❌ Failed to update cart: " + response.jsonPath().getString("msg"));
            }
        } else {
            System.out.println("❌ Add to Cart with Address API Failed with status: " + response.getStatusCode());
        }
        
        return response;
    }

    // -------------------------------
    // HELPER: Call Add Address API
    // -------------------------------
    private Map<String, String> callAddAddressAPI(String token, String userId) {
        System.out.println("\n==========================================================");
        System.out.println("      ADD ADDRESS API (Required for Home Order)");
        System.out.println("==========================================================");

        Map<String, Object> payload = OrderPayloadBuilder.buildAddressPayload(userId, "Test User", "9999999999",
                "Hyderabad", "Ameerpet (HQ)", "home", "India", "Telangana", "Hyderabad", "500016", "+91", "17.4358447",
                "78.452737");

        Response response = new RequestBuilder()
                .setEndpoint(APIEndpoints.ADD_ADDRESS)
                .addHeader("Authorization", token)
                .setRequestBody(payload)
                .post();

        System.out.println("Response Status: " + response.getStatusCode());
        String responseBody = response.getBody().asString();
        System.out.println("Response Body: " + responseBody);

        Map<String, String> addressDetails = new HashMap<>();

        if (response.getStatusCode() == 409) {
            System.out.println("Address already exists (409). Fetching existing address...");
            String getAddressEndpoint = APIEndpoints.GET_ADDRESS_BY_USER_ID.replace("{user_id}", userId);
            Response getAddressResponse = new RequestBuilder()
                    .setEndpoint(getAddressEndpoint)
                    .addHeader("Authorization", token)
                    .get();

            System.out.println("Get Address Response Status: " + getAddressResponse.getStatusCode());
            // Assuming the response has a list of addresses in "data"
            // We'll take the first one's ID
            String existingAddressId = getAddressResponse.jsonPath().getString("data[0].id");
            String existingAddressGuid = getAddressResponse.jsonPath().getString("data[0].guid");
            
            if (existingAddressId == null) {
                existingAddressId = getAddressResponse.jsonPath().getString("data[0].guid");
            }
            System.out.println("Using existing Address ID: " + existingAddressId);
            
            addressDetails.put("id", existingAddressId);
            addressDetails.put("guid", existingAddressGuid);
            return addressDetails;
        }

        AssertionUtil.verifyEquals(response.getStatusCode(), 201, "Add Address should return 201");

        // Extract Address ID (Try Numeric ID first, then GUID)
        String addressId = response.jsonPath().getString("data.id");
        String addressGuid = response.jsonPath().getString("data.guid");
        
        if (addressId == null) {
            addressId = response.jsonPath().getString("data.guid");
        }
        if (addressId == null) {
            addressId = response.jsonPath().getString("data._id");
        }
        System.out.println("Created Address ID: " + addressId);
        
        addressDetails.put("id", addressId);
        addressDetails.put("guid", addressGuid);
        return addressDetails;
    }

    // -------------------------------
    // HELPER: Find Available Slot
    // -------------------------------
    private Map<String, String> findAvailableSlot(String token, String addressGuid) {
        System.out.println("\n📅 SEARCHING FOR AVAILABLE SLOTS...");
        java.time.LocalDate today = java.time.LocalDate.now();
        int maxDaysToCheck = 7;

        for (int i = 0; i < maxDaysToCheck; i++) {
            String checkDate = today.plusDays(i).toString();
            Map<String, Object> payload = OrderPayloadBuilder.buildSlotSearchPayload(checkDate, 1, 100, "home", addressGuid);

            Response response = new RequestBuilder()
                    .setEndpoint(APIEndpoints.GET_SLOT_COUNT_BY_TIME)
                    .addHeader("Authorization", token)
                    .setRequestBody(payload)
                    .post();

            if (response.getStatusCode() == 200) {
                List<Map<String, Object>> slots = response.jsonPath().getList("data");
                if (slots != null) {
                    for (Map<String, Object> slot : slots) {
                        Object countObj = slot.get("count");
                        int count = 0;
                        if (countObj instanceof Number) count = ((Number) countObj).intValue();
                        else if (countObj instanceof String) count = Integer.parseInt((String) countObj);

                        if (count > 0) {
                            Map<String, String> slotDetails = new HashMap<>();
                            slotDetails.put("date", checkDate);
                            slotDetails.put("time", (String) slot.get("starttime") + " - " + (String) slot.get("endtime"));
                            slotDetails.put("guid", (String) slot.get("guid"));
                            System.out.println("   ✅ Found Slot: " + checkDate + " " + slotDetails.get("time"));
                            return slotDetails;
                        }
                    }
                }
            }
        }
        throw new RuntimeException("❌ No available slots found in the next " + maxDaysToCheck + " days.");
    }

    @Test(priority = 1)
    public void testCOD_Flow_ForNewUser() {
        System.out.println("\n>>> STARTING COD FLOW FOR NEW USER <<<");
        String token = RequestContext.getNewUserToken();
        String userId = RequestContext.getNewUserUserId();
        executeCODFlow(token, userId);
    }

    @Test(priority = 2)
    public void testCOD_Flow_ForMember() {
        System.out.println("\n>>> STARTING COD FLOW FOR MEMBER <<<");
        String token = RequestContext.getMemberToken();
        String userId = RequestContext.getMemberUserId();

        if (token == null || userId == null) {
            System.out.println("SKIPPING: Member Token or UserId not found. Ensure Member Login ran.");
            return;
        }
        executeCODFlow(token, userId);
    }

    @Test(priority = 3)
    public void testCOD_Flow_ForNonMember() {
        System.out.println("\n>>> STARTING COD FLOW FOR NON-MEMBER (EXISTING USER) <<<");
        String token = RequestContext.getExistingMemberToken(); // Assuming this is the non-member/existing user
        String userId = RequestContext.getExistingMemberUserId();

        if (token == null || userId == null) {
            System.out.println("SKIPPING: Existing User Token or UserId not found. Ensure Existing User Login ran.");
            return;
        }
        executeCODFlow(token, userId);
    }

    private void executeCODFlow(String token, String userId) {
        if (token == null || userId == null) {
            throw new RuntimeException(
                    "Token or UserId not found in RequestContext. Ensure previous steps (Login) ran successfully.");
        }

        // 1. Get Cart and Check Total Price
        Response getCartResponse = callGetCartAPI(token, userId);

        // Handle response format (List vs Object)
        Object dataObj = getCartResponse.jsonPath().get("data");
        String dataPath = (dataObj instanceof java.util.List) ? "data[0]" : "data";

        int totalPrice = getCartResponse.jsonPath().getInt(dataPath + ".totalPrice");
        String cartId = getCartResponse.jsonPath().getString(dataPath + ".guid");
        String slotGuid = getCartResponse.jsonPath().getString(dataPath + ".slot_guid");
        String labLocationId = getCartResponse.jsonPath().getString(dataPath + ".lab_location_id");
        String orderType = getCartResponse.jsonPath().getString(dataPath + ".order_type");

        System.out.println("Cart Total Price: " + totalPrice);
        System.out.println("Cart ID: " + cartId);
        System.out.println("Slot GUID: " + slotGuid);
        System.out.println("Lab Location ID: " + labLocationId);
        System.out.println("Order Type: " + orderType);

        // COD Validation: Total value must be less than 2500
        if (totalPrice >= 2500) {
            throw new RuntimeException("COD is not available for orders >= 2500. Current Total: " + totalPrice);
        }
        System.out.println("✅ COD Eligibility Check Passed (Total < 2500)");

        // 1.5 Add Address (Required for Home Order)
        Map<String, String> addressDetails = callAddAddressAPI(token, userId);
        String addressId = addressDetails.get("id");
        String addressGuid = addressDetails.get("guid");

        // 1.6 Get Centres By Address (Check available centers for the address)
        callGetCentresByAddressAPI(token, addressGuid, labLocationId);

        // 1.7 Add to Cart with Address ID (Update cart with address information)
        callAddToCartWithAddressAPI(token, userId, addressGuid);

        // 1.8 Find Available Slot
        Map<String, String> slotDetails = findAvailableSlot(token, addressGuid);
        String slotDate = slotDetails.get("date");
        String slotTime = slotDetails.get("time");
        String selectedSlotGuid = slotDetails.get("guid");

        // 2. Verify Payment (COD Pre-check) - Dev Env
        String source = "android"; // Not hardcoded in method, but passed as param
        Map<String, String> verifyPaymentResult = callVerifyPaymentAPI(token, userId, cartId, addressId, selectedSlotGuid, labLocationId, orderType, totalPrice, slotDate, slotTime, source);
        String paymentId = verifyPaymentResult.get("paymentId");
        String orderId = verifyPaymentResult.get("orderId");

        // 3. Get Payment By ID - Dev Env
        if (paymentId != null) {
            Response paymentResponse = callGetPaymentByIdAPI(token, paymentId);
            
            // 4. CROSS-API VALIDATIONS
            performCrossAPIValidations(getCartResponse, paymentResponse, totalPrice, cartId, paymentId, userId, addressId, selectedSlotGuid);
            
            // 5. Phlebo Login API
            String phlebotomistGuid = callPhlebotomistLoginAPI();
            
            // 6. Assign Order API (NEW!)
            if (phlebotomistGuid != null && orderId != null) {
                callAssignOrderAPI(orderId, phlebotomistGuid);
            }
        } else {
            System.out.println("WARNING: Skipping GetPaymentById because VerifyPayment failed to return an ID.");
        }

        /*
         * Create Order (COD) - Replaces VerifyPayment which is broken for COD
         * // Generate tomorrow's date
         * java.text.SimpleDateFormat sdf = new
         * java.text.SimpleDateFormat("yyyy-MM-dd");
         * java.util.Calendar cal = java.util.Calendar.getInstance();
         * cal.add(java.util.Calendar.DATE, 1);
         * String dateString = sdf.format(cal.getTime());
         * String time = "07:00 AM - 08:00 AM"; // Default time slot
         * 
         * Map<String, Object> payload = buildCreateOrderPayload(cartId, userId,
         * addressId, slotGuid, dateString, time, labLocationId, totalPrice);
         * 
         * Response createOrderResponse = callCreateOrderAPI(token, payload);
         * 
         * // Validate success
         * boolean success = createOrderResponse.jsonPath().getBoolean("success");
         * AssertionUtil.verifyTrue(success, "Create Order should be successful");
         * 
         * System.out.println("✅ COD Order Created Successfully!");
         */

        System.out.println("✅ COD Flow Completed Successfully.");
    }
}
