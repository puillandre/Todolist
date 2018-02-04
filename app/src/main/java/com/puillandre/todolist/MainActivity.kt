package com.puillandre.todolist

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.*

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

class ToDo (var name : String = "", var desc : String = ""): Serializable
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
                // Open file and write list
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
                // Open file and read list
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
            addNewItemDialog()
        }

        adapter = ToDoAdapter(this, toDoList)
        items_list!!.setAdapter(adapter)
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

        val itemEditText = EditText (this)
        alert.setMessage("Add New Item")
        alert.setTitle("Enter next Task")

        alert.setView(itemEditText)

        alert.setPositiveButton("Submit") {dialog,
                                           positiveButton ->
            val ToDo = ToDo(itemEditText.text.toString())

            toDoList.add(ToDo)

            dialog.dismiss()

            adapter.notifyDataSetChanged()
        }

        alert.show()
    }
}
