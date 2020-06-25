package com.example.managerfiles;

import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import static com.example.managerfiles.R.layout.index;

public class MainActivity extends AppCompatActivity {
    static final int CREATE_DIR = 1;
    static final int CREATE_FILE = 2;
    static final  int RENAME = 3;

    ListView listView;
    List<String> items;
    File root = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission denied! Asking for permission from user");
                requestPermissions(new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1234);
            }

            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.v("TAG", "Permission denied! Asking for permission from user");
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 5678);
            }
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Files Manager");

        actionBar.setDisplayShowHomeEnabled(true);

        items = new ArrayList<String>();
        listView = findViewById(R.id.list_view);

        displayListDir(root);

        listView.setLongClickable(true);
        registerForContextMenu(listView);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String path = items.get(position);
                File file = new File(path);

                if (file.isFile()) {
                    String extension = path.substring(path.lastIndexOf("."));
                    Log.v("TAG", "la file " + extension);

                    if (extension.equals(".txt")) {
                        showTextViewCustomDialog(path);
                    } else if (extension.equals(".jpg") || extension.equals(".png")) {
                        showImageViewCustomDialog(path);
                    }

                } else if (file.isDirectory()) {
                    //Log.v("TAG", "la thu muc" + path);
                    showListViewCustomDialog(path);
                }
            }
        });
    }

    private void displayListDir(File root) {
        File[] files = root.listFiles();
        items.clear();

        for (File file : files) {
            items.add(file.getPath());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                items
        );

        listView.setAdapter(adapter);
    }

    private void showCustomDialog(final int dowhat, final int index) {
        final Dialog customDialog = new Dialog(MainActivity.this);
        customDialog.setContentView(R.layout.custom_dialog);
        TextView textView = customDialog.findViewById(R.id.text_view);
        textView.setText("Enter name:");

        final EditText editText = customDialog.findViewById(R.id.edit_text);


        customDialog.findViewById(R.id.btn_close).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });

        customDialog.findViewById(R.id.btn_ok).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editText.getText().toString();

                if (dowhat == CREATE_DIR) {
                    createDirectory(name);
                } else if (dowhat == CREATE_FILE) {
                    createFile(name);
                } else if (dowhat == RENAME) {
                    renameFile(items.get(index), name); // pass old_name, new_name
                }

                customDialog.dismiss();
                displayListDir(root);
            }
        });

        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show();

        Window window = customDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); //set width, height for dialog
    }

    private void showAlertDialog(String message, final String path) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

        AlertDialog dialog = builder.setTitle("Delete File")
                .setMessage(message)
                .setNegativeButton("No", null)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        removeFile(path);
                        displayListDir(root);
                        dialog.dismiss();
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void showListViewCustomDialog(String path) {
        final Dialog customDialog = new Dialog(MainActivity.this);
        customDialog.setContentView(R.layout.dialog_listview);
        ListView listView2 = customDialog.findViewById(R.id.list_view_2);
        TextView textView = customDialog.findViewById(R.id.text_view_note);

        File subroot = new File(path);
        File[] files = subroot.listFiles();
        items.clear();

        for (File file : files) {
            items.add(file.getPath());
        }

        if (items.size() == 0) {
            textView.setText("Folder is empty.");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_list_item_1,
                items
        );

        listView2.setAdapter(adapter);

        customDialog.findViewById(R.id.btn_close_listview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
                displayListDir(root);
            }
        });

        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show();

        Window window = customDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); //set width, height for dialog
    }

    private void showTextViewCustomDialog(String path) {
        final Dialog customDialog = new Dialog(MainActivity.this);
        customDialog.setContentView(R.layout.dialog_textview);
        TextView textView = customDialog.findViewById(R.id.text_view_content);

        File file = new File(path);

        if (file.exists()) {
            try {
                InputStream is = new FileInputStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));

                String result = "";
                String line;

                while ((line = reader.readLine()) != null)
                    result += line + "\n";

                reader.close();
                is.close();

                //show file content
                textView.setText(result);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else
            Log.v("TAG", "File khong ton tai");

        customDialog.findViewById(R.id.btn_close_textview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });

        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show();

        Window window = customDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); //set width, height for dialog
    }

    private void showImageViewCustomDialog(String path) {
        final Dialog customDialog = new Dialog(MainActivity.this);
        customDialog.setContentView(R.layout.dialog_image);
        ImageView imageView = customDialog.findViewById(R.id.image_view_2);

        File imgFile = new File(path);

        if (imgFile.exists()) {
            try {
                Bitmap bitmap = BitmapFactory.decodeFile(imgFile.getAbsolutePath());
                imageView.setImageBitmap(bitmap);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else
            Log.v("TAG", "File khong ton tai");

        customDialog.findViewById(R.id.btn_close_imageview).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                customDialog.dismiss();
            }
        });

        customDialog.setCanceledOnTouchOutside(false);
        customDialog.show();

        Window window = customDialog.getWindow();
        window.setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT); //set width, height for dialog
    }

    private void createFile(String filename) {
        try {
            String path = Environment.getExternalStorageDirectory().getAbsolutePath(); //access SD Card
            //String path = getFilesDir().getAbsolutePath(); //access Interal Storage
            File file = new File(path + "/" + filename + ".txt");

            Log.v("TAG", file.getPath());

            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(file));
            writer.write("Create a file successful.");
            writer.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void createDirectory(String name) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath(); //access SD Card
        //String path = getFilesDir().getAbsolutePath(); //access Interal Storage
        File file = new File(path + "/" + name);

        Log.v("TAG", file.getPath());

        if (!file.exists()) {
            try {
                file.mkdirs();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            Log.v("TAG", "File da ton tai");
        }
    }

    private void renameFile(String old_name, String new_name) {
        String path = Environment.getExternalStorageDirectory().getAbsolutePath(); //access SD Card
        File old_file = new File(old_name);

        if (old_file.isFile()) {
            String extension = old_name.substring(old_name.lastIndexOf("."));
            Log.v("TAG", "rename a file " + extension);
            new_name += extension;
        } else if (old_file.isDirectory()) {
            Log.v("TAG", "rename a directory");
        }

        File new_file = new File(path + "/" + new_name);
        //Log.v("TAG", old_name);
        //Log.v("TAG", new_file.getPath());
        old_file.renameTo(new_file);
    }

    private void removeFile(String path) {
        File file = new File(path);
        if (file.exists()) {
            //Log.v("TAG", "File ton tai");
            file.delete();
        } else
            Log.v("TAG", "File khong ton tai");
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        menu.setHeaderTitle("Select an action");
        menu.add(0, 0, 0, "Rename");
        menu.add(0, 1, 0, "Copy");
        menu.add(0, 2, 0, "Delete");
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        Log.v("TAG", items.get(info.position) + " selected");

        int id = item.getItemId();

        if (id == 0) { // Rename
            showCustomDialog(RENAME, info.position);
            Log.v("TAG", "Rename action");
        } else if (id == 1) { // Copy
            Log.v("TAG", "Copy action");
        } else if (id == 2) { // Delete
            showAlertDialog("Are you sure you want to delete the file?", items.get(info.position));
            Log.v("TAG", "Delete action");
        }

        return super.onContextItemSelected(item);
    }

    @SuppressLint("ResourceType")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(index, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_create_dir) {
            showCustomDialog(CREATE_DIR, 0);
            Log.v("TAG", "create dir");
        } else if (id == R.id.action_create_file) {
            showCustomDialog(CREATE_FILE, 0);
            Log.v("TAG", "create file");
        } else if (id == R.id.action_refresh) {
            displayListDir(root);
            Log.v("TAG", "refresh");
        }

        return super.onOptionsItemSelected(item);
    }
}
