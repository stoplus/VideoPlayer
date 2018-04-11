package com.example.den.videoplayer;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class DialogReferenceCar extends DialogFragment {
    private Dialog dialog;
    private ListView listView;
    private LinkedHashMap<String, String> hashMap = new LinkedHashMap<>();
    private FragmentActivity current;
    private DeleteUserInterface datable;

    @Override // Метод onAttach() вызывается в начале жизненного цикла фрагмента
    public void onAttach(Context context) {
        super.onAttach(context);
        datable = (DeleteUserInterface) context;
    } // onAttach

    @NonNull // создание диалога
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        current = getActivity();
        final AlertDialog.Builder builder = new AlertDialog.Builder(current);

        //создаем вид
        View view = getActivity().getLayoutInflater().inflate(R.layout.dialog_reference, null);

        listView = (ListView) view.findViewById(R.id.adm_recycler_reference);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                datable.deleteUser(position);
                dismiss();
            }
        });

        List<String> list = getListVodeo();
        AdapterForList adapter = new AdapterForList(current, list);
        listView.setAdapter(adapter);

        builder.setTitle("Плейлист")
                .setIcon(R.drawable.hplib_img_btn_playlist)
                .setView(view);//показываем созданный вид

        dialog = builder.create();
        dialog.getWindow().setBackgroundDrawableResource(R.color.colorDialog);

        return dialog;
    } // onCreateDialog

    public List<String> getListVodeo() {
        List<String> list = new ArrayList<>();
        ContentResolver contentResolver = current.getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        Cursor cursor = contentResolver.query(uri, null, null, null, null);
        if (cursor == null) {
            Toast.makeText(current, "Ошибка", Toast.LENGTH_LONG).show();
        } else if (!cursor.moveToFirst()) {
            Toast.makeText(current, "На устройстве отсутствует видео файлы", Toast.LENGTH_LONG).show();
        } else {
            int dataColumn = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME);
            do {
                String name = cursor.getString(dataColumn);
                if (name != null) list.add(name);
            } while (cursor.moveToNext());
        }
        if (cursor != null) cursor.close();
        return list;
    }
} // class DialogReferenceCar
