package com.common.library.test.db;

import com.common.library.orm.annotation.Column;
import com.common.library.orm.annotation.DefaultOrderBy;
import com.common.library.orm.annotation.Table;
import com.common.library.orm.sqlite.BaseTable;

@Table(name = "account")
public class Account extends BaseTable {
	private static final long serialVersionUID = -5841044814960073804L;
	public static final String COLUMN_PHONE_NUMBER = "phone_number";
	public static final String COLUMN_DISPLAYING_NAME = "display_name";
	public static final String COLUMN_PASSWORD = "password";
	public static final String COLUMN_ROLE = "role";
	public static final String COLUMN_DELETED = "deleted";
	public static final String COLUMN_REMEBER_PWD = "remember_pwd";
	public static final String COLUMN_AUTO_LOGIN = "auto_login";
	public static final String COLUMN_IS_DEFAULT = "is_default";

	@Column(notNull = true)
	public String phone_number;

	@Column(notNull = true)
	@DefaultOrderBy
	public String display_name;

	@Column(notNull = true)
	public String password;

	@Column(notNull = true)
	public int role;

	@Column
	public boolean deleted;

	@Column
	public boolean remember_pwd;

	@Column
	public boolean auto_login;

	@Column
	public boolean is_default;
}
