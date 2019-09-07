package com.bluapp.audiorecording;

import android.Manifest;
import android.app.SearchManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.LinearLayoutCompat;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.DexterError;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.PermissionRequestErrorListener;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.mikepenz.fastadapter.FastAdapter;
import com.mikepenz.fastadapter.IItemAdapter;
import com.mikepenz.fastadapter.adapters.ItemAdapter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends AppCompatActivity implements RecyclerSelectInterface{
    private FloatingActionButton recordBtn;
    private RecyclerView recyclerView;
    private LinearLayoutCompat allRecord;
    private LinearLayoutCompat ratedRecord;
    private Menu menu;
    private ItemAdapter<RecordListAdapter> recorditemAdapter;
    public FastAdapter<RecordListAdapter> recordfastAdapter;
    private boolean isSortbyNameAsc = true;
    private boolean isSortbyNameDesc = false;
    private boolean isSortbyDateAsc = false;
    private boolean isSortbyDateDesc = false;
    private RecordDAO recordDAO;
    private boolean getRecordByAll = true;
    private boolean getRecordByRated = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recordBtn = (FloatingActionButton) findViewById(R.id.record_btn);
        recyclerView = (RecyclerView) findViewById(R.id.list);
        allRecord = (LinearLayoutCompat) findViewById(R.id.allRecord);
        ratedRecord = (LinearLayoutCompat) findViewById(R.id.ratedRecord);
        recordDAO = (RecordDAO) RecordDatabase.getInstance(getApplicationContext()).recordDAO();

        permissionRequest();
        initRecyclerView();

        recordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, RecordingActivity.class));
            }
        });

        allRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRecordByAll = true;
                getRecordByRated = false;
                getRecordList();
            }
        });

        ratedRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getRecordByRated = true;
                getRecordByAll = false;
                getRecordList();
            }
        });
    }

    private void initRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.addItemDecoration(new CustomDividerItemDecoration(MainActivity.this));
    }



    public void getRecordList() {
        List<RecordListAdapter> dataSource = new ArrayList<>();
        recorditemAdapter = new ItemAdapter<>();
        recordfastAdapter = FastAdapter.with(recorditemAdapter);
        recyclerView.setAdapter(recordfastAdapter);
        if(getRecordByAll){
            recordDAO.getAllRecord().observe(this, (List<Record> record) -> {
                RecordDataSource(record, dataSource);
            });
        }else{
            recordDAO.getRecordByStar(true).observe(this, (List<Record> record) -> {
                RecordDataSource(record, dataSource);
            });
        }

        // recordfastAdapter.withOnClickListener((v, adapter, item, position) -> {

        // return false;
        // });

        // recordfastAdapter.withOnLongClickListener((v, adapter, item, position) -> {

        //    return false;
        // });

        recorditemAdapter.getItemFilter().withFilterPredicate(new IItemAdapter.Predicate<RecordListAdapter>() {
            @Override
            public boolean filter(RecordListAdapter item, CharSequence constraint) {
                return item.getRecordListModel().getTitle().toLowerCase().startsWith(String.valueOf(constraint).toLowerCase());
            }
        });

    }

    private void RecordDataSource(List<Record> record, List<RecordListAdapter> dataSource){
        if(dataSource.size() > 0){
            dataSource.clear();
            recorditemAdapter.clear();
        }
        for (int i = 0; i < record.size(); i++) {
            int id = record.get(i).getId();
            String title = record.get(i).getTitle();
            String filepath = record.get(i).getFilepath();
            String length = record.get(i).getLength();
            Boolean rate = record.get(i).getRate();
            String filesize = record.get(i).getFilesize();
            String createdtime = record.get(i).getCreatedtime();
            dataSource.add(new RecordListAdapter(new RecordListModel(id, title, filepath, length, rate, filesize, createdtime), this, this));
        }
        if (isSortbyNameAsc) {
            Collections.sort(dataSource, new Comparator<RecordListAdapter>() {
                public int compare(RecordListAdapter a, RecordListAdapter b) {
                    return a.getRecordListModel().getTitle().compareTo(b.getRecordListModel().getTitle());
                }
            });
        } else if (isSortbyNameDesc) {
            Collections.sort(dataSource, new Comparator<RecordListAdapter>() {
                public int compare(RecordListAdapter a, RecordListAdapter b) {
                    return a.getRecordListModel().getTitle().compareTo(b.getRecordListModel().getTitle());
                }
            });
            Collections.reverse(dataSource);
        } else if (isSortbyDateAsc) {
            Collections.sort(dataSource, new Comparator<RecordListAdapter>() {
                public int compare(RecordListAdapter a, RecordListAdapter b) {
                    return a.getRecordListModel().getCreatedtime().compareTo(b.getRecordListModel().getCreatedtime());
                }
            });
        } else if (isSortbyDateDesc) {
            Collections.sort(dataSource, new Comparator<RecordListAdapter>() {
                public int compare(RecordListAdapter a, RecordListAdapter b) {
                    return a.getRecordListModel().getCreatedtime().compareTo(b.getRecordListModel().getCreatedtime());
                }
            });
            Collections.reverse(dataSource);
        }

        recorditemAdapter.add(dataSource);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.option_menu, menu);
        this.menu = menu;
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_searchview).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String searchText) {
                recorditemAdapter.filter(searchText);
                return false;
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch (item.getItemId()) {
            case R.id.sort_name_asc:
                isSortbyNameAsc = true;
                isSortbyNameDesc = false;
                isSortbyDateAsc = false;
                isSortbyDateDesc = false;
                getRecordList();
                return true;

            case R.id.sort_name_desc:
                isSortbyNameDesc = true;
                isSortbyNameAsc = false;
                isSortbyDateAsc = false;
                isSortbyDateDesc = false;
                getRecordList();
                return true;

            case R.id.sort_date_asc:
                isSortbyDateAsc = true;
                isSortbyNameAsc = false;
                isSortbyNameDesc = false;
                isSortbyDateDesc = false;
                getRecordList();
                return true;

            case R.id.sort_date_desc:
                isSortbyDateDesc = true;
                isSortbyDateAsc = false;
                isSortbyNameAsc = false;
                isSortbyNameDesc = false;
                getRecordList();
                return true;
        }

        return true;
    }




    private void permissionRequest() {
        Dexter.withActivity(this).withPermissions(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        if (report.areAllPermissionsGranted()) {
                            // do you work now
                            getRecordList();
                        }
                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                            showSettingsDialog();
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .withErrorListener(new PermissionRequestErrorListener() {
                    @Override
                    public void onError(DexterError error) {
                        Toast.makeText(getApplicationContext(), "Error occurred! ", Toast.LENGTH_SHORT).show();
                    }
                })
                .onSameThread()
                .check();
    }

    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Need Permissions");
        builder.setMessage("This app needs permission to use this feature. You can grant them in app settings.");
        builder.setPositiveButton("GOTO SETTINGS", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                openSettings();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void openSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivityForResult(intent, 101);
    }

    @Override
    public void selectedrecord() {
        recordfastAdapter.notifyAdapterDataSetChanged();
    }
}
