package com.zxn.netease.nimsdk.business.session.actions;

import com.zxn.netease.nimsdk.R;
import com.zxn.netease.nimsdk.api.model.location.LocationProvider;
import com.zxn.netease.nimsdk.impl.NimUIKitImpl;
import com.netease.nimlib.sdk.msg.MessageBuilder;
import com.netease.nimlib.sdk.msg.model.IMMessage;

/**
 * Created by hzxuwen on 2015/6/12.
 */
public class LocationAction extends BaseAction {
    private final static String TAG = "LocationAction";

    public LocationAction() {
        super(R.drawable.nim_message_plus_location_selector, R.string.input_panel_location);
    }

    @Override
    public void onClick() {
        if (NimUIKitImpl.getLocationProvider() != null) {
            NimUIKitImpl.getLocationProvider().requestLocation(getActivity(), new LocationProvider.Callback() {
                @Override
                public void onSuccess(double longitude, double latitude, String address) {
                    IMMessage message = MessageBuilder.createLocationMessage(getAccount(), getSessionType(), latitude, longitude,
                            address);
                    sendMessage(message);
                }
            });
        }
    }
}
