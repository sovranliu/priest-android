package com.wehop.priest.view.form;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.SimpleAdapter.ViewBinder;

import com.slfuture.carrie.base.json.JSONVisitor;
import com.slfuture.carrie.base.model.core.IEventable;
import com.slfuture.carrie.base.text.Text;
import com.slfuture.carrie.base.type.Table;
import com.slfuture.pluto.communication.Networking;
import com.slfuture.pluto.communication.response.ImageResponse;
import com.slfuture.pluto.communication.response.JSONResponse;
import com.slfuture.pluto.etc.GraphicsHelper;
import com.slfuture.pluto.view.annotation.ResourceView;
import com.slfuture.pluto.view.component.FragmentEx;
import com.slfuture.pretty.general.utility.GeneralHelper;
import com.slfuture.pretty.qcode.Module;
import com.wehop.priest.R;
import com.wehop.priest.business.Me;
import com.wehop.priest.business.core.IMeListener;
import com.wehop.priest.business.user.Doctor;
import com.wehop.priest.business.user.Patient;

/**
 * 会话界面
 */
@ResourceView(id = R.layout.activity_conversation)
public class ConversationActivity extends FragmentEx implements IMeListener {
	@ResourceView(id = R.id.conversation_image_scan)
	public ImageView imgScan;
	@ResourceView(id = R.id.conversation_image_add)
	public ImageView imgAdd;
	
	@ResourceView(id = R.id.search_text_keword)
	public EditText txtKeyword;

	@ResourceView(id = R.id.conversation_layout_doctor)
	public View viewTabDoctor;
	@ResourceView(id = R.id.conversation_layout_patient)
	public View viewTabPatient;
	@ResourceView(id = R.id.conversation_image_doctor)
	public ImageView imgTabDoctor;
	@ResourceView(id = R.id.conversation_image_patient)
	public ImageView imgTabPatient;
	
	@ResourceView(id = R.id.conversation_list_doctor)
	public ListView listDoctor;
	@ResourceView(id = R.id.conversation_list_patient)
	public ListView listPatient;

	/**
	 * 选项卡
	 */
	public final static int TAB_DOCTOR = 0;
	public final static int TAB_PATIENT = 1;
	
	/**
	 * 当前选项卡：0=医生，1=病人
	 */
	private int tab = TAB_DOCTOR;
	/**
	 * 医生列表
	 */
	private List<Map<String, Object>> doctorList = new ArrayList<Map<String, Object>>();
	/**
	 * 患者列表
	 */
	private List<Map<String, Object>> patientList = new ArrayList<Map<String, Object>>();


