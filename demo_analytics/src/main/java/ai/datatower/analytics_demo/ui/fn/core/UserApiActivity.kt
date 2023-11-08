package ai.datatower.analytics_demo.ui.fn.core

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ai.datatower.analytics.DTAnalytics
import ai.datatower.analytics_demo.R
import org.json.JSONException
import org.json.JSONObject


class UserApiActivity : AppCompatActivity() {
    private var spinner: Spinner? = null
    private val infoMap: MutableMap<String, String?> = mutableMapOf()
    private var editParam: EditText? = null
    private var editDescription: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.user_api_activity)

        findViewById<View>(R.id.user_api_confirm).setOnClickListener {
            spinner?.let {
                val api = it.selectedItem.toString()
                val param = infoMap[api]
                var passParam: String?

                if (param != null) {
                    if (param.isNotEmpty()) {
                        passParam = editParam?.text.toString()
                        if (passParam.isEmpty()) {
                            val text = "pls input the params"
                            Toast.makeText(this@UserApiActivity, text, Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                        // todo only handle json Object now
                        try {
                            val reflectionMethod = DTAnalytics::class.java.getMethod(api, JSONObject::class.java,)
                            val jsonObject = JSONObject(passParam)
                            reflectionMethod.invoke(null, jsonObject)
                        } catch (e: JSONException) {
                            e.printStackTrace()
                            val text = "Error on parsing text to JSON"
                            Toast.makeText(this@UserApiActivity, text, Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        } catch (e: Exception) {
                            val text = e.toString()
                            Toast.makeText(this@UserApiActivity, text, Toast.LENGTH_SHORT).show()
                            return@setOnClickListener
                        }

                    } else {
                        val reflectionMethod = DTAnalytics::class.java.getMethod(api)
                        reflectionMethod.invoke(null)
                    }
                }
            }
        }

        fillinUI()
    }

    private fun fillinUI() {

        spinner = findViewById<Spinner>(R.id.user_api_list)
        editParam = findViewById<EditText>(R.id.user_api_param)
        editDescription = findViewById<TextView>(R.id.user_api_description)

        val reflectionMethods = DTAnalytics::class.java.methods
        for(method in reflectionMethods) {
            if (method.name.startsWith("user")) {
                val paramTypes = method.parameterTypes
                var paramStr = ""
                for (type in paramTypes) {
                    paramStr += type.name
                }

                infoMap[method.name] = paramStr
            }
        }

        val dataAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_item,
                infoMap.keys.toTypedArray()
            )

        spinner?.adapter = dataAdapter
        spinner?.setOnItemSelectedListener(object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                val selectedItem = p0?.getItemAtPosition(p2).toString()
                selectedItem?.let {
                    val description = infoMap[it]
                    editDescription?.setText(description)
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        })
    }


    companion object {
        fun startActivity(context: Context) {
            context.startActivity(Intent(context, UserApiActivity::class.java))
        }
    }
}