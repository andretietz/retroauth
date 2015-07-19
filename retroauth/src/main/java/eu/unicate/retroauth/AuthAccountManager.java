/*
 * Copyright (c) 2015 Andre Tietz
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.unicate.retroauth;

import android.accounts.Account;
import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

public interface AuthAccountManager {
	Account getActiveAccount(String accountType, boolean showDialog);
	Account getAccountByName(String accountName, String accountType);
	String getActiveAccountName(String accountType, boolean showDialog);
	String getTokenFromActiveUser(String accountType, String tokenType);
	String getUserData(String accountType, String key);
	void invalidateTokenFromActiveUser(String accountType, String tokenType);
	Account setActiveUser(String accountName, String accountType);
	void resetActiveUser(String accountType);
	void addAccount(@NonNull Activity activity, @NonNull String accountType, @Nullable String tokenType);
	Account[] showAccountPickerDialog(String accountType, DialogInterface.OnClickListener onItemSelected, DialogInterface.OnClickListener onOkClicked, DialogInterface.OnClickListener onCancelClicked, boolean canAddAccount);
}
