package ru.fivegst.speedtest.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ListView
import androidx.fragment.app.Fragment
import ru.fivegst.speedtest.R
import ru.fivegst.speedtest.domain.StageConfiguration
import ru.fivegst.speedtest.parser.StageConfigurationParser
import ru.fivegst.speedtest.util.StageConfigurationListViewAdapter


class SetupPipelineTab : Fragment() {

    private lateinit var addButton: Button
    private lateinit var adapter: StageConfigurationListViewAdapter
    private lateinit var listView: ListView
    private val stageConfigurationParser =
        _root_ide_package_.ru.fivegst.speedtest.parser.StageConfigurationParser()

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
        addButton = view.findViewById(R.id.addBtn)

        listView = view.findViewById<ListView>(R.id.listview)
        addButton.setOnClickListener { addStage() }
        adapter = StageConfigurationListViewAdapter(
            requireActivity(),
            stageConfigurationParser.getStageFromPrefs(activity)
        );
        listView.adapter = adapter
    }


    private fun addStage() {
        adapter.add(
            StageConfiguration(
                name = "New Stage",
                serverArgs = "",
                deviceArgs = "",
            )
        );
        adapter.notifyDataSetChanged()
        stageConfigurationParser.saveStageToPrefs(activity, adapter.getData())
        listView.setSelection(adapter.count - 1)

    }

}
