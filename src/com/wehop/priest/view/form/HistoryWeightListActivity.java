package com.wehop.priest.view.form;

import java.util.ArrayList;
import java.util.List;

import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.type.core.ICollection;
import com.slfuture.pluto.communication.Networking;
import com.slfuture.pluto.communication.response.JSONResponse;
import com.slfuture.pluto.view.component.ActivityEx;
import com.wehop.priest.R;
import com.wehop.priest.base.DateUtil;
import com.wehop.priest.business.Me;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class HistoryWeightListActivity extends ActivityEx implements OnClickListener {
    private static final String TAG = HistoryWeightListActivity.class.getSimpleName();
    private static final String QUERY_WEIGHT_HISTORY = "queryWeightHistory";
    
    private ListView listView = null;
    private int mPositionToDel = -1;
    private List<DataOfListItem> dataList = new ArrayList<DataOfListItem>();
    private ListViewAdapter listViewAdapter = null;
    private String mMonthString = DateUtil.getCurrentMounth("yyyy年MM月");
    private int userId;
     
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_health_data_list);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.action_bar_health_device_bind);
        View customActionView = actionBar.getCustomView();
        ImageView returnView = (ImageView) customActionView.findViewById(R.id.icon_return);
        returnView.setOnClickListener(this);
        TextView titleView = (TextView) customActionView.findViewById(R.id.title);
        titleView.setText(getString(R.string.history_record));
        
        userId = getIntent().getIntExtra("userId", 0);
        listViewAdapter = new ListViewAdapter(this, dataList);
        listView = (ListView)findViewById(R.id.group_list);
        //listView.setAdapter(adapter);
        listView.setAdapter(listViewAdapter);
        //listView.setOnItemLongClickListener(itemLongClickListener);
        //initLongClickListViewConfig();
        
        loadHistorydate(DateUtil.getCurrentMounth(), "yyyy-MM");
        
        ImageView imageReduceView = (ImageView) findViewById(R.id.image_reduce);
        ImageView imageAddView = (ImageView) findViewById(R.id.image_add);
        imageAddView.requestFocus();
        imageReduceView.setOnClickListener(this);
        imageAddView.setOnClickListener(this);
    }
    
    //token=c9cee9c0-140f-454b-826c-82132a25f590&begin=2016-05-01&end=2016-05-30
    /**
     * 
     * @param mounth
     */
    private void loadHistorydate(String month, String monthFomate) {
        Log.i(TAG, "zhenglihao token=" +  Me.instance.token);
        Networking.doCommand(QUERY_WEIGHT_HISTORY, new JSONResponse(HistoryWeightListActivity.this, null) {
            
            @Override
            public void onFinished(JSONVisitor content) {
                dataList.clear();
                listViewAdapter.notifyDataSetChanged();
                if (null == content || content.getInteger("code") < 0) {
                    // XXX
                    return;
                }
                ICollection<JSONVisitor> datas = content.getVisitors("data");
                if (null == datas) {
                    return;
                }
                
                for (JSONVisitor data : datas) {
                    if (null == data) {
                        continue;
                    }
                    // add tag
                    String date = data.getString("time");//"2016-05-19"
                    DataOfListItem dataOfTagItem = new DataOfListItem(true, date);
                    dataList.add(dataOfTagItem);
                    
                    ICollection<JSONVisitor> subDatas = data.getVisitors("subData");
                    if (null == subDatas) {
                        continue;
                    }
                    for (JSONVisitor subdata : subDatas) {
                        if (null == subdata) {
                            continue;
                        }
                        DataOfListItem dataOfListItem = new DataOfListItem(
                                false, 
                                null, 
                                subdata.getInteger("id", 0), 
                                subdata.getInteger("weight", 0), 
                                subdata.getInteger("fat", 0), 
                                subdata.getInteger("bmi", 0),
                                subdata.getInteger("bone", 0), 
                                subdata.getInteger("mus", 0), 
                                subdata.getInteger("tbw", 0),
                                subdata.getString("time"));
                        dataList.add(dataOfListItem);
                    }
                }
                listViewAdapter.notifyDataSetChanged();
            }
        }, Me.instance.token, userId, DateUtil.getMinMonthDate(month, monthFomate), DateUtil.getMaxMonthDate(month, monthFomate));
    }
    
    void initLongClickListViewConfig()  
    {  
        listView.setOnItemLongClickListener(itemLongClickListener);  
        registerForContextMenu(listView);    
    }
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0,0,0,"删除");
    }
    
    
    private void adjustMounthView(int step) {
        String afterMounth = "";
        afterMounth = DateUtil.getBrotherMonth(mMonthString, "yyyy年MM月", step);
        EditText monthview = (EditText) findViewById(R.id.edit_mounth_choose);
        monthview.setText(afterMounth);
        mMonthString = afterMounth;
        loadHistorydate(afterMounth, "yyyy年MM月");
    }
    
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.icon_return:
            finish();
            break;
        case R.id.image_add:
            adjustMounthView(1);
            break;
        case R.id.image_reduce:
            adjustMounthView(-1);
            break;
        default:
            break;
        }
    }
    
    public class DataOfListItem {
        public boolean isTag = false;
        public int id;
        public int weight;
        public int fat;
        public int bmi;
        public int bone;
        public int mus;
        public int tbw;
        
        public String time;
        public String date;
        public DataOfListItem(boolean istag, String date) {
            this.isTag = istag;
            this.date = date;
        }
        public DataOfListItem(boolean istag, String date, int id, 
                int weight, int fat, int bmi, int bone, int mus, int tbw, String time) {
            this.isTag = istag;
            this.date = date;
            this.id = id;
            this.weight = weight;
            this.fat = fat;
            this.bmi = bmi;
            this.bone = bone;
            this.mus = mus;
            this.tbw = tbw;
            this.time = time;
        }
    }
    
    private class ListViewAdapter extends BaseAdapter {
        private Context context;
        private List<DataOfListItem> dataList;
        private LayoutInflater listContainer;
        public final class ListItemView {
            public TextView tagView;
            public TextView timeView;
            
            public TextView weightView;
            public TextView fatView;
            public TextView boneView;
            public TextView tbwView;
            public TextView musView;
            public TextView bmiView;
        }
        
        public ListViewAdapter(Context context, List<DataOfListItem> dataList) {
            this.context = context;
            this.listContainer = LayoutInflater.from(context);
            this.dataList = dataList;
        }

        @Override
        public int getCount() {
            return dataList.size();
        }

        @Override
        public Object getItem(int arg0) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ListItemView listItemView = null;
            DataOfListItem dataOfListItem = dataList.get(position);
            
//            if (null == convertView) {
                listItemView = new ListItemView();
                if (dataOfListItem.isTag) {
                    convertView = listContainer.inflate(R.layout.history_list_item_tag, null);
                    listItemView.tagView = (TextView) convertView.findViewById(R.id.tv_tag);
                } else {
                    convertView = listContainer.inflate(R.layout.history_weight_list_item, null);
                    listItemView.weightView = (TextView) convertView.findViewById(R.id.tv_weight);
                    listItemView.fatView = (TextView) convertView.findViewById(R.id.tv_fat);
                    listItemView.boneView = (TextView) convertView.findViewById(R.id.tv_bone);
                    listItemView.tbwView = (TextView) convertView.findViewById(R.id.tv_tbw);
                    listItemView.musView = (TextView) convertView.findViewById(R.id.tv_mus);
                    listItemView.bmiView = (TextView) convertView.findViewById(R.id.tv_bmi);
                    
                    listItemView.timeView = (TextView) convertView.findViewById(R.id.tv_time);
                }
                convertView.setTag(listItemView);
//            } else {
//                listItemView = (ListItemView) convertView.getTag();
//            }
            
            if (dataOfListItem.isTag) {
                listItemView.tagView.setText(dataOfListItem.date);
            } else {
                listItemView.timeView.setText(dataOfListItem.time);
                listItemView.weightView.setText(dataOfListItem.weight + "kg");
                listItemView.fatView.setText(dataOfListItem.fat + "%");
                listItemView.boneView.setText(dataOfListItem.bone + "kg");
                listItemView.tbwView.setText(dataOfListItem.tbw + "%");
                listItemView.musView.setText(dataOfListItem.mus + "kg");
                listItemView.bmiView.setText(dataOfListItem.bmi + "");
            }
            
            return convertView;
        }
        
    }
    
    
    OnItemLongClickListener itemLongClickListener = new OnItemLongClickListener() {  
        
        @Override  
        public boolean onItemLongClick(AdapterView<?> arg0, View view, final int arg2, long arg3) {
            DataOfListItem itemData = dataList.get(arg2);
            if (itemData.isTag) {
                return true;
            }
            mPositionToDel = arg2;//itemData.id;
            return false;//let the window create menu
        }
    };

}
