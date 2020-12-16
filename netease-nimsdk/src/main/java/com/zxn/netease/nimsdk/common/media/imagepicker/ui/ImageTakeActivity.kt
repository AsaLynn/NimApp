package com.zxn.netease.nimsdk.common.media.imagepicker.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.Permission
import com.hjq.permissions.XXPermissions
import com.zxn.netease.nimsdk.R
import com.zxn.netease.nimsdk.common.media.imagepicker.Constants
import com.zxn.netease.nimsdk.common.media.imagepicker.ImagePicker
import com.zxn.netease.nimsdk.common.media.imagepicker.ImagePickerLauncher
import com.zxn.netease.nimsdk.common.media.imagepicker.Utils
import com.zxn.netease.nimsdk.common.media.model.GLImage
import com.zxn.netease.nimsdk.common.media.model.GenericFileProvider
import com.zxn.netease.nimsdk.common.util.sys.TimeUtil
import java.io.File

/**
 * Updated by zxn on 2020/12/16.
 */
class ImageTakeActivity : ImageBaseActivity() {

    private var imagePicker: ImagePicker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.nim_activity_image_crop)
        imagePicker = ImagePicker.getInstance()

        if (savedInstanceState == null) {
            XXPermissions.with(this)
                .permission(Permission.CAMERA)
                .permission(Permission.WRITE_EXTERNAL_STORAGE)
                .request(object : OnPermissionCallback {
                    override fun onGranted(permissions: List<String>, all: Boolean) {
                        if (all) {
                            takePicture()
                        }
                    }

                    override fun onDenied(permissions: List<String>, never: Boolean) {}
                })
        }
    }

    override fun clearRequest() {}
    override fun clearMemoryCache() {}
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == Constants.REQUEST_PERMISSION_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                ImagePickerLauncher.takePicture(
                    this,
                    Constants.REQUEST_CODE_TAKE,
                    imagePicker!!.option
                )
            } else {
                showToast("权限被禁止，无法打开相机")
                finish()
            }
        }
    }

    private fun takePicture() {
        var takeImageFile: File? = null
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        takePictureIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        if (takePictureIntent.resolveActivity(packageManager) != null) {
            takeImageFile = if (Utils.existSDCard()) {
                File(Environment.getExternalStorageDirectory(), "/DCIM/camera/")
            } else {
                Environment.getDataDirectory()
            }
            takeImageFile = Utils.createFile(takeImageFile, "IMG_", ".jpg")
            if (takeImageFile != null) {
                // 默认情况下，即不需要指定intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                // 照相机有自己默认的存储路径，拍摄的照片将返回一个缩略图。如果想访问原始图片，
                // 可以通过dat extra能够得到原始图片位置。即，如果指定了目标uri，data就没有数据，
                // 如果没有指定uri，则data就返回有数据！
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    //Android N必须使用这种方式
                    val photoURI = GenericFileProvider.getUriForFile(
                        this, applicationContext.packageName +
                                ".generic.file.provider", takeImageFile
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                } else {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(takeImageFile))
                }
            }
        }
        if (takeImageFile == null) {
            //TODO
            return
        }
        //FIXME
        imagePicker!!.takeImageFile = takeImageFile
        startActivityForResult(takePictureIntent, Constants.REQUEST_CODE_TAKE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.REQUEST_CODE_TAKE) {
            if (resultCode == RESULT_OK) {
                //发送广播通知图片增加了
                Utils.galleryAddPic(this, imagePicker!!.takeImageFile)
                val glImage =
                    GLImage.Builder.newBuilder().setAddTime(TimeUtil.getNow_millisecond()).setPath(
                        imagePicker!!.takeImageFile.absolutePath
                    ).setMimeType("image/jpeg").build()
                imagePicker!!.clearSelectedImages()
                imagePicker!!.addSelectedImageItem(glImage, true)
                if (imagePicker!!.isCrop) {
                    val intent = Intent(this, ImageCropActivity::class.java)
                    startActivityForResult(intent, Constants.REQUEST_CODE_CROP) //单选需要裁剪，进入裁剪界面
                    return
                } else {
                    val intent = Intent()
                    intent.putExtra(Constants.EXTRA_RESULT_ITEMS, imagePicker!!.selectedImages)
                    setResult(RESULT_OK, intent)
                    finish()
                }
            }
            finish()
        } else if (requestCode == Constants.REQUEST_CODE_CROP) {
            if (resultCode == RESULT_OK) {
                setResult(RESULT_OK, data)
                finish()
            }
            finish()
        }
    }
}