	@Override
    public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		//
		imgScan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if(null == Me.instance) {
					Intent intent = new Intent(ConversationActivity.this.getActivity(), LoginActivity.class);
					ConversationActivity.this.startActivity(intent);
					Toast.makeText(ConversationActivity.this.getActivity(), "请先登录账号", Toast.LENGTH_LONG).show();
					return;
				}
				Module.capture(ConversationActivity.this.getActivity(), new IEventable<String>() {
					@Override
					public void on(String data) {
						if(null == data) {
							return;
						}
						if(data.startsWith("add://")) {
							data = data.replace("add://", "");
							Intent intent = new Intent(ConversationActivity.this.getActivity(), AddFriendActivity.class);
							intent.putExtra("phone", data);
							ConversationActivity.this.getActivity().startActivity(intent);
						}
						else if(data.startsWith("http://")) {
							Uri uri = Uri.parse(data);  
				            Intent intent = new Intent(Intent.ACTION_VIEW, uri);  
				            startActivity(intent);  
							return;
						}
					}
				});
			}
		});
		imgAdd.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ConversationActivity.this.getActivity(), AddFriendActivity.class);
				ConversationActivity.this.getActivity().startActivity(intent);
			}
		});
		txtKeyword.addTextChangedListener(new TextWatcher() {
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) { }
			@Override
			public void afterTextChanged(Editable s) {
				refresh();
			}
		});
		viewTabDoctor.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectTab(TAB_DOCTOR);
			}
		});
		viewTabPatient.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				selectTab(TAB_PATIENT);
			}
		});
		SimpleAdapter adapter1 = new SimpleAdapter(this.getActivity(), 
				doctorList,
                R.layout.listitem_conversation,
                new String[] {"photo", "name", "tip"},
                new int[] {R.id.listitem_conversation_image_photo, R.id.listitem_conversation_label_name, R.id.listitem_conversation_label_tip});
		SimpleAdapter adapter2 = new SimpleAdapter(this.getActivity(), 
				patientList,
                R.layout.listitem_conversation,
                new String[] {"photo", "name", "tip"},
                new int[] {R.id.listitem_conversation_image_photo, R.id.listitem_conversation_label_name, R.id.listitem_conversation_label_tip});
		listDoctor.setAdapter(adapter1);
		listPatient.setAdapter(adapter2);
		OnItemClickListener clickListener = new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if(TAB_DOCTOR == tab) {
					Me.instance.doChat(ConversationActivity.this.getActivity(), null, Me.instance.doctors.get(position).imId);
				}
				else if(TAB_PATIENT == tab) {
					Me.instance.doChat(ConversationActivity.this.getActivity(), null, Me.instance.patients.get(position).imId);
				}
			}
		};
		listDoctor.setOnItemClickListener(clickListener);
		listPatient.setOnItemClickListener(clickListener);
		OnItemLongClickListener longClickListener = new OnItemLongClickListener() {
			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
				if(TAB_PATIENT == tab) {
					return true;
				}
				GeneralHelper.showSelector(ConversationActivity.this.getActivity(), new IEventable<Integer>() {
					@Override
					public void on(Integer index) {
						if(0 == index) {
							if(TAB_DOCTOR == tab) {
								Intent intent = new Intent(ConversationActivity.this.getActivity(), AddFriendActivity.class);
								intent.putExtra("userId", Me.instance.doctors.get(position).id);
								ConversationActivity.this.getActivity().startActivity(intent);
							}
							else if(TAB_PATIENT == tab) {
								Intent intent = new Intent(ConversationActivity.this.getActivity(), AddFriendActivity.class);
								intent.putExtra("userId", Me.instance.patients.get(position).id);
								ConversationActivity.this.getActivity().startActivity(intent);
							}
						}
						else if(1 == index) {
							if(TAB_DOCTOR == tab) {
								new AlertDialog.Builder(ConversationActivity.this.getActivity()).setTitle("确认删除吗？")  
								.setIcon(android.R.drawable.ic_dialog_info)  
								.setPositiveButton("确定", new DialogInterface.OnClickListener() {
										@Override  
										public void onClick(DialogInterface dialog, int which) {
											Networking.doCommand("remove", new JSONResponse(ConversationActivity.this.getActivity()) {
												@Override
												public void onFinished(JSONVisitor content) {
													if(null == content || content.getInteger("code", 0) <= 0) {
														return;
													}
													Me.instance.refreshDoctor(ConversationActivity.this.getActivity(),  new IEventable<Boolean>() {
														@Override
														public void on(Boolean result) {
															if(!result) {
																return;
															}
															ConversationActivity.this.refreshDoctor();
														}
													});
												}
											}, Me.instance.token, Me.instance.doctors.get(position).id);
										}  
								}).setNegativeButton("返回", new DialogInterface.OnClickListener() {
							        @Override  
							        public void onClick(DialogInterface dialog, int which) {}  
								}).show();
							}
							else if(TAB_PATIENT == tab) {
								Networking.doCommand("remove", new JSONResponse(ConversationActivity.this.getActivity()) {
									@Override
									public void onFinished(JSONVisitor content) {
										if(null == content || content.getInteger("code", 0) <= 0) {
											return;
										}
										Me.instance.refreshPatient(ConversationActivity.this.getActivity(),  new IEventable<Boolean>() {
											@Override
											public void on(Boolean result) {
												if(!result) {
													return;
												}
												ConversationActivity.this.refreshPatient();
											}
										});
									}
								}, Me.instance.token, Me.instance.patients.get(position).id);
							}
						}
					}
				}, "编  辑", "删  除", "", "取  消");
				return true;
			}
		};
		listDoctor.setOnItemLongClickListener(longClickListener);
		listPatient.setOnItemLongClickListener(longClickListener);
		adapter1.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {  
			    if(view instanceof ImageView) {
			    	if(null ==  data) {
			    		((ImageView) view).setImageResource(R.drawable.drawable_null);
			    	}
			    	else if(data instanceof Bitmap) {
			    		((ImageView) view).setImageBitmap((Bitmap) data);
			    	}
			        return true;
			    }
			    if(view instanceof TextView) {
			    	TextView text = (TextView) view;
			    	if(text.getId() == R.id.listitem_conversation_label_tip) {
				    	if(null == data || 0 == (Integer) data) {
				    		text.setVisibility(View.GONE);
				    	}
				    	else {
				    		text.setVisibility(View.VISIBLE);
				    		text.setText(String.valueOf(data));
				    	}
			    	}
			    	else {
			    		if(null != data) {
				    		text.setText(String.valueOf(data));
			    		}
			    	}
			        return true;
			    }
		        return false;
			}
		});
		adapter2.setViewBinder(new ViewBinder() {
			@Override
			public boolean setViewValue(View view, Object data, String textRepresentation) {  
			    if(view instanceof ImageView) {
			    	if(null ==  data) {
			    		((ImageView) view).setImageResource(R.drawable.drawable_null);
			    	}
			    	else if(data instanceof Bitmap) {
			    		((ImageView) view).setImageBitmap((Bitmap) data);
			    	}
			        return true;
			    }
			    if(view instanceof TextView) {
			    	TextView text = (TextView) view;
			    	if(text.getId() == R.id.listitem_conversation_label_tip) {
				    	if(null == data || 0 == (Integer) data) {
				    		text.setVisibility(View.GONE);
				    	}
				    	else {
				    		text.setVisibility(View.VISIBLE);
				    		text.setText(String.valueOf(data));
				    	}
			    	}
			    	else {
			    		if(null != data) {
				    		text.setText(String.valueOf(data));
			    		}
			    	}
			        return true;
			    }
		        return false;
			}
		});
		selectTab(TAB_DOCTOR);
    }

	@Override
    public void onResume() {
		super.onResume();
		refreshDoctor();
		refreshPatient();
	}

	/**
	 * 选择选项卡
	 * 
	 * @param tab 选项卡
	 */
	public void selectTab(int tab) {
		if(TAB_DOCTOR == tab) {
			imgTabDoctor.setImageResource(R.drawable.icon_conversation_doctor_selected);
			imgTabPatient.setImageResource(R.drawable.icon_conversation_patient);
			listDoctor.setVisibility(View.VISIBLE);
			listPatient.setVisibility(View.GONE);
		}
		else if(TAB_PATIENT == tab) {
			imgTabDoctor.setImageResource(R.drawable.icon_conversation_doctor);
			imgTabPatient.setImageResource(R.drawable.icon_conversation_patient_selected);
			listDoctor.setVisibility(View.GONE);
			listPatient.setVisibility(View.VISIBLE);
		}
		this.tab = tab;
	}
	
	/**
	 * 刷新列表
	 */
	private void refresh() {
		refreshDoctor();
		refreshPatient();
	}

	/**
	 * 刷新医生列表
	 */
	private void refreshDoctor() {		
		if(null == Me.instance) {
			doctorList.clear();
			((SimpleAdapter) listDoctor.getAdapter()).notifyDataSetChanged();
			return;
		}
		doctorList.clear();
		for(Doctor doctor : Me.instance.doctors) {
			String keyword = txtKeyword.getText().toString();
			if(!Text.isBlank(keyword)) {
				boolean sentry = false;
				if(null != doctor.name && doctor.name.contains(keyword)) {
					sentry = true;
				}
				if(null != doctor.relation && doctor.relation.contains(keyword)) {
					sentry = true;
				}
				if(!sentry) {
					continue;
				}
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", doctor.id);
			if(null == doctor.photo) {
				map.put("photo", GraphicsHelper.decodeResource(ConversationActivity.this.getActivity(), R.drawable.icon_user_default));
			}
			else {
				Networking.doImage("image", new ImageResponse(doctor.photo, map) {
					@SuppressWarnings("unchecked")
					@Override
					public void onFinished(Bitmap content) {
						content = GraphicsHelper.makeImageRing(GraphicsHelper.makeCycleImage(content, 200, 200), Color.WHITE, 4);
						((Map<String, Object>) tag).put("photo", content);
						((SimpleAdapter) listDoctor.getAdapter()).notifyDataSetChanged();
					}
				}, doctor.photo);
			}
			map.put("name", doctor.nickname());
			map.put("tip", doctor.unreadMessageCount());
			if(doctor.unreadMessageCount() > 0) {
				doctorList.add(0, map);
			}
			else {
				doctorList.add(map);
			}
		}
		((SimpleAdapter) listDoctor.getAdapter()).notifyDataSetChanged();
	}
	
	/**
	 * 刷新病人列表
	 */
	private void refreshPatient() {		
		if(null == Me.instance) {
			patientList.clear();
			((SimpleAdapter) listPatient.getAdapter()).notifyDataSetChanged();
			return;
		}
		patientList.clear();
		for(Patient patient : Me.instance.patients) {
			String keyword = txtKeyword.getText().toString();
			if(!Text.isBlank(keyword)) {
				boolean sentry = false;
				if(null != patient.nickname && patient.nickname.contains(keyword)) {
					sentry = true;
				}
				if(null != patient.name && patient.name.contains(keyword)) {
					sentry = true;
				}
				if(!sentry) {
					continue;
				}
			}
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("id", patient.id);
			if(null == patient.photo) {
				map.put("photo", GraphicsHelper.decodeResource(ConversationActivity.this.getActivity(), R.drawable.icon_user_default));
			}
			else {
				Networking.doImage("image", new ImageResponse(patient.photo, map) {
					@SuppressWarnings("unchecked")
					@Override
					public void onFinished(Bitmap content) {
						content = GraphicsHelper.makeImageRing(GraphicsHelper.makeCycleImage(content, 200, 200), Color.WHITE, 4);
						((Map<String, Object>) tag).put("photo", content);
						((SimpleAdapter) listPatient.getAdapter()).notifyDataSetChanged();
					}
				}, patient.photo);
			}
			map.put("name", patient.nickname());
			map.put("tip", patient.unreadMessageCount());
			if(patient.unreadMessageCount() > 0) {
				patientList.add(0, map);
			}
			else {
				patientList.add(map);
			}
		}
		((SimpleAdapter) listPatient.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onConflict() {
		doctorList.clear();
		patientList.clear();
		((SimpleAdapter) listDoctor.getAdapter()).notifyDataSetChanged();
		((SimpleAdapter) listPatient.getAdapter()).notifyDataSetChanged();
	}

	@Override
	public void onCommand(String from, String action, Table<String, Object> data) {
		refreshDoctor();
		refreshPatient();
	}
}
