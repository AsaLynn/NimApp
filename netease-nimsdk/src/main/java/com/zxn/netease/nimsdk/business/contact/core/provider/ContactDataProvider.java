package com.zxn.netease.nimsdk.business.contact.core.provider;

import com.zxn.netease.nimsdk.business.contact.core.item.AbsContactItem;
import com.zxn.netease.nimsdk.business.contact.core.item.ItemTypes;
import com.zxn.netease.nimsdk.business.contact.core.query.IContactDataProvider;
import com.zxn.netease.nimsdk.business.contact.core.query.TextQuery;

import java.util.ArrayList;
import java.util.List;

public class ContactDataProvider implements IContactDataProvider {

    private final int[] itemTypes;

    public ContactDataProvider(int... itemTypes) {
        this.itemTypes = itemTypes;
    }

    @Override
    public List<AbsContactItem> provide(TextQuery query) {
        List<AbsContactItem> data = new ArrayList<>();

        for (int itemType : itemTypes) {
            data.addAll(provide(itemType, query));
        }

        return data;
    }

    private final List<AbsContactItem> provide(int itemType, TextQuery query) {
        switch (itemType) {
            case ItemTypes.FRIEND:
                return UserDataProvider.provide(query);
//            case ItemTypes.MSG:
//                return MsgDataProvider.provide(query);

            case ItemTypes.TEAM:
            case ItemTypes.TEAMS.ADVANCED_TEAM:
            case ItemTypes.TEAMS.NORMAL_TEAM:
            default:
                return new ArrayList<>();
        }
    }
}
