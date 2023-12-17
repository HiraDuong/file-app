package com.example.file_app

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ContextMenu
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import java.io.File
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider

class MainActivity : AppCompatActivity() {
    private lateinit var currentDirectory: File
    private lateinit var fileAdapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        currentDirectory = File("/sdcard")

        // Khởi tạo và thiết lập adapter cho ListView hoặc RecyclerView của bạn
        fileAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, getFilesInDirectory(currentDirectory))
        val listView: ListView = findViewById(R.id.listView)

        listView.adapter = fileAdapter
        registerForContextMenu(listView)

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedFolderName = fileAdapter.getItem(position)
            val selectedFolder = File(currentDirectory, selectedFolderName)
            val selectedFileName = fileAdapter.getItem(position)
            val selectedFile = File(currentDirectory, selectedFileName)

            if (selectedFolder.isDirectory) {
                // Xử lý hiển thị nội dung của thư mục (ví dụ: mở thư mục)
                currentDirectory = selectedFolder
                refreshFileList()
            }
            else{
                if (selectedFile.extension.equals("txt", ignoreCase = true)) {
                    // Xử lý hiển thị nội dung file văn bản
                    showTextFileContent(selectedFile)
                } else if (selectedFile.extension.equals("bmp", ignoreCase = true) ||
                    selectedFile.extension.equals("jpg", ignoreCase = true) ||
                    selectedFile.extension.equals("png", ignoreCase = true)) {
                    // Xử lý hiển thị ảnh
                    showImage(selectedFile)
                }
            }

        }

    }

    private fun showTextFileContent(file: File) {
        val uri = FileProvider.getUriForFile(this, "com.example.file_app.fileprovider", file)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "text/plain")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    private fun showImage(imageFile: File) {
        val uri = FileProvider.getUriForFile(this, "com.example.file_app.fileprovider", imageFile)
        val intent = Intent(Intent.ACTION_VIEW)
        intent.setDataAndType(uri, "image/*")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        startActivity(intent)
    }

    private fun showCreateFileDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tạo file văn bản")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Tạo") { dialog, _ ->
            val fileName = input.text.toString()
            val newFile = File(currentDirectory, fileName)


            refreshFileList()
            dialog.dismiss()
        }

        builder.setNegativeButton("Hủy") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    private fun showCreateFolderDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Tạo thư mục")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Tạo") { dialog, _ ->
            val folderName = input.text.toString()
            val newFolder = File(currentDirectory, folderName)

            if (!newFolder.exists()) {
                newFolder.mkdirs()
                refreshFileList()
            } else {
                Toast.makeText(this, "Thư mục đã tồn tại", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Hủy") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_create_folder -> showCreateFolderDialog()
            R.id.menu_create_file -> showCreateFileDialog()
        }

        return super.onOptionsItemSelected(item)
    }

    private fun refreshFileList() {
        fileAdapter.clear()
        fileAdapter.addAll(getFilesInDirectory(currentDirectory))
        fileAdapter.notifyDataSetChanged()
    }

    private fun getFilesInDirectory(directory: File): List<String> {
        val fileList = mutableListOf<String>()
        val files = directory.listFiles()

        files?.forEach {
            if (it.isDirectory) {
                fileList.add(it.name)
            } else {
                fileList.add("[File] ${it.name}")
            }
        }

        return fileList
    }


    override fun onCreateContextMenu(menu: ContextMenu?, v: View?, menuInfo: ContextMenu.ContextMenuInfo?) {
        super.onCreateContextMenu(menu, v, menuInfo)
        menuInflater.inflate(R.menu.context_menu, menu)
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        val selectedFileName = fileAdapter.getItem(info.position)

        when (item.itemId) {
            R.id.menu_rename -> showRenameDialog(selectedFileName)
            R.id.menu_delete -> showDeleteDialog(selectedFileName)
            // Thêm các trường hợp khác tại đây
        }

        return super.onContextItemSelected(item)
    }

    private fun showRenameDialog(fileName: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Đổi tên $fileName")

        val input = EditText(this)
        builder.setView(input)

        builder.setPositiveButton("Lưu") { dialog, _ ->
            val newName = input.text.toString()
            val file = File(currentDirectory, fileName)
            val newFile = File(currentDirectory, newName)

            if (file.renameTo(newFile)) {
                refreshFileList()
            } else {
                Toast.makeText(this, "Không thể đổi tên", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Hủy") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }


    private fun showDeleteDialog(fileName: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Xóa $fileName")

        builder.setPositiveButton("Xóa") { dialog, _ ->
            val file = File(currentDirectory, fileName)

            if (file.delete()) {
                refreshFileList()
            } else {
                Toast.makeText(this, "Không thể xóa", Toast.LENGTH_SHORT).show()
            }

            dialog.dismiss()
        }

        builder.setNegativeButton("Hủy") { dialog, _ ->
            dialog.dismiss()
        }

        builder.show()
    }




}
