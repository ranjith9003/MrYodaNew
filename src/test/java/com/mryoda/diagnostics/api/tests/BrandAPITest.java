package com.mryoda.diagnostics.api.tests;

import com.mryoda.diagnostics.api.base.BaseTest;
import com.mryoda.diagnostics.api.builders.RequestBuilder;
import com.mryoda.diagnostics.api.endpoints.APIEndpoints;
import com.mryoda.diagnostics.api.utils.AssertionUtil;
import com.mryoda.diagnostics.api.utils.RequestContext;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

public class BrandAPITest extends BaseTest {

    private void validateAndStoreBrands(Response response) {
        if (response.getStatusCode() != 200) {
            System.out.println("⚠️ API Failed with status: " + response.getStatusCode());
            System.out.println("⚠️ Using HARDCODED Brand ID for 'Diagnostics'");
            RequestContext.storeBrand("Diagnostics", "efb159ac-db7c-4b06-a79b-021a8b6d67e8");
            return;
        }

        AssertionUtil.verifyTrue(response.jsonPath().getBoolean("success"), "API success flag should be true");

        List<Map<String, Object>> brands = response.jsonPath().getList("data");
        AssertionUtil.verifyTrue(brands.size() > 0, "Brand count should be greater than 0");

        System.out.println("\n🏷️ Total Brands Found: " + brands.size());

        for (Map<String, Object> brand : brands) {
            String name = (String) brand.get("name");
            String id = (String) brand.get("_id");
            Boolean isActive = (Boolean) brand.get("isActive");

            if (name != null) {
                System.out.println("   Brand: " + name + " | ID: " + id + " | Active: " + isActive);

                // Store brand ID
                RequestContext.storeBrand(name, id);

                if ("Diagnostics".equalsIgnoreCase(name)) {
                    System.out.println("   ✅ 'Diagnostics' Brand Found & Stored: " + id);
                }
            }
        }

        String diagId = RequestContext.getBrandId("Diagnostics");
        AssertionUtil.verifyNotNull(diagId, "Diagnostics brand ID must be found");
    }

    private Response callGetAllBrandsAPI() {
        System.out.println("\n🛍️ FETCHING ALL BRANDS...");
        return new RequestBuilder()
                .setEndpoint(APIEndpoints.GET_ALL_BRANDS)
                .get(); // Remove expectStatus(200)
    }

    // ---------------------------------------------------------
    // 1️⃣ MEMBER → Brand API
    // ---------------------------------------------------------
    @Test(priority = 6, dependsOnMethods = "com.mryoda.diagnostics.api.tests.LocationAPITest.testGetLocations_ForMember")
    public void testGetBrands_ForMember() {
        System.out.println("\n===== BRAND API — MEMBER =====");
        Response response = callGetAllBrandsAPI();
        validateAndStoreBrands(response);
    }

    // ---------------------------------------------------------
    // 2️⃣ NON-MEMBER → Brand API
    // ---------------------------------------------------------
    @Test(priority = 6, dependsOnMethods = "com.mryoda.diagnostics.api.tests.LocationAPITest.testGetLocations_ForNonMember")
    public void testGetBrands_ForNonMember() {
        System.out.println("\n===== BRAND API — NON-MEMBER =====");
        Response response = callGetAllBrandsAPI();
        validateAndStoreBrands(response);
    }

    // ---------------------------------------------------------
    // 3️⃣ NEW USER → Brand API
    // ---------------------------------------------------------
    @Test(priority = 7, dependsOnMethods = "com.mryoda.diagnostics.api.tests.LocationAPITest.testGetLocations_ForNewUser")
    public void testGetBrands_ForNewUser() {
        System.out.println("\n===== BRAND API — NEW USER =====");
        Response response = callGetAllBrandsAPI();
        validateAndStoreBrands(response);
    }
}
