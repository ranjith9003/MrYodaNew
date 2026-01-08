package com.mryoda.diagnostics.api.tests;

import com.mryoda.diagnostics.api.utils.RequestContext;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;

public class COD_02_GetCartTest extends CreateOrderCODAPITest {

    @Test
    public void step02_VerifyCartAndPrice() {
        System.out.println("\n>>> STEP 2: GET CART & VERIFY PRICE <<<");
        String token = RequestContext.getToken();
        String userId = RequestContext.getUserId();

        Assert.assertNotNull(token, "Token required from Step 1");
        Assert.assertNotNull(userId, "User ID required from Step 1");

        Response response = callGetCartAPI(token, userId);

        Object dataObj = response.jsonPath().get("data");
        String dataPath = (dataObj instanceof java.util.List) ? "data[0]" : "data";

        int totalPrice = response.jsonPath().getInt(dataPath + ".totalPrice");
        String cartId = response.jsonPath().getString(dataPath + ".guid");
        String orderType = response.jsonPath().getString(dataPath + ".order_type");

        System.out.println("   Cart Total: ₹" + totalPrice);
        System.out.println("   Cart ID: " + cartId);
        System.out.println("   Order Type: " + orderType);

        Assert.assertNotNull(cartId, "Cart ID should not be null");

        // Store for next steps
        RequestContext.setCurrentCartId(cartId);
        RequestContext.setCurrentTotalPrice(totalPrice);

        if (totalPrice >= 2500) {
            Assert.fail("Total Price ₹" + totalPrice + " exceeds COD limit (2500). Cannot proceed with COD test.");
        }
    }
}
