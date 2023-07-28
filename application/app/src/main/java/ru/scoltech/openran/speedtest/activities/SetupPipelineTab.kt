package ru.scoltech.openran.speedtest.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import ru.scoltech.openran.speedtest.R
import ru.scoltech.openran.speedtest.domain.StageConfiguration
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


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setup_pipeline, container, true)
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
