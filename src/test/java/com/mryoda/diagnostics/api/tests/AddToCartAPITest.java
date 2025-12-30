package com.mryoda.diagnostics.api.tests;

import com.mryoda.diagnostics.api.base.BaseTest;
import com.mryoda.diagnostics.api.endpoints.APIEndpoints;
import com.mryoda.diagnostics.api.utils.AssertionUtil;
import com.mryoda.diagnostics.api.utils.RequestContext;
import com.mryoda.diagnostics.api.builders.RequestBuilder;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Add to Cart API Test Class
 * Handles /carts/v2/addCart endpoint
 * - Adds test items to cart with all required data
 * - Uses stored user_id, test_id, brand_id, location_id
 */
public class AddToCartAPITest extends BaseTest {

    private Map<String, Object> buildCartPayloadWithAllTests(String userId, String brandName, String locationName) {

        System.out.println("\n🔍 CUSTOM PAYLOAD BUILDER for " + userId);

        // Get brand and location IDs
        String brandId = RequestContext.getBrandId(brandName);
        String locationId = RequestContext.getLocationId(locationName);

        if (brandId == null || brandId.isEmpty()) {
            System.out.println("   ❌ Brand ID not found for '" + brandName + "'");
            return null;
        }

        if (locationId == null || locationId.isEmpty()) {
            System.out.println("   ❌ Location ID not found for '" + locationName + "'");
            return null;
        }

        Map<String, Map<String, Object>> allTests = RequestContext.getAllTests();
        if (allTests == null || allTests.isEmpty()) {
            System.out.println("   ⚠️  No tests found in RequestContext (must run GlobalSearch first)");
            return null;
        }

        final int MAX_TESTS_TO_CHECK = 2; // Restore limit
        List<Map<String, Object>> productDetailsList = new ArrayList<>();
        int testsChecked = 0;

        for (Map.Entry<String, Map<String, Object>> entry : allTests.entrySet()) {
            if (testsChecked >= MAX_TESTS_TO_CHECK)
                break;

            Map<String, Object> testData = entry.getValue();
            String testId = (String) testData.get("_id");

            // Check home collection
            Object homeCollectionObj = testData.get("home_collection");
            boolean isHome = false;

            if (homeCollectionObj != null) {
                if (homeCollectionObj instanceof Boolean) {
                    isHome = (Boolean) homeCollectionObj;
                } else {
                    String s = homeCollectionObj.toString().trim();
                    isHome = s.equalsIgnoreCase("AVAILABLE") || s.equalsIgnoreCase("YES") || s.equalsIgnoreCase("TRUE")
                            || s.equals("1");
                }
            }

            String testType = isHome ? "home" : "lab";

            if (isHome) {
                Map<String, Object> productDetail = new HashMap<>();
                productDetail.put("product_id", testId);
                productDetail.put("quantity", 1);
                productDetail.put("type", testType);
                productDetail.put("brand_id", brandId);
                productDetail.put("location_id", locationId);

                // Reverting to List<String> as it was working (partially) and allows debugging
                List<String> familyMemberIds = new ArrayList<>();
                familyMemberIds.add(userId);
                productDetail.put("family_member_id", familyMemberIds);

                productDetailsList.add(productDetail);
                System.out.println("      ✅ Added test to payload: " + entry.getKey() + " (Type: " + testType + ")");
            } else {
                System.out.println("      ⏭️  Skipped test (No Home Collection): " + entry.getKey());
            }
            testsChecked++;
        }

        if (productDetailsList.isEmpty()) {
            System.out.println("   ⚠️  No home collection tests were added to payload.");
            return null;
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId);
        payload.put("product_details", productDetailsList);
        // Explicitly adding order_type as requested for home sample collection flow
        payload.put("order_type", "home");

        return payload;
    }

    private Response callAddToCartAPI(String token, Map<String, Object> payload) {

        // Add order_type='home' if home collection tests are present
        List<Map<String, Object>> products = (List<Map<String, Object>>) payload.get("product_details");
        boolean hasHome = false;
        String labLoc = null;

        for (Map<String, Object> p : products) {
            if ("home".equals(p.get("type")))
                hasHome = true;
            if (labLoc == null && p.get("location_id") != null)
                labLoc = p.get("location_id").toString();
        }

        if (hasHome && labLoc != null) {
            payload.put("order_type", "home");
            payload.put("lab_location_id", labLoc);
        }

        Response response = new RequestBuilder()
                .setEndpoint(APIEndpoints.ADD_TO_CART)
                .addHeader("Authorization", token)
                .setRequestBody(payload)
                .post();

        int statusCode = response.getStatusCode();
        if (statusCode != 200 && statusCode != 201) {
            System.out.println("❌ AddToCart Failed with " + statusCode + ": " + response.getBody().asString());
        }

        return response;
    }

