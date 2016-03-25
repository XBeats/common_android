package com.common.library.test.db;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.test.AndroidTestCase;
import android.util.Log;

import com.common.library.orm.sqlite.BatchJobs;
import com.common.library.orm.sqlite.DbUtils;

public class DbTestCases extends AndroidTestCase{
	private static final String TAG = "DbTestCases";
	private DbUtils mDbUtils;
	private long mAccId = Account.NOT_SAVED;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		mDbUtils = TestDbUtils.getDBUtils(getContext());
	}
	
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		mDbUtils.close();
	}
	
	public void testAdd(){
		Account acc = new Account();
		acc.auto_login = true;
		acc.deleted = false;
		acc.password = "123456";
		acc.phone_number = "15262430016";
		acc.display_name = "碉堡了";
		acc.remember_pwd = true;
		acc.is_default = true;
		mAccId = mDbUtils.save(Account.class, acc);
		assertTrue(mAccId != Account.NOT_SAVED);
		
		// 再加一个非default的Account
		acc = new Account();
		acc.auto_login = true;
		acc.deleted = false;
		acc.password = "123456";
		acc.phone_number = "15262430016";
		acc.display_name = "碉堡了";
		acc.remember_pwd = true;
		acc.is_default = false;
		mAccId = mDbUtils.save(Account.class, acc);
		assertTrue(mAccId != Account.NOT_SAVED);
	}
	
	public void testSaveAll(){
		List<Account> accs = new ArrayList<Account>();
		for(int i=0; i < 100; i++){
			Account acc1 = new Account();
			acc1.auto_login = true;
			acc1.deleted = false;
			acc1.password = "123456";
			acc1.phone_number = "15262430016";
			acc1.display_name = "碉堡了";
			acc1.remember_pwd = true;
			acc1.is_default = true;
			accs.add(acc1);
		}
		List<Long> saved = mDbUtils.saveAll(Account.class, accs);
		System.out.println(saved.size());
	}
	
	public void testFindById(){
		Account acc = mDbUtils.findById(Account.class, 1);
		assertTrue(acc != null);
	}
	
	public void testFindFirst(){
		Account acc = mDbUtils.findFirst(
				Account.class, 
				Account.COLUMN_IS_DEFAULT + "=?",
				new String[]{Boolean.TRUE.toString()}, 
				null, null, null);
		assertTrue(acc != null);
	}
	
	public void testFindBySelections(){
		List<Account> accounts = mDbUtils.find(
				Account.class,
				Account.COLUMN_PHONE_NUMBER + "=?",
				new String[]{"15262430016"}, 
				null, null, null);
		
		assertTrue(accounts != null && accounts.size() > 0);
	}
	
	public void testFindAll(){
		List<Account> accounts = mDbUtils.findAll(Account.class);
		Log.d(TAG, "all accounts size:" + accounts);
		assertTrue(accounts != null && accounts.size() > 0);
	}
	
	// 还有用于分页查找的findWithLimit 
	
	public void testCountAll(){
		int count = mDbUtils.count(Account.class);
		assertTrue(count > 0);
	}
	
	public void testCountBySelections(){
		int count = mDbUtils.count(
				Account.class, 
				Account.COLUMN_DELETED + "=?",
				new String[]{Boolean.FALSE.toString()});
		assertTrue(count > 0);
	}
	
	public void testUpdate(){
		// update by specified ContentVaues
		ContentValues values = new ContentValues();
		values.put(Account.COLUMN_DISPLAYING_NAME, "张三");
		mDbUtils.update(Account.class, 1, values);
		
		// update by modify record's property
		Account acc = mDbUtils.findById(Account.class, 1);
		acc.display_name = "赵大麻子";
		mDbUtils.update(acc);
	}
	
	public void testDeleteById(){
		long deletedAccId = mDbUtils.deleteById(Account.class, mAccId);
		assertTrue(deletedAccId != Account.NOT_SAVED);
	}
	
	public void testDeletedBySelections(){
		long deletedAccId = mDbUtils.delete(
				Account.class, 
				Account.COLUMN_IS_DEFAULT + "=?", 
				new String[]{Boolean.TRUE.toString()});
		assertTrue(deletedAccId != Account.NOT_SAVED);
	}
	
	public void testDeleteAll(){
		long deletedCount = mDbUtils.deleteAll(Account.class);
		assertTrue(deletedCount != Account.NOT_SAVED);
	}
	
	public void testTransaction(){
		// add job
		BatchJobs jobs = new BatchJobs();
		Account acc = new Account();
		acc.auto_login = true;
		acc.deleted = false;
		acc.password = "123456";
		acc.phone_number = "15262430016";
		acc.display_name = "碉堡了";
		acc.remember_pwd = true;
		acc.is_default = false;
		jobs.addInsertJob(acc);
		
		// update job
		ContentValues values = acc.toContentValues();
		values.put(Account.COLUMN_PASSWORD, "1234567890");
		jobs.addUpdateJob(Account.class, 1, values);
		
		// delete job
		jobs.addDeleteJob(Account.class, 1);
		
		mDbUtils.executeBatchJobs(jobs);
	}
}
