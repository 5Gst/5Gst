package ru.scoltech.openran.speedtest.util

import android.app.Activity
import android.text.Editable
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageButton
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doOnTextChanged
import ru.scoltech.openran.speedtest.R
import ru.scoltech.openran.speedtest.domain.StageConfiguration
import ru.scoltech.openran.speedtest.parser.StageConfigurationParserNew

class ListViewAdapter(private val context: Activity, private val data: ArrayList<StageConfiguration>): ArrayAdapter<StageConfiguration>(context, R.layout.stage_sample, data) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val listViewNode = inflater.inflate(R.layout.stage_sample,null,true);

        val deviceArgs = listViewNode.findViewById(R.id.device_args) as EditText;
        val serviceArgs = listViewNode.findViewById(R.id.server_args) as EditText;
        val stageName = listViewNode.findViewById(R.id.stage_name) as EditText;
        val imgBtn = listViewNode.findViewById<ImageButton>(R.id.deleteButton);

        stageName.text = data[position].name.toEditable()
        deviceArgs.text = data[position].deviceArgs.toEditable()
        serviceArgs.text = data[position].serverArgs.toEditable()

        stageName.addTextChangedListener {
            data[position].name = stageName.text.toString();
            StageConfigurationParserNew().saveSuggestToPrefs(context,this.convertToMap())
        }
        deviceArgs.addTextChangedListener {
            data[position].deviceArgs = deviceArgs.text.toString();
            StageConfigurationParserNew().saveSuggestToPrefs(context,this.convertToMap())
        }
        serviceArgs.addTextChangedListener {
            data[position].serverArgs = serviceArgs.text.toString();
            StageConfigurationParserNew().saveSuggestToPrefs(context,this.convertToMap())
        }

        imgBtn.setOnClickListener{
            data.removeAt(position);
            this.notifyDataSetChanged()
        };

        return listViewNode
    }

    private fun delElem(){

    }

    private fun String.toEditable(): Editable =  Editable.Factory.getInstance().newEditable(this)

    public fun convertToMap( ): HashMap<String, StageConfiguration> {
        var map: HashMap<String, StageConfiguration> = HashMap()
        for ( stage in  data){
            map.put(stage.name,stage)
        }
        return map
    }


}