    private void clearCart(String token, String userId) {
        System.out.println("\n🧹 CLEARING PREVIOUS CART for User ID: " + userId);
        String locationId = RequestContext.getLocationId(DEFAULT_LOCATION);

        Map<String, Object> payload = new HashMap<>();
        payload.put("user_id", userId);
        payload.put("product_details", new ArrayList<>()); // Empty list

        if (locationId != null) {
            payload.put("lab_location_id", locationId);
            payload.put("order_type", "home");
        }

        Response response = new RequestBuilder()
                .setEndpoint(APIEndpoints.ADD_TO_CART)
                .addHeader("Authorization", token)
                .setRequestBody(payload)
                .post();

        if (response.getStatusCode() == 200 || response.getStatusCode() == 201) {
            System.out.println("   ✅ Cart clear API call successful.");
            System.out.println("   📦 Response Body: " + response.getBody().asString());
        } else {
            System.out.println("   ⚠️  Note: Cart clearing returned status " + response.getStatusCode());
            System.out.println("   📦 Response Body: " + response.getBody().asString());
        }
    }

    private void validateAddToCartResponse(Response response, String userType) {
        if (response.getStatusCode() != 200 && response.getStatusCode() != 201) {
            Assert.fail("AddToCart failed with status " + response.getStatusCode());
        }

        Boolean success = response.jsonPath().getBoolean("success");
        AssertionUtil.verifyTrue(success, "Success flag");

        String cartGuid = response.jsonPath().getString("data.guid");
        Integer cartId = response.jsonPath().getInt("data.id");
        Integer totalAmount = response.jsonPath().getInt("total_amount");

        switch (userType) {
            case "MEMBER":
                RequestContext.setMemberCartId(cartGuid);
                RequestContext.setMemberCartNumericId(cartId);
                if (totalAmount != null)
                    RequestContext.setMemberTotalAmount(totalAmount);
                break;
            case "NON_MEMBER":
                RequestContext.setNonMemberCartId(cartGuid);
                RequestContext.setNonMemberCartNumericId(cartId);
                if (totalAmount != null)
                    RequestContext.setNonMemberTotalAmount(totalAmount);
                break;
            case "NEW_USER":
                RequestContext.setNewUserCartId(cartGuid);
                RequestContext.setNewUserCartNumericId(cartId);
                if (totalAmount != null)
                    RequestContext.setNewUserTotalAmount(totalAmount);
                break;
        }
        System.out.println("   ✅ Cart Data Stored for " + userType + " (GUID: " + cartGuid + ")");
    }

    @Test(priority = 8, dependsOnMethods = "com.mryoda.diagnostics.api.tests.GlobalSearchAPITest.testGlobalSearch_ForMember")
    public void testAddToCart_ForMember() {
        System.out.println("\n--- AddToCart For Member ---");
        String token = RequestContext.getMemberToken();
        String userId = RequestContext.getMemberUserId();

        // 1. Clear existing items first
        clearCart(token, userId);

        Map<String, Object> payload = buildCartPayloadWithAllTests(userId, "Diagnostics", DEFAULT_LOCATION);
        if (payload != null) {
            Response response = callAddToCartAPI(token, payload);
            validateAddToCartResponse(response, "MEMBER");
        }
    }

    @Test(priority = 8, dependsOnMethods = "com.mryoda.diagnostics.api.tests.GlobalSearchAPITest.testGlobalSearch_ForNonMember")
    public void testAddToCart_ForNonMember() {
        System.out.println("\n--- AddToCart For Non-Member ---");
        String token = RequestContext.getNonMemberToken();
        String userId = RequestContext.getNonMemberUserId();

        // 1. Clear existing items first
        clearCart(token, userId);

        Map<String, Object> payload = buildCartPayloadWithAllTests(userId, "Diagnostics", DEFAULT_LOCATION);
        if (payload != null) {
            Response response = callAddToCartAPI(token, payload);
            validateAddToCartResponse(response, "NON_MEMBER");
        }
    }

    @Test(priority = 9, dependsOnMethods = "com.mryoda.diagnostics.api.tests.GlobalSearchAPITest.testGlobalSearch_ForNewUser")
    public void testAddToCart_ForNewUser() {
        System.out.println("\n--- AddToCart For New User ---");
        String token = RequestContext.getNewUserToken();
        String userId = RequestContext.getNewUserUserId();

        // 1. Clear existing items first
        clearCart(token, userId);

        Map<String, Object> payload = buildCartPayloadWithAllTests(userId, "Diagnostics", DEFAULT_LOCATION);
        if (payload != null) {
            Response response = callAddToCartAPI(token, payload);
            validateAddToCartResponse(response, "NEW_USER");
        }
    }
}
