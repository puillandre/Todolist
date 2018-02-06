package com.puillandre.todolist

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*
import java.util.*

class ToDoAdapter(context: Context, toDoItemList: MutableList<ToDo>) : BaseAdapter() {

    private val mInflater: LayoutInflater = LayoutInflater.from(context)
    private var itemList = toDoItemList

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val itemText: String = itemList.get(position).name as String
        val done: Boolean = itemList.get(position).done as Boolean
        val view: View
        val vh: ListRowHolder

        if (convertView == null) {
            view = mInflater.inflate(R.layout.list_items, parent, false)
            vh = ListRowHolder(view)
            view.tag = vh
        } else {
            view = convertView
            vh = view.tag as ListRowHolder
        }
        vh.label.text = itemText
        vh.isDone.isChecked = done

        vh.isDone.setOnClickListener{
            itemList[position].done = vh.isDone.isChecked
        }

        vh.ibDeleteObject.setOnClickListener{
            itemList.removeAt(position)
            this.notifyDataSetChanged()
        }

        vh.label.setOnClickListener {
            val alert = AlertDialog.Builder(mInflater.context)
            val item = itemList.get(position)

            alert.setTitle(item.name)
            alert.setMessage("Description :\n" + item.desc + "\n\nDate :\n${item.day}/${item.month}/${item.year}")
            alert.setPositiveButton("Edit") {dialog,
                                           positiveButton ->
                (mInflater.context as MainActivity).editTask(item)
                dialog.dismiss()
            }
            alert.setNegativeButton("Exit"){dialog,
                                              positiveButton ->
                dialog.dismiss()
            }
            alert.show()
        }

        return view
    }
    override fun getItem(index: Int): Any {
        return itemList.get(index)
    }
    override fun getItemId(index: Int): Long {
        return index.toLong()
    }
    override fun getCount(): Int {
        return itemList.size
    }
    private class ListRowHolder(row: View?) {
        val label: TextView = row!!.findViewById<TextView>(R.id.tv_item_text) as TextView
        val isDone: CheckBox = row!!.findViewById<CheckBox>(R.id.cb_item_is_done) as CheckBox
        val ibDeleteObject: ImageButton = row!!.findViewById<ImageButton>(R.id.iv_cross) as ImageButton
    }
}

class ToDo (var name : String = "",
            var desc : String = "hullo!",
            var year: Int = 2018,
            var month: Int = 1,
            var day: Int = 1): Serializable
{
    var done : Boolean = false
}

class MainActivity : AppCompatActivity() {

    object Storage {
        private val LOG_TAG = Storage::class.java.simpleName
        private val FILE_NAME = "todo_list.ser"

        fun writeData(context: Context, tasks: List<ToDo>?) {
            var fos: FileOutputStream? = null
            var oos: ObjectOutputStream? = null

            try {
                fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE)
                oos = ObjectOutputStream(fos)
                oos.writeObject(tasks)
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Could not write to file.")
                e.printStackTrace()
            } finally {
                try {
                    oos?.close()
                    fos?.close()
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Could not close the file.")
                    e.printStackTrace()
                }

            }
        }

        fun readData(context: Context): MutableList<ToDo>? {
            var fis: FileInputStream? = null
            var ois: ObjectInputStream? = null

            var tasks: MutableList<ToDo>? = ArrayList()

            try {
                fis = context.openFileInput(FILE_NAME)
                ois = ObjectInputStream(fis)

                tasks = ois?.readObject() as? MutableList<ToDo>
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Could not read from file.")
                e.printStackTrace()
            } finally {
                try {
                    ois?.close()
                    fis?.close()
                } catch (e: Exception) {
                    Log.e(LOG_TAG, "Could not close the file.")
                    e.printStackTrace()
                }

            }

            return tasks
        }
    }

    val toDoList = mutableListOf<ToDo>()
    lateinit var adapter: ToDoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        addBtn.setOnClickListener{
            layoutChoice.visibility = VISIBLE
            layoutStart.visibility = INVISIBLE

            addBtnFin.setOnClickListener{
                val selectedYear = datePicker.year
                val selectedMonth = datePicker.month + 1
                val selectedDay = datePicker.dayOfMonth
                val ToDo = ToDo(nameTxt.text.toString(), descTxt.text.toString(),
                        selectedYear, selectedMonth, selectedDay)

                if (nameTxt.text.toString() == "") {
                    addNewItemDialog()
                }
                else {
                    toDoList.add(ToDo)

                    adapter.notifyDataSetChanged()
                    choiceEnd()
                }
            }
        }

        cancelBtn.setOnClickListener{
            choiceEnd()
        }

        adapter = ToDoAdapter(this, toDoList)
        items_list!!.setAdapter(adapter)
    }

    public fun editTask(item : ToDo) {
        datePicker.init(item.year, item.month + 1, item.day, null)
        nameTxt.setText(item.name)
        descTxt.setText(item.desc)

        addBtnFin.setOnClickListener{
            val selectedYear = datePicker.year
            val selectedMonth = datePicker.month + 1
            val selectedDay = datePicker.dayOfMonth
            item.year = selectedYear
            item.month = selectedMonth
            item.day = selectedDay
            item.name = nameTxt.text.toString()
            item.desc = descTxt.text.toString()

            if (nameTxt.text.toString() == "") {
                addNewItemDialog()
            }
            else {
                adapter.notifyDataSetChanged()
                choiceEnd()
            }
        }

        layoutChoice.visibility = VISIBLE
        layoutStart.visibility = INVISIBLE
    }

    private fun choiceEnd() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        datePicker.init(year, month, day, null)
        nameTxt.getText().clear()
        descTxt.getText().clear()
        layoutChoice.visibility = INVISIBLE
        layoutStart.visibility = VISIBLE
    }

    override fun onResume() {
        super.onResume()
        val tasks = Storage.readData(this)
        if (tasks != null)
        {
            toDoList.clear()
            toDoList.addAll(tasks)
            adapter.notifyDataSetChanged()
        }
    }

    override fun onPause() {
        super.onPause()

        Storage.writeData(this, toDoList)
    }

    private fun addNewItemDialog() {
        val alert = AlertDialog.Builder(this)

        alert.setMessage("Need a name !")

        alert.setPositiveButton("OK") {dialog,
                                           positiveButton ->
            dialog.dismiss()
        }

        alert.show()
    }
}
