package com.dimoll.esc_pos_usb_plugin

import android.hardware.usb.UsbDevice
import androidx.annotation.NonNull
import com.dimoll.esc_pos_usb_plugin.adapter.USBPrinterAdapter
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import java.util.*

/** EscPosUsbPlugin */
class EscPosUsbPlugin : FlutterPlugin, MethodCallHandler {
    /// The MethodChannel that will the communication between Flutter and native Android
    ///
    /// This local reference serves to register the plugin with the Flutter Engine and unregister it
    /// when the Flutter Engine is detached from the Activity
    private lateinit var channel: MethodChannel
    private val adapter: USBPrinterAdapter = USBPrinterAdapter.instance

    override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
        channel = MethodChannel(flutterPluginBinding.binaryMessenger, "esc_pos_usb_plugin")
        channel.setMethodCallHandler(this)
        adapter.init(flutterPluginBinding.applicationContext);
    }

    override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
        when (call.method) {
            "getUSBDeviceList" -> {
                getUSBDeviceList(result)
            }
            "connectPrinter" -> {
                val vendor = call.argument<Int>("vendor")
                val product = call.argument<Int>("product")
                connectPrinter(vendor, product, result)
            }
            "closeConn" -> {
                closeConn(result)
            }
            "printText" -> {
                val text = call.argument<String>("text")
                printText(text, result)
            }
            "printRawData" -> {
                val raw = call.argument<String>("raw")
                printRawData(raw, result)
            }
            else -> {
                result.notImplemented()
            }
        }
    }

    override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
        channel.setMethodCallHandler(null)
    }

    private fun getUSBDeviceList(result: Result) {
        val usbDevices: List<UsbDevice> = adapter.deviceList
        val list = ArrayList<HashMap<*, *>>()
        for (usbDevice in usbDevices) {
            val deviceMap: HashMap<Any?, Any?> = HashMap<Any?, Any?>()
            deviceMap["name"] = usbDevice.deviceName
            deviceMap["manufacturer"] = usbDevice.manufacturerName
            deviceMap["product"] = usbDevice.productName
            deviceMap["deviceid"] = usbDevice.deviceId.toString()
            deviceMap["vendorid"] = usbDevice.vendorId.toString()
            deviceMap["productid"] = usbDevice.productId.toString()
            list.add(deviceMap)
        }
        result.success(list)
    }


    private fun connectPrinter(vendorId: Int?, productId: Int?, result: Result) {
        if (!adapter.selectDevice(vendorId!!, productId!!)) {
            result.success(false)
        } else {
            result.success(true)
        }
    }


    private fun closeConn(result: Result) {
        adapter.closeConnectionIfExists()
        result.success(true)
    }


    private fun printText(text: String?, result: Result) {
        adapter.printText(text!!)
        result.success(true)
    }

    private fun printRawData(base64Data: String?, result: Result) {
        adapter.printRawData(base64Data!!)
        result.success(true)
    }
}
