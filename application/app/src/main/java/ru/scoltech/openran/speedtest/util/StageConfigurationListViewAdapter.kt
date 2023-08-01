package ru.scoltech.openran.speedtest.util

import android.app.Activity
import android.text.Editable
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import ru.scoltech.openran.speedtest.R
import ru.scoltech.openran.speedtest.domain.StageConfiguration
import ru.scoltech.openran.speedtest.parser.StageConfigurationParser

class StageConfigurationListViewAdapter(
    private val context: Activity,
    private val data: MutableList<StageConfiguration>
) : ArrayAdapter<StageConfiguration>(context, R.layout.stage_sample, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewNode = inflater.inflate(R.layout.stage_sample, null, true);

        val deviceArgsView = listViewNode.findViewById(R.id.device_args) as EditText;
        val serviceArgsView = listViewNode.findViewById(R.id.server_args) as EditText;
        val stageNameView = listViewNode.findViewById(R.id.stage_name) as EditText;
        val deleteButtonView = listViewNode.findViewById<ImageButton>(R.id.deleteButton);

        stageNameView.text = data[position].name.toEditable()
        deviceArgsView.text = data[position].deviceArgs.toEditable()
        serviceArgsView.text = data[position].serverArgs.toEditable()

        stageNameView.addTextChangedListener {
            data[position] = data[position].copy(name = stageNameView.text.toString())
            StageConfigurationParser().saveStageToPrefs(context, data)
        }
        deviceArgsView.addTextChangedListener {
            data[position] = data[position].copy(deviceArgs = deviceArgsView.text.toString())
            StageConfigurationParser().saveStageToPrefs(context, data)
        }
        serviceArgsView.addTextChangedListener {
            data[position] = data[position].copy(serverArgs = serviceArgsView.text.toString())
            StageConfigurationParser().saveStageToPrefs(context, data)
        }

        deleteButtonView.setOnClickListener {
            data.removeAt(position);
            StageConfigurationParser().saveStageToPrefs(context, data)
            this.notifyDataSetChanged()
        };

        return listViewNode
    }

    private fun String.toEditable(): Editable = Editable.Factory.getInstance().newEditable(this)

    public fun getData(): List<StageConfiguration> {
        return data
    }


}