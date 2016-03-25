package com.common.library.contacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds;
import android.text.TextUtils;

/**
 * Contact Utility.
 * 
 * @author zf08526
 *
 */
public class ContactsUtils {

	/**
	 * Retrieve contact's information, be notice that you should
	 * startActivityForResult like below:
	 * 
	 * <pre>
	 * Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
	 * startActivityForResult(intent, REQ_CODE_CONTACTS);
	 * </pre>
	 * 
	 * @param context android context
	 * @param contactUri
	 *            URI got from intent named "data" in Activity callback
	 *            {@link Activity#onActivityResult(int, int, Intent)}
	 * @return {@link ContactInfo}
	 */
	public static ContactInfo retrieveContactInfo(Context context, Uri contactUri) {
		ContactInfo contactInfo = null;

		if (contactUri == null) {
			return null;
		}

		Cursor cursor = context.getContentResolver().query(contactUri, null, null, null, null);
		if (cursor == null) {
			return null;
		}

		try {
			if (cursor != null && cursor.moveToFirst()) {
				contactInfo = new ContactInfo();

				boolean havePhone = cursor.getInt(cursor.getColumnIndex(CommonDataKinds.Phone.HAS_PHONE_NUMBER)) == 1;
				contactInfo.setHasPhoneNumber(havePhone);

				if (havePhone) {
					String phoneNumber = cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.NUMBER));

					// format phone number
					if (!TextUtils.isEmpty(phoneNumber)) {
						phoneNumber = phoneNumber.replace("+86", "").replaceAll("\\s*", "").replaceAll("-", "");

					}
					contactInfo.setPhoneNumber(phoneNumber);
				}

				String displayName = cursor.getString(cursor.getColumnIndex(CommonDataKinds.Phone.DISPLAY_NAME));
				contactInfo.setDisplayName(displayName);
			}
		} finally {
			cursor.close();
		}

		return contactInfo;
	}
}
