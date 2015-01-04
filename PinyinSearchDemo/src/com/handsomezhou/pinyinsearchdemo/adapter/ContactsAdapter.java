package com.handsomezhou.pinyinsearchdemo.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.SectionIndexer;
import android.widget.TextView;

import com.handsomezhou.pinyinsearchdemo.R;
import com.handsomezhou.pinyinsearchdemo.model.Contacts;
import com.handsomezhou.pinyinsearchdemo.util.ViewUtil;
import com.handsomezhou.pinyinsearchdemo.view.QuickAlphabeticBar;


public class ContactsAdapter extends ArrayAdapter<Contacts> implements SectionIndexer{
	//public static final String PINYIN_FIRST_LETTER_DEFAULT_VALUE="#";
	private Context mContext;
	private int mTextViewResourceId;
	private List<Contacts> mContacts;
	private HashMap<String, Contacts> mSelectedContactsHashMap; //(id+phoneNumber)as key
	private List<Contacts> mSelectedContactsList; 
	private OnContactsAdapter mOnContactsAdapter;
	
	public interface OnContactsAdapter{
		void onContactsSelectedChanged(List<Contacts> sortContacts);
		void onAddContactsSelected(Contacts sortContact);
		void onRemoveContactsSelected(Contacts sortContact);
	}
	
	public ContactsAdapter(Context context, int textViewResourceId,
			List<Contacts> contacts) {
		super(context, textViewResourceId, contacts);
		mContext=context;
		mTextViewResourceId=textViewResourceId;
		mContacts=contacts;
		
		setSelectedContacts(new HashMap<String, Contacts>());
		setSelectedContactsList(new ArrayList<Contacts>());
		getSelectedContacts().clear();
	}

	public HashMap<String, Contacts> getSelectedContacts() {
		return mSelectedContactsHashMap;
	}

	public void setSelectedContacts(HashMap<String, Contacts> selectedContacts) {
		mSelectedContactsHashMap = selectedContacts;
	}

	public List<Contacts> getSelectedContactsList() {
		return mSelectedContactsList;
	}

	public void setSelectedContactsList(List<Contacts> selectedContactsList) {
		mSelectedContactsList = selectedContactsList;
	}
	
	public OnContactsAdapter getOnContactsAdapter() {
		return mOnContactsAdapter;
	}

