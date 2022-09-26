package com.abhilashgupta2706.bluetoothprinting

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.abhilashgupta2706.bluetoothprinting.databinding.ActivityMainBinding
import com.mazenrashed.printooth.Printooth
import com.mazenrashed.printooth.data.printable.Printable
import com.mazenrashed.printooth.data.printable.TextPrintable
import com.mazenrashed.printooth.data.printer.DefaultPrinter
import com.mazenrashed.printooth.ui.ScanningActivity
import com.mazenrashed.printooth.utilities.Printing
import com.mazenrashed.printooth.utilities.PrintingCallback

class MainActivity : AppCompatActivity() {

    private lateinit var vb: ActivityMainBinding
    private var printing: Printing? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)

        Printooth.init(this);

        if (Printooth.hasPairedPrinter())
            printing = Printooth.printer()
        initViews()
        initListeners()


    }

    private fun initViews() {
        vb.btnPairUnpair.text =
            if (Printooth.hasPairedPrinter()) "Un-pair ${Printooth.getPairedPrinter()!!.name}" else "Pair with printer"
    }

    private fun initListeners() {
        vb.btnPrintData.setOnClickListener {
            if (!Printooth.hasPairedPrinter()) startActivityForResult(
                Intent(
                    this,
                    ScanningActivity::class.java
                ),
                ScanningActivity.SCANNING_FOR_PRINTER
            )
            else {
                vb.apply {
                    var fullName = etFullName.text.toString()
                    var contactNum = etContactNum.text.toString()
                    var city = etCity.text.toString()
                    var state = etState.text.toString()
                    var country = etCountry.text.toString()

                    if (fullName.isEmpty() || contactNum.isEmpty() || city.isEmpty() || state.isEmpty() || country.isEmpty()) {
                        Toast.makeText(
                            this@MainActivity,
                            "All fields should be filled",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setOnClickListener
                    }

                    printSomePrintable(fullName, contactNum, city, state, country)
                }
            }
        }

        vb.btnPairUnpair.setOnClickListener {
            if (Printooth.hasPairedPrinter()) {
                Printooth.removeCurrentPrinter()
            } else {
                startActivityForResult(
                    Intent(this, ScanningActivity::class.java),
                    ScanningActivity.SCANNING_FOR_PRINTER
                )
            }
            initViews()
        }

        printing?.printingCallback = object : PrintingCallback {
            override fun connectingWithPrinter() {
                Toast.makeText(this@MainActivity, "Connecting with printer", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun printingOrderSentSuccessfully() {
                Toast.makeText(this@MainActivity, "Order sent to printer", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun connectionFailed(error: String) {
                Toast.makeText(this@MainActivity, "Failed to connect printer", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onError(error: String) {
                Toast.makeText(this@MainActivity, error, Toast.LENGTH_SHORT).show()
            }

            override fun onMessage(message: String) {
                Toast.makeText(this@MainActivity, "Message: $message", Toast.LENGTH_SHORT).show()
            }

            override fun disconnected() {
                Toast.makeText(this@MainActivity, "Disconnected Printer", Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun printSomePrintable(
        fullName: String,
        contactNum: String,
        city: String,
        state: String,
        country: String
    ) {

        // "/r/n" is for Zebra ZQ120 Bluetooth Printer

        val labelTemplate = "! 0 200 200 400 1\r\n" +
                "PW 400\r\n" +
                "TONE 0\r\n" +
                "SPEED 2\r\n" +
                "ON-FEED IGNORE\r\n" +
                "NO-PACE\r\n" +
                "BAR-SENSE\r\n" +
                "T 5 0 144 241 $state,\r\n" +
                "T 5 0 144 296 $country\r\n" +
                "T 5 0 144 185 $city,\r\n" +
                "T 5 0 144 103 $contactNum\r\n" +
                "T 5 0 25 184 Address: \r\n" +
                "T 5 0 149 22 $fullName\r\n" +
                "T 5 0 21 102 Phone no: \r\n" +
                "T 5 0 18 22 Full name: \r\n" +
                "PRINT\r\n"

        var printables = ArrayList<Printable>()
        var printable = TextPrintable.Builder()
            .setText(labelTemplate) //The text you want to print
            .setAlignment(DefaultPrinter.ALIGNMENT_CENTER)
            .setEmphasizedMode(DefaultPrinter.EMPHASIZED_MODE_BOLD) //Bold or normal
            .setFontSize(DefaultPrinter.FONT_SIZE_NORMAL)
            .setUnderlined(DefaultPrinter.UNDERLINED_MODE_ON) // Underline on/off
            .setCharacterCode(DefaultPrinter.CHARCODE_PC437) // Character code to support languages
            .setLineSpacing(DefaultPrinter.LINE_SPACING_30)
            .setNewLinesAfter(0) // To provide n lines after sentence
            .build()
        printables.add(printable)
        Printooth.printer().print(printables)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ScanningActivity.SCANNING_FOR_PRINTER && resultCode == Activity.RESULT_OK) {
            Toast.makeText(this@MainActivity, "Printer Connected", Toast.LENGTH_SHORT).show()
            initViews()
        }
    }
}