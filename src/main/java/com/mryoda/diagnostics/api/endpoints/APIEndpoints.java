package com.mryoda.diagnostics.api.endpoints;

/**
 * API Endpoints - Centralized repository for all API endpoints used in
 * framework
 * All API endpoints are defined here to maintain consistency and easy
 * maintenance
 */
public class APIEndpoints {

    // ========== AUTHENTICATION & USER MANAGEMENT ==========
    public static final String OTP_REQUEST = "/otps/getOtp";
    public static final String OTP_VERIFY = "/otps/getOtp";
    public static final String USER_CREATE = "/users/addUser";
    public static final String USER_PROFILE = "/user/profile";
    public static final String UPDATE_PROFILE = "/user/update";

    // ========== LOCATION & SEARCH ==========
    public static final String GET_LOCATION = "/tests/getlocations";
    public static final String GLOBAL_SEARCH = "tests/adminTests";

    // ========== BRAND & MEMBERSHIP ==========
    public static final String GET_ALL_BRANDS = "https://dev-api-yodamembership.yodaprojects.com/brand/getAllBrands";

    // ========== CART MANAGEMENT ==========
    public static final String ADD_TO_CART = "/carts/v2/addCart";
    public static final String GET_CART_BY_ID = "/carts/v2/getCartById/{user_id}";

    // ========== ADDRESS MANAGEMENT ==========
    public static final String ADD_ADDRESS = "/address/addAddress";
    public static final String GET_ADDRESS_BY_USER_ID = "/address/getAddressByUserId/{user_id}";

    // ========== SLOT & CENTER MANAGEMENT ==========
    public static final String GET_CENTERS_BY_ADD = "/slot/getCentersByadd";
    public static final String GET_SLOT_COUNT_BY_TIME = "/slot/getSlotCountByTime";

    // ========== ORDER MANAGEMENT ==========
    public static final String CREATE_ORDER = "/gateway/v2/CreateOrder";
    public static final String VERIFY_PAYMENT = "/gateway/v2/VerifyPayment";
    public static final String GET_PAYMENT_BY_ID = "/gateway/getPaymentById";
    public static final String GET_ORDER_BY_ID = "/order/getOrderById/";
    public static final String PHLEBO_LOGIN = "/phlebo/loginPhlebo";
    public static final String RAZORPAY_WEBHOOK = "/gateway/razorpay/webhook";

    // ========== BASE URLS ==========
    public static final String MEMBER_BASE_URL = "https://dev-api-yodamembership.yodaprojects.com";
    public static final String DIAGNOSTICS_BASE_URL = "https://dev-api-yodadiagnostics.yodaprojects.com";

    // ========== MEMBERSHIP ==========
    public static final String GET_USER = "/users/getUser/{user_id}";

    // ========== ORDER & TRACKING ==========
    public static final String ASSIGN_ORDER = "/order_tracking/assignOrder";
    public static final String UPDATE_ORDER_TRACKING = "/order_tracking/updateOrderTracking";
    public static final String GET_ORDER_TRACKING_STATUS = "/order_tracking/getOrderTrackingStatus/{guid}";

    // ========== BRAND ==========
    public static final String GET_ALL_BRANDS_PATH = "/brand/getAllBrands";
}
