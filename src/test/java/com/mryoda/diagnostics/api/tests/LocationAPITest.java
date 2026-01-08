package com.mryoda.diagnostics.api.tests;

import com.mryoda.diagnostics.api.base.BaseTest;
import com.mryoda.diagnostics.api.endpoints.APIEndpoints;
import com.mryoda.diagnostics.api.utils.AssertionUtil;
import com.mryoda.diagnostics.api.utils.RequestContext;
import com.mryoda.diagnostics.api.builders.RequestBuilder;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class LocationAPITest extends BaseTest {

    // -------------------------------
    // COMMON REUSABLE VALIDATION LOGIC
    // -------------------------------
    private void validateAndStoreLocations(Response response) {

        AssertionUtil.verifyTrue(response.jsonPath().getBoolean("success"), "API success flag");

        List<Map<String, Object>> locations = response.jsonPath().getList("data");
        AssertionUtil.verifyTrue(locations.size() > 0, "Location count > 0");

        System.out.println("\n📍 Total Locations Found: " + locations.size());

        for (int i = 0; i < locations.size(); i++) {
            String id = response.jsonPath().getString("data[" + i + "]._id");
            String title = response.jsonPath().getString("data[" + i + "].title");
            String status = response.jsonPath().getString("data[" + i + "].status");
            String city = response.jsonPath().getString("data[" + i + "].city");
            String state = response.jsonPath().getString("data[" + i + "].state");

            // Extract google map coordinates
            String latitude = response.jsonPath().getString("data[" + i + "].google_map_latitude");
            String longitude = response.jsonPath().getString("data[" + i + "].google_map_langitude");

            // Validate location is ACTIVE before storing
            if (status != null) {
                System.out.println("   Location: " + title + " | City: " + city + " | State: " + state + " | Status: "
                        + status + " | ID: " + id);
                if (latitude != null && longitude != null) {
                    System.out.println("   📍 Coordinates: Lat=" + latitude + ", Long=" + longitude);
                }
            }

            // Store location ID
            RequestContext.storeLocation(title, id);

            // Store city and state
            if (city != null && state != null) {
                RequestContext.storeLocationCityState(title, city, state);
            }

            // Store coordinates if available
            if (latitude != null && longitude != null) {
                RequestContext.storeLocationCoordinates(title, latitude, longitude);
                System.out.println("✔ Stored: " + title + " → ID: " + id + " | City: " + city + " | State: " + state
                        + " | Coordinates: (" + latitude + ", " + longitude + ")");
            } else {
                System.out.println("✔ Stored: " + title + " → ID: " + id + " | City: " + city + " | State: " + state
                        + " | Coordinates: Not available");
            }
        }

        // Verify critical location exists (Configured location is used in tests)
        String madhapurLocationId = RequestContext.getLocationId(DEFAULT_LOCATION);
        if (madhapurLocationId != null) {
            System.out.println(
                    "\n✅ Critical Location '" + DEFAULT_LOCATION + "' found and stored: " + madhapurLocationId);

            String madhapurLat = RequestContext.getLocationLatitude(DEFAULT_LOCATION);
            String madhapurLong = RequestContext.getLocationLongitude(DEFAULT_LOCATION);
            if (madhapurLat != null && madhapurLong != null) {
                System.out.println(
                        "   📍 " + DEFAULT_LOCATION + " Coordinates: Lat=" + madhapurLat + ", Long=" + madhapurLong);
            }

            System.out.println("   This location will be used in GlobalSearch and AddToCart APIs");
        } else {
            System.out.println("\n⚠️  Warning: '" + DEFAULT_LOCATION + "' location not found in response");
        }

        System.out.println("\n🟢 Locations stored for reuse in next APIs\n");
    }

    private Response callLocationAPI(String token) {

        return new RequestBuilder()
                .setEndpoint(APIEndpoints.GET_LOCATION)
                .addHeader("Authorization", token)
                .expectStatus(200)
                .post(); // yes, endpoint is POST
    }

    // ---------------------------------------------------------
    // 1️⃣ MEMBER → Location API
    // ---------------------------------------------------------
    @Test(priority = 5, dependsOnMethods = "com.mryoda.diagnostics.api.tests.LoginAPITest.testLoginWithOTP")
    public void testGetLocations_ForMember() {

        System.out.println("\n===== LOCATION API — MEMBER =====");

        String token = RequestContext.getMemberToken();
        Response response = callLocationAPI(token);

        validateAndStoreLocations(response);
    }

    // ---------------------------------------------------------
    // 2️⃣ NON-MEMBER (Mobile: 8220220227) → Location API
    // ---------------------------------------------------------
    @Test(priority = 5, dependsOnMethods = "com.mryoda.diagnostics.api.tests.LoginAPITest.testLoginWithOTP_NonMember")
    public void testGetLocations_ForNonMember() {

        System.out.println("\n===== LOCATION API — NON-MEMBER (8220220227 - NOT a paid member) =====");

        String token = RequestContext.getNonMemberToken();
        Response response = callLocationAPI(token);

        validateAndStoreLocations(response);
    }

    // ---------------------------------------------------------
    // 3️⃣ NEW USER → Location API
    // ---------------------------------------------------------
    @Test(priority = 6, dependsOnMethods = "com.mryoda.diagnostics.api.tests.LoginAPITest.testLoginWithOTP_NewlyRegisteredUser")
    public void testGetLocations_ForNewUser() {

        System.out.println("\n===== LOCATION API — NEW USER =====");

        String token = RequestContext.getNewUserToken();
        Response response = callLocationAPI(token);

        validateAndStoreLocations(response);
    }
}
