package ru.scoltech.openran.speedtest.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import ru.scoltech.openran.speedtest.R
import ru.scoltech.openran.speedtest.domain.StageConfiguration
import ru.scoltech.openran.speedtest.parser.StageConfigurationParser
import ru.scoltech.openran.speedtest.parser.StageConfigurationParserNew
import ru.scoltech.openran.speedtest.util.ListViewAdapter


class SetupPipelineTab : Fragment() {

    private lateinit var addBtn: Button
    private var arr: ArrayList<StageConfiguration>  =  ArrayList()
    private lateinit var adapter: ListViewAdapter
    private lateinit var listView: ListView

    companion object {
        private val TAG = SetupPipelineTab::class.java.simpleName
    }

    private val stageConfigurationParser = StageConfigurationParser()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setup_pipeline, container, true)
    }

    private fun serializeStage(stageView: View): String {
        val name = stageView.findViewById<TextInputEditText>(R.id.stage_name).text
            .toString()
        val deviceArgs = stageView.findViewById<TextInputEditText>(R.id.device_args).text
            .toString()
        val serverArgs = stageView.findViewById<TextInputEditText>(R.id.server_args).text
            .toString()

        return stageConfigurationParser.serializeStage(
            StageConfiguration(name, serverArgs, deviceArgs)
        )
    }

    private fun addSerializationListener(
        listeningEditTextView: EditText,
        stageView: View,
        preferencesKey: String,
    ) {
        listeningEditTextView.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val activity = requireActivity()
                    activity.runOnUiThread {
                        val serializedStage = serializeStage(stageView)
                        activity.getSharedPreferences(
                            "iperf_args_pipeline",
                            AppCompatActivity.MODE_PRIVATE,
                        ).edit {
                            putString(preferencesKey, serializedStage)
                        }
                    }
                }
            })
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addBtn = view.findViewById(R.id.addBtn)

        listView = view.findViewById<ListView>(R.id.listview)
        addBtn.setOnClickListener{addStage()}
        adapter = ListViewAdapter(requireActivity(), StageConfigurationParserNew().getSuggestFromPrefs(activity));
        listView.adapter = adapter
    }


    private fun addStage(){
        adapter.add(StageConfiguration("New Stage","",""));
        adapter.notifyDataSetChanged()
        StageConfigurationParserNew().saveSuggestToPrefs(activity,adapter.convertToMap())
        listView.setSelection(adapter.count-1)

    }

}
