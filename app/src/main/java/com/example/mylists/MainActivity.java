package com.example.mylists;

//фио студентов и тд

/*
Новый проект
открывается список студентов
выбранный студент подсвечивает
в меню добавить удалить изменить студента
проверка ввода
по нажатию открываются предметы
меню аналогично студента
 */

import static android.Manifest.permission.CALL_PHONE;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MyLog";
    private int mPosition;
    private Menu mMenu;
    private ActivityResultLauncher<Intent> mIntentActivityResultLauncher;
    private dbHelperStudent dbHelperStudent;
    private SQLiteDatabase db;
    private Cursor userCursor;
    private Integer userId=0;
    private Integer lastID=0;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        mMenu = menu;
        try {
            if (mStudents.size() == 0 || mPosition == -1) {
                mMenu.findItem(R.id.stChange).setVisible(false);
                mMenu.findItem(R.id.stDelete).setVisible(false);
            }
        }catch(Exception e){

        }
        return true;
    }

    @SuppressLint("NonConstantResourceId")
    public boolean onOptionsItemSelected(MenuItem item){
        switch (item.getItemId()) {
            case R.id.miAbout:{
                AlertDialog.Builder infoDialog = new AlertDialog.Builder(MainActivity.this);
                infoDialog.setTitle("О программе");
                infoDialog.setMessage("Это программа группы 4ПМ/2!");
                infoDialog.setCancelable(false);
                infoDialog.setPositiveButton("Прочитано", null);
                infoDialog.show();
                return true;
            }
            case R.id.stAdd:{
                addStudent("", "", "");
                return true;
            }
            case R.id.stChange:{
                changeStudent(mPosition);
                return true;
            }
            case R.id.stDelete:{
                deleteStudent(mPosition);
                return true;
            }
            case R.id.miExit:{
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dowloadFromDB dfd = new dowloadFromDB();
        dfd.execute();
        setContentView(R.layout.main_ll);
        ((LinearLayout) findViewById(R.id.llInput)).setVisibility(
                ((Button) findViewById(R.id.bAddStudent)).getVisibility()
        );
        mPosition = -1;
        //mStudents = new ArrayList<>();
        delStdID = new ArrayList<>();
//        dbHelperStudent = new dbHelperStudent(getApplicationContext());
//        db = dbHelperStudent.getReadableDatabase();
//        userCursor =  db.rawQuery("select * from "+ dbHelperStudent.TABLE, null);
////        SharedPreferences sPref = getPreferences(MODE_PRIVATE);
////        int size = sPref.getInt("count", 0);
////        if (size>0){
////            Gson gson = (new GsonBuilder()).create();
////            for (int i=0;i<size;++i){
////                String s = sPref.getString("student"+i, "");
////                if(!s.equals("")){
////                    Student st = gson.fromJson(s, Student.class);
////                    mStudents.add(st);
////                }
////            }
////        }
//        while(userCursor.moveToNext()) {
//            userId=userCursor.getInt(0);
//            Student st = new Student(userCursor.getInt(0), userCursor.getString(1), userCursor.getString(2), userCursor.getString(3));
//            mStudents.add(st);
//        }
//        lastID=userId;
//        createStudentList(null);
        mIntentActivityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        if(result.getResultCode() == Activity.RESULT_OK) {
                            Intent intent = result.getData();
                            Student s = intent.getParcelableExtra("student");
                            System.out.println(s);
                            System.out.println(s.getSubjects().size());
                            if (mPosition == -1) {
                                mStudents.add(s);
                                mPosition = mStudents.size()-1;
                            }
                            else {
                                mStudents.set(mPosition, s);
                            }
                            mMenu.findItem(R.id.stChange).setVisible(false);
                            mMenu.findItem(R.id.stDelete).setVisible(false);
                            mStudentListAdapter.notifyDataSetChanged();
                        }
                    }
                }
        );

        View.OnLongClickListener OLCL_FIO= new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Collections.sort(mStudents,new CompareByFio());
                mStudentListAdapter.notifyDataSetChanged();
                return false;
            }
        };

        View.OnLongClickListener OLCL_Fac= new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Collections.sort(mStudents,new CompareByFac());
                mStudentListAdapter.notifyDataSetChanged();
                return false;
            }
        };

        View.OnLongClickListener OLCL_Group= new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Collections.sort(mStudents,new CompareByGroup());
                mStudentListAdapter.notifyDataSetChanged();
                return false;
            }
        };

        View.OnLongClickListener OLCL_Phone= new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Intent i = new Intent(Intent.ACTION_CALL);
                i.setData(Uri.parse("tel:"+mStudents.get(mPosition).getmPhone().toString()));
                if (ContextCompat.checkSelfPermission(getApplicationContext(), CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
                    startActivity(i);
                } else {
                    requestPermissions(new String[]{CALL_PHONE},1);
                }
                return false;
            }
        };

        ((LinearLayout) findViewById(R.id.llInfo_FIO)).setOnLongClickListener(OLCL_FIO);
        ((TextView) findViewById(R.id.tvInfo_FIO)).setOnLongClickListener(OLCL_FIO);
        ((TextView) findViewById(R.id.textView7)).setOnLongClickListener(OLCL_FIO);
        ((LinearLayout) findViewById(R.id.llInfo_Fac)).setOnLongClickListener(OLCL_Fac);
        ((TextView) findViewById(R.id.tvInfo_Fac)).setOnLongClickListener(OLCL_Fac);
        ((TextView) findViewById(R.id.textView14)).setOnLongClickListener(OLCL_Fac);
        ((LinearLayout) findViewById(R.id.llInfo_Group)).setOnLongClickListener(OLCL_Group);
        ((TextView) findViewById(R.id.tvInfo_Group)).setOnLongClickListener(OLCL_Group);
        ((TextView) findViewById(R.id.textView18)).setOnLongClickListener(OLCL_Group);
        ((LinearLayout) findViewById(R.id.llInfo_Phone)).setOnLongClickListener(OLCL_Phone);
        ((TextView) findViewById(R.id.tvInfo_Phone)).setOnLongClickListener(OLCL_Phone);
        ((TextView) findViewById(R.id.textView9)).setOnLongClickListener(OLCL_Phone);
    }

    ArrayList<Student> mStudents;
    ArrayList<Integer> delStdID;
    StudentListAdapter mStudentListAdapter;

    public void createStudentList(View view) {
        ListView listView = findViewById(R.id.lvList2);
        mStudentListAdapter=new StudentListAdapter(mStudents,this);
        listView.setAdapter(mStudentListAdapter);

        ((LinearLayout) findViewById(R.id.llInput)).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.bAddStudent)).setVisibility(View.VISIBLE);
        ((Button) findViewById(R.id.bCreateStudentList)).setVisibility(View.GONE);

        AdapterView.OnItemLongClickListener clLStudent = new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
                for (int j = 0; j< listView.getCount();j++) {
                    listView.getChildAt(j).setBackgroundColor(getResources().getColor(R.color.white));
                }
                mPosition = -1;
                mMenu.findItem(R.id.stChange).setVisible(false);
                mMenu.findItem(R.id.stDelete).setVisible(false);
                return true;
            }
        };
        AdapterView.OnItemClickListener clStudent = new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                for (int j = 0; j < listView.getCount(); j++) {
                    if (j == i)
                        listView.getChildAt(j).setBackgroundColor(getResources().getColor(R.color.odd_element));
                    else
                        listView.getChildAt(j).setBackgroundColor(getResources().getColor(R.color.white));
                }
                if ((((LinearLayout) findViewById(R.id.ll_info_student)).getVisibility()==View.GONE) || (mPosition!=i)){
                    ((LinearLayout) findViewById(R.id.ll_info_student)).setVisibility(View.VISIBLE);
                    ((TextView) findViewById(R.id.tvInfo_FIO)).setText(mStudents.get(i).getFIO());
                    ((TextView) findViewById(R.id.tvInfo_Fac)).setText(mStudents.get(i).getFaculty());
                    ((TextView) findViewById(R.id.tvInfo_Group)).setText(mStudents.get(i).getGroup());
                    ((TextView) findViewById(R.id.tvInfo_Phone)).setText(mStudents.get(i).getmPhone());
                }
                else{
                    ((LinearLayout) findViewById(R.id.ll_info_student)).setVisibility(View.GONE);
                }
                mPosition = i;
                mMenu.findItem(R.id.stChange).setVisible(true);
                mMenu.findItem(R.id.stDelete).setVisible(true);
            }
        };
        listView.setOnItemLongClickListener(clLStudent);
        listView.setOnItemClickListener(clStudent);
    }
    public void changeStudent(int position) {
        Intent intent = new Intent(MainActivity.this, StudentInfoActivity.class);
        Log.d(TAG, mStudents.get(mPosition).toString());
        intent.putExtra("student", mStudents.get(mPosition));
        mMenu.findItem(R.id.stChange).setVisible(true);
        mMenu.findItem(R.id.stDelete).setVisible(true);
        mIntentActivityResultLauncher.launch(intent);
    }

    public void addStudent(String fio, String facultet, String group) {
        Intent intent = new Intent(MainActivity.this, StudentInfoActivity.class);
        mPosition = -1;
        intent.putExtra("student", new Student(lastID+1, "", "", "",""));
        mMenu.findItem(R.id.stChange).setVisible(true);
        mMenu.findItem(R.id.stDelete).setVisible(true);
        mIntentActivityResultLauncher.launch(intent);
        lastID++;
    }

    public void deleteStudent(int position) {
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("Удалить студента!");
        inputDialog.setCancelable(false);

        inputDialog.setPositiveButton("Да", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                int id = mStudents.get(position).getID();
                delStdID.add(id);
                mStudents.remove(position);
                if(mStudents.size()==0){
                    mMenu.findItem(R.id.stChange).setVisible(false);
                    mMenu.findItem(R.id.stDelete).setVisible(false);
                }
                mPosition = -1;
                mMenu.findItem(R.id.stChange).setVisible(false);
                mMenu.findItem(R.id.stDelete).setVisible(false);
                mStudentListAdapter.notifyDataSetChanged();
            }
        })
                .setNegativeButton("Нет", null);
        inputDialog.show();
    }

    protected void onDestroy(){
        saveToDB std = new saveToDB();
        std.execute(mStudents);
//        if(mStudents!=null) {
////            SharedPreferences.Editor ed = getPreferences(MODE_PRIVATE).edit();
////            GsonBuilder builder = new GsonBuilder();
////            Gson gson = builder.create();
////            ed.putInt("count", mStudents.size());
//
//            Log.d(TAG, Integer.toString(userId));
//            Log.d(TAG, Integer.toString(mStudents.size()));
//            for (int i=0;i<mStudents.size();++i){
//                Student sdb = mStudents.get(i);
////                String s = gson.toJson(mStudents.get(i));
////                ed.putString("student"+i, s);
//                ContentValues cv = new ContentValues();
//                cv.put(dbHelperStudent.COLUMN_ID, sdb.getID());
//                cv.put(dbHelperStudent.COLUMN_fio, sdb.getFIO());
//                cv.put(dbHelperStudent.COLUMN_faculty, sdb.getFaculty());
//                cv.put(dbHelperStudent.COLUMN_group, sdb.getGroup());
//                Log.d(TAG, Integer.toString(sdb.getID()));
//                if (sdb.getID() <= userId) {
//                    Log.d(TAG, "update");
//                    Log.d(TAG, cv.toString());
//                    db.update(dbHelperStudent.TABLE, cv, dbHelperStudent.COLUMN_ID + "=" + sdb.getID(), null);
//                } else if (sdb.getID() > userId){
//                    Log.d(TAG, "insert");
//                    db.insert(dbHelperStudent.TABLE, null, cv);
//                }
//            }
//
//            for (int i=0; i<delStdID.size();i++){
//                Log.d(TAG, "delete");
//                dbHelperSubject dbHelperSubject = new dbHelperSubject(getApplicationContext());
//                SQLiteDatabase dbC = dbHelperSubject.getReadableDatabase();
//                dbC.delete(com.example.mylists.dbHelperSubject.TABLE, "id = ?", new String[]{String.valueOf(delStdID.get(i))});
//                db.delete(com.example.mylists.dbHelperStudent.TABLE, "id = ?", new String[]{String.valueOf(delStdID.get(i))});
//            }
////            ed.commit();
//        }
        super.onDestroy();
    }

    class dowloadFromDB extends AsyncTask<Void, ArrayList<Student>, ArrayList<Student>>{
        ArrayList<Student> resultStudents = new ArrayList<>();
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Wait to download students from db");
            super.onPreExecute();
        }
        @Override
        protected ArrayList<Student> doInBackground(Void... voids) {
            Log.d(TAG, "Start download students from db");
            dbHelperStudent = new dbHelperStudent(getApplicationContext());
            db = dbHelperStudent.getReadableDatabase();
            dbHelperStudent.onCreate(db);
            userCursor =  db.rawQuery("select * from "+ dbHelperStudent.TABLE, null);
            while(userCursor.moveToNext()) {
                userId=userCursor.getInt(0);
                Log.d(TAG, "Downloaded "+userId+" student!");
                Student st = new Student(userCursor.getInt(0),
                        userCursor.getString(1),
                        userCursor.getString(2),
                        userCursor.getString(3),
                        userCursor.getString(4));
                resultStudents.add(st);
            }
            lastID=userId;
            return resultStudents;
        }
        @Override
        public void onPostExecute(ArrayList<Student> results) {
            super.onPostExecute(results);
            Log.d("MYLOG", "Size "+ String.valueOf(results.size()));
            mStudents = results;
            createStudentList(null);
            Log.d(TAG, "End download students from db");
        }
    }

    class saveToDB extends AsyncTask<ArrayList<Student>, ArrayList<Student>, Void> {
        ArrayList<Student> resultStudents;

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "Wait to save students to db");
            super.onPreExecute();
        }
        @Override
        protected Void doInBackground(ArrayList<Student>... arrayLists) {
            resultStudents = arrayLists[0];
            Log.d(TAG, "Start save students to db");
            if(resultStudents!=null) {
                Log.d(TAG, Integer.toString(userId));
                Log.d(TAG, Integer.toString(resultStudents.size()));
                for (int i=0;i<resultStudents.size();++i){
                    Student sdb = resultStudents.get(i);
                    ContentValues cv = new ContentValues();
                    cv.put(dbHelperStudent.COLUMN_ID, sdb.getID());
                    cv.put(dbHelperStudent.COLUMN_fio, sdb.getFIO());
                    cv.put(dbHelperStudent.COLUMN_faculty, sdb.getFaculty());
                    cv.put(dbHelperStudent.COLUMN_group, sdb.getGroup());
                    cv.put(dbHelperStudent.COLUMN_phone, sdb.getmPhone());
                    Log.d(TAG, Integer.toString(sdb.getID()));
                    if (sdb.getID() <= userId) {
                        Log.d(TAG, "update");
                        Log.d(TAG, cv.toString());
                        db.update(dbHelperStudent.TABLE, cv, dbHelperStudent.COLUMN_ID + "=" + sdb.getID(), null);
                    } else if (sdb.getID() > userId){
                        Log.d(TAG, "insert");
                        db.insert(dbHelperStudent.TABLE, null, cv);
                    }
                }
                for (int i=0; i<delStdID.size();i++){
                    Log.d(TAG, "delete");
                    dbHelperSubject dbHelperSubject = new dbHelperSubject(getApplicationContext());
                    SQLiteDatabase dbC = dbHelperSubject.getReadableDatabase();
                    dbC.delete(com.example.mylists.dbHelperSubject.TABLE, "id_student = ?", new String[]{String.valueOf(delStdID.get(i))});
                    db.delete(com.example.mylists.dbHelperStudent.TABLE, "id = ?", new String[]{String.valueOf(delStdID.get(i))});
                }
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            Log.d(TAG, "End save students to db");
        }
    }
}