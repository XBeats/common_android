package com.common.library.test.db;

import com.common.library.orm.annotation.Column;
import com.common.library.orm.annotation.Foreign;
import com.common.library.orm.annotation.Table;
import com.common.library.orm.sqlite.BaseTable;

@Table(name = "permission")
public class Permission extends BaseTable {

	private static final long serialVersionUID = 1L;
	public static final String COLUMN_ACCOUNT_ID = "account_id";
	public static final String COLUMN_PERMISSION = "permission";

	@Column(columnName = "account_id")
	@Foreign(tableClass = Account.class)
	public long accountId;

	@Column(columnName = "permission")
	public int permission;
}