	public void setOnContactsAdapter(OnContactsAdapter onContactsAdapter) {
		mOnContactsAdapter = onContactsAdapter;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view=null;
		ViewHolder viewHolder;
		Contacts contacts=getItem(position);
		if(null==convertView){
			view=LayoutInflater.from(mContext).inflate(mTextViewResourceId, null);
			viewHolder=new ViewHolder();
			viewHolder.mAlphabetTv=(TextView)view.findViewById(R.id.alphabet_text_view);
			viewHolder.mSelectContactsCB=(CheckBox) view.findViewById(R.id.select_contacts_check_box);
			viewHolder.mNameTv=(TextView) view.findViewById(R.id.name_text_view);
			viewHolder.mPhoneNumber=(TextView) view.findViewById(R.id.phone_number_text_view);
			view.setTag(viewHolder);
		}else{
			view=convertView;
			viewHolder=(ViewHolder) view.getTag();
		}
		
		//show the first alphabet of name
		showAlphabetIndex(viewHolder.mAlphabetTv, position, contacts);
		//show name and phone number
		switch (contacts.getSearchByType()) {
		case SearchByNull:
			ViewUtil.showTextNormal(viewHolder.mNameTv, contacts.getName());
			if(contacts.getPhoneNumberList().size()<=1){
				ViewUtil.showTextNormal(viewHolder.mPhoneNumber, contacts.getPhoneNumberList().get(0));
			}else{
				ViewUtil.showTextNormal(viewHolder.mPhoneNumber, contacts.getPhoneNumberList().get(0)+mContext.getString(R.string.phone_number_count, contacts.getPhoneNumberList().size()));
			}
			break;
		case SearchByPhoneNumber:
			ViewUtil.showTextNormal(viewHolder.mNameTv, contacts.getName());
			ViewUtil.showTextHighlight(viewHolder.mPhoneNumber, contacts.getPhoneNumberList().get(0), contacts.getMatchKeywords().toString());
			break;
		case SearchByName:
			ViewUtil.showTextHighlight(viewHolder.mNameTv, contacts.getName(), contacts.getMatchKeywords().toString());
			ViewUtil.showTextNormal(viewHolder.mPhoneNumber, contacts.getPhoneNumberList().get(0));
			break;
		default:
			break;
		}	
		
		viewHolder.mSelectContactsCB.setTag(position);
		viewHolder.mSelectContactsCB.setChecked(contacts.isSelected());
		viewHolder.mSelectContactsCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				int position = (Integer) buttonView.getTag();
				Contacts contacts = getItem(position);
				if((true==isChecked)&&(false==contacts.isSelected())){
					contacts.setSelected(isChecked);
					addSelectedContacts(contacts);
					
				}else if((false==isChecked)&&(true==contacts.isSelected())){
					contacts.setSelected(isChecked);
					removeSelectedContacts(contacts);
				}else{
					return;
				}
				
				if(null!=mOnContactsAdapter){
					if(null==mSelectedContactsList){
						mSelectedContactsList=new ArrayList<Contacts>();
					}else{
						mSelectedContactsList.clear();
					}
					mSelectedContactsList.addAll(mSelectedContactsHashMap.values());
					mOnContactsAdapter.onContactsSelectedChanged(mSelectedContactsList);
					
				}
			}
		});
		return view;
	}
	
	private class ViewHolder{
		TextView mAlphabetTv;
		CheckBox mSelectContactsCB;
		TextView mNameTv;
		TextView mPhoneNumber;
	}
	
	private void showAlphabetIndex(TextView textView, int position, final Contacts contacts){
		if((null==textView)||position<0||(null==contacts)){
			return;
		}
		String curAlphabet=getAlphabet(contacts.getSortKey());
		if(position>0){
			Contacts preContacts=getItem(position-1);
			String preAlphabet=getAlphabet(preContacts.getSortKey());
			if(curAlphabet.equals(preAlphabet)){
				textView.setVisibility(View.GONE);
				textView.setText(curAlphabet);
			}else{
				textView.setVisibility(View.VISIBLE);
				textView.setText(curAlphabet);
			}
		}else {
			textView.setVisibility(View.VISIBLE);
			textView.setText(curAlphabet);
		}
		
		return ;
	}
	
	private String getAlphabet(String str){
		if((null==str)||(str.length()<=0)){
			return String.valueOf(QuickAlphabeticBar.DEFAULT_INDEX_CHARACTER);
		}
		String alphabet=null;
		char chr=str.charAt(0);
		if (chr >= 'A' && chr <= 'Z') {
			alphabet = String.valueOf(chr);
		} else if (chr >= 'a' && chr <= 'z') {
			alphabet = String.valueOf((char) ('A' + chr - 'a'));
		} else {
			alphabet = String.valueOf(QuickAlphabeticBar.DEFAULT_INDEX_CHARACTER);
		}
		return alphabet;
	}

	@Override
	public Object[] getSections() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getPositionForSection(int section) {
		Contacts contacts=null;
		if(QuickAlphabeticBar.DEFAULT_INDEX_CHARACTER==section){
			return 0;
		}else{
			int count=getCount();
			for(int i=0; i<count; i++){
				contacts=getItem(i);
				char firstChar=contacts.getSortKey().charAt(0);
				if(firstChar==section){
					return i;
				}
			}
		}
		
		return -1;
	}

	@Override
	public int getSectionForPosition(int position) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void clearSelectedContacts(){
		//clear data
		for(Contacts sortContact:mSelectedContactsList){
			sortContact.setSelected(false);
		}
		
		mSelectedContactsList.clear();
		mSelectedContactsHashMap.clear();
		
		//refresh view
		notifyDataSetChanged();
	}

	private boolean addSelectedContacts(Contacts contacts){
		
		
		do{
			if(null==contacts){
				break;
			}
			
			if(null==mSelectedContactsHashMap){
				mSelectedContactsHashMap=new HashMap<String, Contacts>();
			}
			
			mSelectedContactsHashMap.put(getSelectedContactsKey(contacts), contacts);
			if(null!=mOnContactsAdapter){
				mOnContactsAdapter.onAddContactsSelected(contacts);
			}
			
			return true;
		}while(false);
		
		return false;
	
	}
	
	private void removeSelectedContacts(Contacts contacts){
		if(null==contacts){
			return;
		}
		
		if(null==mSelectedContactsHashMap){
			return;
		}
		
		mSelectedContactsHashMap.remove(getSelectedContactsKey(contacts));
		if(null!=mOnContactsAdapter){
			mOnContactsAdapter.onRemoveContactsSelected(contacts);
		}
	}
	
	/**
	 * key=id+phoneNumber
	 * */
	private String getSelectedContactsKey(Contacts contacts){
		if(null==contacts){
			return null;
		}
		
		return contacts.getId()+contacts.getPhoneNumber();
	}
	
}
