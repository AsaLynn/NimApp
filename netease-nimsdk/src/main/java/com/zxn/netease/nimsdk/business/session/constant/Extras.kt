package com.zxn.netease.nimsdk.business.session.constant

interface Extras {
    companion object {
        //返回结果
        const val RESULT_NAME = "result_name"
        const val EXTRA_FILE_PATH = "file_path"
        const val EXTRA_DATA = "data"
        const val EXTRA_FROM = "from"

        // 选择图片
        const val EXTRA_NEED_CROP = "need-crop"
        const val EXTRA_OUTPUTX = "outputX"
        const val EXTRA_OUTPUTY = "outputY"
        const val EXTRA_FROM_LOCAL = "from_local"
        const val EXTRA_SRC_FILE = "src-file"
        const val EXTRA_RETURN_DATA = "return-data"

        // 参数
        const val EXTRA_ACCOUNT = "account"
        const val EXTRA_NAME = "name"
        const val EXTRA_STATE = "state"
        const val EXTRA_TYPE = "type"
        const val EXTRA_ANCHOR = "anchor"
        const val EXTRA_ITEMS = "items"
        const val EXTRA_FORWARD = "forward"
        const val EXTRA_START = "start"
        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_CUSTOMIZATION = "customization"
        const val EXTRA_BACK_TO_CLASS = "backToClass"

        //图片选自器
        const val EXTRA_PHOTO_LISTS = "photo_list"
        const val EXTRA_SELECTED_IMAGE_LIST = "selected_image_list"
        const val EXTRA_MUTI_SELECT_MODE = "muti_select_mode"
        const val EXTRA_MUTI_SELECT_SIZE_LIMIT = "muti_select_size_limit"
        const val EXTRA_SUPPORT_ORIGINAL = "support_original"
        const val EXTRA_IS_ORIGINAL = "is_original"
        const val EXTRA_PREVIEW_CURRENT_POS = "current_pos"
        const val EXTRA_PREVIEW_IMAGE_BTN_TEXT = "preview_image_btn_text"
        const val EXTRA_SCALED_IMAGE_LIST = "scaled_image_list"
        const val EXTRA_ORIG_IMAGE_LIST = "orig_image_list"
        const val EXTRA_NEED_SHOW_SEND_ORIGINAL = "need_show_send_original_image"
    }
}