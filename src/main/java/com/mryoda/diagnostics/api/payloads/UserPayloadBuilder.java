package com.mryoda.diagnostics.api.payloads;

import org.json.JSONObject;
import com.mryoda.diagnostics.api.utils.RandomDataUtil;
import com.mryoda.diagnostics.api.utils.RequestContext;
import com.mryoda.diagnostics.api.config.ConfigLoader;

public class UserPayloadBuilder {

    public static JSONObject buildNewUserPayload() {
        JSONObject body = new JSONObject();
        body.put("first_name", RandomDataUtil.getRandomFirstName());
        body.put("last_name", RandomDataUtil.getRandomLastName());
        body.put("middle_name", RandomDataUtil.getRandomMiddleName());
        body.put("gender", RandomDataUtil.getRandomGender());
        body.put("dob", RandomDataUtil.getRandomDOB());
        body.put("mobile", RequestContext.getMobile());
        body.put("country_code", ConfigLoader.getConfig().countryCode());
        body.put("email", RandomDataUtil.getRandomEmail());
        body.put("profile_pic", RandomDataUtil.getRandomProfilePic());
        body.put("alt_mobile", RandomDataUtil.getRandomMobile());
        return body;
    }
}
